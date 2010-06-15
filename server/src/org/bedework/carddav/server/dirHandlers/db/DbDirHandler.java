/* **********************************************************************
    Copyright 2008 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
import org.bedework.carddav.server.dirHandlers.AbstractDirHandler;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.server.filter.PropFilter;
import org.bedework.carddav.server.filter.TextMatch;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.util.DbDirHandlerConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.vcard.Card;

import edu.rpi.cct.webdav.servlet.shared.UrlHandler;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.sss.util.Util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** Provide some common methods for db based directory handlers.
 *
 * @author douglm
 *
 */
public abstract class DbDirHandler extends AbstractDirHandler {
  protected DbDirHandlerConfig dbConfig;

  /** When we were created for debugging */
  protected Timestamp objTimestamp;

  /** Ensure we don't open while open
   */
  protected boolean isOpen;

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

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.dirHandlers.AbstractDirHandler#init(org.bedework.carddav.util.CardDAVConfig, org.bedework.carddav.util.DirHandlerConfig, edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler)
   */
  @Override
  public void init(final CardDAVConfig cdConfig,
                   final DirHandlerConfig dhConfig,
                   final UrlHandler urlHandler) throws WebdavException {
    super.init(cdConfig, dhConfig, urlHandler);

    dbConfig = (DbDirHandlerConfig)dhConfig;

    try {
      objTimestamp = new Timestamp(System.currentTimeMillis());
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCard(java.lang.String, java.lang.String)
   */
  public Card getCard(final String path, final String name) throws WebdavException {
    verifyPath(path);

    try {
      openSession();

      StringBuilder sb = new StringBuilder();

      sb.append("from ");
      sb.append(DbCard.class.getName());
      sb.append(" card where card.parentPath=:path");
      sb.append(" and card.name=:name");

      sess.createQuery(sb.toString());
      sess.setString("path", path);
      sess.setString("name", name);

      DbCard card = (DbCard)sess.getUnique();

      if (card == null) {
        return null;
      }

      return makeVcard(card);
    } catch (WebdavException wde) {
      rollbackTransaction();
      throw wde;
    } catch (Throwable t) {
      rollbackTransaction();
      throw new WebdavException(t);
    } finally {
      endTransaction();
      closeSession();
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCards(java.lang.String, org.bedework.carddav.server.filter.Filter, org.bedework.carddav.server.SysIntf.GetLimits)
   */
  @SuppressWarnings("unchecked")
  public GetResult getCards(final String path,
                            final Filter filter,
                            final GetLimits limits) throws WebdavException {
    verifyPath(path);

    try {
      openSession();

      StringBuilder sb = new StringBuilder();

      sb.append("from ");
      sb.append(DbCard.class.getName());
      sb.append(" card join card.properties props where card.parentPath=:path");

      makeFilter(sb, filter);

      sess.createQuery(sb.toString());
      sess.setString("path", path);

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
    } catch (WebdavException wde) {
      rollbackTransaction();
      throw wde;
    } catch (Throwable t) {
      rollbackTransaction();
      throw new WebdavException(t);
    } finally {
      endTransaction();
      closeSession();
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCollection(java.lang.String)
   */
  public CarddavCollection getCollection(final String path) throws WebdavException {
    verifyPath(path);

    /* We're fetching a collection entity with a fully specified path */
    try {
      openSession();

      StringBuilder sb = new StringBuilder();

      sb.append("from ");
      sb.append(DbCollection.class.getName());
      sb.append(" where col.parentPath=:path");
      sb.append(" and col.name=:name");

      SplitResult sr = splitUri(path);

      sess.createQuery(sb.toString());
      sess.setString("path", sr.path);
      sess.setString("name", sr.name);

      DbCollection col = (DbCollection)sess.getUnique();

      if (col == null) {
        return null;
      }

      return makeCdCollection(col);
    } catch (WebdavException wde) {
      rollbackTransaction();
      throw wde;
    } catch (Throwable t) {
      rollbackTransaction();
      throw new WebdavException(t);
    } finally {
      endTransaction();
      closeSession();
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCollections(java.lang.String, org.bedework.carddav.server.SysIntf.GetLimits)
   */
  @SuppressWarnings("unchecked")
  public GetResult getCollections(final String path,
                                  final GetLimits limits)
         throws WebdavException {
    verifyPath(path);

    try {
      openSession();

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
    } catch (WebdavException wde) {
      rollbackTransaction();
      throw wde;
    } catch (Throwable t) {
      rollbackTransaction();
      throw new WebdavException(t);
    } finally {
      endTransaction();
      closeSession();
    }
  }

  /* ====================================================================
   *  Protected methods.
   * ==================================================================== */

  protected CarddavCollection makeCdCollection(final DbCollection col) throws WebdavException {
    CarddavCollection cdc = new CarddavCollection();

    if (dbConfig.getAddressBook()) {
      /* This prefix is flagged as an address book. */
      cdc.setAddressBook(true);
    }

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

    return cdc;
  }

  private static final String parPrefix = "FPAR";

  private static class Fltr {
    int findex;
    List<String> params = new ArrayList<String>();
  }

  private Fltr makeFilter(final StringBuilder sb, final Filter filter) {
    if (filter == null) {
      return null;
    }

    Collection<PropFilter> pfilters = filter.getPropFilters();

    if ((pfilters == null) || pfilters.isEmpty()) {
      return null;
    }

    Fltr fltr = new Fltr();

    boolean first = true;

    for (PropFilter pfltr: pfilters) {
      makePropFilterExpr(fltr, sb, pfltr, first);

      first = false;
    }

    return fltr;
  }

  private void makePropFilterExpr(final Fltr fltr,
                                  final StringBuilder sb,
                                  final PropFilter filter,
                                  final boolean first) {
    TextMatch tm = filter.getMatch();

    if (tm == null) {
      return;
    }

    int testAllAnyProps = filter.getTestAllAny();

    String name = filter.getName();

    int cpos = name.indexOf(',');

    if ((cpos < 0) && (Util.isEmpty(filter.getParamFilters()))) {
      // No group - no params - single attribute

      if (!first) {
        if (testAllAnyProps == Filter.testAllOf) {
          sb.append(" and (");
        } else {
          sb.append(" or (");
        }
      }

      makePropFilterExpr(fltr, sb, name, tm);
    }

    /* TODO Do this later
    if (cpos > 0) {
      if (name.endsWith(",")) {
        // Don't do that
        return;
      }

      name = name.substring(cpos + 1);
    }

    Collection<String> anames = LdapMapping.toLdapAttrNoGroup.get(name);
    if (Util.isEmpty(anames)) {
      return;
    }

    StringBuilder sb = new StringBuilder();

    boolean first = true;

    for (String attrId: anames) {
      String ptest = makePropFilterExpr(attrId, tm);

      if (ptest == null) {
        continue;
      }

      sb.append(ptest);

      if (first) {
        first = false;
        continue;
      }

      sb.append(")");

      if (testAllAnyProps == Filter.testAllOf) {
        sb.append(" and ");
      } else {
        sb.append(" or ");
      }
    }

    return sb.toString();
    */
  }

  private void addPar(final Fltr fltr,
                      final StringBuilder sb,
                      final String val) {
    sb.append(":");
    sb.append(parPrefix);
    sb.append(fltr.findex);
    fltr.findex++;

    fltr.params.add(val);
  }

  private String makePropFilterExpr(final Fltr fltr,
                                    final StringBuilder sb,
                                    final String name, final TextMatch tm) {
    sb.append("(prop.name=");
    addPar(fltr, sb, name);
    sb.append(" and prop.value");

    int mt = tm.getMatchType();

    if (mt == TextMatch.matchTypeEquals) {
      sb.append("=");
      addPar(fltr, sb, tm.getVal());
    } else {
      sb.append(" like ");

      String val;

      if ((mt == TextMatch.matchTypeContains) ||
          (mt == TextMatch.matchTypeEndsWith)) {
        val = "%" + tm.getVal();
      } else {
        val = tm.getVal();
      }

      if ((mt == TextMatch.matchTypeContains) ||
          (mt == TextMatch.matchTypeStartsWith)) {
        val += "%";
      }

      addPar(fltr, sb, val);
    }

    sb.append(")");

    return sb.toString();
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
    if (!isOpen) {
      throw new WebdavException("Session call when closed");
    }
  }

  protected synchronized void openSession() throws WebdavException {
    if (isOpen) {
      throw new WebdavException("Already open");
    }

    isOpen = true;

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
    if (!isOpen) {
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
      isOpen = false;
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
