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
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.util.DirectoryInfo;
import org.bedework.carddav.util.Group;
import org.bedework.carddav.util.User;

import edu.rpi.cct.webdav.servlet.shared.UrlHandler;
import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.Ace;

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
  public void open(final String account) throws WebdavException {
    this.account = account;
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#close()
   */
  public void close() throws WebdavException {
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#init(org.bedework.carddav.util.CardDAVConfig, org.bedework.carddav.util.DirHandlerConfig, edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler)
   */
  public void init(final CardDAVConfig cdConfig,
                   final DirHandlerConfig dhConfig,
                   final UrlHandler urlHandler) throws WebdavException {
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
  public boolean isPrincipal(final String val) throws WebdavException {
    if (val == null) {
      return false;
    }

    return val.startsWith(cdConfig.getPrincipalRoot() + "/");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getPrincipal(java.lang.String)
   */
  public AccessPrincipal getPrincipal(final String path) throws WebdavException {
    try {
      if (!isPrincipal(path)) {
        return null;
      }

      int start = -1;

      int end = path.length();
      if (path.endsWith("/")) {
        end--;
      }

      int whoType;
      String who = null;

      for (String prefix: toWho.keySet()) {
        if (!path.startsWith(prefix)) {
          continue;
        }

        whoType = toWho.get(prefix);
        start = prefix.length();

        if (start == end) {
          // Trying to browse user principals?
        } else if (path.charAt(start) != '/') {
          throw new WebdavException(principalNotFound);
        } else if ((whoType == Ace.whoTypeUser) ||
                   (whoType == Ace.whoTypeGroup)) {
          /* Strip off the principal prefix for real users.
           */
          who = path.substring(start + 1, end);
        } else {
          who = path;
        }

        AccessPrincipal ap = null;

        if (who != null) {
          if (whoType == Ace.whoTypeUser) {
            ap = new User(who);
          } else if (whoType == Ace.whoTypeGroup) {
            ap = new Group(who);
          }
        }

        ap.setPrincipalRef(path);

        return ap;
      }

      throw new WebdavException(principalNotFound);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#makePrincipalUri(edu.rpi.cmt.access.AccessPrincipal)
   */
  public String makePrincipalUri(final AccessPrincipal p) throws WebdavException {
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
                                     final String principalUrl) throws WebdavException {
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

  public String getprincipalHome() throws WebdavException {
    return dhConfig.getPathPrefix() + "/" + account + "/";
  }

  /* ====================================================================
   *  Protected methods.
   * ==================================================================== */

  /** Ensure path matches our prefix.
   *
   * @param path
   * @throws WebdavException
   */
  protected void verifyPath(final String path) throws WebdavException {
    if (!path.startsWith(dhConfig.getPathPrefix())) {
      throw new WebdavBadRequest("Invalid path for handler" + path);
    }

    int prefixLen = dhConfig.getPathPrefix().length();
    if ((path.length() > prefixLen + 1) && (path.charAt(prefixLen) != '/')) {
      throw new WebdavBadRequest("Invalid path for handler" + path);
    }
  }

  /** See if we already checked this user
   *
   * @param account
   * @return boolean
   */
  protected synchronized boolean lookupUser(final String account) {
    if ((lastFlush != 0) &&
        (System.currentTimeMillis() - lastFlush > flushTime)) {
      validUsers.clear();
    }

    return validUsers.containsKey(account);
  }

  /** Add a checked user to the table
   * @param account
   */
  protected void addValidUser(final String account) {
    validUsers.put(account, account);
  }

  /** Make a principal ref from an account
   * @param account
   * @param type
   */
  protected String makePrincipalHref(final String account,
                                     final int whoType) throws WebdavException {
    String root = fromWho.get(whoType);

    if (root == null) {
      throw new WebdavException(unknownPrincipalType);
    }

    return root + "/" + account;
  }

  protected static class SplitResult {
    /** */
    public String path;
    /** */
    public String name;

    SplitResult(final String path, final String name) {
      this.path = path;
      this.name = name;
    }
  }

  /* Split the uri so that result.path is the path up to the name part result.name
   *
   * NormalizeUri was called previously so we have no trailing "/"
   */
  protected SplitResult splitUri(final String uri) throws WebdavException {
    String noEndSlash;

    if (uri.endsWith("/")) {
      noEndSlash = uri.substring(0, uri.length() - 1);
    } else {
      noEndSlash = uri;
    }

    int pos = noEndSlash.lastIndexOf("/");
    if (pos < 0) {
      // bad uri
      throw new WebdavBadRequest("Invalid uri: " + uri);
    }

    if (pos == 0) {
      return new SplitResult(noEndSlash, null);
    }

    return new SplitResult(noEndSlash.substring(0, pos), noEndSlash.substring(pos + 1));
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
  protected void error(final Throwable t) {
    getLogger().error(this, t);
  }

  /**
   * @param msg
   */
  protected void warn(final String msg) {
    getLogger().warn(msg);
  }

  /**
   * @param msg
   */
  protected void trace(final String msg) {
    getLogger().debug(msg);
  }

  /* ====================================================================
   *  Private methods.
   * ==================================================================== */

  private void initWhoMaps(final String prefix, final int whoType) {
    toWho.put(prefix, whoType);
    fromWho.put(whoType, prefix);
  }
}
