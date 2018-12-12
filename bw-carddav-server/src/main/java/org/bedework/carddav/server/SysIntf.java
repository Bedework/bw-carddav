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
package org.bedework.carddav.server;

import org.bedework.access.AccessPrincipal;
import org.bedework.access.Acl;
import org.bedework.access.CurrentAccess;
import org.bedework.carddav.common.CarddavCollection;
import org.bedework.carddav.common.GetLimits;
import org.bedework.carddav.common.GetResult;
import org.bedework.carddav.common.filter.Filter;
import org.bedework.carddav.common.vcard.Card;
import org.bedework.carddav.server.PropertyHandler.PropertyType;
import org.bedework.carddav.server.config.CardDAVConfig;
import org.bedework.carddav.server.config.CardDAVContextConfig;
import org.bedework.webdav.servlet.shared.PrincipalPropertySearch;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WdSysIntf;
import org.bedework.webdav.servlet.shared.WebdavException;

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
public interface SysIntf extends WdSysIntf {
  /** Called before any other method is called to allow initialisation to
   * take place at the first or subsequent requests
   *
   * @param req
   * @param account
   * @param conf  global service configuration
   * @param ctxConf  per application type configuration
   * @throws WebdavException
   */
  public void init(HttpServletRequest req,
                   String account,
                   CardDAVConfig conf,
                   CardDAVContextConfig ctxConf) throws WebdavException;

  /** Return the current principal
  *
  * @return String
  * @throws WebdavException
  */
 public AccessPrincipal getPrincipal() throws WebdavException;

  /** Get a property handler
   *
   * @param ptype
   * @return PropertyHandler
   * @throws WebdavException
   */
  public PropertyHandler getPropertyHandler(PropertyType ptype) throws WebdavException;

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
   * @return AccessPrincipal
   * @throws WebdavException
   */
  public AccessPrincipal getPrincipal(String href) throws WebdavException;

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
  public static class PrincipalInfo implements Serializable {
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

    /** Path to card for principal
     */
    public String principalCardPath;

    /** Some directory information for the user.
     */
    public Card directoryInfo;

    /**
     * @param account
     * @param principalPathPrefix
     * @param userHomePath
     * @param defaultAddressbookPath
     * @param principalCardPath
     * @param directoryInfo
     */
    public PrincipalInfo(final String account, final String principalPathPrefix,
                    final String userHomePath,
                    final String defaultAddressbookPath,
                    final String principalCardPath,
                    final Card directoryInfo) {
      this.account = account;
      this.principalPathPrefix = principalPathPrefix;
      this.userHomePath = userHomePath;
      this.defaultAddressbookPath = defaultAddressbookPath;
      this.principalCardPath = principalCardPath;
      this.directoryInfo = directoryInfo;
    }
  }

  /** Given a valid principal return the associated information
   * needed for carddav interactions.
   *
   * @param pcpl         the principal
   * @param getDirInfo  get directory info if true and available.
   * @return PrincipalInfo or null
   * @throws WebdavException  for errors
   */
  public PrincipalInfo getPrincipalInfo(AccessPrincipal pcpl,
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
  public Collection<PrincipalInfo> getPrincipals(String resourceUri,
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
                     Card card) throws WebdavException;

  /** Update a card.
   *
   * @param path       to collection
   * @param card         Object to be updated
   * @throws WebdavException
   */
  public void updateCard(String path,
                         Card card) throws WebdavException;

  /** Return the cards for the current user in the given collection using the
   * supplied filter.
   *
   * @param col       collection
   * @param filter - if non-null defines a search filter
   * @param limits - applied to this query
   * @return Collection  populated card objects
   * @throws WebdavException
   */
  public GetResult getCards(CarddavCollection col,
                            Filter filter,
                            GetLimits limits) throws WebdavException;

  /** Get card given the collection and String name.
   *
   * @param path       to collection
   * @param name       String possible name
   * @return Vcard or null
   * @throws WebdavException
   */
  public Card getCard(String path, String name)
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
  public boolean copyMove(Card from,
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
   * @param limits
   * @return GetResult    with collection of WdCollection
   * @throws WebdavException
   */
  public GetResult getCollections(CarddavCollection col,
                                  GetLimits limits)
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
