/* **********************************************************************
    Copyright 2010 Rensselaer Polytechnic Institute. All worldwide rights reserved.

    Redistribution and use of this distribution in source and binary forms,
    with or without modification, are permitted provided that:
       The above copyright notice and this permission notice appear in all
        copies and supporting documentation;

        The name, identifiers, and trademarks of Rensselaer Polytechnic
        Institute are not used in advertising or publicity without the
        express prior written permission of Rensselaer Polytechnic Institute;

    DISCLAIMER: The software is distributed" AS IS" without any express or
    implied warranty, including but not limited to, any implied warranties
    of merchantability or fitness for a particular purpose or any warrant)'
    of non-infringement of any current or pending patent rights. The authors
    of the software make no representations about the suitability of this
    software for any particular purpose. The entire risk as to the quality
    and performance of the software is with the user. Should the software
    prove defective, the user assumes the cost of all necessary servicing,
    repair or correction. In particular, neither Rensselaer Polytechnic
    Institute, nor the authors of the software are liable for any indirect,
    special, consequential, or incidental damages related to the software,
    to the maximum extent the law permits.
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
import edu.rpi.cmt.access.PrivilegeDefs;
import edu.rpi.cmt.access.WhoDefs;
import edu.rpi.cmt.access.Acl.CurrentAccess;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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
   * SessionFactory per server for the calendar.
   *
   * <p>static fields used this way are illegal in the j2ee specification
   * though we might get away with it here as the session factory only
   * contains parsed mappings for the calendar configuration. This should
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
//    try {
      DbCard card = getDbCard(path, name);

      if (card == null) {
        return null;
      }

      return makeVcard(card);/*
    } catch (WebdavException wde) {
      rollbackTransaction();
      throw wde;
    } catch (Throwable t) {
      rollbackTransaction();
      throw new WebdavException(t);
    } finally {
      endTransaction();
      closeSession();
    }*/
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCards(java.lang.String, org.bedework.carddav.server.filter.Filter, org.bedework.carddav.server.SysIntf.GetLimits)
   */
  @SuppressWarnings("unchecked")
  public GetResult getCards(final String path,
                            final Filter filter,
                            final GetLimits limits) throws WebdavException {
    verifyPath(path);

//    try {
  //    openSession();

      StringBuilder sb = new StringBuilder();

      sb.append("select distinct card from ");
      sb.append(DbCard.class.getName());
      sb.append(" card join card.properties props where card.parentPath=:path");

      DbFilter fltr = new DbFilter(sb);

      fltr.makeFilter(filter);

      sess.createQuery(sb.toString());
      sess.setString("path", path);

      fltr.parReplace(sess);

      List<DbCard> l = sess.getList();

      GetResult res = new GetResult();

      res.cards = new ArrayList<Card>();

      if (l == null) {
        return res;
      }

      for (DbCard dbc: l) {
        res.cards.add(makeVcard(dbc));
      }

      return res;
/*    } catch (WebdavException wde) {
      rollbackTransaction();
      throw wde;
    } catch (Throwable t) {
      rollbackTransaction();
      throw new WebdavException(t);
    } finally {
      endTransaction();
      closeSession();
    }*/
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
    sess.setString("path", path);

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

  protected DbCard getDbCard(final String path, final String name) throws WebdavException {
    verifyPath(path);

//    openSession();

    StringBuilder sb = new StringBuilder();

    sb.append("from ");
    sb.append(DbCard.class.getName());
    sb.append(" card where card.parentPath=:path");
    sb.append(" and card.name=:name");

    sess.createQuery(sb.toString());
    sess.setString("path", path);
    sess.setString("name", name);

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
    cdc.setPath(col.getParentPath() + "/" + col.getName());
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

    sess.setString("path", path);

    return (DbCollection)sess.getUnique();
  }

  /**
   * @param attrs
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
//    sess.close();
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

        conf.configure();

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
