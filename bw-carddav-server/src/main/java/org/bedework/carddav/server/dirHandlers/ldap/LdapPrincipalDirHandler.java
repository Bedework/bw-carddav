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

import org.bedework.carddav.common.CarddavCollection;
import org.bedework.carddav.common.config.CardDAVConfigI;
import org.bedework.carddav.common.config.DirHandlerConfig;
import org.bedework.carddav.common.vcard.Card;
import org.bedework.carddav.server.config.LdapDirHandlerConfig;
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
  public void init(final CardDAVConfigI cdConfig,
                   final DirHandlerConfig dhConfig,
                   final UrlHandler urlHandler) {
    super.init(cdConfig, dhConfig, urlHandler);

    ldapConfig = (LdapDirHandlerConfig)dhConfig;
  }

  /* ====================================================================
   *                   Principals
   * ==================================================================== */

  public Card getPrincipalCard(final String path) {
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
                                     final String principalUrl) {
    throw new WebdavException("unimplemented");
  }

  /* ====================================================================
   *                   Cards
   * ==================================================================== */

  @Override
  public void addCard(final String path,
                      final Card card) {
    throw new WebdavException("unimplemented");
  }

  @Override
  public void updateCard(final String path,
                         final Card card) {
    throw new WebdavException("unimplemented");
  }

  @Override
  public void deleteCard(final String path) {
    throw new WebdavException("unimplemented");
  }

  @Override
  public Iterator<Card> getAll(final String path) {
    return null;
  }

  /* ====================================================================
   *                   Collections
   * ==================================================================== */

  @Override
  public int makeCollection(final CarddavCollection col,
                            final String parentPath) {
    throw new WebdavException("unimplemented");
  }

  @Override
  public void deleteCollection(final WdCollection<?> col) {
    throw new WebdavException("unimplemented");
  }

  @Override
  public int rename(final WdCollection col,
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
  public void updateCollection(final WdCollection val) {
    throw new WebdavException("unimplemented");
  }
}
