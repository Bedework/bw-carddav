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

/** A representation of a vcard property parameter for database persistance in cardDAV.
 * Allows us to index the values for searching
 *
 * @author douglm
 *
 */
public class DbCardParam extends UnversionedDbentity<DbCardParam> {
  private DbCardProperty property;

  private String name;

  private String value;

  /** Null constructor
   *
   */
  public DbCardParam() {
  }

  /** Create DbCardParam
   *
   * @param name of param
   * @param value of param
   * @param property property
   */
  public DbCardParam(final String name,
                     final String value,
                     final DbCardProperty property) {
    this.name = name;
    this.value = value;
    this.property = property;
  }

  /** Set the name
   *
   * @param val    String name
   */
  public void setName(final String val) {
    name = val;
  }

  /** Get the name
   *
   * @return String   name
   */
  public String getName() {
    return name;
  }

  /** Set the value
   *
   * @param val    String value
   */
  public void setValue(final String val) {
    value = val;
  }

  /** Get the value
   *
   * @return String   value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param val a property
   */
  public void setProperty(final DbCardProperty val) {
    property = val;
  }

  /**
   * @return card.
   */
  public DbCardProperty getProperty() {
    return property;
  }

  /* ==============================================================
   *                   Object methods
   * ============================================================== */

  @Override
  public int compareTo(final DbCardParam that) {
    try {
      int res = Util.compareStrings(getName(), that.getName());

      if (res != 0) {
        return res;
      }

      res = Util.compareStrings(getValue(), that.getValue());

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
    return new ToString(this)
            .append("name", getName())
            .append("value", getValue())
            .toString();
  }

  @Override
  public int hashCode() {
    int h = getName().hashCode();

    if (getValue() != null) {
      h *= getValue().hashCode();
    }

    return h;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof final DbCardParam that)) {
      return false;
    }

    if (!getName().equals(that.getName())) {
      return false;
    }

    return Util.compareStrings(getValue(), that.getValue()) == 0;
  }
}
