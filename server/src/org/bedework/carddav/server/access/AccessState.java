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
package org.bedework.carddav.server.access;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cmt.access.Acl.CurrentAccess;

import java.util.HashMap;
import java.util.Map;

/** An object to preserve the current state of access calculations. Embedding
 * this in an entity instance avoids recalculation.
 *
 * @author Mike Douglass douglm @ rpi.edu
 * @version 1.0
 */
public class AccessState {
  private SharedEntity entity;

  /* Current access for the current principal.
   */
  private CurrentAccess currentAccess;

  private Map<Integer, CurrentAccess> caMap =
    new HashMap<Integer, CurrentAccess>(20);

  private int lastDesiredAccess;

  /** Constructor
   *
   * @param entity
   */
  public AccessState(final SharedEntity entity) {
    this.entity = entity;
  }

  /**
   * @return the entity
   */
  public SharedEntity fetchEntity() {
    return entity;
  }

  /* ====================================================================
   *                   Wrapper object methods
   * ==================================================================== */

  /**
   * @throws WebdavException
   */
  public void clearCurrentAccess() throws WebdavException {
    caMap.clear();
  }

  /**
   * @return current access object
   * @throws WebdavException
   */
  public CurrentAccess getCurrentAccess() throws WebdavException {
    if (currentAccess != null) {
      return currentAccess;
    }

    return getCurrentAccess(AccessUtilI.privAny);
  }

  /**
   * @param desiredAccess
   * @return currentAccess;
   * @throws WebdavException
   */
  public CurrentAccess getCurrentAccess(final int desiredAccess) throws WebdavException {
    if ((desiredAccess == lastDesiredAccess) &&
        (currentAccess != null)) {
      return currentAccess;
    }

    currentAccess = caMap.get(desiredAccess);
    lastDesiredAccess = desiredAccess;

    return currentAccess;
  }

  /**
   * @param ca
   * @param desiredAccess
   */
  public void setCurrentAccess(final CurrentAccess ca, final int desiredAccess) {
    currentAccess = ca;
    lastDesiredAccess = desiredAccess;
    caMap.put(desiredAccess , ca);
  }

  /**
   * @return int last desiredAccess
   */
  public int getLastDesiredAccess() {
    return lastDesiredAccess;
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("CalendarWrapper{");

    sb.append(entity.toString());

    try {
      if (getCurrentAccess() != null) {
        sb.append(", currentAccess=");
        sb.append(getCurrentAccess());
      }
    } catch (WebdavException cfe) {
      sb.append("exception");
      sb.append(cfe.getMessage());
    }
    sb.append("}");

    return sb.toString();
  }
}
