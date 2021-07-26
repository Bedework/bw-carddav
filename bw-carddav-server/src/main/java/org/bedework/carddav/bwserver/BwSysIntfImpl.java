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
package org.bedework.carddav.bwserver;

import org.bedework.access.Access;
import org.bedework.access.Access.AccessCb;
import org.bedework.access.AccessPrincipal;
import org.bedework.access.Ace;
import org.bedework.access.AceWho;
import org.bedework.access.Acl;
import org.bedework.access.CurrentAccess;
import org.bedework.access.EvaluatedAccessCache;
import org.bedework.access.Privilege;
import org.bedework.carddav.common.CarddavCollection;
import org.bedework.carddav.common.DirHandler;
import org.bedework.carddav.common.GetLimits;
import org.bedework.carddav.common.GetResult;
import org.bedework.carddav.common.config.DirHandlerConfig;
import org.bedework.carddav.common.filter.Filter;
import org.bedework.carddav.common.util.Group;
import org.bedework.carddav.common.util.User;
import org.bedework.carddav.common.vcard.Card;
import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.carddav.server.CarddavColNode;
import org.bedework.carddav.server.CarddavResource;
import org.bedework.carddav.server.PropertyHandler;
import org.bedework.carddav.server.PropertyHandler.PropertyType;
import org.bedework.carddav.server.SysIntf;
import org.bedework.carddav.server.config.CardDAVConfig;
import org.bedework.carddav.server.config.CardDAVContextConfig;
import org.bedework.carddav.server.dirHandlers.DirHandlerFactory;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;
import org.bedework.util.xml.tagdefs.CarddavTags;
import org.bedework.util.xml.tagdefs.WebdavTags;
import org.bedework.webdav.servlet.shared.PrincipalPropertySearch;
import org.bedework.webdav.servlet.shared.UrlHandler;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WebdavException;
import org.bedework.webdav.servlet.shared.WebdavForbidden;
import org.bedework.webdav.servlet.shared.WebdavNotFound;
import org.bedework.webdav.servlet.shared.WebdavNsNode.PropertyTagEntry;
import org.bedework.webdav.servlet.shared.WebdavProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

/** Bedework implementation of carddav SysIntf.
 *
 * @author Mike Douglass douglm at rpi.edu
 */
public class BwSysIntfImpl implements Logged, SysIntf {
  private DirHandlerFactory dhf;

  private String account;

  private UrlHandler urlHandler;

  private CardDAVConfig conf;

  private CardDAVContextConfig ctxConf;

  private static String userPrincipalRoot;
  private static String groupPrincipalRoot;
  private static String resourcePrincipalRoot;
  private static String venuePrincipalRoot;
  private static String ticketPrincipalRoot;
  private static String hostPrincipalRoot;

  /**
   * @author douglm
   */
  private static class CDAccessCb implements AccessCb {
    @Override
    public String makeHref(final String id, final int whoType) {
      if (id.startsWith("/")) {
        return id;
      }

      if (whoType == Ace.whoTypeUser) {
        return userPrincipalRoot + id;
      }

      if (whoType == Ace.whoTypeGroup) {
        return groupPrincipalRoot + id;
      }

      if (whoType == Ace.whoTypeResource) {
        return resourcePrincipalRoot + id;
      }

      if (whoType == Ace.whoTypeVenue) {
        return venuePrincipalRoot + id;
      }

      if (whoType == Ace.whoTypeTicket) {
        return ticketPrincipalRoot + id;
      }

      if (whoType == Ace.whoTypeHost) {
        return hostPrincipalRoot + id;
      }

      return id;
    }
  }

  private final CDAccessCb accessCb = new CDAccessCb();

  @Override
  public void init(final HttpServletRequest req,
                   final String account,
                   final CardDAVConfig conf,
                   final CardDAVContextConfig ctxConf) throws WebdavException {
    try {
      this.account = account;
      this.conf = conf;
      this.ctxConf = ctxConf;

      userPrincipalRoot= setRoot(conf.getUserPrincipalRoot());
      groupPrincipalRoot = setRoot(conf.getGroupPrincipalRoot());
      resourcePrincipalRoot = setRoot(conf.getResourcePrincipalRoot());
      venuePrincipalRoot = setRoot(conf.getVenuePrincipalRoot());
      ticketPrincipalRoot = setRoot(conf.getTicketPrincipalRoot());
      hostPrincipalRoot = setRoot(conf.getHostPrincipalRoot());

      urlHandler = new UrlHandler(req, true);

      dhf = new DirHandlerFactory(conf);

      if (account != null) {
        ensureProvisioned();
      }
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public String getDefaultContentType() {
    return "application/vcard";
  }

  @Override
  public String getNotificationURL() {
    return null;
  }

  @Override
  public AccessPrincipal getPrincipal() throws WebdavException {
    return getPrincipal(Util.buildPath(true, conf.getUserPrincipalRoot(),
                                       "/", account));
  }

  private static class MyPropertyHandler extends PropertyHandler {
    private final static HashMap<QName, PropertyTagEntry> propertyNames =
      new HashMap<>();

    @Override
    public Map<QName, PropertyTagEntry> getPropertyNames() {
      return propertyNames;
    }
  }

  @Override
  public PropertyHandler getPropertyHandler(final PropertyType ptype) {
    return new MyPropertyHandler();
  }

  @Override
  public UrlHandler getUrlHandler() {
    return urlHandler;
  }

  @Override
  public boolean allowsSyncReport(final WdCollection col) {
    return false;
  }

  @Override
  public boolean isPrincipal(final String val) throws WebdavException {
    try {
      if ((val == null) || val.endsWith(".vcf")) {
        return false;
      }
      final DirHandler dh = getPrincipalHandler(val, false);

      if (dh == null) {
        return false;
      }

      return dh.isPrincipal(val);
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public AccessPrincipal getPrincipal(final String href) throws WebdavException {
    final String path = pathFromHref(href);
    final AccessPrincipal ap =
            getPrincipalHandler(path, true).getPrincipal(path);

    if (ap == null) {
      throw new WebdavNotFound(href);
    }

    return ap;
  }

  @Override
  public String makeHref(final AccessPrincipal p) throws WebdavException {
    try {
      String principalPrefix = p.getPrincipalRef();

      if (principalPrefix == null) {
        if (p instanceof User) {
          principalPrefix = userPrincipalRoot;
        } else if (p instanceof Group) {
          principalPrefix = groupPrincipalRoot;
        } else {
          principalPrefix = conf.getPrincipalRoot();
        }
      }

      return getUrlHandler().prefix(
              dhf.getPrincipalHandler(principalPrefix,
                                      account,
                                      urlHandler,
                                      true).makePrincipalUri(p));
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public Collection<String>getGroups(final String rootUrl,
                                     final String principalUrl) throws WebdavException {
    try {
      return getHandler(rootUrl).getGroups(rootUrl, principalUrl);
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public boolean getDirectoryBrowsingDisallowed() {
    return ctxConf.getDirectoryBrowsingDisallowed();
  }

  @Override
  public PrincipalInfo getPrincipalInfo(final AccessPrincipal pcpl,
                                        final boolean getDirInfo) throws WebdavException {
    try {
      if ((pcpl == null) || pcpl.getUnauthenticated()) {
        return null;
      }

      final String principalUri = getHandler(conf.getPrincipalRoot()).makePrincipalUri(pcpl);

      final DirHandler addrBookHandler = getHandler(ctxConf.getAddressBookHandlerPrefix());

      final String userHomePath = addrBookHandler.getprincipalHome(pcpl);

      final String defaultAddressbookPath = userHomePath + conf.getDefaultAddressbook();

      Card dirInfo = null;

      final String cardPath = getCardPath(pcpl);

      if (getDirInfo && (cardPath != null)) {
        final DirHandler cardHandler = getHandler(cardPath);

        final int p = cardPath.lastIndexOf('/');

        dirInfo = cardHandler.getCard(cardPath.substring(0, p),
                                      cardPath.substring(p));
      }

      // XXX Cheat at this - we should just use principals throughout.
      final String prefix =
              principalUri.substring(0, principalUri.length() -
                      pcpl.getAccount().length());

      return new PrincipalInfo(pcpl.getAccount(),
                               prefix,
                               userHomePath,
                               defaultAddressbookPath,
                               cardPath,
                               dirInfo);
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  private String getCardPath(final AccessPrincipal pcpl) throws WebdavException {
    final DirHandlerConfig<?> dhc = 
            conf.findPrincipalDirhandler(pcpl.getPrincipalRef());

    // XXX This is a temp fix to get us a linkage to our vcard
    // It needs more work

    String cardPathPrefix = null;

    // Try the prefixes

    final String cardPathPrefixes = dhc.getCardPathPrefixes();

    /* The dirhandler prefix */
    /*
    String pfx = dhc.getPathPrefix();

    if (!pfx.endsWith("/")) {
      pfx += "/";
    }

    if (!pcpl.getPrincipalRef().startsWith(pfx)) {
      // Something wrong
      return null;
    }
    */

    String account = pcpl.getAccount();

    if (cardPathPrefixes != null) {
      final String[] prefixInfo = cardPathPrefixes.split(",");

      for (final String pi: prefixInfo) {
        if (pi == null) {
          continue;
        }

        if (!pi.contains(":")) {
          // Default
          cardPathPrefix = pi;
          continue;
        }

        final String[] pfxPath = pi.split(":");

        if (account.startsWith(pfxPath[0])) {
          cardPathPrefix = pfxPath[1];
          account = account.substring(pfxPath[0].length());
          break;
        }
      }
    }

    if (cardPathPrefix == null) {
      // Try explicit single path
      cardPathPrefix = dhc.getCardPathPrefix();
    }

    if (cardPathPrefix == null) {
      // Use the path prefix for this handler.
      cardPathPrefix = dhc.getPathPrefix();
//      return null;
    }

    return Util.buildPath(false, cardPathPrefix, "/", account, ".vcf");
  }

  @Override
  public Collection<String> getPrincipalCollectionSet(final String resourceUri)
          throws WebdavException {
    try {
      final ArrayList<String> al = new ArrayList<>();

      al.add(conf.getPrincipalRoot());

      return al;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public Collection<PrincipalInfo> getPrincipals(String resourceUri,
                                               final PrincipalPropertySearch pps)
          throws WebdavException {
    final ArrayList<PrincipalInfo> principals = new ArrayList<>();

    if (pps.applyToPrincipalCollectionSet) {
      /* I believe it's valid (if unhelpful) to return nothing
       */
      return principals;
    }

    if (!resourceUri.endsWith("/")) {
      resourceUri += "/";
    }

    String proot = conf.getPrincipalRoot();

    if (!proot.endsWith("/")) {
      proot += "/";
    }

    if (!resourceUri.equals(proot)) {
      return principals;
    }

    /* If we don't support any of the properties in the searches we don't match.
     *
     * Currently we only support addressbookHomeSet.
     *
     * For addressbookHomeSet it must be a valid home uri
     */
    String matchVal = null;
    boolean addressbookHomeSet = false;

    for (final WebdavProperty prop: pps.props) {
      if (CarddavTags.addressbookHomeSet.equals(prop.getTag())) {
        if ((matchVal != null) && (!matchVal.equals(prop.getPval()))) {
          return principals;
        }

        addressbookHomeSet = true;
        matchVal = prop.getPval();
      } else {
        return principals;
      }
    }

    PrincipalInfo cui = null;

    if (addressbookHomeSet) {
      final String path = getUrlHandler().unprefix(matchVal);

      final CarddavCollection col = getCollection(path);
      if (col != null) {
        cui = getPrincipalInfo(col.getOwner(), true);
      }
    }

    if (cui != null) {
      principals.add(cui);
    }

    return principals;
  }

  /* ====================================================================
   *                   Cards
   * ==================================================================== */

  @Override
  public void addCard(final String path,
                      final Card card) throws WebdavException {
    try {
      if (card.getOwner() == null) {
        card.setOwner(makeOwner());
      }

      getHandler(path).addCard(path, card);
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void updateCard(final String path,
                         final Card card) throws WebdavException {
    try {
      //card.setLastmod();
      getHandler(path).updateCard(path, card);
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public GetResult getCards(final CarddavCollection col,
                            final Filter filter,
                            final GetLimits limits) throws WebdavException {
    try {
      return getHandler(col.getPath()).getCards(col.getPath(),
                                                filter, limits);
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public Card getCard(final String path,
                       final String name) throws WebdavException {
    try {
      return getHandler(path).getCard(path, name);
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void deleteCard(final CarddavCardNode card) throws WebdavException {
    try {
      getHandler(card.getPath()).deleteCard(card.getPath());
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void updateAccess(final CarddavCardNode card,
                           final Acl acl) {
  }

  // XXX d access properly. For the moment owner is unlim, otherwise read only.

  private static final String accessString;

  static {
    final AceWho owner = AceWho.getAceWho(null, Ace.whoTypeOwner, false);
    final AceWho other = AceWho.getAceWho(null, Ace.whoTypeOther, false);

    final Collection<Privilege> allPrivs = new ArrayList<>();
    allPrivs.add(Access.all);

    final Collection<Privilege> readPrivs = new ArrayList<>();
    readPrivs.add(Access.read);

    try {
      final Collection<Ace> aces = new ArrayList<>();

      aces.add(Ace.makeAce(owner, allPrivs, null));
      aces.add(Ace.makeAce(other, readPrivs, null));
      accessString = new String(new Acl(aces).encode());
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public CurrentAccess checkAccess(final CarddavCollection ent,
                                   final int desiredAccess,
                                   final boolean returnResult)
          throws WebdavException {
    try {
      return EvaluatedAccessCache.evaluateAccess(accessCb,
                                                 getPrincipal(),
                                                 ent.getOwner(),
                                                 Access.privSetAny,
                                                 accessString.toCharArray(),
                                                 null);
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void updateAccess(final CarddavColNode col,
                           final Acl acl) {
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#makeCollection(org.bedework.carddav.server.CarddavCollection, java.lang.String)
   */
  public int makeCollection(final CarddavCollection col,
                            final String parentPath) throws WebdavException {
    try {
      if (col.getOwner() == null) {
        col.setOwner(makeOwner());
      }

      handleStatus(getHandler(parentPath).makeCollection(col, parentPath));
      return HttpServletResponse.SC_CREATED;
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void deleteCollection(final WdCollection<?> col) throws WebdavException {
    try {
      getHandler(col.getPath()).deleteCollection(col);
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void copyMove(final WdCollection<?> from,
                       final WdCollection<?> to,
                       final boolean copy,
                       final boolean overwrite) throws WebdavException {
    try {
      if (!copy) {
        /* Move the from collection to the new location "to".
         * If the parent calendar is the same in both cases, this is just a rename.
         */
        if ((from.getPath() == null) || (to.getPath() == null)) {
          throw new WebdavForbidden("Cannot move root");
        }

        if (from.getPath().equals(to.getPath())) {
          // Rename
          handleStatus(getHandler(from.getPath()).rename(from, to.getName()));
          return;
        }
      }
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }

    throw new WebdavException("unimplemented");
  }

  @Override
  public boolean copyMove(final Card from,
                          final WdCollection<?> to,
                          final String name,
                          final boolean copy,
                          final boolean overwrite) throws WebdavException {
    try {
      return handleStatus(
           getHandler(to.getPath()).copyMove(from,
                                             to.getPath(), name,
                                             copy,
                                             overwrite)) == DirHandler.statusCreated;
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public CarddavCollection getCollection(final String path) throws WebdavException {
    try {
      return getHandler(path).getCollection(path);
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void updateCollection(final WdCollection<?> val) throws WebdavException {
    try {
      getHandler(val.getPath()).updateCollection(val);
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getCollections(org.bedework.carddav.server.CarddavCollection, org.bedework.carddav.server.SysIntf.GetLimits)
   */
  public GetResult getCollections(final CarddavCollection val,
                                  final GetLimits limits) throws WebdavException {
    try {
      return getHandler(val.getPath()).getCollections(val.getPath(), limits);
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* ====================================================================
   *                   Files
   * ==================================================================== */

  @Override
  public void putFile(final WdCollection<?> coll,
                      final CarddavResource val) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public CarddavResource getFile(final WdCollection<?> coll,
                            final String name) throws WebdavException {
    try {
      return null;
      //throw new WebdavException("unimplemented");
    //} catch (final WebdavException wde) {
    //  throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void getFileContent(final CarddavResource val) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public Collection<CarddavResource> getFiles(final WdCollection<?> coll) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void updateFile(final CarddavResource val,
                         final boolean updateContent) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void deleteFile(final CarddavResource val) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public boolean copyMoveFile(final CarddavResource from,
                              final WdCollection<?> to,
                              final String name,
                              final boolean copy,
                              final boolean overwrite) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getMaxUserEntitySize()
   */
  public int getMaxUserEntitySize() throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void close() throws WebdavException {
    dhf.close();
  }

  /* ====================================================================
   *                         Private methods
   * ==================================================================== */

  private DirHandler getHandler(final String path) throws WebdavException {
    return dhf.getHandler(path, account, urlHandler);
  }

  private DirHandler getPrincipalHandler(final String href,
                                         final boolean required) throws WebdavException {
    return dhf.getPrincipalHandler(href, account, urlHandler, required);
  }

  private int handleStatus(final int st) throws WebdavException {
    if (st == DirHandler.statusOK) {
      return st;
    }

    if (st == DirHandler.statusCreated) {
      return st;
    }

    if (st == DirHandler.statusDuplicateUid) {
      throw new WebdavForbidden("duplicate uid");
    }

    if (st == DirHandler.statusDuplicate) {
      throw new WebdavForbidden(WebdavTags.resourceMustBeNull);
    }

    if (st == DirHandler.statusIllegal) {
      throw new WebdavForbidden();
    }

    if (st == DirHandler.statusNoAccess) {
      throw new WebdavForbidden();
    }

    if (st == DirHandler.statusChangeUid) {
      throw new WebdavForbidden("Cannot change uid");
    }

    if (st == DirHandler.statusDestinationExists) {
      throw new WebdavForbidden("Destination exists");
    }

    throw new WebdavException(st);
  }

  private String pathFromHref(final String href) {
    return urlHandler.unprefix(href);
  }

  private String setRoot(final String val) {
    if (!val.endsWith("/")) {
      return val + "/";
    }

    return val;
  }

  private void ensureProvisioned() throws WebdavException {
    /* Ensure the current account is fully provisioned - i.e. has home and
     * address book.
     */
    if (conf.getDefaultAddressbook() == null) {
      throw new WebdavException("No default address book");
    }

    if (ctxConf.getAddressBookHandlerPrefix() == null) {
      throw new WebdavException("No default address book handler prefix");
    }

    final StringBuilder sb = new StringBuilder();

    sb.append(ctxConf.getAddressBookHandlerPrefix());
    sb.append("/");

    final String adbhPfx = sb.toString();

    sb.append(account);
    sb.append("/");

    final String userHome = sb.toString();

    CarddavCollection col = getCollection(userHome);

    final boolean noHome = col == null;
    final User owner = new User(account);
    owner.setPrincipalRef(userPrincipalRoot + account);

    if (noHome) {
      final int resp = makeCollection(makeCdCollection(adbhPfx,
                                                       account,
                                                       false,
                                                       owner),
                                      adbhPfx);
      if (resp != HttpServletResponse.SC_CREATED) {
        throw new WebdavException("Unable to create user home: " + resp);
      }
    }

    sb.append(conf.getDefaultAddressbook());
    sb.append("/");

    boolean noAddrbook = noHome;

    if (!noHome) {
      col = getCollection(sb.toString());
      noAddrbook = col == null;
    }

    if (!noAddrbook) {
      return;
    }

    final int resp = makeCollection(
            makeCdCollection(userHome,
                             conf.getDefaultAddressbook(),
                             true,
                             owner),
            userHome);
    if (resp != HttpServletResponse.SC_CREATED) {
      throw new WebdavException("Unable to create user addressbook: " + resp);
    }
  }

  private CarddavCollection makeCdCollection(final String parentPath,
                                             final String name,
                                             final boolean addrBook,
                                             final AccessPrincipal owner) throws WebdavException {
    final CarddavCollection cdc = new CarddavCollection();

    cdc.setAddressBook(addrBook);
    cdc.setName(name);
    cdc.setDisplayName(name);

    cdc.setOwner(owner);
    cdc.setPath(Util.buildPath(true, parentPath, "/", name));
    cdc.setParentPath(parentPath);

    return cdc;
  }

  private AccessPrincipal makeOwner() {
    final User owner = new User(account);
    owner.setPrincipalRef(userPrincipalRoot + account);

    return owner;
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
