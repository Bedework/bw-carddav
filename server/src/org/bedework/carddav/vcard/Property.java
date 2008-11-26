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
    this(null, name, (Collection<Param>)null, value);
  }

  /**
   * @param group
   * @param name
   * @param value
   */
  public Property(String group, String name, String value) {
    this(group, name, (Collection<Param>)null, value);
  }

  /**
   * @param name
   * @param params
   * @param value
   */
  public Property(String name,
                  Collection<Param> params,
                  String value) {
    this(null, name, params, value);
  }

  /**
   * @param group
   * @param name
   * @param params
   * @param value
   */
  public Property(String group,
                  String name,
                  Collection<Param> params,
                  String value) {
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
    /* "email" : [
    {"type" : ["pref"], "value" : "foo at example.com"},
    {"value" : "bar at example.com"}
  ]
 */

    if (value == null) {
      return;
    }

    sb.append(indent);
    sb.append("\"");
    sb.append(Util.jsonName(name));
    sb.append("\" : [\n");

    String saveIndent = indent;
    indent +="  ";

    if (params != null) {
      for (Param par: params) {
        par.outputJson(indent, sb);
        sb.append(",\n");
      }
    }

    sb.append(indent);
    sb.append("{\"value\" : ");
    sb.append(Util.jsonEncode(value));
    sb.append("}\n");

    indent = saveIndent;
    sb.append(indent);
    sb.append("]\n");
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    output(sb);

    return sb.toString();
  }
}