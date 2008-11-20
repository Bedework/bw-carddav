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
package org.bedework.carddav.server.dirHandlers;

import org.bedework.carddav.bwserver.DirHandler;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.util.DirectoryInfo;
import org.bedework.carddav.util.DirHandlerConfig;

import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.Ace;
import edu.rpi.cmt.access.PrincipalInfo;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;

/** A base implementation of DirHandler which handles some generic directory
* methods.
*
* <p>One of those is to map an apparently flat identifier space onto a
* principal hierarchy more appropriate to the needs of webdav. For example we
* might have a user account "jim" or a ticket "TKT12345". These could be mapped
* on to "/principals/users/jim" and "/principals/tickets/12345".
*
* @author Mike Douglass douglm rpi.edu
* @version 1.0
*/
public abstract class AbstractDirHandler implements DirHandler {
  /** */
  protected CardDAVConfig cdConfig;
  /** */
  protected DirHandlerConfig dhConfig;

  /** */
  protected UrlHandler urlHandler;

  /** */
  protected boolean open;

  /** */
  protected boolean debug;

  /** */
  protected String account;

  /** */
  public final static String unknownPrincipalType =
    "org.bedework.webdav.unknownprincipaltype";

  /** */
  public final static String principalNotFound =
    "org.bedework.webdav.principalnotfound";

  private transient Logger log;

  private HashMap<String, Integer> toWho = new HashMap<String, Integer>();
  private HashMap<Integer, String> fromWho = new HashMap<Integer, String>();

  private HashMap<String, String> validUsers = new HashMap<String, String>();
  private long lastFlush;
  private static long flushTime = 60 * 1000;  // 1 minute

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#open(java.lang.String)
   */
  public void open(String account) throws WebdavException {
    this.account = account;
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#close()
   */
  public void close() {
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#init(org.bedework.carddav.util.CardDAVConfig, org.bedework.carddav.util.DirHandlerConfig, edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler)
   */
  public void init(CardDAVConfig cdConfig,
                   DirHandlerConfig dhConfig,
                   UrlHandler urlHandler) throws WebdavException {
    this.cdConfig = cdConfig;
    this.dhConfig = dhConfig;
    this.urlHandler = urlHandler;

    debug = dhConfig.getDebug();

    initWhoMaps(cdConfig.getUserPrincipalRoot(), Ace.whoTypeUser);
    initWhoMaps(cdConfig.getGroupPrincipalRoot(), Ace.whoTypeGroup);
    initWhoMaps(cdConfig.getTicketPrincipalRoot(), Ace.whoTypeTicket);
    initWhoMaps(cdConfig.getResourcePrincipalRoot(), Ace.whoTypeResource);
    initWhoMaps(cdConfig.getVenuePrincipalRoot(), Ace.whoTypeVenue);
    initWhoMaps(cdConfig.getHostPrincipalRoot(), Ace.whoTypeHost);
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#isOpen()
   */
  public boolean isOpen() {
    return open;
  }

  /**
   * @return
   * @throws WebdavException
   */
  public DirectoryInfo getDirectoryInfo() throws WebdavException {
    DirectoryInfo info = new DirectoryInfo();

    info.setPrincipalRoot(cdConfig.getPrincipalRoot());
    info.setUserPrincipalRoot(cdConfig.getUserPrincipalRoot());
    info.setGroupPrincipalRoot(cdConfig.getGroupPrincipalRoot());
    info.setTicketPrincipalRoot(cdConfig.getTicketPrincipalRoot());
    info.setResourcePrincipalRoot(cdConfig.getResourcePrincipalRoot());
    info.setVenuePrincipalRoot(cdConfig.getVenuePrincipalRoot());
    info.setHostPrincipalRoot(cdConfig.getHostPrincipalRoot());

    return info;
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#isPrincipal(java.lang.String)
   */
  public boolean isPrincipal(String val) throws WebdavException {
    if (val == null) {
      return false;
    }

    return val.startsWith(cdConfig.getPrincipalRoot() + "/");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getPrincipalInfo(java.lang.String)
   */
  public PrincipalInfo getPrincipalInfo(String path) throws WebdavException {
    PrincipalInfo pi = new PrincipalInfo();

    try {
      if (!isPrincipal(path)) {
        return null;
      }

      int start = -1;

      int end = path.length();
      if (path.endsWith("/")) {
        end--;
      }

      for (String prefix: toWho.keySet()) {
        if (!path.startsWith(prefix)) {
          continue;
        }

        pi.prefix = prefix;
        pi.whoType = toWho.get(prefix);
        start = prefix.length();

        if (start == end) {
          // Trying to browse user principals?
          pi.who = null;
        } else if (path.charAt(start) != '/') {
          throw new WebdavException(principalNotFound);
        } else if (pi.whoType == Ace.whoTypeUser) {
          /* Strip off the principal prefix for real users.
           */
          pi.who = path.substring(start + 1, end);
        } else {
          pi.who = path;
        }

        // XXX do this correctly - e.g. see if in directory
        pi.valid = true;

        return pi;
      }

      throw new WebdavException(principalNotFound);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#makePrincipalUri(edu.rpi.cmt.access.AccessPrincipal)
   */
  public String makePrincipalUri(AccessPrincipal p) throws WebdavException {
    if (isPrincipal(p.getAccount())) {
      return p.getAccount();
    }

    String root = fromWho.get(p.getKind());

    if (root == null) {
      throw new WebdavException(unknownPrincipalType);
    }

    return root + "/" + p.getAccount();
  }

  /* *
   * @return
   * @throws WebdavException
   * /
  public String getPrincipalRoot() throws WebdavException {
    return cdConfig.getPrincipalRoot();
  } */

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getGroups(java.lang.String, java.lang.String)
   */
  public Collection<String>getGroups(String rootUrl,
                                     String principalUrl) throws WebdavException {
    Collection<String> urls = new TreeSet<String>();

    if (principalUrl == null) {
      /* for the moment if the root url is the user principal hierarchy root
       * just return the current user principal
       */
      if (rootUrl.endsWith("/")) {
        rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
      }

      /* ResourceUri should be the principals root or user principal root */
      if (!rootUrl.equals(cdConfig.getPrincipalRoot()) &&
          !rootUrl.equals(cdConfig.getUserPrincipalRoot())) {
        return urls;
      }

//      urls.add(cdConfig.getUserPrincipalRoot() + "/" +
//               cb.getCurrentUser().getAccount());
    } else {
      // XXX incomplete
    }

    return urls;
  }

  /* ====================================================================
   *  Protected methods.
   * ==================================================================== */

  /** Ensure path matches our prefix.
   *
   * @param path
   * @throws WebdavException
   */
  protected void verifyPath(String path) throws WebdavException {
    if (!path.startsWith(dhConfig.getPathPrefix())) {
      throw new WebdavBadRequest("Invalid path for handler" + path);
    }

    int prefixLen = dhConfig.getPathPrefix().length();
    if ((path.length() > prefixLen) && (path.charAt(prefixLen) != '/')) {
      throw new WebdavBadRequest("Invalid path for handler" + path);
    }
  }

  /** See if we already cehcked this user
   *
   * @param account
   * @return boolean
   */
  protected synchronized boolean lookupUser(String account) {
    if ((lastFlush != 0) &&
        (System.currentTimeMillis() - lastFlush > flushTime)) {
      validUsers.clear();
    }

    return validUsers.containsKey(account);
  }

  /** Add a checked user to the table
   * @param account
   */
  protected void addValidUser(String account) {
    validUsers.put(account, account);
  }

  /**
   * @return Logger
   */
  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  /**
   * @param t
   */
  protected void error(Throwable t) {
    getLogger().error(this, t);
  }

  /**
   * @param msg
   */
  protected void trace(String msg) {
    getLogger().debug(msg);
  }

  /* ====================================================================
   *  Private methods.
   * ==================================================================== */

  private void initWhoMaps(String prefix, int whoType) {
    toWho.put(prefix, whoType);
    fromWho.put(whoType, prefix);
  }
}
