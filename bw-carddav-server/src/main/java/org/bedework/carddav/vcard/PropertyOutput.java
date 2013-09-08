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
package org.bedework.carddav.vcard;

import org.bedework.carddav.vcard.VcardDefs.PropertyDef;
import org.bedework.util.json.JsonUtil;

import net.fortuna.ical4j.vcard.Property;

import java.util.List;

/** Vcard property
 *
 * @author douglm
 *
 */
public class PropertyOutput {
  private boolean first;

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
   * @param first
   */
  public PropertyOutput(final String name,
                        final String value,
                        final boolean first) {
    def = VcardDefs.getPropertyDef(name);
    this.name = name;
    this.value = value;
    this.first = first;
  }

  /**
   * @param p
   * @param first
   */
  public PropertyOutput(final Property p,
                        final boolean first) {
    def = VcardDefs.getPropertyDef(p.getId().toString());
    this.p = p;
    this.first = first;
  }

  /**
   * @param ps
   * @param first
   */
  public PropertyOutput(final List<Property> ps,
                        final boolean first) {
    p = ps.get(0); // For the name
    def = VcardDefs.getPropertyDef(p.getId().toString());
    this.ps = ps;
    this.first = first;
  }

  /**
   * @param indent
   * @param sb
   */
  public void outputJson(final String indent,
                         final StringBuilder sb) {
    outputJsonStart(indent, sb);

    if (ps == null) {
      outputJsonValue(p, indent, sb, true);
    } else {
      boolean firstVal = true;

      for (Property pr: ps) {
        outputJsonValue(pr, indent + "        ", sb, firstVal);

        firstVal = false;
      }
    }

    outputJsonEnd(indent + "        ", sb);
  }

  /**
   * @param indent
   * @param sb
   */
  private void outputJsonStart(final String indent,
                               final StringBuilder sb) {
    boolean cardinalityZeroOrMore = (def == null) ||
               (def.getCardinality() == VcardDefs.cardinalityZeroOrMore);

    if (!first) {
      sb.append(",\n");
    }

    sb.append(indent);
    sb.append("\"");

    String nm;

    if (name != null) {
      nm = name;
    } else {
      nm = p.getId().toString();
    }

    sb.append(JsonUtil.jsonName(nm));

    if (cardinalityZeroOrMore) {
      sb.append("\" : [\n");
    } else {
      sb.append("\" : ");
    }
  }

  /**
   * @param pval
   * @param indent
   * @param sb
   * @param first
   */
  private void outputJsonValue(final Property pval,
                               String indent,
                               final StringBuilder sb,
                               final boolean first) {
    boolean cardinalityZeroOrMore = (def == null) ||
                (def.getCardinality() == VcardDefs.cardinalityZeroOrMore);

    /* "email" : [
    {"type" : ["pref"], "value" : "foo at example.com"},
    {"value" : "bar at example.com"}
  ]
 */

    if (!first) {
      sb.append(",\n");
    }

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
      sb.append(JsonUtil.jsonEncode(""));
    } else {
      sb.append(JsonUtil.jsonEncode(val));
    }
    sb.append("}");

    indent = saveIndent;
  }

  /**
   * @param indent
   * @param sb
   */
  private void outputJsonEnd(final String indent,
                             final StringBuilder sb) {
    boolean cardinalityZeroOrMore = (def == null) ||
               (def.getCardinality() == VcardDefs.cardinalityZeroOrMore);

    if (cardinalityZeroOrMore) {
      sb.append("\n");
      sb.append(indent);
      sb.append("]");
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    outputJson("", sb);

    return sb.toString();
  }
}
