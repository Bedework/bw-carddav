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

import net.fortuna.ical4j.vcard.Property;

import org.bedework.carddav.vcard.VcardDefs.PropertyDef;

import edu.rpi.sss.util.Util;

import java.util.List;

/** Vcard property
 *
 * @author douglm
 *
 */
public class PropertyOutput {
  //private String group;
  private String name;

  private String value;

  private Property p;

  private PropertyDef def;

  private List<Property> ps;

  /**
   *
   */
  public PropertyOutput() {
  }

  /**
   * @param name
   * @param value
   */
  public PropertyOutput(final String name, final String value) {
    def = VcardDefs.getPropertyDef(name);
    this.name = name;
    this.value = value;
  }

  /**
   * @param p
   */
  public PropertyOutput(final Property p) {
    def = VcardDefs.getPropertyDef(p.getId().toString());
    this.p = p;
  }

  /**
   * @param ps
   */
  public PropertyOutput(final List<Property> ps) {
    p = ps.get(0); // For th ename
    def = VcardDefs.getPropertyDef(p.getId().toString());
    this.ps = ps;
  }

  /**
   * @param indent
   * @param sb
   */
  public void outputJson(final String indent, final StringBuilder sb) {
    outputJsonStart(indent, sb);

    if (ps == null) {
      outputJsonValue(p, indent, sb);
    } else {
      for (Property pr: ps) {
        outputJsonValue(pr, indent, sb);
      }
    }

    outputJsonEnd(indent, sb);
  }

  /**
   * @param indent
   * @param sb
   */
  private void outputJsonStart(final String indent, final StringBuilder sb) {
    boolean cardinalityZeroOrMore = (def == null) ||
               (def.getCardinality() == VcardDefs.cardinalityZeroOrMore);

    sb.append(indent);
    sb.append("\"");

    String nm;

    if (name != null) {
      nm = name;
    } else {
      nm = p.getId().toString();
    }

    sb.append(Util.jsonName(nm));

    if (cardinalityZeroOrMore) {
      sb.append("\" : [");
    } else {
      sb.append("\" : ");
    }
  }

  /**
   * @param pval
   * @param indent
   * @param sb
   * @param def
   */
  private void outputJsonValue(final Property pval,
                               String indent,
                               final StringBuilder sb) {
    boolean cardinalityZeroOrMore = (def == null) ||
                (def.getCardinality() == VcardDefs.cardinalityZeroOrMore);

    /* "email" : [
    {"type" : ["pref"], "value" : "foo at example.com"},
    {"value" : "bar at example.com"}
  ]
 */

    if (cardinalityZeroOrMore) {
      sb.append(indent);
    }

    sb.append("{");

    String saveIndent = indent;
    indent +="  ";

    /*
    if (!Util.isEmpty(params)) {
      for (Param par: params) {
        par.outputJson(indent, sb);
        sb.append(",\n");
      }

      sb.append(indent);
    }
    */

    sb.append("\"value\" : ");

    String val;

    if (name != null) {
      val = value;
    } else {
      val = pval.getValue();
    }

    if (val == null) {
      sb.append(Util.jsonEncode(""));
    } else {
      sb.append(Util.jsonEncode(val));
    }
    sb.append("},\n");

    indent = saveIndent;
  }

  /**
   * @param indent
   * @param sb
   * @param def
   */
  private void outputJsonEnd(final String indent, final StringBuilder sb) {
    boolean cardinalityZeroOrMore = (def == null) ||
               (def.getCardinality() == VcardDefs.cardinalityZeroOrMore);
    sb.append(indent);

    if (cardinalityZeroOrMore) {
      sb.append("],");
    }

    sb.append("\n");
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    outputJson("", sb);

    return sb.toString();
  }
}