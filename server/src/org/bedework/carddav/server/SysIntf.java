/* **********************************************************************
    Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
package org.bedework.carddav.server;

import org.bedework.webdav.WdCollection;
import org.bedework.carddav.server.PropertyHandler.PropertyType;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.util.CardDAVConfig;

import edu.rpi.cct.webdav.servlet.shared.PrincipalPropertySearch;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.Acl;
import edu.rpi.cmt.access.PrincipalInfo;
import edu.rpi.cmt.access.Acl.CurrentAccess;

import java.io.Serializable;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

/** All interactions with the underlying calendar system are made via this
 * interface.
 *
 * <p>We're using the bedework object classes here. To simplify matters (a little)
 * we don't have distinct event, todo and journal classes. They are all currently
 * the BwEvent class with an entityType defining what the object represents.
 *
 * @author Mike Douglass douglm at rpi.edu
 */
public interface SysIntf {
  /** Called before any other method is called to allow initialisation to
   * take place at the first or subsequent requests
   *
   * @param req
   * @param account
   * @param conf  per application type configuration
   * @param debug
   * @throws WebdavException
   */
  public void init(HttpServletRequest req,
                   String account,
                   CardDAVConfig conf,
                   boolean debug) throws WebdavException;

  /** Return the current account
   *
   * @return String
   * @throws WebdavException
   */
  public String getAccount() throws WebdavException;

  /** Get a property handler
   *
   * @param ptype
   * @return PropertyHandler
   * @throws WebdavException
   */
  public PropertyHandler getPropertyHandler(PropertyType ptype) throws WebdavException;

  /**
   * @return UrlHandler object to manipulate urls.
   */
  public UrlHandler getUrlHandler();

  /* *
   * @return String url prefix derived from request.
   * /
  public String getUrlPrefix();

  /* *
   * @return boolean - true if using relative urls for broken clients
   * /
  public boolean getRelativeUrls();*/

  /** Does the value appear to represent a valid principal?
   *
   * @param val
   * @return true if it's a (possible) principal
   * @throws WebdavException
   */
  public boolean isPrincipal(String val) throws WebdavException;

  /** Return principal information for the given href. Also tests for a valid
   * principal.
   *
   * @param href
   * @return PrincipalInfo
   * @throws WebdavException
   */
  public PrincipalInfo getPrincipalInfo(String href) throws WebdavException;

  /**
   * @param p
   * @return String href
   * @throws WebdavException
   */
  public String makeHref(AccessPrincipal p) throws WebdavException;

  /** The urls should be principal urls. principalUrl can null for the current user.
   * The result is a collection of principal urls of which the given url is a
   * member, based upon rootUrl. For example, if rootUrl points to the base of
   * the user principal hierarchy, then the rsult should be at least the current
   * user's principal url, remembering that user principals are themselves groups
   * and the user is considered a member of their own group.
   *
   * @param rootUrl - url to base search on.
   * @param principalUrl - url of principal or null for current user
   * @return Collection of urls - always non-null
   * @throws WebdavException
   */
  public Collection<String>getGroups(String rootUrl,
                                     String principalUrl) throws WebdavException;

  /** Do we allow browsing of directories?
   *
   * @return boolean true if browsing disallowed
   * @throws WebdavException  for errors
   */
  public boolean getDirectoryBrowsingDisallowed() throws WebdavException;

  /**
   * @author Mike Douglass
   */
  public static class UserInfo implements Serializable {
    /** account as returned by caladdrToUser
     *
     * Currently tail end of principal path
     */
    public String account;

    /** principal path prefix
     */
    public String principalPathPrefix;

    /** Path to user home
     */
    public String userHomePath;

    /** Path to default address book
     */
    public String defaultAddressbookPath;

    /** Some directory information for the user.
     */
    public Vcard directoryInfo;

    /**
     * @param account
     * @param principalPathPrefix
     * @param userHomePath
     * @param defaultAddressbookPath
     * @param directoryInfo
     */
    public UserInfo(String account, String principalPathPrefix,
                    String userHomePath,
                    String defaultAddressbookPath, Vcard directoryInfo) {
      this.account = account;
      this.principalPathPrefix = principalPathPrefix;
      this.userHomePath = userHomePath;
      this.defaultAddressbookPath = defaultAddressbookPath;
      this.directoryInfo = directoryInfo;
    }
  }

  /** Given a valid user account return the associated calendar user information
   * needed for caldav interactions.
   *
   * @param pcpl         the principal
   * @param getDirInfo  get directory info if true and available.
   * @return CalUserInfo or null if not caladdr for this system
   * @throws WebdavException  for errors
   */
  public UserInfo getUserInfo(AccessPrincipal pcpl,
                              boolean getDirInfo) throws WebdavException;

  /** Given a uri returns a Collection of uris that allow search operations on
   * principals for that resource.
   *
   * @param resourceUri
   * @return Collection of String
   * @throws WebdavException
   */
  public Collection<String> getPrincipalCollectionSet(String resourceUri)
         throws WebdavException;

  /** Given a PrincipalPropertySearch returns a Collection of matching principals.
   *
   * @param resourceUri
   * @param pps Collection of PrincipalPropertySearch
   * @return Collection of CalUserInfo
   * @throws WebdavException
   */
  public Collection<UserInfo> getPrincipals(String resourceUri,
                                  PrincipalPropertySearch pps)
          throws WebdavException;

  /* ====================================================================
   *                   Cards
   * ==================================================================== */

  /** Add a card.
   *
   * @param path       to collection
   * @param card         Object to be added
   * @throws WebdavException
   */
 public void addCard(String path,
                     Vcard card) throws WebdavException;

  /** Update a card.
   *
   * @param path       to collection
   * @param card         Object to be updated
   * @throws WebdavException
   */
  public void updateCard(String path,
                         Vcard card) throws WebdavException;

  /** Return the cards for the current user in the given collection using the
   * supplied filter.
   *
   * @param col       collection
   * @param filter - if non-null defines a search filter
   * @return Collection  populated card objects
   * @throws WebdavException
   */
  public Collection<Vcard> getCards(CarddavCollection col,
                                    Filter filter)
          throws WebdavException;

  /** Get card given the collection and String name.
   *
   * @param path       to collection
   * @param name       String possible name
   * @return Vcard or null
   * @throws WebdavException
   */
  public Vcard getCard(String path, String name)
          throws WebdavException;

  /**
   * @param card
   * @throws WebdavException
   */
  public void deleteCard(CarddavCardNode card) throws WebdavException;

  /**
   * @param card
   * @param acl
   * @throws WebdavException
   */
  public void updateAccess(CarddavCardNode card,
                           Acl acl) throws WebdavException;

  /** Check the access for the given entity. Returns the current access
   * or null or optionally throws a no access exception.
   *
   * @param ent
   * @param desiredAccess
   * @param returnResult
   * @return CurrentAccess
   * @throws WebdavException if returnResult false and no access
   */
  public CurrentAccess checkAccess(CarddavCollection ent,
                                   int desiredAccess,
                                   boolean returnResult)
          throws WebdavException;

  /**
   * @param col
   * @param acl
   * @throws WebdavException
   */
  public void updateAccess(CarddavColNode col,
                           Acl acl) throws WebdavException;

  /**
   * @param col   Initialised collection object
   * @param addressBook
   * @param parentPath
   * @return int status
   * @throws WebdavException
   */
  public int makeCollection(CarddavCollection col,
                            String parentPath) throws WebdavException;

  /**
   * @param cal
   * @throws WebdavException
   */
  public void deleteCollection(WdCollection cal) throws WebdavException;

  /** Copy or move the collection to another location.
   * Status is set on return
   *
   * @param from      Source collection
   * @param to        Destination collection
   * @param copy      true for copying
   * @param overwrite destination exists
   * @throws WebdavException
   */
  public void copyMove(WdCollection from,
                       WdCollection to,
                       boolean copy,
                       boolean overwrite) throws WebdavException;

  /** Copy or move the given entity to the destination collection with the given name.
   * Status is set on return
   *
   * @param from      Source entity
   * @param to        Destination collection
   * @param name      String name of new entity
   * @param copy      true for copying
   * @param overwrite destination exists
   * @return true if destination created (i.e. not updated)
   * @throws WebdavException
   */
  public boolean copyMove(Vcard from,
                          WdCollection to,
                          String name,
                          boolean copy,
                          boolean overwrite) throws WebdavException;

  /** Get a collection given the path
   *
   * @param  path     String path of collection
   * @return WdCollection null for unknown collection
   * @throws WebdavException
   */
  public CarddavCollection getCollection(String path) throws WebdavException;

  /** Update a collection.
   *
   * @param val           updated WdCollection object
   * @throws WebdavException
   */
  public void updateCollection(WdCollection val) throws WebdavException;

  /** Returns children of the given collection to which the current user has
   * some access.
   *
   * @param  col          parent collection
   * @return Collection   of WdCollection
   * @throws WebdavException
   */
  public Collection<CarddavCollection> getCollections(CarddavCollection col)
          throws WebdavException;

  /* ====================================================================
   *                   Files
   * ==================================================================== */

  /** PUT a file.
   *
   * @param coll         WdCollection defining recipient collection
   * @param val          BwResource
   * @throws WebdavException
   */
  public void putFile(WdCollection coll,
                      CarddavResource val) throws WebdavException;

  /** GET a file.
   *
   * @param coll         WdCollection containing file
   * @param name
   * @return BwResource
   * @throws WebdavException
   */
  public CarddavResource getFile(WdCollection coll,
                            String name) throws WebdavException;

  /** Get resource content given the resource. It will be set in the resource
   * object
   *
   * @param  val BwResource
   * @throws WebdavException
   */
  public void getFileContent(CarddavResource val) throws WebdavException;

  /** Get the files in a collection.
   *
   * @param coll         WdCollection containing file
   * @return Collection of BwResource
   * @throws WebdavException
   */
  public Collection<CarddavResource> getFiles(WdCollection coll) throws WebdavException;

  /** Update a file.
   *
   * @param val          BwResource
   * @param updateContent if true we also update the content
   * @throws WebdavException
   */
  public void updateFile(CarddavResource val,
                         boolean updateContent) throws WebdavException;

  /** Delete a file.
   *
   * @param val          BwResource
   * @throws WebdavException
   */
  public void deleteFile(CarddavResource val) throws WebdavException;

  /** Copy or move the given file to the destination collection with the given name.
   * Status is set on return
   *
   * @param from      Source resource
   * @param to        Destination collection
   * @param name      String name of new entity
   * @param copy      true for copying
   * @param overwrite destination exists
   * @return true if destination created (i.e. not updated)
   * @throws WebdavException
   */
  public boolean copyMoveFile(CarddavResource from,
                              WdCollection to,
                              String name,
                              boolean copy,
                              boolean overwrite) throws WebdavException;

  /** Max size for an entity
   *
   * @return int
   * @throws WebdavException
   */
  public int getMaxUserEntitySize() throws WebdavException;

  /** End any transactions.
   *
   * @throws WebdavException
   */
  public void close() throws WebdavException;
}
