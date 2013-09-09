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
package org.bedework.carddav.server.dirHandlers.db;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.vcard.VCard;

import org.bedework.carddav.bwserver.DirHandler;
import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.util.CardDAVBadData;
import org.bedework.carddav.util.CardDAVContextConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.vcard.Card;

import edu.rpi.cct.webdav.servlet.shared.UrlHandler;
import edu.rpi.cct.webdav.servlet.shared.WdCollection;
import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavForbidden;
import edu.rpi.cct.webdav.servlet.shared.WebdavNotFound;

import java.util.Collection;

/**
 * @author douglm
 *
 */
public class DbAddrBookDirHandler extends DbDirHandler {
  /* (non-Javadoc)
   * @see org.bedework.carddav.server.dirHandlers.LdapDirHandler#init(org.bedework.carddav.util.CardDAVConfig, org.bedework.carddav.util.DirHandlerConfig, edu.bedework.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler)
   */
  @Override
  public void init(final CardDAVContextConfig cdConfig,
                   final DirHandlerConfig dhConfig,
                   final UrlHandler urlHandler) throws WebdavException {
    super.init(cdConfig, dhConfig, urlHandler);
  }

  /* ====================================================================
   *                   Principals
   * ==================================================================== */

  public Card getPrincipalCard(final String href) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  @Override
  public Collection<String>getGroups(final String rootUrl,
                                     final String principalUrl) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* ====================================================================
   *                   Cards
   * ==================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#addCard(java.lang.String, org.bedework.carddav.server.Vcard)
   */
  public void addCard(final String path,
                      final Card card) throws WebdavException {
    /** Build a directory record and add the attributes
     */

    VCard vc = card.getVcard();

    try {
      vc.validate();
    } catch (Throwable t) {
      if (debug) {
        error(t);
      }
      throw new CardDAVBadData(t.getMessage());
    }

    DbCard dc = new DbCard(vc);

    if (card.getName() == null) {
      throw new WebdavBadRequest();
    }

    dc.setName(card.getName());
    dc.setParentPath(ensureSlashAtEnd(path));
    dc.setPath(dc.getParentPath() + dc.getName());
    dc.setOwnerHref(card.getOwner().getPrincipalRef());
    dc.setCreatorHref(dc.getOwnerHref());

    create(dc);
  }

  private boolean create(final DbCard card) throws WebdavException {
    card.setDtstamps();
    card.output(); // Ensure string form exists

    sess.save(card);

    return true;
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#updateCard(java.lang.String, org.bedework.carddav.server.Vcard)
   */
  public void updateCard(final String path,
                         final Card card) throws WebdavException {
    if (card.getName() == null) {
      throw new WebdavBadRequest();
    }

    VCard vc = card.getVcard();

    try {
      vc.validate();
    } catch (Throwable t) {
      throw new CardDAVBadData(t.getMessage());
    }

    //DbCard dc = new DbCard(vc);
    DbCard dc = getDbCard(path, card.getName());

    if (dc == null) {
      throw new WebdavException("Card does not exist");
    }

    dc.setVcard(vc);
    dc.setDtstamps();

    // Rewrite string form
    dc.setStrForm(null);
    dc.output();

    sess.update(dc);
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#deleteCard(org.bedework.carddav.server.CarddavCardNode)
   */
  public void deleteCard(final CarddavCardNode val) throws WebdavException {
    DbCard dcd = getDbCard(val.getPath());

    if (dcd == null) {
      throw new WebdavNotFound();
    }

    deleteDbCard(dcd);
  }

  /* ====================================================================
   *                   Collections
   * ==================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#makeCollection(org.bedework.carddav.server.CarddavCollection, java.lang.String)
   */
  public int makeCollection(final CarddavCollection col,
                            final String parentPath) throws WebdavException {
    boolean home = parentPath.equals(userHomeRoot);

    /* If home ensure the user root exists */
    if (home) {
      StringBuilder sb = new StringBuilder();
      sb.append("select col.name from ");
      sb.append(DbCollection.class.getName());
      sb.append(" col where col.name=:name and col.parentPath is null");

      sess.createQuery(sb.toString());

      String rootName = userHomeRoot.substring(1, userHomeRoot.length() - 1);
      sess.setString("name", rootName);

      Collection res = sess.getList();

      if (res.size() == 0) {
        /* Create user root */
        DbCollection root = new DbCollection();

        root.setName(rootName);
//        root.setParentPath("/");
        root.setPath(userHomeRoot);
        root.setAddressBook(false);
        root.setAccess(dbConfig.getRootAccess() + " "); // Loses trailing " "
        root.setOwnerHref(dbConfig.getRootOwner());
        root.setCreatorHref(dbConfig.getRootOwner());

        DateTime dt = new DateTime(true);

        root.setLastmod(new LastModified(dt).getValue());
        root.setCreated(new Created(dt).getValue());

        sess.save(root);
        sess.flush();
      }
    }

    /* Ensure doesn't exist */
    StringBuilder sb = new StringBuilder();
    sb.append("select col.name from ");
    sb.append(DbCollection.class.getName());
    sb.append(" col where col.name=:name and col.parentPath=:pp");

    sess.createQuery(sb.toString());

    sess.setString("name", col.getName());
    sess.setString("pp", ensureSlashAtEnd(parentPath));

    Collection res = sess.getList();
    if (res.size() > 0) {
      return DirHandler.statusDuplicate;
    }

    if (!home) {
      // Check access on parent
      if (getDbCollection(parentPath, privBind) == null) {
        throw new WebdavForbidden();
      }
    }

    DbCollection dbc = new DbCollection();

    dbc.setName(col.getName());
    dbc.setDescription(col.getDescription());
    dbc.setAddressBook(col.getAddressBook());

    dbc.setOwnerHref(col.getOwner().getPrincipalRef());
    dbc.setCreatorHref(dbc.getOwnerHref());

    dbc.setParentPath(ensureSlashAtEnd(parentPath));
    dbc.setPath(dbc.getParentPath() + dbc.getName() + "/");

    DateTime dt = new DateTime(true);

    if (col.getLastmod() == null) {
      dbc.setLastmod(new LastModified(dt).getValue());
    } else {
      dbc.setLastmod(col.getLastmod());
    }

    if (col.getCreated() == null) {
      dbc.setCreated(new Created(dt).getValue());
    } else {
      dbc.setCreated(col.getCreated());
    }

    sess.save(dbc);

    if (home) {
      sess.flush();
    }

    return DirHandler.statusCreated;
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#deleteCollection(org.bedework.webdav.WdCollection)
   */
  public void deleteCollection(final WdCollection col) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#rename(org.bedework.webdav.WdCollection, java.lang.String)
   */
  public int rename(final WdCollection col,
                    final String newName) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#copyMove(org.bedework.carddav.server.Vcard, java.lang.String, java.lang.String, boolean, boolean)
   */
  public int copyMove(final Card from,
                      final String toPath,
                      final String name,
                      final boolean copy,
                      final boolean overwrite) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#updateCollection(org.bedework.webdav.WdCollection)
   */
  public void updateCollection(final WdCollection val) throws WebdavException {
    throw new WebdavException("unimplemented");
  }
}
