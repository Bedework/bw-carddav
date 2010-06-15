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
package org.bedework.carddav.server.dirHandlers.db;

import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.vcard.Card;

import edu.rpi.cct.webdav.servlet.shared.UrlHandler;
import edu.rpi.cct.webdav.servlet.shared.WdCollection;
import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;

import java.util.Collection;

/**
 * @author douglm
 *
 */
public class DbAddrBookDirHandler extends DbDirHandler {
  /* (non-Javadoc)
   * @see org.bedework.carddav.server.dirHandlers.LdapDirHandler#init(org.bedework.carddav.util.CardDAVConfig, org.bedework.carddav.util.DirHandlerConfig, edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler)
   */
  @Override
  public void init(final CardDAVConfig cdConfig,
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
    DbCard dc = new DbCard(card.getVcard());

    if (card.getName() == null) {
      throw new WebdavBadRequest();
    }

    dc.setName(card.getName());
    dc.setParentPath(path);

    create(dc);
  }

  private boolean create(final DbCard card) throws WebdavException {
    card.output(); // Ensure string form exists
    try {
      openSession();

      sess.save(card);

      return true;
    } catch (WebdavException wde) {
      rollbackTransaction();
      throw wde;
    } catch (Throwable t) {
      rollbackTransaction();
      throw new WebdavException(t);
    } finally {
      endTransaction();
      closeSession();
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#updateCard(java.lang.String, org.bedework.carddav.server.Vcard)
   */
  public void updateCard(final String path,
                         final Card card) throws WebdavException {
    try {
      openSession();

      sess.update(card);
    } catch (WebdavException wde) {
      rollbackTransaction();
      throw wde;
    } catch (Throwable t) {
      rollbackTransaction();
      throw new WebdavException(t);
    } finally {
      endTransaction();
      closeSession();
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#deleteCard(org.bedework.carddav.server.CarddavCardNode)
   */
  public void deleteCard(final CarddavCardNode val) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* ====================================================================
   *                   Collections
   * ==================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#makeCollection(org.bedework.carddav.server.CarddavCollection, java.lang.String)
   */
  public int makeCollection(final CarddavCollection col,
                            final String parentPath) throws WebdavException {
    throw new WebdavException("unimplemented");
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
