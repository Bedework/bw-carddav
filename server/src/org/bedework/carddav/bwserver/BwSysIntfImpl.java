/* **********************************************************************
    Copyright 2007 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
package org.bedework.carddav.bwserver;

import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.carddav.server.CarddavColNode;
import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.server.CarddavResource;
import org.bedework.carddav.server.PropertyHandler;
import org.bedework.carddav.server.SysIntf;
import org.bedework.carddav.server.PropertyHandler.PropertyType;
import org.bedework.carddav.server.dirHandlers.DirHandlerFactory;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.util.User;
import org.bedework.carddav.vcard.Card;

import edu.rpi.cct.webdav.servlet.shared.PrincipalPropertySearch;
import edu.rpi.cct.webdav.servlet.shared.UrlHandler;
import edu.rpi.cct.webdav.servlet.shared.WdCollection;
import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavForbidden;
import edu.rpi.cct.webdav.servlet.shared.WebdavNotFound;
import edu.rpi.cct.webdav.servlet.shared.WebdavProperty;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.PropertyTagEntry;
import edu.rpi.cmt.access.Access;
import edu.rpi.cmt.access.AccessException;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.Ace;
import edu.rpi.cmt.access.AceWho;
import edu.rpi.cmt.access.Acl;
import edu.rpi.cmt.access.Privilege;
import edu.rpi.cmt.access.Access.AccessCb;
import edu.rpi.cmt.access.Acl.CurrentAccess;
import edu.rpi.sss.util.xml.XmlUtil;
import edu.rpi.sss.util.xml.tagdefs.CarddavTags;
import edu.rpi.sss.util.xml.tagdefs.WebdavTags;

import org.apache.log4j.Logger;

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
public class BwSysIntfImpl implements SysIntf {
  private boolean debug;

  protected transient Logger log;

  private String account;

  private UrlHandler urlHandler;

  private CardDAVConfig conf;

  private static class HandlerKey {
    String prefix;
    String account;

    HandlerKey(final String prefix, final String account) {
      this.prefix = prefix;
      this.account = account;
    }

    @Override
    public int hashCode() {
      int hc = prefix.hashCode();
      if (account != null) {
        hc *= account.hashCode();
      }

      return hc;
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof HandlerKey)) {
        return false;
      }

      if (this == o) {
        return true;
      }

      HandlerKey that = (HandlerKey)o;

      if (!prefix.equals(that.prefix)) {
        return false;
      }

      if ((account != null) && (that.account != null)) {
        return account.equals(that.account);
      }

      return (account == null) && (that.account == null);
    }
  }
  /* Indexed by the prefix from the config and the account. */
  private Map<HandlerKey, DirHandler> handlers;

  private ArrayList<DirHandler> openHandlers = new ArrayList<DirHandler>();

  private static String userPrincipalRoot;
  private static String groupPrincipalRoot;
  private static String resourcePrincipalRoot;
  private static String venuePrincipalRoot;
  private static String ticketPrincipalRoot;
  private static String hostPrincipalRoot;

  /**
   * @author douglm
   */
  private class CDAccessCb implements AccessCb {
    public String makeHref(final String id, final int whoType) throws AccessException {
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

  private CDAccessCb accessCb = new CDAccessCb();

  public void init(final HttpServletRequest req,
                   final String account,
                   final CardDAVConfig conf,
                   final boolean debug) throws WebdavException {
    try {
      this.account = account;
      this.conf = conf;
      this.debug = debug;

      userPrincipalRoot= setRoot(conf.getUserPrincipalRoot());
      groupPrincipalRoot = setRoot(conf.getGroupPrincipalRoot());
      resourcePrincipalRoot = setRoot(conf.getResourcePrincipalRoot());
      venuePrincipalRoot = setRoot(conf.getVenuePrincipalRoot());
      ticketPrincipalRoot = setRoot(conf.getTicketPrincipalRoot());
      hostPrincipalRoot = setRoot(conf.getHostPrincipalRoot());

      urlHandler = new UrlHandler(req, true);

      if (account != null) {
        ensureProvisioned();
      }
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getPrincipal()
   */
  public AccessPrincipal getPrincipal() throws WebdavException {
    String href = conf.getUserPrincipalRoot();

    if (!href.endsWith("/")) {
      href += "/";
    }

    return getPrincipal(href + account);
  }

  private static class MyPropertyHandler extends PropertyHandler {
    private final static HashMap<QName, PropertyTagEntry> propertyNames =
      new HashMap<QName, PropertyTagEntry>();

    @Override
    public Map<QName, PropertyTagEntry> getPropertyNames() {
      return propertyNames;
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getPropertyHandler(org.bedework.carddav.server.PropertyHandler.PropertyType)
   */
  public PropertyHandler getPropertyHandler(final PropertyType ptype) throws WebdavException {
    return new MyPropertyHandler();
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getUrlHandler()
   */
  public UrlHandler getUrlHandler() {
    return urlHandler;
  }

  /* (non-Javadoc)
   * @see org.bedework.caldav.server.SysIntf#getUrlPrefix()
   * /
  public String getUrlPrefix() {
    return urlPrefix;
  }

  /* (non-Javadoc)
   * @see org.bedework.caldav.server.SysIntf#getRelativeUrls()
   * /
  public boolean getRelativeUrls() {
    return relativeUrls;
  }*/

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#isPrincipal(java.lang.String)
   */
  public boolean isPrincipal(final String val) throws WebdavException {
    try {
      return getHandler(val).isPrincipal(val);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getPrincipal(java.lang.String)
   */
  public AccessPrincipal getPrincipal(final String href) throws WebdavException {
    String path = pathFromHref(href);
    AccessPrincipal ap = getHandler(path).getPrincipal(path);

    if (ap == null) {
      throw new WebdavNotFound(href);
    }

    return ap;
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#makeHref(edu.rpi.cmt.access.AccessPrincipal)
   */
  public String makeHref(final AccessPrincipal p) throws WebdavException {
    try {
      return getUrlHandler().prefix(getHandler(conf.getPrincipalRoot()).makePrincipalUri(p));
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getGroups(java.lang.String, java.lang.String)
   */
  public Collection<String>getGroups(final String rootUrl,
                                     final String principalUrl) throws WebdavException {
    try {
      return getHandler(rootUrl).getGroups(rootUrl, principalUrl);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getDirectoryBrowsingDisallowed()
   */
  public boolean getDirectoryBrowsingDisallowed() throws WebdavException {
    return conf.getDirectoryBrowsingDisallowed();
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getPrincipalInfo(edu.rpi.cmt.access.AccessPrincipal, boolean)
   */
  public PrincipalInfo getPrincipalInfo(final AccessPrincipal pcpl,
                                        final boolean getDirInfo) throws WebdavException {
    try {
      if ((pcpl == null) || pcpl.getUnauthenticated()) {
        return null;
      }

      String principalUri = getHandler(conf.getPrincipalRoot()).makePrincipalUri(pcpl);

      DirHandler addrBookHandler = getHandler(conf.getAddressBookHandlerPrefix());

      String userHomePath = addrBookHandler.getprincipalHome(pcpl);

      String defaultAddressbookPath = userHomePath + conf.getDefaultAddressbook();

      Card dirInfo = null;

      String cardPath = getCardPath(pcpl);

      if (getDirInfo && (cardPath != null)) {
        DirHandler cardHandler = getHandler(cardPath);

        int p = cardPath.lastIndexOf('/');

        dirInfo = cardHandler.getCard(cardPath.substring(0, p),
                                      cardPath.substring(p));
      }

      // XXX Cheat at this - we should just use principals throughout.
      String prefix = principalUri.substring(0, principalUri.length() -
                                             pcpl.getAccount().length());

      return new PrincipalInfo(pcpl.getAccount(),
                               prefix,
                               userHomePath,
                               defaultAddressbookPath,
                               cardPath,
                               dirInfo);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private String getCardPath(final AccessPrincipal pcpl) throws WebdavException {
    DirHandlerConfig dhc = conf.findDirhandler(pcpl.getPrincipalRef());

    // XXX This is a temp fix to get us a linkage to our vcard
    // It needs more work

    String cardPathPrefix = null;

    // Try the prefixes

    String cardPathPrefixes = dhc.getCardPathPrefixes();

    /* The dirhandler prefix */
    String pfx = dhc.getPathPrefix();

    if (!pfx.endsWith("/")) {
      pfx += "/";
    }

    if (!pcpl.getPrincipalRef().startsWith(pfx)) {
      // Something wrong
      return null;
    }

    String account = pcpl.getPrincipalRef().substring(pfx.length());

    if (cardPathPrefixes != null) {
      String[] prefixInfo = cardPathPrefixes.split(",");

      for (String pi: prefixInfo) {
        if (pi == null) {
          continue;
        }

        if (!pi.contains(":")) {
          // Default
          cardPathPrefix = pi;
          continue;
        }

        String[] pfxPath = pi.split(":");

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
      return null;
    }

    StringBuilder sb = new StringBuilder(cardPathPrefix);

    if (!cardPathPrefix.endsWith("/")) {
      sb.append("/");
    }

    sb.append(account);
    sb.append(".vcf");

    return sb.toString();
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getPrincipalCollectionSet(java.lang.String)
   */
  public Collection<String> getPrincipalCollectionSet(final String resourceUri)
          throws WebdavException {
    try {
      ArrayList<String> al = new ArrayList<String>();

      al.add(conf.getPrincipalRoot());

      return al;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getPrincipals(java.lang.String, edu.rpi.cct.webdav.servlet.shared.PrincipalPropertySearch)
   */
  public Collection<PrincipalInfo> getPrincipals(String resourceUri,
                                               final PrincipalPropertySearch pps)
          throws WebdavException {
    ArrayList<PrincipalInfo> principals = new ArrayList<PrincipalInfo>();

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

    for (PrincipalPropertySearch.PropertySearch ps: pps.propertySearches) {
      for (WebdavProperty prop: ps.props) {
        if (CarddavTags.addressbookHomeSet.equals(prop.getTag())) {
          addressbookHomeSet = true;
        } else {
          return principals;
        }
      }

      String mval;
      try {
        mval = XmlUtil.getElementContent(ps.match);
      } catch (Throwable t) {
        throw new WebdavException("org.bedework.carddavintf.badvalue");
      }

      if (debug) {
        debugMsg("Try to match " + mval);
      }

      if ((matchVal != null) && (!matchVal.equals(mval))) {
        return principals;
      }

      matchVal = mval;
    }

    PrincipalInfo cui = null;

    if (addressbookHomeSet) {
      String path = getUrlHandler().unprefix(matchVal);

      CarddavCollection col = getCollection(path);
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

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#addCard(java.lang.String, org.bedework.carddav.server.Vcard)
   */
  public void addCard(final String path,
                      final Card card) throws WebdavException {
    try {
      if (card.getOwner() == null) {
        card.setOwner(makeOwner());
      }

      getHandler(path).addCard(path, card);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#updateCard(java.lang.String, org.bedework.carddav.server.Vcard)
   */
  public void updateCard(final String path,
                         final Card card) throws WebdavException {
    try {
      getHandler(path).updateCard(path, card);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getCards(org.bedework.carddav.server.CarddavCollection, org.bedework.carddav.server.filter.Filter, org.bedework.carddav.server.SysIntf.GetLimits)
   */
  public GetResult getCards(final CarddavCollection col,
                                 final Filter filter,
                                 final GetLimits limits) throws WebdavException {
    try {
      return getHandler(col.getPath()).getCards(col.getPath(),
                                                filter, limits);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getCard(org.bedework.carddav.server.CarddavColNode, java.lang.String)
   */
  public Card getCard(final String path,
                       final String name) throws WebdavException {
    try {
      return getHandler(path).getCard(path, name);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#deleteCard(org.bedework.carddav.server.CarddavCardNode)
   */
  public void deleteCard(final CarddavCardNode card) throws WebdavException {
    try {
      getHandler(card.getPath()).deleteCard(card);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void updateAccess(final CarddavCardNode card,
                           final Acl acl) throws WebdavException{
    try {
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  // XXX d access properly. For the moment owner is unlim, otherwise read only.

  private static String accessString;

  static {
    AceWho owner = AceWho.getAceWho(null, Ace.whoTypeOwner, false);
    AceWho other = AceWho.getAceWho(null, Ace.whoTypeOther, false);

    Collection<Privilege> allPrivs = new ArrayList<Privilege>();
    allPrivs.add(Access.all);

    Collection<Privilege> readPrivs = new ArrayList<Privilege>();
    readPrivs.add(Access.read);

    try {
      Collection<Ace> aces = new ArrayList<Ace>();

      aces.add(Ace.makeAce(owner, allPrivs, null));
      aces.add(Ace.makeAce(other, readPrivs, null));
      accessString = new String(new Acl(aces).encode());
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public CurrentAccess checkAccess(final CarddavCollection ent,
                                   final int desiredAccess,
                                   final boolean returnResult)
          throws WebdavException {
    try {
      return Acl.evaluateAccess(accessCb,
                                getPrincipal(),
                                ent.getOwner(),
                                Access.privSetAny,
                                accessString.toCharArray(),
                                null);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void updateAccess(final CarddavColNode col,
                           final Acl acl) throws WebdavException {
    try {
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
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
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void deleteCollection(final WdCollection col) throws WebdavException {
    try {
      getHandler(col.getPath()).deleteCollection(col);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#copyMove(edu.rpi.cct.webdav.servlet.shared.WdCollection, edu.rpi.cct.webdav.servlet.shared.WdCollection, boolean, boolean)
   */
  public void copyMove(final WdCollection from,
                       final WdCollection to,
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
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }

    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#copyMove(org.bedework.carddav.server.Vcard, org.bedework.webdav.WdCollection, java.lang.String, boolean, boolean)
   */
  public boolean copyMove(final Card from,
                          final WdCollection to,
                          final String name,
                          final boolean copy,
                          final boolean overwrite) throws WebdavException {
    try {
      return handleStatus(
           getHandler(to.getPath()).copyMove(from,
                                             to.getPath(), name,
                                             copy,
                                             overwrite)) == DirHandler.statusCreated;
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getCollection(java.lang.String)
   */
  public CarddavCollection getCollection(final String path) throws WebdavException {
    try {
      return getHandler(path).getCollection(path);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#updateCollection(edu.rpi.cct.webdav.servlet.shared.WdCollection)
   */
  public void updateCollection(final WdCollection val) throws WebdavException {
    try {
      getHandler(val.getPath()).updateCollection(val);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
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
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* ====================================================================
   *                   Files
   * ==================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#putFile(org.bedework.webdav.WdCollection, org.bedework.carddav.server.CarddavResource)
   */
  public void putFile(final WdCollection coll,
                      final CarddavResource val) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getFile(org.bedework.webdav.WdCollection, java.lang.String)
   */
  public CarddavResource getFile(final WdCollection coll,
                            final String name) throws WebdavException {
    try {
      return null;
      //throw new WebdavException("unimplemented");
    //} catch (WebdavException wde) {
    //  throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getFileContent(org.bedework.carddav.server.CarddavResource)
   */
  public void getFileContent(final CarddavResource val) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getFiles(org.bedework.webdav.WdCollection)
   */
  public Collection<CarddavResource> getFiles(final WdCollection coll) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#updateFile(org.bedework.carddav.server.CarddavResource, boolean)
   */
  public void updateFile(final CarddavResource val,
                         final boolean updateContent) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#deleteFile(org.bedework.carddav.server.CarddavResource)
   */
  public void deleteFile(final CarddavResource val) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#copyMoveFile(org.bedework.carddav.server.CarddavResource, org.bedework.webdav.WdCollection, java.lang.String, boolean, boolean)
   */
  public boolean copyMoveFile(final CarddavResource from,
                              final WdCollection to,
                              final String name,
                              final boolean copy,
                              final boolean overwrite) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getMaxUserEntitySize()
   */
  public int getMaxUserEntitySize() throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void close() throws WebdavException {
    for (DirHandler handler: openHandlers) {
      if (handler.isOpen()) {
        handler.close();
      }
    }

    openHandlers.clear();
  }

  /* ====================================================================
   *                         Private methods
   * ==================================================================== */

  /**
   * @return DirHandler
   * @throws WebdavException
   */
  private DirHandler getHandler(final String path) throws WebdavException {
    try {
      /* First determine which configuration handles this path */
      DirHandlerConfig dhc = conf.findDirhandler(path);

      if (dhc == null) {
        throw new WebdavBadRequest("Bad path " + path);
      }

      DirHandler dh = null;

      /* See if we have a handler for this path and this account */

      HandlerKey hk = new HandlerKey(dhc.getPathPrefix(), account);

      if (handlers != null) {
        dh = handlers.get(hk);
      }

      if (dh == null) {
        // Get one from the factory and open it.
        dh = DirHandlerFactory.getHandler(dhc.getClassName());
        dh.init(conf, dhc, urlHandler);

        if (handlers == null) {
          handlers = new HashMap<HandlerKey, DirHandler>();
        }

        handlers.put(hk, dh);
      }

      if (dh == null) {
        throw new WebdavBadRequest("Bad path " + path);
      }
      dh.open(account);
      openHandlers.add(dh);

      return dh;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
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

  private String pathFromHref(final String href) throws WebdavException {
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

    if (conf.getAddressBookHandlerPrefix() == null) {
      throw new WebdavException("No default address book handler prefix");
    }

    StringBuilder sb = new StringBuilder();

    sb.append(conf.getAddressBookHandlerPrefix());
    sb.append("/");

    String adbhPfx = sb.toString();

    sb.append(account);
    sb.append("/");

    String userHome = sb.toString();

    CarddavCollection col = getCollection(userHome);

    boolean noHome = col == null;
    User owner = new User(account);
    owner.setPrincipalRef(userPrincipalRoot + account);

    if (noHome) {
      int resp = makeCollection(makeCdCollection(adbhPfx,
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

    int resp = makeCollection(makeCdCollection(userHome,
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
    CarddavCollection cdc = new CarddavCollection();

    cdc.setAddressBook(addrBook);
    cdc.setName(name);
    cdc.setDisplayName(name);

    cdc.setOwner(owner);
    cdc.setPath(parentPath + "/" + name);
    cdc.setParentPath(parentPath);

    return cdc;
  }

  private AccessPrincipal makeOwner() {
    User owner = new User(account);
    owner.setPrincipalRef(userPrincipalRoot + account);

    return owner;
  }

  /* ====================================================================
   *                        Protected methods
   * ==================================================================== */

  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  protected void trace(final String msg) {
    getLogger().debug(msg);
  }

  protected void debugMsg(final String msg) {
    getLogger().debug(msg);
  }

  protected void warn(final String msg) {
    getLogger().warn(msg);
  }

  protected void error(final Throwable t) {
    getLogger().error(this, t);
  }

  protected void logIt(final String msg) {
    getLogger().info(msg);
  }
}
