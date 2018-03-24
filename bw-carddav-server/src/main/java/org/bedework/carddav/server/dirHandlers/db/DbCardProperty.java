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

import org.bedework.util.misc.Util;

import java.util.ArrayList;
import java.util.List;

/** A representation of a vcard property for database persistance in cardDAV.
 * Allows us to index the values for searching
 *
 * @author douglm
 *
 */
public class DbCardProperty extends UnversionedDbentity<DbCardProperty> {
  private DbCard card;

  private String name;

  private List<DbCardParam> params;

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
      //                  final DbCard card,
                        final DbCardParam... params) {
    this.name = name;
    this.value = value;
    //this.card = card;

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
  // @ JoinColumn(name = "BWCD_PROPID", nullable = false)
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
  public int compareTo(final DbCardProperty that) {
    try {
      int res = Util.compareStrings(getName(), that.getName());

      if (res != 0) {
        return res;
      }

      res = Util.compareStrings(getValue(), that.getValue());

      if (res != 0) {
        return res;
      }

//      return Util.compareStrings(getName(), that.getName());
      // TODO - compare params
      return res;
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

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
