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
