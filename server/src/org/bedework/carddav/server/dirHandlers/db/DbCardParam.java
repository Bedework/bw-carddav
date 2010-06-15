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

import edu.rpi.sss.util.Util;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/** A representation of a vcard property parameter for database persistance in cardDAV.
 * Allows us to index the values for searching
 *
 * @author douglm
 *
 */
@Table(name = "BWCD_PARS")
public class DbCardParam {
  @ManyToOne
  @JoinColumn(name = "BWCD_PROPID", nullable = false,
              updatable = false, insertable = false)
  private DbCardProperty property;

  @Column(name = "BWCD_PARNAME")
  private String name;

  @Column(name = "BWCD_PARVALUE")
  private String value;

  /** Null constructor
   *
   */
  public DbCardParam() {
  }

  /** Create DbCardProperty
   *
   * @param name
   * @param value
   * @param property
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
   * @param val
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

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DbCardParam{");

    sb.append("name=");
    sb.append(getName());
    sb.append(", value=");
    sb.append(getValue());

    sb.append("}");

    return sb.toString();
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

    if (!(o instanceof DbCardParam)) {
      return false;
    }

    DbCardParam that = (DbCardParam)o;

    if (!getName().equals(that.getName())) {
      return false;
    }

    return Util.compareStrings(getValue(), that.getValue()) == 0;
  }
}
