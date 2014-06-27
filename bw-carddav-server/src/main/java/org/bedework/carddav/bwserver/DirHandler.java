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

import org.bedework.access.AccessPrincipal;
import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.server.SysIntf.GetLimits;
import org.bedework.carddav.server.SysIntf.GetResult;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.util.CardDAVContextConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.vcard.Card;
import org.bedework.webdav.servlet.shared.UrlHandler;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WebdavException;

import java.util.Collection;
import java.util.Iterator;

/** Interface defining a directory handler. The implementing class provides an
 * interface between the CardDAV server and a portion of the directory structure.
 *
 * <p>The address book model provided by the CardDAV service looks like a
 * unified set of tree structures, main hierarchies being the principal
 * hierarchy, which defines users, groups etc adn the content hierarchies
 * within which we find the address books.
 *
 * <p>The content hierarchy looks like a conventional file system with folders
 * and files, but we define two types of folder, the normal WebDAV collection
 * and special collections which hold address book entities, probably vcards.
 *
 * <p>Each of the DirHandler interfaces will handle part of that styructure
 * specified by the path prefix, e.g. a path prefix of "/principal" means that
 * the handler manages anythign wit a path starting with that prefix, unless
 * that is, there is a handler defined with a longer prefix wich also matches.
 *
 * <p>In the methods below, wherever we have a path liek parameter it is always
 * a path rooted at the path prefix for the handler. Hrefs have been processed
 * and only the path part is passed to these methods.
 *
 * <p>Implementations need only implement some of the methods. A read only
 * interface should indicate that collections are read-only and may throw an
 * exception if a write method is called.
 *
 * @author douglm
 */
public interface DirHandler /*extends DynamicMBean */ {
  /** Initialise the class, called before open.
   *
   * @param cdConfig context config
   * @param dhConfig handler config
   * @param urlHandler - to allow creation of urls in returned objects.
   * @throws WebdavException
   */
  void init(CardDAVContextConfig cdConfig,
            DirHandlerConfig dhConfig,
            UrlHandler urlHandler) throws WebdavException;

  /** Open the handler.
   *
   * @param account - null for anonymous
   * @throws WebdavException
   */
  void open(String account) throws WebdavException;

  /** Close the handler
   *
   * @throws WebdavException
   */
  void close() throws WebdavException;

  /**
   * @return boolean
   */
  boolean isOpen();

  /** No problem */
  final int statusOK = 0;

  /** Created ok */
  final int statusCreated = 1;

  /** Cannot have duplicate uid */
  final int statusDuplicateUid = 2;

  /** Duplicate object */
  final int statusDuplicate = 3;

  /** Just can't do that */
  final int statusIllegal = 4;

  /** No access */
  final int statusNoAccess = 5;

  /** Cannot change uid on entity */
  final int statusChangeUid = 6;

  /** destination already exists */
  final int statusDestinationExists = 7;

  /* ====================================================================
   *                   Principals
   * ==================================================================== */

  /** Does the value appear to represent a valid principal?
   *
   * @param val possible href
   * @return true if it's a (possible) principal
   * @throws WebdavException
   */
  boolean isPrincipal(String val) throws WebdavException;

  /** Return principal information for the given path. Also tests for a valid
   * principal. Return null or set PrincipalInfo.valid = false for no principal.
   *
   * @param path principal href
   * @return AccessPrincipal
   * @throws WebdavException
   */
  AccessPrincipal getPrincipal(String path) throws WebdavException;

  /** Return a card for the principal with the given path. Also tests for a valid
   * principal. Return null for no principal.
   *
   * @param path principal href
   * @return Vcard
   * @throws WebdavException
   */
  @SuppressWarnings("UnusedDeclaration")
  Card getPrincipalCard(String path) throws WebdavException;

  /**
   * @param p principal
   * @return String principal uri
   * @throws WebdavException
   */
  String makePrincipalUri(AccessPrincipal p) throws WebdavException;

  /** The urls should be principal urls. principalUrl can null for the current user.
   * The result is a collection of principal urls of which the given url is a
   * member, based upon rootUrl. For example, if rootUrl points to the base of
   * the user principal hierarchy, then the result should be at least the current
   * user's principal url, remembering that user principals are themselves groups
   * and the user is considered a member of their own group.
   *
   * @param rootUrl - url to base search on.
   * @param principalUrl - url of principal or null for current user
   * @return Collection of urls - always non-null
   * @throws WebdavException
   */
  Collection<String>getGroups(String rootUrl,
                              String principalUrl) throws WebdavException;

  /** Return the user home for the current principal
   *
   * @return String home
   * @throws WebdavException
   */
  @SuppressWarnings("UnusedDeclaration")
  String getprincipalHome() throws WebdavException;

  /** Return the user home for the given principal
   *
   * @param p principal
   * @return String home
   * @throws WebdavException
   */
  String getprincipalHome(AccessPrincipal p) throws WebdavException;

  /* ====================================================================
   *                   Cards
   * ==================================================================== */

  /** Add a card.
   *
   * @param path       to collection
   * @param card         Object to be added
   * @throws WebdavException
   */
  void addCard(String path,
               Card card) throws WebdavException;

  /** Update a card.
   *
   * @param path       to collection
   * @param card         Object to be updated
   * @throws WebdavException
   */
  void updateCard(String path,
                  Card card) throws WebdavException;

  /** Get card given the collection and String name.
   *
   * @param path       to collection
   * @param name        String possible name
   * @return Vcard or null
   * @throws WebdavException
   */
  Card getCard(String path, String name)
          throws WebdavException;

  /** Get card given the collection and the uid.
   *
   * @param path       to collection
   * @param uid        UID value
   * @return Vcard or null
   * @throws WebdavException
   */
  Card getCardByUid(String path, String uid)
          throws WebdavException;

  /** Return the cards for the current user in the given collection using the
   * supplied filter.
   *
   * @param path       to collection
   * @param filter - if non-null defines a search filter
   * @param limits - applied to this query - possibly null
   * @return GetResult with populated card objects
   * @throws WebdavException
   */
  GetResult getCards(String path,
                     Filter filter,
                     GetLimits limits) throws WebdavException;

  /**
   * @param val node representing card
   * @throws WebdavException
   */
  void deleteCard(CarddavCardNode val) throws WebdavException;

  /**
   * @param path to collection
   * @return iterator over cards in that collection or null if not allowed
   * @throws WebdavException
   */
  Iterator<Card> getAll(String path) throws WebdavException;

  /* ====================================================================
   *                   Collections
   * ==================================================================== */

  /**
   * @param col   Initialised collection object
   * @param parentPath of its parent
   * @return int status
   * @throws WebdavException
   */
  int makeCollection(CarddavCollection col,
                     String parentPath) throws WebdavException;

  /**
   * @param col webdav collection object
   * @throws WebdavException
   */
  void deleteCollection(WdCollection col) throws WebdavException;

  /**
   * @param col   Initialised collection object
   * @param newName the name to set
   * @return int status
   * @throws WebdavException
   */
  int rename(WdCollection col,
             String newName) throws WebdavException;

  /** Copy or move the given entity to the destination collection with the given name.
   * Status is set on return
   *
   * @param from      Source entity
   * @param toPath    Destination collection
   * @param name      String name of new entity
   * @param copy      true for copying
   * @param overwrite destination exists
   * @return true if destination created (i.e. not updated)
   * @throws WebdavException
   */
  int copyMove(Card from,
               String toPath,
               String name,
               boolean copy,
               boolean overwrite) throws WebdavException;

  /** Get a collection given the path
   *
   * @param  path     String path of collection
   * @return CarddavCollection null for unknown collection
   * @throws WebdavException
   */
  CarddavCollection getCollection(String path) throws WebdavException;

  /** Update a collection.
   *
   * @param val           updated WdCollection object
   * @throws WebdavException
   */
  void updateCollection(WdCollection val) throws WebdavException;

  /** Returns children of the given collection to which the current user has
   * some access.
   *
   * @param  path          parent collection path
   * @param limits - applied to this query - possibly null
   * @return Collection   of CarddavCollection
   * @throws WebdavException
   */
  GetResult getCollections(String path,
                           GetLimits limits) throws WebdavException;
}
