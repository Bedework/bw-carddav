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
package org.bedework.carddav.server.dirHandlers.db;

import org.bedework.base.ToString;
import org.bedework.util.misc.Util;
import org.bedework.webdav.servlet.access.AccessState;

/** A representation of a vcard and properties for database persistence in cardDAV
 *
 * @author douglm
 *
 */
public class DbCollection extends DbNamedEntity<DbCollection> {
  private String lastmod;

  private String description;

  private boolean addressBook;

  private AccessState accessState;

  /** Create DbCollection
   *
   */
  public DbCollection() {
  }

  /**
   * @param val last mod date/time
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
   * @param val a description
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
   * @param val true for address book
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

  /* ==============================================================
   *                   SharedEntity methods
   * ============================================================== */

  @Override
  public boolean isCollection() {
    return true;
  }

  @Override
  public void setAccessState(final AccessState val) {
    accessState = val;
  }

  @Override
  public AccessState getAccessState() {
    return accessState;
  }

  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.newLine()
      .append("lastmod", getLastmod())
      .append("description", getDescription());
  }

  /* ==============================================================
   *                   Object methods
   * ============================================================== */

  @Override
  public int compareTo(final DbCollection that) {
    try {
      final int res = Util.compareStrings(getParentPath(),
                                          that.getParentPath());

      if (res != 0) {
        return res;
      }

      return Util.compareStrings(getName(), that.getName());
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
