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
import org.bedework.carddav.server.SysIntf.GetLimits;
import org.bedework.carddav.server.SysIntf.GetResult;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.vcard.Card;

import edu.rpi.cct.webdav.servlet.shared.WdCollection;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavForbidden;

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
