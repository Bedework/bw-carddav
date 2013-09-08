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

import org.bedework.carddav.server.CarddavCollection;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cmt.access.AccessPrincipal;

import net.fortuna.ical4j.data.FoldingWriter;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Escapable;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.util.Strings;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.property.Revision;
import net.fortuna.ical4j.vcard.property.Uid;
import net.fortuna.ical4j.vcard.property.Version;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** A vcard and properties for cardDAV
 *
 * @author douglm
 *
 */
public class Card {

  private AccessPrincipal owner;

  private String name;

  private CarddavCollection parent;

  private String created;

  private VCard vcard;

  private String strForm;

  private String jsonStrForm;

  private String prevLastmod;

  /** Create Card with a new embedded VCard
   *
   */
  public Card() {
    vcard = new VCard();
    vcard.getProperties().add(Version.VERSION_4_0);
  }

  /** Create card with supplied vcard
   *
   * @param vcard
   */
  public Card(final VCard vcard) {
    this.vcard = vcard;
    prevLastmod = getLastmod();
  }

  /**
   * @param val
   */
  public void setOwner(final AccessPrincipal val) {
    owner = val;
  }

  /**
   * @return AccessPrincipal
   */
  public AccessPrincipal getOwner() {
    return owner;
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

  /**
   * @param val
   */
  public void setCreated(final String val) {
    created = val;
  }

  /**
   * @return String created
   */
  public String getCreated() {
    return created;
  }

  /**
   * @throws WebdavException
   */
  public void setLastmod() throws WebdavException {
    DateTime dt = new DateTime(true);

    setLastmod(new LastModified(dt).getValue());
  }

  /**
   * @param val
   * @throws WebdavException
   */
  public void setLastmod(final String val) throws WebdavException {
    try {
      replaceProperty(new Revision(new ArrayList<Parameter>(), val));
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /**
   * @return String
   */
  public String getLastmod() {
    Revision rev = (Revision)findProperty(Property.Id.REV);

    if (rev == null) {
      return null;
    }

    return rev.getValue();
  }

  /** Lastmod before any changes were made
   *
   * @return String
   */
  public String getPrevLastmod() {
    return prevLastmod;
  }

  /**
   * @param val
   * @throws WebdavException
   */
  public void setUid(final String val) throws WebdavException {
    try {
      replaceProperty(new Uid(null, val));
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /**
   * @return String
   * @throws WebdavException
   */
  public String getUid() throws WebdavException {
    Uid uid = (Uid)findProperty(Property.Id.UID);

    if (uid == null) {
      setUid(org.bedework.util.misc.Uid.getUid());
      uid = (Uid)findProperty(Property.Id.UID);
    }

    return uid.getValue();
  }

  /**
   * @param val
   */
  public void setParent(final CarddavCollection val) {
    parent = val;
  }

  /**
   * @return parent.
   */
  public CarddavCollection getParent() {
    return parent;
  }

  /**
   * @return vcard or null
   */
  public VCard getVcard() {
    return vcard;
  }

  /**
   * @param val
   */
  public void addProperty(final Property val) {
    if ((val.getId() != Property.Id.VERSION) ||
        (findProperty(Property.Id.VERSION) == null)) {
      vcard.getProperties().add(val);
    }
  }

  /**
   * @param id
   * @return property or null
   */
  public Property findProperty(final Id id) {
    return vcard.getProperty(id);
  }

  /**
   * @param name
   * @return property or null
   */
  public Property findProperty(final String name) {
    Property.Id id = null;

    for (Property.Id i: Property.Id.values()) {
      if (i.toString().equals(name)) {
        id = i;
        break;
      }
    }

    if (id != null) {
      return vcard.getProperty(id);
    }

    return vcard.getExtendedProperty(name);
  }

  /**
   * @param name
   * @return property or null
   */
  public List<Property> findProperties(final String name) {
    Property.Id id = null;

    for (Property.Id i: Property.Id.values()) {
      if (i.toString().equals(name)) {
        id = i;
        break;
      }
    }

    if (id != null) {
      return vcard.getProperties(id);
    }

    return vcard.getExtendedProperties(name);
  }

  /**
   * @param rdr
   * @return Vcard
   * @throws WebdavException
   */
  public void parse(final Reader rdr) throws WebdavException {
    try {
      vcard = new VCardBuilder(rdr).build();
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /**
   * @return String
   * @throws WebdavException
   */
  public String output(final String version) throws WebdavException {
    if (strForm != null) {
      return strForm;
    }

    StringWriter sw = new StringWriter();

    final FoldingWriter wtr = new FoldingWriter(sw,
                                                FoldingWriter.REDUCED_FOLD_LENGTH);
    try {
      wtr.write("BEGIN:VCARD");
      wtr.write(Strings.LINE_SEPARATOR);

      /* Version should come before anything else. */
      boolean version4 = false;

      if (version != null) {
        version4 = version.equals("4.0");
      } else {
        Version v = (Version)vcard.getProperty(Property.Id.VERSION);

        if (v != null) {
          version4 = v.equals(Version.VERSION_4_0);
        }
      }

      if (version4) {
        wtr.write(Version.VERSION_4_0.toString());
      } else {
        wtr.write(new Version("3.0").toString());
      }

      for (Property prop : vcard.getProperties()) {
        if (prop.getId() == Property.Id.VERSION) {
          continue;
        }

        if (version4) {
          wtr.write(prop.toString());
          continue;
        }

        /* Attempt to downgrade by turning some properties into x-props. */
        appendDowngraded(wtr, prop);
      }

      wtr.write("END:VCARD");
      wtr.write(Strings.LINE_SEPARATOR);
    } catch (Throwable t) {
      throw new WebdavException(t);
    } finally {
      try {
        wtr.close();
      } catch (Throwable t) {
        throw new WebdavException(t);
      }
    }

    strForm = sw.toString();

    return strForm;
  }

  /**
   * @param indent
   * @return String
   */
  public String outputJson(String indent) {
    if (jsonStrForm != null) {
      return jsonStrForm;
    }

    StringBuilder sb = new StringBuilder();

    sb.append(indent);
    sb.append("{\n");

    indent += "";

    new PropertyOutput(vcard.getProperty(Property.Id.VERSION),
                       true).outputJson(indent, sb);

    Set<String> pnames = VcardDefs.getPropertyNames();

    /* Output known properties first */

    for (String pname: pnames) {
      if ("VERSION".equals(pname)) {
        continue;
      }

      List<Property> props = findProperties(pname);

      if (!props.isEmpty()) {
        new PropertyOutput(props, false).outputJson(indent, sb);
      }
    }

    /* Now ouput any extra unknown properties */

    List<Property> props = vcard.getProperties();

    if (props != null) {
      for (Property p: props) {
        if (!pnames.contains(p.getId().toString())) {
          new PropertyOutput(p, false).outputJson(indent, sb);
        }
      }
    }

    sb.append("\n");
    sb.append(indent);
    sb.append("}");

    jsonStrForm = sb.toString();

    return jsonStrForm;
  }

  @Override
  public String toString() {
    try {
      return output(null);
    } catch (Throwable t) {
      return t.getMessage();
    }
  }

  private void replaceProperty(final Property val) {
    List<Property> ps = vcard.getProperties();

    Property p = vcard.getProperty(val.getId());

    if (p != null) {
      ps.remove(p);
    }

    ps.add(val);
  }

  private void appendDowngraded(final Writer wtr,
                                final Property prop) throws IOException {
    /* From rfc6350 - vcard 4.0

Appendix A. Differences from RFCs 2425 and 2426
 This appendix contains a high-level overview of the major changes
 that have been made in the vCard specification from RFCs 2425 and
 2426.  It is incomplete, as it only lists the most important changes.

A.1. New Structure
 o  [RFC2425] and [RFC2426] have been merged.

 o  vCard is now not only a MIME type but a stand-alone format.

 o  A proper MIME type registration form has been included.

 o  UTF-8 is now the only possible character set.

 o  New vCard elements can be registered from IANA.

A.2. Removed Features
 o  The CONTEXT and CHARSET parameters are no more.

 o  The NAME, MAILER, LABEL, and CLASS properties are no more.

 o  The "intl", "dom", "postal", and "parcel" TYPE parameter values
    for the ADR property have been removed.

 o  In-line vCards (such as the value of the AGENT property) are no
    longer supported.

A.3. New Properties and Parameters
 o  The KIND, GENDER, LANG, ANNIVERSARY, XML, and CLIENTPIDMAP
    properties have been added.

 o  [RFC2739], which defines the FBURL, CALADRURI, CAPURI, and CALURI
    properties, has been merged in.

 o  [RFC4770], which defines the IMPP property, has been merged in.

 o  The "work" and "home" TYPE parameter values are now applicable to
    many more properties.

 o  The "pref" value of the TYPE parameter is now a parameter of its
    own, with a positive integer value indicating the level of
    preference.

 o  The ALTID and PID parameters have been added.

 o  The MEDIATYPE parameter has been added and replaces the TYPE
    parameter when it was used for indicating the media type of the
    property's content.
     */

    if (v3Ok(prop)) {
      wtr.write(prop.toString());
      return;
    }

    /* x-prop already? */

    if (Property.Id.EXTENDED == prop.getId()) {
      wtr.write(prop.toString());
      return;
    }

    /* Output as x-prop */
    if (prop.getGroup() != null) {
      wtr.write(prop.getGroup().toString());
      wtr.write('.');
    }

    wtr.write(VcardDefs.v4AsXpropPrefix);
    wtr.write(prop.getId().getPropertyName());

    for (Parameter param : prop.getParameters()) {
      wtr.write(';');

      /* Watch for non v3 */
      wtr.write(param.toString());
    }
    wtr.write(':');

    if (prop instanceof Escapable) {
      wtr.write(Strings.escape(Strings.valueOf(prop.getValue())));
    }
    else {
      wtr.write(Strings.valueOf(prop.getValue()));
    }

    wtr.write(Strings.LINE_SEPARATOR);
  }

  private static Set<Property.Id> notV3Ok = new TreeSet<Property.Id>();

  static {
    notV3Ok.add(Property.Id.KIND);
    notV3Ok.add(Property.Id.GENDER);
    notV3Ok.add(Property.Id.LANG);
    notV3Ok.add(Property.Id.ANNIVERSARY);
    notV3Ok.add(Property.Id.XML);
    notV3Ok.add(Property.Id.CLIENTPIDMAP);
  }

  /* Return true if this property should be OK for v3 */
  private boolean v3Ok(final Property prop) {
    Property.Id id = prop.getId();

    if (notV3Ok.contains(id)) {
  return false;
    }

    Parameter par = prop.getParameter(Parameter.Id.ALTID);
    if (par != null) {
      return false;
    }

    par = prop.getParameter(Parameter.Id.PID);
    if (par != null) {
      return false;
    }

    return true;
  }
}
