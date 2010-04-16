/* **********************************************************************
    Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.VCardOutputter;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.property.Revision;
import net.fortuna.ical4j.vcard.property.Uid;

import org.bedework.carddav.server.CarddavCollection;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cmt.access.AccessPrincipal;

import java.io.Reader;
import java.io.StringWriter;
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

  private static VCardOutputter cardOut = new VCardOutputter();

  /** Create Card with a new embedded VCard
   *
   */
  public Card() {
    this(new VCard());
  }

  /** Create card with supplied vcard
   *
   * @param vcard
   */
  public Card(final VCard vcard) {
    this.vcard = vcard;
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
   * @param val
   * @throws WebdavException
   */
  public void setLastmod(final String val) throws WebdavException {
    try {
      replaceProperty(new Revision(null, val));
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
   * @param val
   */
  public void addProperty(final Property val) {
    vcard.getProperties().add(val);
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

    PropertyOutput version = new PropertyOutput("VERSION", "4.0");

    version.outputJson(indent, sb);

    Set<String> pnames = VcardDefs.getPropertyNames();

    for (String pname: pnames) {
      if ("VERSION".equals(pname)) {
        continue;
      }

      List<Property> props = findProperties(pname);

      if (!props.isEmpty()) {
        new PropertyOutput(props).outputJson(indent, sb);
      }
    }

    List<Property> props = vcard.getProperties();

    if (props != null) {
      for (Property p: props) {
        if (!pnames.contains(p.getId().toString())) {
          new PropertyOutput(p).outputJson(indent, sb);
        }
      }
    }

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