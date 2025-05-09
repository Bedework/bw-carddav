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
package org.bedework.carddav.server;

import org.bedework.base.ToString;
import org.bedework.util.misc.Util;

import java.io.Serializable;
import java.util.Arrays;

/** Represent the content for a resource stored within the system, e.g an attachment or an
 * image. The actual content is stored in a BwResourceContent object to allow us
 * to put off retrieval of content - or maybe even store outside of the db.
 *
 *  @author Mike Douglass   douglm - rpi.edu
 */
public class CarddavResourceContent
        implements Comparable<CarddavResourceContent>, Serializable {
  /* The collection this belongs to
   */
  private CarddavColNode collection;

  private String name;

  private byte[] value;

  /** Constructor
   *
   */
  public CarddavResourceContent() {
  }

  /* ==============================================================
   *                      Bean methods
   * ============================================================== */

  // CALWRAPPER
  /** Set the object's collection
   *
   * @param val    CarddavColNode object's collection
   */
  public void setParent(final CarddavColNode val) {
    collection = val;
  }

  // CALWRAPPER
  /** Get the object's collection
   *
   * @return the object's collection
   */
  public CarddavColNode getParent() {
    return collection;
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
   *  @param  val   byte[]
   */
  public void setValue(final byte[] val) {
    value = val;
  }

  /** Get the value
   *
   *  @return byte[]     length
   */
  public byte[] getValue() {
    return value;
  }

  /* ==============================================================
   *                   Other non-db methods
   * ============================================================== */

  /** Copy this objects values into the parameter
   *
   * @param val target
   */
  public void copyTo(final CarddavResourceContent val) {
    val.setParent(getParent());
    val.setName(getName());
    val.setValue(getValue());
  }

  /* ==============================================================
   *                   Object methods
   * ============================================================== */

  public int hashCode() {
    return Arrays.hashCode(getValue());
  }

  public int compareTo(CarddavResourceContent that)  {
    if (this == that) {
      return 0;
    }

    final int res = Util.cmpObjval(getParent().getWdCollection(),
                                   that.getParent().getWdCollection());
    if (res != 0) {
      return res;
    }

    final byte[] thisone = getValue();
    final byte[] thatone = that.getValue();

    if (thisone == null) {
      if (thatone == null) {
        return 0;
      }

      return -1;
    }

    if (thatone == null) {
      return 1;
    }

    if (thisone.length < thatone.length) {
      return -1;
    }

    if (thatone.length < thisone.length) {
      return 1;
    }

    for (int i = 0; i < thisone.length; i++) {
      final byte thisbyte = thisone[i];
      final byte thatbyte = thatone[i];

      if (thisbyte < thatbyte) {
        return -1;
      }

      if (thatbyte < thisbyte) {
        return 1;
      }
    }

    return 0;
  }

  public String toString() {
    return new ToString(this)
            .append("path", getParent().getPath())
            .append("name", getName())
            .append("value.length", getValue().length)
            .toString();
  }

  public Object clone() {
    final CarddavResourceContent nobj = new CarddavResourceContent();
    copyTo(nobj);

    return nobj;
  }
}

