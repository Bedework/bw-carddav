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

/** Vcard parameter
 *
 * @author douglm
 *
 */
public class Param {
  private String name;

  private String value;

  /**
   *
   */
  public Param() {
  }

  /**
   * @param name
   * @param value
   */
  public Param(String name, String value) {
    this.name = name;
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

  /** Crude vcard parameter output method
   *
   * @param sb
   */
  public void output(StringBuilder sb) {
    if (value == null) {
      return;
    }

    sb.append(name);
    sb.append("=");
    sb.append(value);
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
    sb.append("{\"");
    sb.append(Util.jsonName(name));
    sb.append("\", \"value\" : ");
    sb.append(Util.jsonEncode(value));
    sb.append("}");
  }


  /**
   * @param tokeniser
   * @return
   * @throws WebdavException
   */
  public Param parse(Tokenizer tokeniser) throws WebdavException {
    try {
      tokeniser.assertWord();

      name = tokeniser.sval;

      if (tokeniser.nextToken() == '=') {
        tokeniser.assertWord();

        value = tokeniser.sval;
      } else {
        tokeniser.pushBack();
      }
    } catch (IOException e) {
      throw new WebdavException(e);
    }

    return this;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();

    output(sb);

    return sb.toString();
  }
}