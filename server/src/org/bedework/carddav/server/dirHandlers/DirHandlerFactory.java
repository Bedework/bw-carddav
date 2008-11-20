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

import org.bedework.carddav.bwserver.DirHandler;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;

/** Create a directory handler for CardDAV
 *
 * @author douglm
 */
public class DirHandlerFactory {
  /**
   * @param name
   * @return DirHandler
   * @throws WebdavException
   */
  public static DirHandler getHandler(String name) throws WebdavException  {
    try {
      Object o = Class.forName(name).newInstance();

      if (o == null) {
        throw new WebdavException("Class " + name + " not found");
      }

      if (!(o instanceof DirHandler)) {
        throw new WebdavException("Class " + name +
                                   " is not a subclass of " +
                                   DirHandler.class.getName());
       }

      DirHandler dh = (DirHandler)o;

      return dh;
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

}
