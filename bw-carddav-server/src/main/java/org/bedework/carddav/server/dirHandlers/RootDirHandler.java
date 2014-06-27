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

import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.server.SysIntf.GetLimits;
import org.bedework.carddav.server.SysIntf.GetResult;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.vcard.Card;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WebdavException;
import org.bedework.webdav.servlet.shared.WebdavForbidden;

import java.util.Iterator;

/** An implementation of DirHandler which handles the root paths.
*
* @author Mike Douglass douglm rpi.edu
* @version 1.0
*/
public class RootDirHandler extends AbstractDirHandler {

  @Override
  public void addCard(final String path, final Card card) throws WebdavException {
    throw new WebdavForbidden();
  }

  @Override
  public int copyMove(final Card from, final String toPath, final String name, final boolean copy,
                      final boolean overwrite) throws WebdavException {
    throw new WebdavForbidden();
  }

  @Override
  public void deleteCard(final CarddavCardNode val) throws WebdavException {
    throw new WebdavForbidden();
  }

  @Override
  public Iterator<Card> getAll(final String path)
          throws WebdavException {
    return null;
  }

  @Override
  public void deleteCollection(final WdCollection col) throws WebdavException {
    throw new WebdavForbidden();
  }

  @Override
  public Card getCard(final String path,
                      final String name) throws WebdavException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Card getCardByUid(final String path,
                           final String uid) throws WebdavException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GetResult getCards(final String path,
                            final Filter filter,
                            final GetLimits limits) throws WebdavException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public CarddavCollection getCollection(final String path) throws WebdavException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public GetResult getCollections(final String path,
                                  final GetLimits limits) throws WebdavException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Card getPrincipalCard(final String path) throws WebdavException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int makeCollection(final CarddavCollection col,
                            final String parentPath) throws WebdavException {
    throw new WebdavForbidden();
  }

  @Override
  public int rename(final WdCollection col,
                    final String newName) throws WebdavException {
    throw new WebdavForbidden();
  }

  @Override
  public void updateCard(final String path,
                         final Card card) throws WebdavException {
    throw new WebdavForbidden();
  }

  @Override
  public void updateCollection(final WdCollection val) throws WebdavException {
    throw new WebdavForbidden();
  }
}
