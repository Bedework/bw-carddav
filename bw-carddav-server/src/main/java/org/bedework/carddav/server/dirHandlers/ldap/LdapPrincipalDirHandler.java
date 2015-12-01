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
package org.bedework.carddav.server.dirHandlers.ldap;

import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.server.config.CardDAVConfig;
import org.bedework.carddav.server.config.DirHandlerConfig;
import org.bedework.carddav.server.config.LdapDirHandlerConfig;
import org.bedework.carddav.vcard.Card;
import org.bedework.webdav.servlet.shared.UrlHandler;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WebdavException;

import java.util.Collection;
import java.util.Iterator;

/** A directory handler for principals.
 *
 * @author douglm
 *
 */
public class LdapPrincipalDirHandler extends LdapDirHandler {
  public void init(final CardDAVConfig cdConfig,
                   final DirHandlerConfig dhConfig,
                   final UrlHandler urlHandler) throws WebdavException {
    super.init(cdConfig, dhConfig, urlHandler);

    ldapConfig = (LdapDirHandlerConfig)dhConfig;
  }

  /* ====================================================================
   *                   Principals
   * ==================================================================== */

  public Card getPrincipalCard(final String path) throws WebdavException {
    verifyPath(path);

    try {
      openContext();

      final LdapObject ldo = getObject(path, false);

      if (ldo.getAttrs() == null) {
        return null;
      }

      return makeVcard(path, true, ldo.getAttrs(), ldo.getFullname());
    } finally {
      closeContext();
    }
  }

  @Override
  public Collection<String>getGroups(final String rootUrl,
                                     final String principalUrl) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* ====================================================================
   *                   Cards
   * ==================================================================== */

  @Override
  public void addCard(final String path,
                      final Card card) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  @Override
  public void updateCard(final String path,
                         final Card card) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  @Override
  public void deleteCard(final CarddavCardNode val) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  @Override
  public Iterator<Card> getAll(final String path)
          throws WebdavException {
    return null;
  }

  /* ====================================================================
   *                   Collections
   * ==================================================================== */

  @Override
  public int makeCollection(final CarddavCollection col,
                            final String parentPath) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#deleteCollection(WdCollection)
   */
  public void deleteCollection(final WdCollection col) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#rename(WdCollection, String)
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
