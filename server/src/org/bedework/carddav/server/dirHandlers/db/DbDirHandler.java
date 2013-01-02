/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.carddav.server.dirHandlers.db;

import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.server.SysIntf.GetLimits;
import org.bedework.carddav.server.SysIntf.GetResult;
import org.bedework.carddav.server.access.AccessUtil;
import org.bedework.carddav.server.access.AccessUtilI;
import org.bedework.carddav.server.access.SharedEntity;
import org.bedework.carddav.server.dirHandlers.AbstractDirHandler;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.util.DbDirHandlerConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.util.User;
import org.bedework.carddav.vcard.Card;

import edu.rpi.cct.webdav.servlet.shared.UrlHandler;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cmt.access.Access;
import edu.rpi.cmt.access.AccessException;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.Acl.CurrentAccess;
import edu.rpi.cmt.access.PrivilegeDefs;
import edu.rpi.cmt.access.WhoDefs;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/** Provide some common methods for db based directory handlers.
 *
 * @author douglm
 *
 */
public abstract class DbDirHandler extends AbstractDirHandler implements PrivilegeDefs {
  protected DbDirHandlerConfig dbConfig;

  protected String userHomeRoot;

  /** When we were created for debugging */
  protected Timestamp objTimestamp;

  /** Current hibernate session - exists only across one user interaction
   */
  protected HibSession sess;

  /** We make this static for this implementation so that there is only one
   * SessionFactory per server for the card server.
   *
   * <p>static fields used this way are illegal in the j2ee specification
   * though we might get away with it here as the session factory only
   * contains parsed mappings for the card configuration. This should
   * be the same for any machine in a cluster so it might work OK.
   *
   * <p>It might be better to find some other approach for the j2ee world.
   */
  private static SessionFactory sessionFactory;
  //private static Statistics dbStats;

  /** For evaluating access control
   */
  private AccessUtilI access;

  /**
   * @author douglm
   *
   */
  private class AccessUtilCb extends AccessUtilI.CallBack {
    DbDirHandler hdlr;

    AccessUtilCb(final DbDirHandler hdlr) {
      this.hdlr = hdlr;
    }

    @Override
    public AccessPrincipal getPrincipal(final String href) throws WebdavException {
      return hdlr.getPrincipal(href);
    }

    @Override
    public String getUserHomeRoot() throws WebdavException {
      return hdlr.userHomeRoot;
    }

    /* (non-Javadoc)
     * @see edu.rpi.cmt.access.Access.AccessCb#makeHref(java.lang.String, int)
     */
    public String makeHref(final String id, final int whoType) throws AccessException {
      try {
        return hdlr.makePrincipalHref(id, whoType);
      } catch (Throwable t) {
        throw new AccessException(t);
      }
    }

    @Override
    public SharedEntity getCollection(final String path) throws WebdavException {
      DbCollection col = hdlr.getDbCollection(path);

      if (col == null) {
        if (path.equals("/")) {
          // Make a root collection
          col = new DbCollection();
          col.setPath("/");

          // Use this for owner/creator
          col.setOwnerHref(dbConfig.getRootOwner());
          col.setCreatorHref(dbConfig.getRootOwner());
          col.setAccess(Access.getDefaultPublicAccess());
        } else {
          return null;
        }
      }

      return col;
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.dirHandlers.AbstractDirHandler#init(org.bedework.carddav.util.CardDAVConfig, org.bedework.carddav.util.DirHandlerConfig, edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler)
   */
  @Override
  public void init(final CardDAVConfig cdConfig,
                   final DirHandlerConfig dhConfig,
                   final UrlHandler urlHandler) throws WebdavException {
    super.init(cdConfig, dhConfig, urlHandler);

    dbConfig = (DbDirHandlerConfig)dhConfig;

    userHomeRoot = cdConfig.getUserHomeRoot();
    if (!userHomeRoot.endsWith("/")) {
      userHomeRoot += "/";
    }

    AccessUtilCb acb = new AccessUtilCb(this);

    access = new AccessUtil();
    access.init(acb);

    try {
      objTimestamp = new Timestamp(System.currentTimeMillis());
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#open(java.lang.String)
   */
  @Override
  public void open(final String account) throws WebdavException {
    super.open(account);

    if (isOpen()) {
      return;
    }
    openSession();
    open = true;

    access.setAuthUser((User)getPrincipal(makePrincipalHref(account,
                                                            WhoDefs.whoTypeUser)));
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#close()
   */
  @Override
  public void close() throws WebdavException {
    super.close();

    try {
      endTransaction();
    } catch (WebdavException wde) {
      try {
        rollbackTransaction();
      } catch (WebdavException wde1) {}
      throw wde;
    } finally {
      try {
        closeSession();
      } catch (WebdavException wde1) {}
    }
  }


  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCard(java.lang.String, java.lang.String)
   */
  public Card getCard(final String path, final String name) throws WebdavException {
    DbCard card = getDbCard(path, name);

    if (card == null) {
      return null;
    }

    return makeVcard(card);
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCards(java.lang.String, org.bedework.carddav.server.filter.Filter, org.bedework.carddav.server.SysIntf.GetLimits)
   */
  @SuppressWarnings("unchecked")
  public GetResult getCards(final String path,
                            final Filter filter,
                            final GetLimits limits) throws WebdavException {
    verifyPath(path);

    StringBuilder sb = new StringBuilder();

    sb.append("select card from ");
    sb.append(DbCard.class.getName());
    sb.append(" card join card.properties props where card.parentPath=:path");

    DbFilter fltr = new DbFilter(sb);

    fltr.makeFilter(filter);

    sess.createQuery(sb.toString());
    sess.setString("path", ensureSlashAtEnd(path));

    fltr.parReplace(sess);

    /* We couldn't use DISTINCT in the query (it's a CLOB) so make it
     * distinct with a set
     */
    Set<DbCard> cardSet = new TreeSet<DbCard>(sess.getList());

    GetResult res = new GetResult();

    res.cards = new ArrayList<Card>();

    for (DbCard dbc: cardSet) {
      res.cards.add(makeVcard(dbc));
    }

    return res;
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCollection(java.lang.String)
   */
  public CarddavCollection getCollection(final String path) throws WebdavException {
    verifyPath(path);

    DbCollection col = getDbCollection(ensureEndSlash(path), privRead);

    if (col == null) {
      return null;
    }

    return makeCdCollection(col);
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCollections(java.lang.String, org.bedework.carddav.server.SysIntf.GetLimits)
   */
  @SuppressWarnings("unchecked")
  public GetResult getCollections(final String path,
                                  final GetLimits limits)
         throws WebdavException {
    verifyPath(path);

    StringBuilder sb = new StringBuilder();

    sb.append("from ");
    sb.append(DbCollection.class.getName());
    sb.append(" col where col.parentPath=:path");

    sess.createQuery(sb.toString());
    sess.setString("path", ensureSlashAtEnd(path));

    List<DbCollection> l = sess.getList();

    GetResult res = new GetResult();

    res.collections = new ArrayList<CarddavCollection>();

    if (l == null) {
      return res;
    }

    for (DbCollection col: l) {
      res.collections.add(makeCdCollection(col));
    }

    return res;
  }

  /* ====================================================================
   *  Protected methods.
   * ==================================================================== */

  protected DbCard getDbCard(final String parentPath,
                             final String name) throws WebdavException {
    verifyPath(parentPath);

    StringBuilder sb = new StringBuilder();

    sb.append("from ");
    sb.append(DbCard.class.getName());
    sb.append(" card where card.parentPath=:path");
    sb.append(" and card.name=:name");

    sess.createQuery(sb.toString());
    sess.setString("path", ensureEndSlash(parentPath));
    sess.setString("name", name);

    return (DbCard)sess.getUnique();
  }

  protected DbCard getDbCard(final String path) throws WebdavException {
    verifyPath(path);

    StringBuilder sb = new StringBuilder();

    sb.append("from ");
    sb.append(DbCard.class.getName());
    sb.append(" card where card.path=:path");

    sess.createQuery(sb.toString());
    sess.setString("path", path);

    return (DbCard)sess.getUnique();
  }

  protected DbCollection getDbCollection(final String path,
                                         final int access) throws WebdavException {
    return (DbCollection)checkAccess(getDbCollection(path),
                                     access, true);
  }

  protected CarddavCollection makeCdCollection(final DbCollection col) throws WebdavException {
    CarddavCollection cdc = new CarddavCollection();

    cdc.setAddressBook(col.getAddressBook());

    cdc.setCreated(col.getCreated());
    cdc.setLastmod(col.getLastmod());

    /* The name of this card comes from the attribute specified in the
     * config - addressbookEntryIdAttr
     */

    cdc.setName(col.getName());
    cdc.setDisplayName(col.getName());

    /** Ensure uniqueness - lastmod only down to second.
     */
    //private int sequence;

    cdc.setDescription(col.getDescription());

    // parent          CarddavCollection
    // addressBook     boolean

    cdc.setOwner(getPrincipal(col.getOwnerHref()));
    cdc.setPath(ensureSlashAtEnd(col.getParentPath()) + col.getName());
    cdc.setParentPath(col.getParentPath());

    return cdc;
  }

  private DbCollection getDbCollection(final String path) throws WebdavException {
    if (path.equals("/")) {
      // Make a root collection
      DbCollection col = new DbCollection();
      col.setPath("/");

      col.setOwnerHref(dbConfig.getRootOwner());
      col.setCreatorHref(dbConfig.getRootOwner());
      //col.setAccess(Access.getDefaultPublicAccess());

      return col;
    }

    verifyPath(path);

    StringBuilder sb = new StringBuilder();

    sb.append("from ");
    sb.append(DbCollection.class.getName());
    sb.append(" col where col.path=:path");

    sess.createQuery(sb.toString());

    sess.setString("path", ensureEndSlash(path));

    return (DbCollection)sess.getUnique();
  }

  /**
   * @param dbcard
   * @return a Card object
   * @throws WebdavException
   */
  protected Card makeVcard(final DbCard dbcard) throws WebdavException {
    Card card = new Card(dbcard.getVcard());

    card.setCreated(dbcard.getCreated());
    card.setLastmod(dbcard.getLastmod());
    card.setName(dbcard.getName());

    return card;
  }

  /**
   * @throws WebdavException
   */
  protected void deleteDbCard(final DbCard dbcard) throws WebdavException {
    sess.delete(dbcard);
  }

  /* ====================================================================
   *                   Session methods
   * ==================================================================== */

  protected void checkOpen() throws WebdavException {
    if (!isOpen()) {
      throw new WebdavException("Session call when closed");
    }
  }

  protected synchronized void openSession() throws WebdavException {
    if (isOpen()) {
      throw new WebdavException("Already open");
    }

    open = true;

    if (sess != null) {
      warn("Session is not null. Will close");
      try {
        close();
      } finally {
      }
    }

    if (sess == null) {
      if (debug) {
        trace("New hibernate session for " + objTimestamp);
      }
      sess = new HibSessionImpl();
      sess.init(getSessionFactory(), getLogger());
      trace("Open session for " + objTimestamp);
    }

    beginTransaction();
  }

  protected synchronized void closeSession() throws WebdavException {
    if (!isOpen()) {
      if (debug) {
        trace("Close for " + objTimestamp + " closed session");
      }
      return;
    }

    if (debug) {
      trace("Close for " + objTimestamp);
    }

    try {
      if (sess != null) {
        if (sess.rolledback()) {
          sess = null;
          return;
        }

        if (sess.transactionStarted()) {
          sess.rollback();
        }
//        sess.disconnect();
        sess.close();
        sess = null;
      }
    } catch (Throwable t) {
      try {
        sess.close();
      } catch (Throwable t1) {}
      sess = null; // Discard on error
    } finally {
      open = false;
    }
  }

  protected void beginTransaction() throws WebdavException {
    checkOpen();

    if (debug) {
      trace("Begin transaction for " + objTimestamp);
    }
    sess.beginTransaction();
  }

  protected void endTransaction() throws WebdavException {
    checkOpen();

    if (debug) {
      trace("End transaction for " + objTimestamp);
    }

    if (!sess.rolledback()) {
      sess.commit();
    }
  }

  protected void rollbackTransaction() throws WebdavException {
    try {
      checkOpen();
      sess.rollback();
    } finally {
    }
  }

  /*
  private void flush() throws WebdavException {
    if (debug) {
      trace("flush for " + objTimestamp);
    }
    if (sess.isOpen()) {
      sess.flush();
    }
  }*/

  protected String ensureSlashAtEnd(final String val) {
    if (val.endsWith("/")) {
      return val;
    }

    return val + "/";
  }

  protected SharedEntity checkAccess(final SharedEntity ent,
                                     final int desiredAccess,
                                     final boolean alwaysReturnResult) throws WebdavException {
    if (ent == null) {
      return null;
    }

    boolean noAccessNeeded = desiredAccess == privNone;

    CurrentAccess ca = access.checkAccess(ent, desiredAccess,
                                          alwaysReturnResult || noAccessNeeded);

    if (!noAccessNeeded && !ca.getAccessAllowed()) {
      return null;
    }

    return ent;
  }

  /* ====================================================================
   *                   private methods
   * ==================================================================== */

  private SessionFactory getSessionFactory() throws WebdavException {
    if (sessionFactory != null) {
      return sessionFactory;
    }

    synchronized (this) {
      if (sessionFactory != null) {
        return sessionFactory;
      }

      /** Get a new hibernate session factory. This is configured from an
       * application resource hibernate.cfg.xml together with some run time values
       */
      try {
        Configuration conf = new Configuration();

        /*
        if (props != null) {
          String cachePrefix = props.getProperty("cachePrefix");
          if (cachePrefix != null) {
            conf.setProperty("hibernate.cache.use_second_level_cache",
                             props.getProperty("cachingOn"));
            conf.setProperty("hibernate.cache.region_prefix",
                             cachePrefix);
          }
        }
        */

        StringBuilder sb = new StringBuilder();

        List<String> ps = dbConfig.getHibernateProperties();

        for (String p: ps) {
          sb.append(p);
          sb.append("\n");
        }

        Properties hprops = new Properties();
        hprops.load(new StringReader(sb.toString()));

        conf.addProperties(hprops).configure();

        sessionFactory = conf.buildSessionFactory();

        return sessionFactory;
      } catch (Throwable t) {
        // Always bad.
        error(t);
        throw new WebdavException(t);
      }
    }
  }
}
