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

import org.bedework.carddav.common.vcard.Card;
import org.bedework.base.ToString;
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
   */
  public DbCard() {
  }

  /** Create DbCard with new card
   *
   * @param fn full name
   */
  public DbCard(final String fn) {
    vcard = new VCard();
    vcard.getProperties().add(Version.VERSION_4_0);

    setFn(fn);
    setUid(org.bedework.util.misc.Uid.getUid());
  }

  /** Create DbCard with supplied vcard
   *
   * @param vcard the card
   */
  public DbCard(final VCard vcard) {
    this.vcard = vcard;

    initFromVcard();
  }

  /** Set the fn
   *
   * @param val    String fn
   */
  public void setFn(final String val) {
    fn = val;
  }

  /** Get the name
   *
   * @return String   name
   */
  public String getFn() {
    return fn;
  }

  /**
   * @param val uid
   */
  public void setUid(final String val) {
    uid = val;
  }

  /**
   * @return String
   */
  public String getUid() {
    return uid;
  }

  /**
   * @param val kind
   */
  public void setKind(final String val) {
    kind = val;
  }

  /**
   * @return String
   */
  public String getKind() {
    return kind;
  }

  /**
   * @param val list of properties
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
   * @param val lastmod
   */
  public void setLastmod(final String val) {
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
   * @param val a property
   */
  public void addProperty(final Property val) {
    vcard.getProperties().add(val);
  }

  /**
   * @param id of property
   * @return property or null
   */
  public Property findProperty(final Id id) {
    return vcard.getProperty(id);
  }

  /**
   * @param name of property
   * @return property or null
   */
  public Property findProperty(final String name) {
    Property.Id id = null;

    for (final Property.Id i: Property.Id.values()) {
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
   * @param name of property
   * @return property or null
   */
  public List<Property> findProperties(final String name) {
    Property.Id id = null;

    for (final Property.Id i: Property.Id.values()) {
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
   * @param val the card
   */
  public void setVcard(final VCard val) {
    vcard = val;
    if (val == null) {
      vcard = new VCard();
    }
    initFromVcard();
  }

  /**
   * @return vcard or null
   */
  public VCard getVcard() {
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
   * @param rdr card reader
   */
  public void parse(final Reader rdr) {
    try {
      vcard = new VCardBuilder(rdr).build();
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  /**
   * @return String
   */
  public String output() {
    try {
      replaceProperty(new Revision(new ArrayList<>(), getLastmod()));
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }

    if (strForm != null) {
      return strForm;
    }

    final Card cd = new Card(vcard);
    strForm = cd.outputVcard(null);

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
   */
  public void setDtstamps() {
    final DateTime dt = new DateTime(true);
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
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public int hashCode() {
    return getPath().hashCode();
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }

  /* ==============================================================
   *                   Private methods
   * ============================================================== */

  private void initFromVcard() {
    // XXX Do this the inefficient way for the moment
    if (getProperties() != null) {
      getProperties().clear();
    }

    setFn(findProperty(Property.Id.FN).getValue());

    final Property k = findProperty(Property.Id.KIND);

    if (k != null) {
      setKind(k.getValue());
    } else {
      setKind(Kind.INDIVIDUAL.getValue());
    }

    final Uid uid = (Uid)findProperty(Property.Id.UID);

    if (uid == null) {
      setUid(org.bedework.util.misc.Uid.getUid());
    } else {
      setUid(uid.getValue());
    }

    for (final Property p: vcard.getProperties()) {
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
   * @param name of db property
   * @return property or null
   */
  private DbCardProperty findDbProperty(final String name) {
    if (getProperties() == null) {
      return null;
    }

    for (final DbCardProperty p: getProperties()) {
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

    final List<Property> ps = vcard.getProperties();

    final Property p = vcard.getProperty(val.getId());

    if (p != null) {
      ps.remove(p);
    }

    ps.add(val);

    final String name = val.getId().toString();
    final DbCardProperty prop = findDbProperty(name);

    if ((prop != null) && (getProperties() != null)) {
      getProperties().remove(prop);
    }

    addDbProperty(makeDbProperty(val));
  }

  private void addDbProperty(final DbCardProperty val) {
    if (getProperties() == null) {
      setProperties(new ArrayList<>());
    }

    val.setCard(this);
    getProperties().add(val);
  }

  private DbCardProperty makeDbProperty(final Property val) {
    final String name = val.getId().toString().toUpperCase();
    final String value = val.getValue();

    final DbCardProperty dbp = new DbCardProperty(name, value/*, this*/);

    for (final Parameter par: val.getParameters()) {
      dbp.addParam(new DbCardParam(par.getId().toString(), par.getValue(), dbp));
    }

    dbp.setCard(this);
    return dbp;
  }
}
