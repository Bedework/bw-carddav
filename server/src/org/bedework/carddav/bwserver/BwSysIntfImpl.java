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

import org.bedework.webdav.WdCollection;
import org.bedework.carddav.server.CarddavResource;
import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.server.PropertyHandler;
import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.carddav.server.CarddavColNode;
import org.bedework.carddav.server.SysIntf;
import org.bedework.carddav.server.Vcard;
import org.bedework.carddav.server.PropertyHandler.PropertyType;
import org.bedework.carddav.server.dirHandlers.DirHandlerFactory;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.util.User;

import edu.rpi.cct.webdav.servlet.shared.PrincipalPropertySearch;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavForbidden;
import edu.rpi.cct.webdav.servlet.shared.WebdavNotFound;
import edu.rpi.cct.webdav.servlet.shared.WebdavProperty;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.PropertyTagEntry;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler;
import edu.rpi.cmt.access.Access;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.Ace;
import edu.rpi.cmt.access.Acl;
import edu.rpi.cmt.access.PrincipalInfo;
import edu.rpi.cmt.access.Privilege;
import edu.rpi.cmt.access.Privileges;
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

  private String principalRoot;

  private static class HandlerKey {
    String prefix;
    String account;

    HandlerKey(String prefix, String account) {
      this.prefix = prefix;
      this.account = account;
    }

    public int hashCode() {
      int hc = prefix.hashCode();
      if (account != null) {
        hc *= account.hashCode();
      }

      return hc;
    }

    public boolean equals(Object o) {
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

  public void init(HttpServletRequest req,
                   String account,
                   CardDAVConfig conf,
                   boolean debug) throws WebdavException {
    try {
      this.account = account;
      this.conf = conf;
      this.debug = debug;

      urlHandler = new UrlHandler(req, true);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getAccount()
   */
  public String getAccount() throws WebdavException {
    return account;
  }

  private static class MyPropertyHandler extends PropertyHandler {
    private final static HashMap<QName, PropertyTagEntry> propertyNames =
      new HashMap<QName, PropertyTagEntry>();

    public Map<QName, PropertyTagEntry> getPropertyNames() {
      return propertyNames;
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getPropertyHandler(org.bedework.carddav.server.PropertyHandler.PropertyType)
   */
  public PropertyHandler getPropertyHandler(PropertyType ptype) throws WebdavException {
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
  public boolean isPrincipal(String val) throws WebdavException {
    try {
      return getHandler(val).isPrincipal(val);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getPrincipalInfo(java.lang.String)
   */
  public PrincipalInfo getPrincipalInfo(String href) throws WebdavException {
    String path = pathFromHref(href);
    PrincipalInfo pi = getHandler(path).getPrincipalInfo(path);

    if ((pi == null) || !pi.valid) {
      throw new WebdavNotFound(href);
    }

    return pi;
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#makeHref(edu.rpi.cmt.access.AccessPrincipal)
   */
  public String makeHref(AccessPrincipal p) throws WebdavException {
    try {
      return getUrlHandler().prefix(getHandler(principalRoot).makePrincipalUri(p));
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getGroups(java.lang.String, java.lang.String)
   */
  public Collection<String>getGroups(String rootUrl,
                                     String principalUrl) throws WebdavException {
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
   * @see org.bedework.carddav.server.SysIntf#getUserInfo(edu.rpi.cmt.access.AccessPrincipal, boolean)
   */
  public UserInfo getUserInfo(AccessPrincipal pcpl,
                              boolean getDirInfo) throws WebdavException {
    try {
      if ((pcpl == null) || pcpl.getUnauthenticated()) {
        return null;
      }

      String principalUri = getHandler(principalRoot).makePrincipalUri(pcpl);

      String userHomePath = principalUri + "/";

      String defaultAddressbookPath = userHomePath + conf.getDefaultAddressbook();

      Vcard dirInfo = null;

      DirHandler rootHandler = getHandler(principalRoot);

      if (getDirInfo) {
        dirInfo = rootHandler.getPrincipalCard(principalUri);
      }

      // XXX Cheat at this - we should just use principals throughout.
      String prefix = principalUri.substring(0, principalUri.length() -
                                             pcpl.getAccount().length());

      return new UserInfo(pcpl.getAccount(),
                          prefix,
                          userHomePath,
                          defaultAddressbookPath,
                          dirInfo);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getPrincipalCollectionSet(java.lang.String)
   */
  public Collection<String> getPrincipalCollectionSet(String resourceUri)
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
  public Collection<UserInfo> getPrincipals(String resourceUri,
                                               PrincipalPropertySearch pps)
          throws WebdavException {
    ArrayList<UserInfo> principals = new ArrayList<UserInfo>();

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

    UserInfo cui = null;

    if (addressbookHomeSet) {
      String path = getUrlHandler().unprefix(matchVal);

      CarddavCollection col = getCollection(path);
      if (col != null) {
        cui = getUserInfo(col.getOwner(), true);
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
  public void addCard(String path,
                      Vcard card) throws WebdavException {
    try {
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
  public void updateCard(String path,
                         Vcard card) throws WebdavException {
    try {
      getHandler(path).updateCard(path, card);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getCards(CarddavColNode, BwFilter)
   */
  public Collection<Vcard> getCards(CarddavCollection col, Filter filter)
          throws WebdavException {
    try {
      return getHandler(col.getPath()).getCards(col.getPath(),
                                                filter);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getCard(org.bedework.carddav.server.CarddavColNode, java.lang.String)
   */
  public Vcard getCard(String path,
                       String name) throws WebdavException {
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
  public void deleteCard(CarddavCardNode card) throws WebdavException {
    try {
      getHandler(card.getPath()).deleteCard(card);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void updateAccess(CarddavCardNode card,
                           Acl acl) throws WebdavException{
    try {
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  // XXX d access properly. For the moment owner is unlim, otherwise read only.

  private static String accessString;

  static {
    Acl acl = new Acl();

    try {
      acl.addAce(new Ace(null, false, Ace.whoTypeOwner, Access.all));
      acl.addAce(new Ace(null, false, Ace.whoTypeOther, Access.read));
      accessString = new String(acl.encode());
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public CurrentAccess checkAccess(CarddavCollection ent,
                                   int desiredAccess,
                                   boolean returnResult)
          throws WebdavException {
    try {
      return new Acl(debug).evaluateAccess(new User(account),
                                           ent.getOwner().getAccount(),
                                           new Privilege[]{Privileges.makePriv(desiredAccess)},
                                           accessString.toCharArray(),
                                           null);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void updateAccess(CarddavColNode col,
                           Acl acl) throws WebdavException {
    try {
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#makeCollection(org.bedework.carddav.server.CarddavCollection, java.lang.String)
   */
  public int makeCollection(CarddavCollection col,
                            String parentPath) throws WebdavException {
    try {
      handleStatus(getHandler(parentPath).makeCollection(col, parentPath));
      return HttpServletResponse.SC_CREATED;
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void deleteCollection(WdCollection col) throws WebdavException {
    try {
      getHandler(col.getPath()).deleteCollection(col);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#copyMove(org.bedework.webdav.WdCollection, org.bedework.webdav.WdCollection, boolean, boolean)
   */
  public void copyMove(WdCollection from,
                       WdCollection to,
                       boolean copy,
                       boolean overwrite) throws WebdavException {
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
  public boolean copyMove(Vcard from,
                          WdCollection to,
                          String name,
                          boolean copy,
                          boolean overwrite) throws WebdavException {
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
  public CarddavCollection getCollection(String path) throws WebdavException {
    try {
      return getHandler(path).getCollection(path);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#updateCollection(org.bedework.webdav.WdCollection)
   */
  public void updateCollection(WdCollection val) throws WebdavException {
    try {
      getHandler(val.getPath()).updateCollection(val);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getCollections(org.bedework.webdav.WdCollection)
   */
  public Collection<CarddavCollection> getCollections(CarddavCollection val) throws WebdavException {
    try {
      return getHandler(val.getPath()).getCollections(val.getPath());
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
  public void putFile(WdCollection coll,
                      CarddavResource val) throws WebdavException {
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
  public CarddavResource getFile(WdCollection coll,
                            String name) throws WebdavException {
    try {
      throw new WebdavException("unimplemented");
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.SysIntf#getFileContent(org.bedework.carddav.server.CarddavResource)
   */
  public void getFileContent(CarddavResource val) throws WebdavException {
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
  public Collection<CarddavResource> getFiles(WdCollection coll) throws WebdavException {
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
  public void updateFile(CarddavResource val,
                         boolean updateContent) throws WebdavException {
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
  public void deleteFile(CarddavResource val) throws WebdavException {
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
  public boolean copyMoveFile(CarddavResource from,
                              WdCollection to,
                              String name,
                              boolean copy,
                              boolean overwrite) throws WebdavException {
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
  private DirHandler getHandler(String path) throws WebdavException {
    try {
      /* First determine which configuration handles this path */
      DirHandlerConfig dhc = conf.findDirhandler(path);

      if (dhc == null) {
        return null;
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

      if (dh != null) {
        dh.open(account);
        openHandlers.add(dh);
      }

      return dh;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private int handleStatus(int st) throws WebdavException {
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

  private String pathFromHref(String href) throws WebdavException {
    return urlHandler.unprefix(href);
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

  protected void trace(String msg) {
    getLogger().debug(msg);
  }

  protected void debugMsg(String msg) {
    getLogger().debug(msg);
  }

  protected void warn(String msg) {
    getLogger().warn(msg);
  }

  protected void error(Throwable t) {
    getLogger().error(this, t);
  }

  protected void logIt(String msg) {
    getLogger().info(msg);
  }
}
