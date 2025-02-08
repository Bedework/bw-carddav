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

import org.bedework.access.Access;
import org.bedework.access.AccessException;
import org.bedework.access.AccessPrincipal;
import org.bedework.access.CurrentAccess;
import org.bedework.access.PrivilegeDefs;
import org.bedework.access.WhoDefs;
import org.bedework.base.exc.BedeworkException;
import org.bedework.carddav.common.AbstractDirHandler;
import org.bedework.carddav.common.CarddavCollection;
import org.bedework.carddav.common.GetLimits;
import org.bedework.carddav.common.GetResult;
import org.bedework.carddav.common.config.CardDAVConfigI;
import org.bedework.carddav.common.config.DirHandlerConfig;
import org.bedework.carddav.common.filter.Filter;
import org.bedework.carddav.common.vcard.Card;
import org.bedework.carddav.server.config.DbDirHandlerConfig;
import org.bedework.database.db.DbSession;
import org.bedework.database.hibernate.HibSessionImpl;
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
public abstract class DbDirHandler extends AbstractDirHandler
        implements PrivilegeDefs {
  protected DbDirHandlerConfig dbConfig;

  protected String userHomeRoot;

  /** When we were created for debugging */
  protected Timestamp objTimestamp;

  /** Current hibernate session - exists only across one user interaction
   */
  protected DbSession sess;

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
    public AccessPrincipal getPrincipal(final String href) {
      return hdlr.getPrincipal(href);
    }

    @Override
    public String getUserHomeRoot() {
      return hdlr.userHomeRoot;
    }

    @Override
    public String makeHref(final String id, final int whoType) throws AccessException {
      try {
        return hdlr.makePrincipalHref(id, whoType);
      } catch (final Throwable t) {
        throw new AccessException(t);
      }
    }

    @Override
    public SharedEntity getCollection(final String path) {
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
  public void init(final CardDAVConfigI cdConfig,
                   final DirHandlerConfig<?> dhConfig,
                   final UrlHandler urlHandler) {
    super.init(cdConfig, dhConfig, urlHandler);

    dbConfig = (DbDirHandlerConfig)dhConfig;

    userHomeRoot = cdConfig.getUserHomeRoot();
    if (!userHomeRoot.endsWith("/")) {
      userHomeRoot += "/";
    }

    final AccessUtilCb acb = new AccessUtilCb(this);

    access = new AccessHelper();
    access.init(acb);

    try {
      objTimestamp = new Timestamp(System.currentTimeMillis());
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void open(final String account) {
    super.open(account);

    if (isOpen()) {
      return;
    }
    openSession();
    open = true;

    access.setAuthPrincipal(getPrincipal(makePrincipalHref(account,
                                                           WhoDefs.whoTypeUser)));
  }

  @Override
  public void close() {
    super.close();

    try {
      endTransaction();
    } catch (final WebdavException wde) {
      try {
        rollbackTransaction();
      } catch (final WebdavException ignored) {}
      throw wde;
    } finally {
      try {
        closeSession();
      } catch (final WebdavException ignored) {}
    }
  }

  @Override
  public Card getCard(final String path,
                      final String name) {
    final DbCard card = getDbCard(path, name, privRead);

    if (card == null) {
      return null;
    }

    return makeVcard(card);
  }

  @Override
  public Card getCardByUid(final String path,
                           final String uid) {
    final DbCard card = getDbCardByUid(path, uid);

    if (card == null) {
      return null;
    }

    return makeVcard(card);
  }

  private final static String queryGetCards =
          "select card from DbCard card " +
                  "join card.properties props " +
                  "where card.parentPath=:path";

  @Override
  @SuppressWarnings("unchecked")
  public GetResult getCards(final String path,
                            final Filter filter,
                            final GetLimits limits) {
    verifyPath(path);

    final StringBuilder sb = new StringBuilder(queryGetCards);

    final DbFilter fltr = new DbFilter(sb);

    fltr.makeFilter(filter);

    try {
      sess.createQuery(sb.toString());
      sess.setString("path", ensureSlashAtEnd(path));

      fltr.parReplace(sess);

      /* We couldn't use DISTINCT in the query (it's a CLOB) so make it
       * distinct with a set
       */
      final Set<DbCard> cardSet =
              new TreeSet<>((List<DbCard>)sess.getList());

      final GetResult res = new GetResult();

      for (final DbCard dbc: cardSet) {
        res.cards.add(makeVcard(dbc));
      }

      return res;
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  private final static String queryGetCardNames =
          "select card.name from DbCard card " +
                  "where card.parentPath=:path";

  private class CardIterator implements Iterator<Card> {
    private String parentPath;
    private List<String> names;

    private Iterator<String> it;

    private DbSession sess;

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
  public Iterator<Card> getAll(final String path) {
    verifyPath(path);

    try {
      sess.createQuery(queryGetCardNames);
      sess.setString("path", ensureSlashAtEnd(path));

      final CardIterator ci = new CardIterator();
      ci.parentPath = path;
      //noinspection unchecked
      ci.names = (List<String>)sess.getList();
      ci.it = ci.names.iterator();
      ci.sess = sess;

      return ci;
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }


  @Override
  public CarddavCollection getCollection(final String path) {
    verifyPath(path);

    final DbCollection col = getDbCollection(ensureEndSlash(path), privRead);

    if (col == null) {
      return null;
    }

    return makeCdCollection(col);
  }

  private static final String queryGetCollections =
          "select col from DbCollection col " +
                  "where col.parentPath=:path";

  @Override
  @SuppressWarnings("unchecked")
  public GetResult getCollections(final String path,
                                  final GetLimits limits) {
    verifyPath(path);

    try {
      sess.createQuery(queryGetCollections);
      sess.setString("path", ensureSlashAtEnd(path));

      final List<DbCollection> l = (List<DbCollection>)sess.getList();

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
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  protected static class CollectionsBatchImpl
          implements CollectionBatcher {
    private Collection<CarddavCollection> cols;
    private boolean called;

    @Override
    public Collection<CarddavCollection> next() {
      if (called) {
        //noinspection unchecked
        return Collections.EMPTY_LIST;
      }

      called = true;
      return cols;
    }
  }

  @Override
  public CollectionBatcher getCollections(final String path) {
    final CollectionsBatchImpl cbi = new CollectionsBatchImpl();

    final GetResult gr = getCollections(path, null);

    cbi.cols = Collections.unmodifiableCollection(gr.collections);

    return cbi;
  }

  protected void updateCollection(final DbCollection col) {
    try {
      sess.update(col);
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  /* ====================================================================
   *  Protected methods.
   * ==================================================================== */

  private static final String queryGetCardByName =
          "select card from DbCard card " +
                  " where card.parentPath=:path" +
                  " and card.name=:name";

  protected DbCard getDbCard(final String parentPath,
                             final String name,
                             final int access) {
    verifyPath(parentPath);

    final DbCollection col = getDbCollection(ensureEndSlash(parentPath), access);

    if (col == null) {
      return null;
    }

    try {
      sess.createQuery(queryGetCardByName);
      sess.setString("path", ensureEndSlash(parentPath));
      sess.setString("name", name);

      return (DbCard)sess.getUnique();
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  private static final String queryGetCardByUid =
          "select card from DbCard card " +
                  " where card.parentPath=:path" +
                  " and card.uid=:uid";

  protected DbCard getDbCardByUid(final String parentPath,
                                  final String uid) {
    verifyPath(parentPath);

    try {
      sess.createQuery(queryGetCardByUid);
      sess.setString("path", ensureEndSlash(parentPath));
      sess.setString("uid", uid);

      return (DbCard)sess.getUnique();
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  private static final String queryGetCard =
          "select card from DbCard card " +
                  " where card.path=:path";

  protected DbCard getDbCard(final String path) {
    verifyPath(path);

    try {
      sess.createQuery(queryGetCard);
      sess.setString("path", path);

      return (DbCard)sess.getUnique();
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  protected DbCollection getDbCollection(final String path,
                                         final int access) {
    return (DbCollection)checkAccess(getDbCollection(path),
                                     access, true);
  }

  protected CarddavCollection makeCdCollection(final DbCollection col) {
    final CarddavCollection cdc = new CarddavCollection();

    cdc.setAddressBook(col.getAddressBook());

    cdc.setCreated(col.getCreated());
    cdc.setLastmod(col.getLastmod());

    /* The name of this card comes from the attribute specified in the
     * config - addressbookEntryIdAttr
     */

    cdc.setName(col.getName());
    cdc.setDisplayName(col.getName());

    /* Ensure uniqueness - lastmod only down to second.
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
          "select col from DbCollection col " +
                  "where col.path=:path";

  private DbCollection getDbCollection(final String path) {
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

    try {
      sess.createQuery(queryGetCollection);
      sess.setString("path", ensureEndSlash(path));

      return (DbCollection)sess.getUnique();
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  private static final String queryDeleteCollection =
          "delete DbCollection col where col.path=:path";

  private static final String queryGetColCards =
          "select card from DbCard card " +
                  " where card.parentPath=:path";

  protected int deleteDbCollection(final String path) {
    if (path.equals("/")) {
      return 0;
    }

    if (getDbCollection(path, privUnbind) ==null) {
      throw new WebdavForbidden();
    }

    verifyPath(path);

    try {
      sess.createQuery(queryGetColCards);
      sess.setString("path", ensureEndSlash(path));

      /* We couldn't use DISTINCT in the query (it's a CLOB) so make it
       * distinct with a set
       */
      final var cardSet =
              new TreeSet<>((List<DbCard>)sess.getList());

      for (final DbCard cd: cardSet) {
        deleteDbCard(cd);
      }

      sess.createQuery(queryDeleteCollection);
      sess.setString("path", ensureEndSlash(path));

      return cardSet.size() + sess.executeUpdate();
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  /**
   * @param dbcard dbcopy
   * @return a Card object
   */
  protected Card makeVcard(final DbCard dbcard) {
    final Card card = new Card(dbcard.getVcard());

    card.setCreated(dbcard.getCreated());
    card.setLastmod(dbcard.getLastmod());
    card.setName(dbcard.getName());

    return card;
  }

  /**
   */
  protected void deleteDbCard(final DbCard dbcard) {
    try {
      sess.delete(dbcard);
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  /* ==============================================================
   *                   Session methods
   * ============================================================== */

  protected void checkOpen() {
    if (!isOpen()) {
      throw new WebdavException("Session call when closed");
    }
  }

  protected synchronized void openSession() {
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
      if (debug()) {
        debug("New hibernate session for " + objTimestamp);
      }
      sess = new HibSessionImpl();
      try {
        sess.init(getSessionFactory());
      } catch (final BedeworkException e) {
        throw new WebdavException(e);
      }
      debug("Open session for " + objTimestamp);
    }

    beginTransaction();
  }

  protected synchronized void closeSession() {
    if (!isOpen()) {
      if (debug()) {
        debug("Close for " + objTimestamp + " closed session");
      }
      return;
    }

    if (debug()) {
      debug("Close for " + objTimestamp);
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

        sess.close();
        sess = null;
      }
    } catch (final Throwable t) {
      try {
        sess.close();
      } catch (final Throwable ignored) {}
      sess = null; // Discard on error
    } finally {
      open = false;
    }
  }

  protected void beginTransaction() {
    checkOpen();

    if (debug()) {
      debug("Begin transaction for " + objTimestamp);
    }
    try {
      sess.beginTransaction();
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  protected void endTransaction() {
    checkOpen();

    if (debug()) {
      debug("End transaction for " + objTimestamp);
    }

    try {
      if (!sess.rolledback()) {
        sess.commit();
      }
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  protected void rollbackTransaction() {
    try {
      checkOpen();
      sess.rollback();
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    } finally {
    }
  }

  /*
  private void flush() {
    if (debug()) {
      debug("flush for " + objTimestamp);
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
                                     final boolean alwaysReturnResult) {
    if (ent == null) {
      return null;
    }

    if (superUser) {
      return ent;
    }

    final boolean noAccessNeeded = desiredAccess == privNone;

    final CurrentAccess ca =
            access.checkAccess(ent, desiredAccess,
                               alwaysReturnResult || noAccessNeeded);

    if (!noAccessNeeded && !ca.getAccessAllowed()) {
      return null;
    }

    return ent;
  }

  /* ==============================================================
   *                   private methods
   * ============================================================== */

  private SessionFactory getSessionFactory() {
    if (sessionFactory != null) {
      return sessionFactory;
    }

    synchronized (this) {
      if (sessionFactory != null) {
        return sessionFactory;
      }

      /* Get a new hibernate session factory. This is configured from an
       * application resource hibernate.cfg.xml together with some run time values
       */
      try {
        final Configuration conf = new Configuration();

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

        final StringBuilder sb = new StringBuilder();

        final List<String> ps = dbConfig.getOrmProperties();

        for (final String p: ps) {
          sb.append(p);
          sb.append("\n");
        }

        final Properties hprops = new Properties();
        hprops.load(new StringReader(sb.toString()));

        conf.addProperties(hprops).configure();

        sessionFactory = conf.buildSessionFactory();

        return sessionFactory;
      } catch (final Throwable t) {
        // Always bad.
        error(t);
        throw new WebdavException(t);
      }
    }
  }
}
