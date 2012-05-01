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
package org.bedework.carddav.util;

import edu.rpi.sss.util.Util;

import java.util.Collection;

/** An arbitrary property value in bedework carddav.
 *
 *  @version 1.0
 */
public class CarddavProperty extends DbItem<CarddavProperty> {
  private String name;

  private String value;

  /** Constructor
   */
  public CarddavProperty() {
    super();
  }

  /** Create a string by specifying all its fields
   *
   * @param name        String property name
   * @param value       String value
   */
  public CarddavProperty(final String name,
                    final String value) {
    super();
    this.name = name;
    this.value = value;
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
   *  @return String   value
   */
  public String getValue() {
    return value;
  }

  /* ====================================================================
   *                        Convenience methods
   * ==================================================================== */

  /** Search the collection for a string that matches the given name.
   *
   * <p>We return the first one we found.
   *
   * @param name
   * @param c
   * @return BwProperty or null if no strings.
   */
  public static CarddavProperty findName(final String name,
                                       final Collection<CarddavProperty> c) {
    if (c == null) {
      return null;
    }

    if (name == null) {
      return null;
    }

    for (CarddavProperty p: c) {
      String pname = p.getName();

      if (Util.cmpObjval(name, pname) == 0) {
        return p;
      }
    }

    return null;
  }

  /** Figure out what's different and update it. This should reduce the number
   * of spurious changes to the db.
   *
   * @param from
   * @return true if we changed something.
   */
  public boolean update(final CarddavProperty from) {
    if (Util.cmpObjval(getName(), from.getName()) != 0) {
      return false;
    }

    if (Util.cmpObjval(getValue(), from.getValue()) != 0) {
      setValue(from.getValue());
      return true;
    }

    return false;
  }

  /** Check this is properly trimmed
   *
   * @return boolean true if changed
   */
  public boolean checkNulls() {
    boolean changed = false;

    String str = Util.checkNull(getName());
    if (Util.compareStrings(str, getName()) != 0) {
      setName(str);
      changed = true;
    }

    str = Util.checkNull(getValue());
    if (Util.compareStrings(str, getValue()) != 0) {
      setValue(str);
      changed = true;
    }

    return changed;
  }

  /* ====================================================================
   *                        Object methods
   * ==================================================================== */

  @Override
  public int compareTo(final CarddavProperty that) {
    if (that == this) {
      return 0;
    }

    if (that == null) {
      return -1;
    }

    int res = Util.cmpObjval(getName(), that.getName());

    if (res != 0) {
      return res;
    }

    return Util.cmpObjval(getValue(), that.getValue());
  }

  @Override
  public int hashCode() {
    int hc = 7;

    if (getName() != null) {
      hc *= getName().hashCode();
    }

    if (getValue() != null) {
      hc *= getValue().hashCode();
    }

    return hc;
  }

  @Override
  protected void toStringSegment(final StringBuilder sb) {
    super.toStringSegment(sb);
    sb.append(", name=");
    sb.append(getName());
    sb.append(", value=");
    sb.append(getValue());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append("{");

    toStringSegment(sb);
    sb.append("}");

    return sb.toString();
  }

  @Override
  public Object clone() {
    return new CarddavProperty(getName(), getValue());
  }
}
