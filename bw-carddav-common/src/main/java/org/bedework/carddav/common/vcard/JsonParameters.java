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

import org.bedework.webdav.servlet.shared.WebdavException;

import com.fasterxml.jackson.core.JsonGenerator;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.parameter.Type;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Mike Douglass douglm rpi.edu
 * @version 1.0
 */
public class JsonParameters implements Serializable {
  /* An entry in here if the value may be multi-valued..
   */
  private final static Map<String, String> multiMap = new HashMap<>();

  static {
    //multiMap.put("pid", ""); ical4j wrong - pid multi-valued
    multiMap.put("type", "");
    //multiMap.put("sort-as", ""); ical4j wrong
  }

  public static void addFields(final JsonGenerator jgen,
                               final Property prop) throws WebdavException {
    try {
      jgen.writeStartObject();

      final List<Parameter> pl = prop.getParameters();

      if ((pl != null) && (pl.size() > 0)) {
        for (final Parameter p: pl) {
          final String nm = p.getId().getPname().toLowerCase();
          jgen.writeFieldName(nm);

          if (multiMap.get(nm) == null) {
            jgen.writeString(p.getValue());
          } else {
            outValue(jgen, p);
          }
        }
      }

      jgen.writeEndObject();
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  private static void outValue(final JsonGenerator jgen,
                               final Parameter par) throws Throwable {
    if (par instanceof Type) {
      final Type t = (Type)par;

      outVal(jgen, t.getTypes());
//      return;
    }
  }

  private static void outVal(final JsonGenerator jgen,
                               final String[] val) throws Throwable {
    if (val.length == 1) {
      jgen.writeString(val[0]);
      return;
    }

    jgen.writeStartArray();

    for (final String s: val) {
      jgen.writeString(s);
    }

    jgen.writeEndArray();
  }
}
