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
package org.bedework.carddav.server.dirHandlers;

import org.bedework.carddav.common.AbstractDirHandler;
import org.bedework.carddav.common.CarddavCollection;
import org.bedework.carddav.common.GetLimits;
import org.bedework.carddav.common.GetResult;
import org.bedework.carddav.common.filter.Filter;
import org.bedework.carddav.common.vcard.Card;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WebdavForbidden;

import java.util.Iterator;

/** An implementation of DirHandler which handles the root paths.
*
* @author Mike Douglass douglm rpi.edu
* @version 1.0
*/
public class RootDirHandler extends AbstractDirHandler {

  @Override
  public void addCard(final String path, final Card card) {
    throw new WebdavForbidden();
  }

  @Override
  public int copyMove(final Card from, final String toPath, final String name, final boolean copy,
                      final boolean overwrite) {
    throw new WebdavForbidden();
  }

  @Override
  public void deleteCard(final String path) {
    throw new WebdavForbidden();
  }

  @Override
  public Iterator<Card> getAll(final String path) {
    return null;
  }

  @Override
  public void deleteCollection(final WdCollection<?> col) {
    throw new WebdavForbidden();
  }

  @Override
  public Card getCard(final String path,
                      final String name) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Card getCardByUid(final String path,
                           final String uid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GetResult getCards(final String path,
                            final Filter filter,
                            final GetLimits limits) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CarddavCollection getCollection(final String path) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GetResult getCollections(final String path,
                                  final GetLimits limits) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Card getPrincipalCard(final String path) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int makeCollection(final CarddavCollection col,
                            final String parentPath) {
    throw new WebdavForbidden();
  }

  @Override
  public int rename(final WdCollection<?> col,
                    final String newName) {
    throw new WebdavForbidden();
  }

  @Override
  public void updateCard(final String path,
                         final Card card) {
    throw new WebdavForbidden();
  }

  @Override
  public void updateCollection(final WdCollection<?> val) {
    throw new WebdavForbidden();
  }
}
