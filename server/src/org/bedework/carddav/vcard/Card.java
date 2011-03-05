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

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.VCardOutputter;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.property.Revision;
import net.fortuna.ical4j.vcard.property.Uid;
import net.fortuna.ical4j.vcard.property.Version;

import org.bedework.carddav.server.CarddavCollection;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cmt.access.AccessPrincipal;

import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

  private static VCardOutputter cardOut = new VCardOutputter(false);

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
      setUid(edu.rpi.sss.util.Uid.getUid());
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
  public String output() throws WebdavException {
    if (strForm != null) {
      return strForm;
    }

    StringWriter sw = new StringWriter();

    synchronized (cardOut) {
      try {
        cardOut.output(vcard, sw);
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
      return output();
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
}
