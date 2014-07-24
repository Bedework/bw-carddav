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

import org.bedework.access.*;
import org.bedework.access.Acl.CurrentAccess;
import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.server.SysIntf.GetLimits;
import org.bedework.carddav.server.SysIntf.GetResult;
import org.bedework.carddav.server.config.CardDAVConfig;
import org.bedework.carddav.server.config.DbDirHandlerConfig;
import org.bedework.carddav.server.config.DirHandlerConfig;
import org.bedework.carddav.server.dirHandlers.AbstractDirHandler;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.vcard.Card;
import org.bedework.webdav.servlet.access.AccessHelper;
import org.bedework.webdav.servlet.access.AccessHelperI;
import org.bedework.webdav.servlet.access.SharedEntity;
import org.bedework.webdav.servlet.shared.UrlHandler;
import org.bedework.webdav.servlet.shared.WebdavException;
import org.bedework.webdav.servlet.shared.WebdavForbidden;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.io.StringReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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
  private AccessHelperI access;

  /**
   * @author douglm
   *
   */
  private class AccessUtilCb extends AccessHelperI.CallBack {
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

    @Override
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

    access = new AccessHelper();
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

    access.setAuthPrincipal(getPrincipal(makePrincipalHref(account,
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

  @Override
  public Card getCard(final String path,
                      final String name) throws WebdavException {
    final DbCard card = getDbCard(path, name, privRead);

    if (card == null) {
      return null;
    }

    return makeVcard(card);
  }

  @Override
  public Card getCardByUid(final String path,
                           final String uid) throws WebdavException {
    final DbCard card = getDbCardByUid(path, uid);

    if (card == null) {
      return null;
    }

    return makeVcard(card);
  }

  private final static String queryGetCards =
          "select card from " + DbCard.class.getName() +
                  " card join card.properties props " +
                  "where card.parentPath=:path";

  @Override
  @SuppressWarnings("unchecked")
  public GetResult getCards(final String path,
                            final Filter filter,
                            final GetLimits limits) throws WebdavException {
    verifyPath(path);

    final StringBuilder sb = new StringBuilder(queryGetCards);

    final DbFilter fltr = new DbFilter(sb);

    fltr.makeFilter(filter);

    sess.createQuery(sb.toString());
    sess.setString("path", ensureSlashAtEnd(path));

    fltr.parReplace(sess);

    /* We couldn't use DISTINCT in the query (it's a CLOB) so make it
     * distinct with a set
     */
    final Set<DbCard> cardSet = new TreeSet<DbCard>(sess.getList());

    final GetResult res = new GetResult();

    for (final DbCard dbc: cardSet) {
      res.cards.add(makeVcard(dbc));
    }

    return res;
  }

  private final static String queryGetCardNames =
          "select card.name from " + DbCard.class.getName() +
                  " card where card.parentPath=:path";

  private class CardIterator implements Iterator<Card> {
    private String parentPath;
    private List<String> names;

    private Iterator<String> it;

    private HibSession sess;

    @Override
    public boolean hasNext() {
      return it.hasNext();
    }

    @Override
    public Card next() {
      if (!it.hasNext()) {
        return null;
      }

      try {
        return getCard(parentPath, it.next());
      } catch (final WebdavException we) {
        throw new RuntimeException(we);
      }
    }

    @Override
    public void remove() {
    }
  }
  @Override
  public Iterator<Card> getAll(final String path) throws WebdavException {
    verifyPath(path);

    sess.createQuery(queryGetCardNames);
    sess.setString("path", ensureSlashAtEnd(path));

    final CardIterator ci = new CardIterator();
    ci.parentPath = path;
    //noinspection unchecked
    ci.names = sess.getList();
    ci.it = ci.names.iterator();
    ci.sess = sess;

    return ci;
  }


  @Override
  public CarddavCollection getCollection(final String path) throws WebdavException {
    verifyPath(path);

    final DbCollection col = getDbCollection(ensureEndSlash(path), privRead);

    if (col == null) {
      return null;
    }

    return makeCdCollection(col);
  }

  private static final String queryGetCollections =
          "from " + DbCollection.class.getName() +
                  " col where col.parentPath=:path";

  @Override
  @SuppressWarnings("unchecked")
  public GetResult getCollections(final String path,
                                  final GetLimits limits)
         throws WebdavException {
    verifyPath(path);

    sess.createQuery(queryGetCollections);
    sess.setString("path", ensureSlashAtEnd(path));

    final List<DbCollection> l = sess.getList();

    final GetResult res = new GetResult();

    res.collections = new ArrayList<>();

    if (l == null) {
      return res;
    }

    for (final DbCollection col: l) {
      if (checkAccess(col, privRead, true) == null) {
        continue;
      }

      res.collections.add(makeCdCollection(col));
    }

    return res;
  }

  protected class CollectionsBatchImpl implements CollectionBatcher {
    private Collection<CarddavCollection> cols;
    private boolean called;

    @Override
    public Collection<CarddavCollection> next() throws WebdavException {
      if (called) {
        //noinspection unchecked
        return Collections.EMPTY_LIST;
      }

      called = true;
      return cols;
    }
  }

  @Override
  public CollectionBatcher getCollections(final String path) throws WebdavException {
    final CollectionsBatchImpl cbi = new CollectionsBatchImpl();

    final GetResult gr = getCollections(path, null);

    cbi.cols = Collections.unmodifiableCollection(gr.collections);

    return cbi;
  }

  protected void updateCollection(final DbCollection col) throws WebdavException {
    sess.update(col);
  }

  /* ====================================================================
   *  Protected methods.
   * ==================================================================== */

  private static final String queryGetCardByName =
          "from " + DbCard.class.getName() +
                  " card where card.parentPath=:path" +
                  " and card.name=:name";

  protected DbCard getDbCard(final String parentPath,
                             final String name,
                             final int access) throws WebdavException {
    verifyPath(parentPath);

    final DbCollection col = getDbCollection(ensureEndSlash(parentPath), access);

    if (col == null) {
      return null;
    }

    sess.createQuery(queryGetCardByName);
    sess.setString("path", ensureEndSlash(parentPath));
    sess.setString("name", name);

    return (DbCard)sess.getUnique();
  }

  private static final String queryGetCardByUid =
          "from " + DbCard.class.getName() +
                  " card where card.parentPath=:path" +
                  " and card.uid=:uid";

  protected DbCard getDbCardByUid(final String parentPath,
                                  final String uid) throws WebdavException {
    verifyPath(parentPath);

    sess.createQuery(queryGetCardByUid);
    sess.setString("path", ensureEndSlash(parentPath));
    sess.setString("uid", uid);

    return (DbCard)sess.getUnique();
  }

  private static final String queryGetCard =
          "from " + DbCard.class.getName() +
                  " card where card.path=:path";

  protected DbCard getDbCard(final String path) throws WebdavException {
    verifyPath(path);

    sess.createQuery(queryGetCard);
    sess.setString("path", path);

    return (DbCard)sess.getUnique();
  }

  protected DbCollection getDbCollection(final String path,
                                         final int access) throws WebdavException {
    return (DbCollection)checkAccess(getDbCollection(path),
                                     access, true);
  }

  protected CarddavCollection makeCdCollection(final DbCollection col) throws WebdavException {
    final CarddavCollection cdc = new CarddavCollection();

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

  private static final String queryGetCollection =
          "from " + DbCollection.class.getName() +
                  " col where col.path=:path";

  private DbCollection getDbCollection(final String path) throws WebdavException {
    if (path.equals("/")) {
      // Make a root collection
      final DbCollection col = new DbCollection();
      col.setPath("/");

      col.setOwnerHref(dbConfig.getRootOwner());
      col.setCreatorHref(dbConfig.getRootOwner());
      //col.setAccess(Access.getDefaultPublicAccess());

      return col;
    }

    verifyPath(path);

    sess.createQuery(queryGetCollection);
    sess.setString("path", ensureEndSlash(path));

    return (DbCollection)sess.getUnique();
  }

  private static final String queryDeleteCollection =
          "delete " + DbCollection.class.getName() +
                  " col where col.path=:path";

  private static final String queryGetColCards =
          "from " + DbCard.class.getName() +
                  " card where card.parentPath=:path";

  protected int deleteDbCollection(final String path) throws WebdavException {
    if (path.equals("/")) {
      return 0;
    }

    if (getDbCollection(path, privUnbind) ==null) {
      throw new WebdavForbidden();
    }

    verifyPath(path);

    sess.createQuery(queryGetColCards);
    sess.setString("path", ensureEndSlash(path));

    /* We couldn't use DISTINCT in the query (it's a CLOB) so make it
     * distinct with a set
     */
    final Set<DbCard> cardSet = new TreeSet<DbCard>(sess.getList());

    for (final DbCard cd: cardSet) {
      deleteDbCard(cd);
    }

    sess.createQuery(queryDeleteCollection);
    sess.setString("path", ensureEndSlash(path));

    return cardSet.size() + sess.executeUpdate();
  }

  /**
   * @param dbcard dbcopy
   * @return a Card object
   * @throws WebdavException
   */
  protected Card makeVcard(final DbCard dbcard) throws WebdavException {
    final Card card = new Card(dbcard.getVcard());

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

    if (superUser) {
      return ent;
    }

    final boolean noAccessNeeded = desiredAccess == privNone;

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
