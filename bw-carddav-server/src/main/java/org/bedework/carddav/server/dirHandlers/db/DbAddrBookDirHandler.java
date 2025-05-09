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

import org.bedework.base.exc.BedeworkException;
import org.bedework.carddav.common.CarddavCollection;
import org.bedework.carddav.common.DirHandler;
import org.bedework.carddav.common.config.CardDAVConfigI;
import org.bedework.carddav.common.config.DirHandlerConfig;
import org.bedework.carddav.common.util.CardDAVBadData;
import org.bedework.carddav.common.util.CardDAVDuplicateUid;
import org.bedework.carddav.common.vcard.Card;
import org.bedework.util.misc.Util;
import org.bedework.webdav.servlet.shared.UrlHandler;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WebdavBadRequest;
import org.bedework.webdav.servlet.shared.WebdavException;
import org.bedework.webdav.servlet.shared.WebdavForbidden;
import org.bedework.webdav.servlet.shared.WebdavNotFound;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.vcard.VCard;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author douglm
 *
 */
public class DbAddrBookDirHandler extends DbDirHandler {
  @Override
  public void init(final CardDAVConfigI cdConfig,
                   final DirHandlerConfig<?> dhConfig,
                   final UrlHandler urlHandler) {
    super.init(cdConfig, dhConfig, urlHandler);
  }

  @Override
  public boolean exportData(final String dataOutPath) {
    final File f = new File(dataOutPath);

    final File dumpDir = makeDir(f, dhConfig.getPathPrefix());

    final CollectionBatcher cb = getCollections(dhConfig.getPathPrefix());

    /* First level should be user home */
    while (true) {
      final Collection<CarddavCollection> ccs = cb.next();
      if (Util.isEmpty(ccs)) {
        break;
      }

      for (final CarddavCollection cc: ccs) {
        if (debug()) {
          debug("Dumping " + cc.getPath());
        }

        dumpUserDir(cc, dumpDir);
      }
    }

    return false;
  }

  private File makeDir(final File parent,
                       final String name) {
    if (!parent.isDirectory()) {
      throw new WebdavException(parent.getAbsolutePath() +
                                        " is not a directory");
    }

    final File newDir = new File(parent.getAbsolutePath(),
                                 name);
    if (!newDir.mkdir()) {
      throw new WebdavException("Unable to create directory " +
                                        newDir.getAbsolutePath());
    }

    return newDir;
  }

  private void dumpUserDir(final CarddavCollection col,
                           final File dumpDir) {
    final File userDumpDir = makeDir(dumpDir, col.getName());

    if (col.getAddressBook()) {
      // Dump address data

      final Iterator<Card> cards = getAll(col.getPath());
      while (cards.hasNext()) {
        final Card c = cards.next();

        final String vc = c.outputVcard(cdConfig.getDefaultVcardVersion());

        FileWriter fw = null;
        final File cardFile;
        try {
          cardFile = new File(userDumpDir.getAbsolutePath(),
                              c.getName());
          fw = new FileWriter(cardFile);

          fw.write(vc);
        } catch (final Throwable t) {
          error(t);
        } finally {
          try {
            fw.close();
          } catch (final Throwable t) {
            error(t);
          }
        }
      }
      return;
    }

    // Might be a resource directory - dump any resources then sub collections.

    final CollectionBatcher cb = getCollections(col.getPath());

    while (true) {
      final Collection<CarddavCollection> ccs = cb.next();
      if (Util.isEmpty(ccs)) {
        break;
      }

      for (final CarddavCollection cc: ccs) {
        if (debug()) {
          debug("Dumping " + cc.getPath());
        }

        dumpUserDir(cc, userDumpDir);
      }
    }
  }

  /* ====================================================================
   *                   Principals
   * ==================================================================== */

  @Override
  public Card getPrincipalCard(final String href) {
    throw new WebdavException("unimplemented");
  }

  @Override
  public Collection<String>getGroups(final String rootUrl,
                                     final String principalUrl) {
    throw new WebdavException("unimplemented");
  }

  /* ====================================================================
   *                   Cards
   * ==================================================================== */

  @Override
  public void addCard(final String path,
                      final Card card) {
    if (card.getUid() == null) {
      throw new WebdavBadRequest();
    }

    if (getCardByUid(path, card.getUid()) != null) {
      throw new CardDAVDuplicateUid();
    }

    /* Build a directory record and add the attributes
     */

    final VCard vc = card.getVcard();

    try {
      vc.validate();
    } catch (final Throwable t) {
      if (debug()) {
        error(t);
      }
      throw new CardDAVBadData(t.getMessage());
    }

    final DbCollection col = getDbCollection(ensureEndSlash(path), privBind);

    if (col == null) {
      throw new WebdavForbidden();
    }

    final DbCard dc = new DbCard(vc);

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

  @Override
  public void updateCard(final String path,
                         final Card card) {
    if (card.getName() == null) {
      throw new WebdavBadRequest();
    }

    final VCard vc = card.getVcard();

    try {
      vc.validate();
    } catch (final Throwable t) {
      throw new CardDAVBadData(t.getMessage());
    }

    final DbCard dc = getDbCard(path, card.getName(), privWriteContent);

    if (dc == null) {
      throw new WebdavException("Card does not exist");
    }

    // UID must match
    if (!card.getUid().equals(dc.getUid())) {
      throw new CardDAVBadData("Cannot change UID");
    }

    dc.setVcard(vc);
    dc.setDtstamps();

    // Rewrite string form
    dc.setStrForm(null);
    dc.output();

    try {
      getSess().update(dc);
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  @Override
  public void deleteCard(final String path) {
    final DbCard dcd = getDbCard(path);

    if (dcd == null) {
      throw new WebdavNotFound();
    }

    checkAccess(dcd, privUnbind, false);

    deleteDbCard(dcd);
  }

  /* ====================================================================
   *                   Collections
   * ==================================================================== */

  private final static String queryGetUserRootName =
          "select col.name from DbCollection col " +
                  "where col.name=:name and col.parentPath is null";

  private final static String queryGetCollectionName =
          "select col.name from DbCollection col " +
                  "where col.name=:name and col.parentPath=:pp";


  @Override
  public int makeCollection(final CarddavCollection col,
                            final String parentPath) {
    final boolean home = parentPath.equals(userHomeRoot);

    try {
      /* If home ensure the user root exists */
      if (home) {
        final String rootName =
                userHomeRoot.substring(1, userHomeRoot.length() - 1);

        final var res = createQuery(queryGetUserRootName)
                .setString("name", rootName)
                .getList();

        if (res.isEmpty()) {
          /* Create user root */
          final DbCollection root = new DbCollection();

          root.setName(rootName);
//        root.setParentPath("/");
          root.setPath(userHomeRoot);
          root.setAddressBook(false);
          root.setAccess(dbConfig.getRootAccess() + " "); // Loses trailing " "
          root.setOwnerHref(dbConfig.getRootOwner());
          root.setCreatorHref(dbConfig.getRootOwner());

          final DateTime dt = new DateTime(true);

          root.setLastmod(new LastModified(dt).getValue());
          root.setCreated(new Created(dt).getValue());

          getSess().add(root).flush();
        }
      }

      /* Ensure doesn't exist */
      final var res = createQuery(queryGetCollectionName)
              .setString("name", col.getName())
              .setString("pp", ensureSlashAtEnd(parentPath))
              .getList();
      if (!res.isEmpty()) {
        return DirHandler.statusDuplicate;
      }

      if (!home) {
        // Check access on parent
        if (getDbCollection(parentPath, privBind) == null) {
          throw new WebdavForbidden();
        }
      }

      final DbCollection dbc = new DbCollection();

      dbc.setName(col.getName());
      dbc.setDescription(col.getDescription());
      dbc.setAddressBook(col.getAddressBook());

      dbc.setOwnerHref(col.getOwner().getPrincipalRef());
      dbc.setCreatorHref(dbc.getOwnerHref());

      dbc.setParentPath(ensureSlashAtEnd(parentPath));
      dbc.setPath(dbc.getParentPath() + dbc.getName() + "/");

      final DateTime dt = new DateTime(true);

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

      getSess().add(dbc);

      if (home) {
        getSess().flush();
      }

      return DirHandler.statusCreated;
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }
  }

  @Override
  public void deleteCollection(final WdCollection<?> col) {
    deleteDbCollection(col.getPath());
  }

  @Override
  public int rename(final WdCollection<?> col,
                    final String newName) {
    throw new WebdavException("unimplemented");
  }

  @Override
  public int copyMove(final Card from,
                      final String toPath,
                      final String name,
                      final boolean copy,
                      final boolean overwrite) {
    throw new WebdavException("unimplemented");
  }

  @Override
  public void updateCollection(final WdCollection<?> val) {
    throw new WebdavException("unimplemented");
  }

  private boolean create(final DbCard card) {
    card.setDtstamps();
    card.output(); // Ensure string form exists

    try {
      getSess().add(card);
    } catch (final BedeworkException e) {
      throw new WebdavException(e);
    }

    return true;
  }
}
