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
package org.bedework.carddav.common;

import org.bedework.access.AccessPrincipal;
import org.bedework.access.Ace;
import org.bedework.carddav.common.config.CardDAVConfigI;
import org.bedework.carddav.common.config.DirHandlerConfig;
import org.bedework.carddav.common.util.DirectoryInfo;
import org.bedework.carddav.common.util.Group;
import org.bedework.carddav.common.util.User;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;
import org.bedework.webdav.servlet.shared.UrlHandler;
import org.bedework.webdav.servlet.shared.WebdavBadRequest;
import org.bedework.webdav.servlet.shared.WebdavException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
public abstract class AbstractDirHandler
        implements Logged, DirHandler {
  /** */
  protected CardDAVConfigI cdConfig;
  /** */
  protected DirHandlerConfig<?> dhConfig;

  /** */
  protected UrlHandler urlHandler;

  /** */
  protected boolean open;

  /** */
  protected String account;

  /** */
  protected boolean superUser;

  /** */
  public final static String unknownPrincipalType =
    "org.bedework.webdav.unknownprincipaltype";

  /** */
  public final static String principalNotFound =
    "org.bedework.webdav.principalnotfound";

  /* This DOES NOT have the trailing "/" on the root prefix */
  private final HashMap<String, Integer> toWho = new HashMap<>();

  /* This DOES have the trailing "/" on the root prefix */
  private final HashMap<Integer, String> fromWho = new HashMap<>();

  private final HashMap<String, String> validUsers = new HashMap<>();
  private long lastFlush;
  private final static long flushTime = 60 * 1000;  // 1 minute

  public void open(final String account) {
    this.account = account;
    superUser = "root".equals(account) || // allow SuperUser
            "admin".equals(account);
  }

  /* standard mbean attributes - unfinished * /
  private static List<MBeanAttributeInfo> standardMbeanAttrs;

  static {
    standardMbeanAttrs = new ArrayList<MBeanAttributeInfo>();
    standardMbeanAttrs.add(MBeanUtil.stringAttrInfo("pathPrefix",
                                                  "Selector for handler"));

    standardMbeanAttrs.add(MBeanUtil.stringAttrInfo("cardPathPrefix",
        "If non-null defines the prefix for principal cards"));

    standardMbeanAttrs.add(MBeanUtil.stringAttrInfo("cardPathPrefixes",
        "If non-null defines the prefixes for principal cards based on an account prefix"));

    standardMbeanAttrs.add(MBeanUtil.stringAttrInfo("addressBook",
        "True if this prefix represents an addressbook"));

    standardMbeanAttrs.add(MBeanUtil.stringAttrInfo("className",
                                                    "Handler class"));

    standardMbeanAttrs.add(MBeanUtil.stringAttrInfo("ownerHref",
                                                    "Href of owner for this path"));

    standardMbeanAttrs.add(MBeanUtil.stringAttrInfo("cardKind",
                                                    "If set defines the default kind in this directory"));
  } */

  @Override
  public void close() {
  }

  @Override
  public void init(final CardDAVConfigI cdConfig,
                   final DirHandlerConfig<?> dhConfig,
                   final UrlHandler urlHandler) {
    this.cdConfig = cdConfig;
    this.dhConfig = dhConfig;
    this.urlHandler = urlHandler;

    initWhoMaps(cdConfig.getUserPrincipalRoot(), Ace.whoTypeUser);
    initWhoMaps(cdConfig.getGroupPrincipalRoot(), Ace.whoTypeGroup);
    initWhoMaps(cdConfig.getTicketPrincipalRoot(), Ace.whoTypeTicket);
    initWhoMaps(cdConfig.getResourcePrincipalRoot(), Ace.whoTypeResource);
    initWhoMaps(cdConfig.getVenuePrincipalRoot(), Ace.whoTypeVenue);
    initWhoMaps(cdConfig.getHostPrincipalRoot(), Ace.whoTypeHost);

    /* This is wrong - needs to be done as a one time setup - perhaps in the
     * constructors

    //get the available MBean servers
    ArrayList list = MBeanServerFactory.findMBeanServer(null);
    //take the first one
    MBeanServer server = (MBeanServer)list.get(0);

    //build the MBean name
    try {
      ObjectName on = new ObjectName("org.bedework:service=carddav,handler=" + dhConfig.getPathPrefix());
      server.registerMBean(this, on);
    } catch (Throwable t) {
      t.printStackTrace();
    } */
  }

  @Override
  public boolean isOpen() {
    return open;
  }

  @Override
  public boolean exportData(final String dataOutPath) {
    return false;
  }

  /**
   * @return info
   */
  public DirectoryInfo getDirectoryInfo() {
    final DirectoryInfo info = new DirectoryInfo();

    info.setPrincipalRoot(cdConfig.getPrincipalRoot());
    info.setUserPrincipalRoot(cdConfig.getUserPrincipalRoot());
    info.setGroupPrincipalRoot(cdConfig.getGroupPrincipalRoot());
    info.setTicketPrincipalRoot(cdConfig.getTicketPrincipalRoot());
    info.setResourcePrincipalRoot(cdConfig.getResourcePrincipalRoot());
    info.setVenuePrincipalRoot(cdConfig.getVenuePrincipalRoot());
    info.setHostPrincipalRoot(cdConfig.getHostPrincipalRoot());

    return info;
  }

  @Override
  public boolean isPrincipal(final String val) {
    if (val == null) {
      return false;
    }

    return val.startsWith(cdConfig.getPrincipalRoot() + "/");
  }

  @Override
  public AccessPrincipal getPrincipal(final String path) {
    try {
      if (!isPrincipal(path)) {
        return null;
      }

      int end = path.length();
      if (path.endsWith("/")) {
        end--;
      }

      final int whoType;
      String who = null;

      for (final String prefix: toWho.keySet()) {
        if (!path.startsWith(prefix)) {
          continue;
        }

        whoType = toWho.get(prefix);
        final int start = prefix.length();

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

        if (ap == null) {
          return null;
        }

        ap.setPrincipalRef(path);

        return ap;
      }

      throw new WebdavException(principalNotFound);
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public String makePrincipalUri(final AccessPrincipal p) {
    if (isPrincipal(p.getAccount())) {
      return p.getAccount();
    }

    final String root = fromWho.get(p.getKind());

    if (root == null) {
      throw new WebdavException(unknownPrincipalType);
    }

    return root + p.getAccount();
  }

  @Override
  public Collection<String>getGroups(String rootUrl,
                                     final String principalUrl) {
    final Collection<String> urls = new TreeSet<>();

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

  @Override
  public String getprincipalHome() {
    return Util.buildPath(true, dhConfig.getPathPrefix(), "/", account);
  }

  @Override
  public String getprincipalHome(final AccessPrincipal p) {
    return Util.buildPath(true, dhConfig.getPathPrefix(), "/", p.getAccount());
  }

  @Override
  public CollectionBatcher getCollections(final String path) {
    return null;
  }

  /* ==============================================================
   *  Protected methods.
   * ============================================================== */

  /** Ensure path matches our prefix - without any trailing "/".
   *
   * @param path to match
   */
  protected void verifyPath(final String path) {
    if (!path.startsWith(dhConfig.getPathPrefix())) {
      throw new WebdavBadRequest("Invalid path for handler" + path);
    }

    final int prefixLen = dhConfig.getPathPrefix().length();
    if ((path.length() > (prefixLen + 1)) && (path.charAt(prefixLen) != '/')) {
      throw new WebdavBadRequest("Invalid path for handler" + path);
    }
  }

  /** See if we already checked this user
   *
   * @param account to check
   * @return boolean
   */
  protected synchronized boolean lookupUser(final String account) {
    if ((lastFlush != 0) &&
        ((System.currentTimeMillis() - lastFlush) > flushTime)) {
      validUsers.clear();
    }

    return validUsers.containsKey(account);
  }

  /** Add a checked user to the table
   * @param account to add
   */
  protected void addValidUser(final String account) {
    validUsers.put(account, account);
  }

  /** Make a principal ref from an account
   * @param account id
   * @param whoType type of principal
   */
  protected String makePrincipalHref(final String account,
                                     final int whoType) {
    final String root = fromWho.get(whoType);

    if (root == null) {
      throw new WebdavException(unknownPrincipalType);
    }

    return root + account;
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
  protected SplitResult splitUri(final String uri) {
    final String noEndSlash;

    if (uri.endsWith("/")) {
      noEndSlash = uri.substring(0, uri.length() - 1);
    } else {
      noEndSlash = uri;
    }

    final int pos = noEndSlash.lastIndexOf("/");
    if (pos < 0) {
      // bad uri
      throw new WebdavBadRequest("Invalid uri: " + uri);
    }

    if (pos == 0) {
      return new SplitResult(noEndSlash, null);
    }

    return new SplitResult(noEndSlash.substring(0, pos), noEndSlash.substring(pos + 1));
  }

  protected String ensureEndSlash(final String val) {
    if (val.endsWith("/")) {
      return val;
    }

    return  val + "/";
  }

  /* ==============================================================
   *  Dynamic mbean methods. - unfinishd
   * ============================================================== * /

  public Object getAttribute(final String attribute)
      throws AttributeNotFoundException, MBeanException, ReflectionException {
    if (dhConfig == null) {
      return null;
    }

    String val = dhConfig.getPropertyValue(attribute);
    if (val != null) {

    }
    return null;
  }
  */

  private final Map<String, String> toPrincipalPrefix = new HashMap<>();

  /** Convert the prefix that selects the dir handler e.g. "/public/people/"
   * to a valid principal prefix, e.g. "/principals/users/"
   *
   * @param ourPrefix selects the dir handler
   * @param principalPrefix e.g. "/principals/users/"
   */
  public void addToPrincipal(final String ourPrefix,
                             final String principalPrefix) {
    toPrincipalPrefix.put(ourPrefix, principalPrefix);
  }

  public String getToPrincipal(final String ourPrefix) {
    return toPrincipalPrefix.get(ourPrefix);
  }

  /* ==============================================================
   *  Private methods.
   * ============================================================== */

  private void initWhoMaps(final String prefix, final int whoType) {
    toWho.put(prefix, whoType);

    if (prefix.endsWith("/")) {
      fromWho.put(whoType, prefix);
    } else {
      fromWho.put(whoType, prefix + "/");
    }
  }

  /* ==============================================================
   *                   Logged methods
   * ============================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
