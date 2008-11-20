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
package org.bedework.carddav.server;

import java.io.Serializable;

import edu.rpi.sss.util.Util;

/** Represent the content for a resource stored within the system, e.g an attachment or an
 * image. The actual content is stored in a BwResourceContent object to allow us
 * to put off retrieval of content - or maybe even store outside of the db.
 *
 *  @author Mike Douglass   douglm - rpi.edu
 */
public class CarddavResourceContent implements Comparable<CarddavResourceContent>, Serializable {
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

  /* ====================================================================
   *                      Bean methods
   * ==================================================================== */

  // CALWRAPPER
  /** Set the object's collection
   *
   * @param val    CarddavColNode object's collection
   */
  public void setParent(CarddavColNode val) {
    collection = val;
  }

  // CALWRAPPER
  /** Get the object's collection
   *
   * @return BwCalendar   the object's collection
   */
  public CarddavColNode getParent() {
    return collection;
  }

  /** Set the name
   *
   * @param val    String name
   */
  public void setName(String val) {
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
  public void setValue(byte[] val) {
    value = val;
  }

  /** Get the value
   *
   *  @return byte[]     length
   */
  public byte[] getValue() {
    return value;
  }

  /* ====================================================================
   *                   Other non-db methods
   * ==================================================================== */

  /** Copy this objects values into the parameter
   *
   * @param val
   */
  public void copyTo(CarddavResourceContent val) {
    val.setParent(getParent());
    val.setName(getName());
    val.setValue(getValue());
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  public int hashCode() {
    return getValue().hashCode();
  }

  public int compareTo(CarddavResourceContent that)  {
    if (this == that) {
      return 0;
    }

    int res = Util.cmpObjval(getParent().getWdCollection(),
                             that.getParent().getWdCollection());
    if (res != 0) {
      return res;
    }

    byte[] thisone = getValue();
    byte[] thatone = that.getValue();

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
      byte thisbyte = thisone[i];
      byte thatbyte = thatone[i];

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
    StringBuilder sb = new StringBuilder("BwAttachment{");

    sb.append("path=");
    sb.append(getParent().getPath());
    sb.append(", name=");
    sb.append(getName());

    sb.append(", value.length=");
    sb.append(getValue().length);

    sb.append("}");

    return sb.toString();
  }

  public Object clone() {
    CarddavResourceContent nobj = new CarddavResourceContent();
    copyTo(nobj);

    return nobj;
  }
}

