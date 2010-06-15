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

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/** A representation of a vcard property for database persistance in cardDAV.
 * Allows us to index the values for searching
 *
 * @author douglm
 *
 */
@Table(name = "BWCD_PROPS")
public class DbCardProperty extends UnversionedDbentity<DbCardProperty> {
  @ManyToOne
  @JoinColumn(name = "BWCD_CARDID", nullable = false,
              updatable = false, insertable = false)
  private DbCard card;

  @Column(name = "BWCD_PNAME")
  private String name;

  @OneToMany
  @JoinColumn(name = "BWCD_PROPID", nullable = false)
  private List<DbCardParam> params;

  @Column(name = "BWCD_PVALUE")
  private String value;

  /** Null constructor
   *
   */
  public DbCardProperty() {
  }

  /** Create DbCardProperty
   *
   * @param name
   * @param value
   * @param card
   * @param params
   */
  public DbCardProperty(final String name,
                        final String value,
                        final DbCard card,
                        final DbCardParam... params) {
    this.name = name;
    this.value = value;
    this.card = card;

    for (DbCardParam param: params) {
      if (this.params == null) {
        this.params = new ArrayList<DbCardParam>();
      }

      this.params.add(param);
    }
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
  public void setParams(final List<DbCardParam> val) {
    params = val;
  }

  /**
   * @return parameters or null
   */
  public List<DbCardParam> getParams() {
    return params;
  }

  /**
   * @param val
   */
  public void setCard(final DbCard val) {
    card = val;
  }

  /**
   * @return card.
   */
  public DbCard getCard() {
    return card;
  }

  /**
   * @param val
   */
  public void addParam(final DbCardParam val) {
    if (params == null) {
      params = new ArrayList<DbCardParam>();
    }

    if (!params.contains(val)) {
      params.add(val);
    }
  }

  /**
   * @param name
   * @return param or null
   */
  public DbCardParam findParam(final String name) {
    if (params == null) {
      return null;
    }

    for (DbCardParam param: params) {
      if (name.equals(param.getName())) {
        return param;
      }
    }

    return null;
  }

  /**
   * @param name
   * @return params or null
   */
  public List<DbCardParam> findParams(final String name) {
    if (params == null) {
      return null;
    }

    List<DbCardParam> ps = null;

    for (DbCardParam param: params) {
      if (name.equals(param.getName())) {
        if (ps == null) {
          ps = new ArrayList<DbCardParam>();
        }

        ps.add(param);
      }
    }

    return ps;
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DbCardProperty{");

    sb.append("name=");
    sb.append(getName());

    if (getParams() != null) {
      for (DbCardParam param: params) {
        sb.append(", ");
        sb.append(param.toString());
      }
    }

    sb.append(", value=");
    sb.append(getValue());

    sb.append("}");

    return sb.toString();
  }
}
