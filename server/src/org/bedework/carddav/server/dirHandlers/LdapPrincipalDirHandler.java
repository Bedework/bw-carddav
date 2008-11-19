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
package org.bedework.carddav.server.dirHandlers;

import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.server.Vcard;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.util.LdapDirHandlerConfig;
import org.bedework.webdav.WdCollection;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler;

import java.util.ArrayList;
import java.util.Collection;

import javax.naming.NamingEnumeration;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/** A directory handler for principals.
 *
 * @author douglm
 *
 */
public class LdapPrincipalDirHandler extends LdapDirHandler {
  DirContext ctx;
  String searchBase; // searchBase which resulted in sresult;

  SearchControls constraints;
  NamingEnumeration<SearchResult> sresult;

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.dirHandlers.LdapDirHandler#init(org.bedework.carddav.util.CardDAVConfig, org.bedework.carddav.util.DirHandlerConfig, edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler)
   */
  public void init(CardDAVConfig cdConfig,
                   DirHandlerConfig dhConfig,
                   UrlHandler urlHandler) throws WebdavException {
    super.init(cdConfig, dhConfig, urlHandler);

    ldapConfig = (LdapDirHandlerConfig)dhConfig;
  }

  /* ====================================================================
   *                   Principals
   * ==================================================================== */

  public Vcard getPrincipalCard(String path) throws WebdavException {
    verifyPath(path);

    try {
      openContext();

      Attributes attrs = getObject(path, false);

      if (attrs == null) {
        return null;
      }

      return makeVcard(path, true, attrs);
    } finally {
      closeContext();
    }
  }

  public Collection<String>getGroups(String rootUrl,
                                     String principalUrl) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* ====================================================================
   *                   Cards
   * ==================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#addCard(java.lang.String, org.bedework.carddav.server.Vcard)
   */
  public void addCard(String path,
                      Vcard card) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#updateCard(java.lang.String, org.bedework.carddav.server.Vcard)
   */
  public void updateCard(String path,
                         Vcard card) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#deleteCard(org.bedework.carddav.server.CarddavCardNode)
   */
  public void deleteCard(CarddavCardNode val) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* ====================================================================
   *                   Collections
   * ==================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#makeCollection(org.bedework.carddav.server.CarddavCollection, java.lang.String)
   */
  public int makeCollection(CarddavCollection col,
                            String parentPath) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#deleteCollection(org.bedework.webdav.WdCollection)
   */
  public void deleteCollection(WdCollection col) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#rename(org.bedework.webdav.WdCollection, java.lang.String)
   */
  public int rename(WdCollection col,
                    String newName) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#copyMove(org.bedework.carddav.server.Vcard, java.lang.String, java.lang.String, boolean, boolean)
   */
  public int copyMove(Vcard from,
                      String toPath,
                      String name,
                      boolean copy,
                      boolean overwrite) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCollection(java.lang.String)
   */
  public CarddavCollection getCollection(String path) throws WebdavException {
    verifyPath(path);

    /* We're fetching a collection entity with a fully specified path */
    try {
      openContext();

      Attributes attrs = getObject(path, true);

      if (attrs == null) {
        return null;
      }

      return makeCdCollection(path, true, attrs);
    } finally {
      closeContext();
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#updateCollection(org.bedework.webdav.WdCollection)
   */
  public void updateCollection(WdCollection val) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  public Collection<CarddavCollection> getCollections(String path)
         throws WebdavException {
    verifyPath(path);

    try {
      openContext();

      if (!searchChildren(path, false)) {
        return null;
      }

      Collection<CarddavCollection> res = new ArrayList<CarddavCollection>();

      for (;;) {
        CarddavCollection cdc = nextCdCollection(path, false);

        if (cdc == null) {
          break;
        }

        res.add(cdc);
      }

      return res;
    } finally {
      closeContext();
    }
  }
}
