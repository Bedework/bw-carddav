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
package org.bedework.carddav.common.vcard;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/** Vcard definitions
 *
 * @author douglm
 *
 */
public class VcardDefs {
  /** Prefix for V4 properties converted to v3 */
  public static final String v4AsXpropPrefix = "X-ICAL4J-TOV3-";

  /** Valid versions
   */
  public static final Set<String> validVersions;

  static {
    Set<String> vs = new TreeSet<String>();
    vs.add("3.0");
    vs.add("4.0");

    validVersions = Collections.unmodifiableSet(vs);
  }

  /** */
  public static final int cardinalityZeroOrOne = 0;
  /** */
  public static final int cardinalityOne = 1;
  /** */
  public static final int cardinalityZeroOrMore = 2;

  /** Information about a vcard property
   *
   */
  public static class PropertyDef {
    private String name;

    private int cardinality;

    /**
     * @param name
     * @param cardinality
     */
    public PropertyDef(final String name,
                       final int cardinality) {
      this.name = name;
      this.cardinality = cardinality;
    }

    /**
     * @return String name
     */
    public String getName() {
      return name;
    }

    /**
     * @return int cardinality
     */
    public int getCardinality() {
      return cardinality;
    }
  }

  private static Map<String, PropertyDef> propertyDefs = new HashMap<String, PropertyDef>();

  static {
    addPropertyDef("SOURCE", cardinalityZeroOrMore);
    addPropertyDef("NAME", cardinalityOne);
    addPropertyDef("KIND", cardinalityZeroOrOne);
    addPropertyDef("FN", cardinalityOne);
    addPropertyDef("N", cardinalityZeroOrOne);
    addPropertyDef("NICKNAME", cardinalityZeroOrMore);
    addPropertyDef("PHOTO", cardinalityZeroOrOne);
    addPropertyDef("BDAY", cardinalityZeroOrOne);
    addPropertyDef("DDAY", cardinalityZeroOrOne);
    addPropertyDef("BIRTH", cardinalityZeroOrOne);
    addPropertyDef("DEATH", cardinalityZeroOrOne);
    addPropertyDef("GENDER", cardinalityZeroOrOne);
    addPropertyDef("ADR", cardinalityZeroOrMore);
    addPropertyDef("LABEL", cardinalityZeroOrOne);
    addPropertyDef("TEL", cardinalityZeroOrMore);
    addPropertyDef("EMAIL", cardinalityZeroOrMore);
    addPropertyDef("IMPP", cardinalityZeroOrMore);
    addPropertyDef("LANG", cardinalityOne);
    addPropertyDef("TZ", cardinalityOne);
    addPropertyDef("GEO", cardinalityZeroOrOne);
    addPropertyDef("TITLE", cardinalityZeroOrOne);
    addPropertyDef("ROLE", cardinalityZeroOrOne);
    addPropertyDef("LOGO", cardinalityZeroOrMore);
    addPropertyDef("ORG", cardinalityZeroOrMore);
    addPropertyDef("MEMBER", cardinalityZeroOrMore);
    addPropertyDef("RELATED", cardinalityZeroOrMore);
    addPropertyDef("CATEGORIES", cardinalityZeroOrMore);
    addPropertyDef("NOTE", cardinalityZeroOrOne);
    addPropertyDef("PRODID", cardinalityZeroOrOne);
    addPropertyDef("REV", cardinalityZeroOrOne);
    addPropertyDef("SORT-STRING", cardinalityZeroOrOne);
    addPropertyDef("SOUND", cardinalityZeroOrOne);
    addPropertyDef("UID", cardinalityOne);
    addPropertyDef("URL", cardinalityZeroOrMore);
    addPropertyDef("VERSION", cardinalityZeroOrOne);
    addPropertyDef("CLASS", cardinalityZeroOrOne);
    addPropertyDef("KEY", cardinalityZeroOrMore);
    addPropertyDef("FBURL", cardinalityZeroOrOne);
    addPropertyDef("CALADRURI", cardinalityZeroOrOne);
    addPropertyDef("CALURI", cardinalityZeroOrOne);

    // Resource properties

    addPropertyDef("AUTOACCEPT", cardinalityOne);
    addPropertyDef("BOOKINGSTART", cardinalityOne);
    addPropertyDef("BOOKINGEND", cardinalityOne);
    addPropertyDef("CAPACITY", cardinalityOne);
    addPropertyDef("COSTINFO", cardinalityOne);
  }

  /** XXX This should probably be cloned
   *
   * @param name
   * @return
   */
  public static PropertyDef getPropertyDef(final String name) {
    return propertyDefs.get(name.toUpperCase());
  }

  /**
   * @return Set<String>
   */
  public static Set<String> getPropertyNames() {
    return propertyDefs.keySet();
  }

  private static void addPropertyDef(final String name,
                                     final int cardinality) {
    propertyDefs.put(name, new PropertyDef(name, cardinality));
  }
}
