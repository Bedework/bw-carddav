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

import org.bedework.carddav.vcard.VcardDefs.PropertyDef;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.sss.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/** Vcard property
 *
 * @author douglm
 *
 */
/**
 * @author douglm
 *
 */
public class Property {
  String group;
  String name;

  Collection<Param> params;

  String value;

  private PropertyDef def;

  /**
   *
   */
  public Property() {
  }

  /**
   * @param name
   * @param value
   */
  public Property(String name, String value) {
    this(null, null, name, (Collection<Param>)null, value);
  }

  /**
   * @param group
   * @param name
   * @param value
   */
  public Property(String group, String name, String value) {
    this(null, group, name, (Collection<Param>)null, value);
  }

  /**
   * @param group
   * @param def
   * @param value
   */
  public Property(PropertyDef def, String value) {
    this(def, null, def.getName(), (Collection<Param>)null, value);
  }

  /**
   * @param group
   * @param def
   * @param value
   */
  public Property(String group, PropertyDef def, String value) {
    this(def, group, def.getName(), (Collection<Param>)null, value);
  }

  /**
   * @param def
   * @param name
   * @param params
   * @param value
   */
  public Property(PropertyDef def,
                  String name,
                  Collection<Param> params,
                  String value) {
    this(def, null, name, params, value);
  }

  /**
   * @param def
   * @param group
   * @param name
   * @param params
   * @param value
   */
  public Property(PropertyDef def,
                  String group,
                  String name,
                  Collection<Param> params,
                  String value) {
    if (def == null) {
      def = VcardDefs.getPropertyDef(name);
    }
    this.def = def;
    this.group = group;
    this.name = name;
    this.params = params;
    this.value = value;
  }

  /**
   * @return String name
   */
  public String getName() {
    return name;
  }

  /**
   * @return String value
   */
  public String getValue() {
    return value;
  }

  /**
   * @param val
   */
  public void addParam(Param val) {
    if (params == null) {
      params = new ArrayList<Param>();
    }

    params.add(val);
  }

  /**
   * @param tokeniser
   * @return Property
   * @throws WebdavException
   */
  public Property parse(Tokenizer tokeniser) throws WebdavException {
    try {
      tokeniser.assertWord();

      String temp = tokeniser.sval;

      if (tokeniser.nextToken() == ',') {
        group = temp;
        tokeniser.assertWord();

        temp = tokeniser.sval;
      } else {
        tokeniser.pushBack();
      }

      name = temp;
      def = VcardDefs.getPropertyDef(name);

      while (tokeniser.nextToken() == ';') {
        addParam(new Param().parse(tokeniser));
      }

      tokeniser.assertToken(':');
    } catch (IOException e) {
      throw new WebdavException(e);
    }

    return this;
  }

  /** Crude vcard property output method
   *
   * @param sb
   */
  public void output(StringBuilder sb) {
    if (value == null) {
      return;
    }

    sb.append(name);

    if (params != null) {
      for (Param par: params) {
        sb.append(";");
        par.output(sb);
      }
    }

    sb.append(":");
    sb.append(value);
    sb.append("\n");
  }

  /**
   * @param indent
   * @param sb
   */
  public void outputJson(String indent, StringBuilder sb) {
    outputJsonStart(indent, sb);
    outputJsonValue(indent, sb);
    outputJsonEnd(indent, sb);
  }

  /**
   * @param indent
   * @param sb
   */
  public void outputJsonStart(String indent, StringBuilder sb) {
    boolean cardinalityZeroOrMore = (def == null) ||
               (def.getCardinality() == VcardDefs.cardinalityZeroOrMore);

    sb.append(indent);
    sb.append("\"");
    sb.append(Util.jsonName(name));

    if (cardinalityZeroOrMore) {
      sb.append("\" : [");
    } else {
      sb.append("\" : ");
    }
  }

  /**
   * @param indent
   * @param sb
   * @param def
   */
  public void outputJsonValue(String indent, StringBuilder sb) {
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

    if (!Util.isEmpty(params)) {
      for (Param par: params) {
        par.outputJson(indent, sb);
        sb.append(",\n");
      }

      sb.append(indent);
    }

    sb.append("\"value\" : ");

    if (value == null) {
      sb.append(Util.jsonEncode(""));
    } else {
      sb.append(Util.jsonEncode(value));
    }
    sb.append("},\n");

    indent = saveIndent;
  }

  /**
   * @param indent
   * @param sb
   * @param def
   */
  public void outputJsonEnd(String indent, StringBuilder sb) {
    boolean cardinalityZeroOrMore = (def == null) ||
               (def.getCardinality() == VcardDefs.cardinalityZeroOrMore);
    sb.append(indent);

    if (cardinalityZeroOrMore) {
      sb.append("]");
    }

    sb.append("\n");
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    output(sb);

    return sb.toString();
  }
}