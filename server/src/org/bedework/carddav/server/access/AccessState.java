/* **********************************************************************
    Copyright 2007 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
