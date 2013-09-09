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
package org.bedework.carddav.server.dirHandlers.db;

import org.bedework.carddav.vcard.Card;
import org.bedework.util.misc.Util;
import org.bedework.webdav.servlet.access.AccessState;
import org.bedework.webdav.servlet.shared.WebdavException;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.Property.Id;
import net.fortuna.ical4j.vcard.VCard;
import net.fortuna.ical4j.vcard.VCardBuilder;
import net.fortuna.ical4j.vcard.property.Kind;
import net.fortuna.ical4j.vcard.property.Revision;
import net.fortuna.ical4j.vcard.property.Uid;
import net.fortuna.ical4j.vcard.property.Version;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/** A representation of a vcard and properties for database persistance in cardDAV
 *
 * @author douglm
 *
 */
public class DbCard extends DbNamedEntity<DbCard> {
  private String fn;

  private String uid;

  private String kind;

  private List<DbCardProperty> properties;

  private String lastmod;

  private VCard vcard;

  private String strForm;

  private String prevLastmod;

  //private static VCardOutputter cardOut = new VCardOutputter();

  /** Create DbCard with a new embedded VCard
   *
   * @throws WebdavException
   */
  public DbCard() throws WebdavException {
  }

  /** Create DbCard with new card
   *
   * @param fn
   * @throws WebdavException
   */
  public DbCard(final String fn) throws WebdavException {
    vcard = new VCard();
    vcard.getProperties().add(Version.VERSION_4_0);

    setFn(fn);
    setUid(org.bedework.util.misc.Uid.getUid());
  }

  /** Create DbCard with supplied vcard
   *
   * @param vcard
   * @throws WebdavException
   */
  public DbCard(final VCard vcard) throws WebdavException {
    this.vcard = vcard;

    initFromVcard();
  }

  /** Set the fn
   *
   * @param val    String fn
   * @throws WebdavException
   */
  public void setFn(final String val) throws WebdavException {
    fn = val;
  }

  /** Get the name
   *
   * @return String   name
   * @throws WebdavException
   */
  public String getFn() throws WebdavException {
    return fn;
  }

  /**
   * @param val
   * @throws WebdavException
   */
  public void setUid(final String val) throws WebdavException {
    uid = val;
  }

  /**
   * @return String
   * @throws WebdavException
   */
  public String getUid() throws WebdavException {
    return uid;
  }

  /**
   * @param val
   * @throws WebdavException
   */
  public void setKind(final String val) throws WebdavException {
    kind = val;
  }

  /**
   * @return String
   * @throws WebdavException
   */
  public String getKind() throws WebdavException {
    return kind;
  }

  /**
   * @param val
   */
  public void setProperties(final List<DbCardProperty> val) {
    properties = val;
  }

  /**
   * @return DbCardProperty list
   */
  public List<DbCardProperty> getProperties() {
    return properties;
  }

  /** Set the string form of the card
   *
   * @param val    String
   */
  public void setStrForm(final String val) {
    strForm = val;
  }

  /** Get the name
   *
   * @return String   name
   */
  public String getStrForm() {
    return strForm;
  }

  /**
   * @param val
   * @throws WebdavException
   */
  public void setLastmod(final String val) throws WebdavException {
    lastmod = val;
  }

  /**
   * @return String
   */
  public String getLastmod() {
    return lastmod;
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
   * @param val
   * @throws WebdavException
   */
  public void setVcard(final VCard val) throws WebdavException {
    vcard = val;
    if (val == null) {
      vcard = new VCard();
    }
    initFromVcard();
  }

  /**
   * @return vcard or null
   * @throws WebdavException
   */
  public VCard getVcard() throws WebdavException {
    if (vcard != null) {
      return vcard;
    }

    if (strForm == null) {
      return null;
    }

    parse(new StringReader(strForm));

    return vcard;
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
    try {
      replaceProperty(new Revision(new ArrayList<Parameter>(), getLastmod()));
    } catch (Throwable t) {
      throw new WebdavException(t);
    }

    if (strForm != null) {
      return strForm;
    }

    Card cd = new Card(vcard);
    strForm = cd.output(null);

    /*
    StringWriter sw = new StringWriter();

    synchronized (cardOut) {
      try {
        cardOut.output(vcard, sw);
      } catch (Throwable t) {
        throw new WebdavException(t);
      }
    }

    strForm = sw.toString();
    */

    return strForm;
  }

  /* ====================================================================
   *                   SharedEntity methods
   * ==================================================================== */

  @Override
  public boolean isCollection() {
    return false;
  }

  @Override
  public void setAccessState(final AccessState val) {
    // Don't do this.
  }

  @Override
  public AccessState getAccessState() {
    return null;
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  /** Set the lastmod and created if created is not set already.
   * @throws WebdavException
   */
  public void setDtstamps() throws WebdavException {
    DateTime dt = new DateTime(true);
    setLastmod(new LastModified(dt).getValue());

    if (getCreated() == null) {
      setCreated(new Created(dt).getValue());
    }
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public int compareTo(final DbCard that) {
    try {
      int res = Util.compareStrings(getParentPath(),
                                    that.getParentPath());

      if (res != 0) {
        return res;
      }

      res = Util.compareStrings(getUid(), that.getUid());

      if (res != 0) {
        return res;
      }

      return Util.compareStrings(getName(), that.getName());
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Override
  public int hashCode() {
    return getPath().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("DbCard{");

    toStringSegment(sb);

    return sb.toString();
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private void initFromVcard() throws WebdavException {
    // XXX Do this the inefficient way for the moment
    if (getProperties() != null) {
      getProperties().clear();
    }

    setFn(findProperty(Property.Id.FN).getValue());

    Property k = findProperty(Property.Id.KIND);

    if (k != null) {
      setKind(k.getValue());
    } else {
      setKind(Kind.INDIVIDUAL.getValue());
    }

    Uid uid = (Uid)findProperty(Property.Id.UID);

    if (uid == null) {
      setUid(org.bedework.util.misc.Uid.getUid());
    } else {
      setUid(uid.getValue());
    }

    for (Property p: vcard.getProperties()) {
      if (p.getId() == Property.Id.FN) {
        setFn(p.getValue());
      }

      if (p.getId() == Property.Id.KIND) {
        setKind(p.getValue());
      }

      if (p.getId() == Property.Id.UID) {
        setUid(p.getValue());
      }

      addDbProperty(makeDbProperty(p));
    }
  }

  /**
   * @param name
   * @return property or null
   */
  private DbCardProperty findDbProperty(final String name) {
    if (getProperties() == null) {
      return null;
    }

    for (DbCardProperty p: getProperties()) {
      if (name.toUpperCase().equals(p.getName())) {
        return p;
      }
    }

    return null;
  }

  private void replaceProperty(final Property val) {
    if (vcard == null) {
      // Might happen during read from db
      return;
    }

    List<Property> ps = vcard.getProperties();

    Property p = vcard.getProperty(val.getId());

    if (p != null) {
      ps.remove(p);
    }

    ps.add(val);

    String name = val.getId().toString();
    DbCardProperty prop = findDbProperty(name);

    if ((prop != null) && (getProperties() != null)) {
      getProperties().remove(prop);
    }

    addDbProperty(makeDbProperty(val));
  }

  private void addDbProperty(final DbCardProperty val) {
    if (getProperties() == null) {
      setProperties(new ArrayList<DbCardProperty>());
    }

    val.setCard(this);
    getProperties().add(val);
  }

  private DbCardProperty makeDbProperty(final Property val) {
    String name = val.getId().toString().toUpperCase();
    String value = val.getValue();

    DbCardProperty dbp = new DbCardProperty(name, value/*, this*/);

    for (Parameter par: val.getParameters()) {
      dbp.addParam(new DbCardParam(par.getId().toString(), par.getValue(), dbp));
    }

    dbp.setCard(this);
    return dbp;
  }
}
