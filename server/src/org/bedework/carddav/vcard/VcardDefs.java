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
package org.bedework.carddav.vcard;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** Vcard definitions
 *
 * @author douglm
 *
 */
public class VcardDefs {
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
    public PropertyDef(String name,
                       int cardinality) {
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
  }

  /** XXX This should probably be cloned
   *
   * @param name
   * @return
   */
  public static PropertyDef getPropertyDef(String name) {
    return propertyDefs.get(name.toUpperCase());
  }

  /**
   * @return Set<String>
   */
  public static Set<String> getPropertyNames() {
    return propertyDefs.keySet();
  }

  private static void addPropertyDef(String name,
                                     int cardinality) {
    propertyDefs.put(name, new PropertyDef(name, cardinality));
  }
}