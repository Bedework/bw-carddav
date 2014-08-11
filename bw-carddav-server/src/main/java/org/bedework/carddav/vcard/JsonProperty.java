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

import org.bedework.util.calendar.XcalUtil;
import org.bedework.util.vcard.CardPropertyIndex.DataType;
import org.bedework.util.vcard.CardPropertyIndex.PropertyInfoIndex;
import org.bedework.webdav.servlet.shared.WebdavException;

import com.fasterxml.jackson.core.JsonGenerator;
import net.fortuna.ical4j.model.TextList;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.property.Categories;
import net.fortuna.ical4j.vcard.property.Xproperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Mike Douglass douglm rpi.edu
 * @version 1.0
 */
@SuppressWarnings("ConstantConditions")
public class JsonProperty implements Serializable {
  public static void addFields(final JsonGenerator jgen,
                               final Property prop) throws WebdavException {
    try {
      jgen.writeStartArray();

      jgen.writeString(getPname(prop));

      JsonParameters.addFields(jgen, prop);

      final DataType type = getType(prop);
      jgen.writeString(type.getJsonType());

      outValue(jgen, prop, type);

      jgen.writeEndArray();
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* An entry in here if the value may have different types..
   */
  private final static Set<String> types = new TreeSet<>();

  static {
  }

  private static String getPname(final Property prop) {
    if (prop instanceof Xproperty) {
      return ((Xproperty)prop).getExtendedName().toLowerCase();
    }

    return prop.getId().getPropertyName().toLowerCase();
  }

  private static DataType getType(final Property prop) {
    final String pname = getPname(prop);
    final PropertyInfoIndex pii = PropertyInfoIndex.fromName(pname);

    if (pii == null) {
      return DataType.TEXT;
    }

    final DataType dtype = pii.getPtype();
    if (dtype == null) {
      return DataType.TEXT;
    }

    if ((dtype != DataType.SPECIAL) && (!types.contains(pname))) {
      return dtype;
    }

    // in the absence of anything else callit text
    return DataType.TEXT;
  }

  private abstract static class PropertyValueEmitter {
    abstract void emitValue(JsonGenerator jgen,
                            Property prop) throws Throwable;

    protected void emitValue(final JsonGenerator jgen,
                             final Property prop,
                             final DataType type) throws Throwable {
      throw new RuntimeException("Unimplemented");
    }
  }

  /* An entry in here if we special case the value..
   */
  private final static Map<String, PropertyValueEmitter> valMap = new HashMap<>();

  static {
    valMap.put("categories", new CategoriesValueEmitter());
  }

  private static class SingleValueEmitter extends PropertyValueEmitter {
    @Override
    public void emitValue(final JsonGenerator jgen,
                          final Property prop) throws Throwable {
      emitValue(jgen, prop, DataType.TEXT);
      jgen.writeString(prop.getValue());
    }

    @Override
    public void emitValue(final JsonGenerator jgen,
                          final Property prop,
                          final DataType type) throws Throwable {
      switch (type) {
        case BOOLEAN:
          jgen.writeBoolean(Boolean.valueOf(prop.getValue()));
          break;
        case DATE:
        case DATE_AND_OR_TIME:
        case DATE_TIME:
          jgen.writeString(XcalUtil.getXmlFormatDateTime(
                  prop.getValue()));
          break;
        case FLOAT:
          jgen.writeNumber(Float.valueOf(prop.getValue()));
          break;
        case INTEGER:
          jgen.writeNumber(Integer.valueOf(prop.getValue()));
          break;
        case PERIOD:
          // Should not get here - just write something out
          jgen.writeString(prop.getValue());
          break;
        case BINARY:
        case CUA:
        case TEXT:
        case URI:
          jgen.writeString(prop.getValue());
          break;
        case TIME:
          jgen.writeString(XcalUtil.getXmlFormatTime(prop.getValue()));
          break;
        case UTC_OFFSET:
          jgen.writeString(XcalUtil.getXmlFormatUtcOffset(
                  prop.getValue()));
          break;
        case SPECIAL:
          break;
        case HREF:
          break;
      }
    }
  }

  private static class CategoriesValueEmitter extends PropertyValueEmitter {
    @Override
    public void emitValue(final JsonGenerator jgen,
                          final Property prop) throws Throwable {
      final Categories p = (Categories)prop;

      jgen.writeStartArray();

      final TextList tl = p.getCategories();
      final Iterator it = tl.iterator();
      while (it.hasNext()){
        jgen.writeString(it.next().toString());
      }

      jgen.writeEndArray();
    }
  }

  private final static PropertyValueEmitter defValEmitter = new SingleValueEmitter();

  private static void outValue(final JsonGenerator jgen,
                               final Property prop,
                               final DataType type) throws Throwable {
    final PropertyValueEmitter pve = valMap.get(getPname(prop));

    if (pve == null) {
      defValEmitter.emitValue(jgen, prop, type);
      return;
    }

    pve.emitValue(jgen, prop);
  }
}
