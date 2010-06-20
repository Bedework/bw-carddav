/* **********************************************************************
    Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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

package org.bedework.carddav.server.dirHandlers.db;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.sss.util.Util;

/** A representation of a vcard and properties for database persistence in cardDAV
 *
 * @author douglm
 *
 */
public class DbCollection extends DbNamedEntity<DbCollection> {
  private String lastmod;

  private String description;

  private boolean addressBook;

  /** Create DbCollection
   *
   * @throws WebdavException
   */
  public DbCollection() throws WebdavException {
  }

  /**
   * @param val
   */
  public void setLastmod(final String val) {
    lastmod = val;
  }

  /**
   * @return String
   */
  public String getLastmod() {
    return lastmod;
  }

  /**
   * @param val
   */
  public void setDescription(final String val) {
    description = val;
  }

  /**
   * @return String
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param val
   */
  public void setAddressBook(final boolean val) {
    addressBook = val;
  }

  /**
   * @return boolean
   */
  public boolean getAddressBook() {
    return addressBook;
  }

  /**
   * @param sb
   */
  @Override
  public void toStringSegment(final StringBuilder sb) {
    super.toStringSegment(sb);

    sb.append(", \n   lastmod=");
    sb.append(getLastmod());
    sb.append(", \n   description=");
    sb.append(getDescription());
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public int compareTo(final DbCollection that) {
    try {
      int res = Util.compareStrings(getParentPath(), that.getParentPath());

      if (res != 0) {
        return res;
      }

      return Util.compareStrings(getName(), that.getName());
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DbCollection{");

    toStringSegment(sb);

    return sb.toString();
  }
}
