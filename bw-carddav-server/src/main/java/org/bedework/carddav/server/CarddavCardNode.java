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
package org.bedework.carddav.server;

import org.bedework.access.AccessPrincipal;
import org.bedework.access.CurrentAccess;
import org.bedework.access.PrivilegeDefs;
import org.bedework.carddav.common.CarddavCollection;
import org.bedework.carddav.common.vcard.Card;
import org.bedework.util.misc.ToString;
import org.bedework.util.xml.XmlEmit;
import org.bedework.util.xml.tagdefs.CarddavTags;
import org.bedework.webdav.servlet.shared.WebdavException;
import org.bedework.webdav.servlet.shared.WebdavNsIntf;

import org.w3c.dom.Element;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

/** Class to represent an entity such as events in caldav.
 *
 *   @author Mike Douglass   douglm  rpi.edu
 */
public class CarddavCardNode extends CarddavNode {
  private Card card;

  private String vcardVersion;

  private String entityName;

  private CarddavCollection col;

  private final static HashMap<QName, PropertyTagEntry> propertyNames =
          new HashMap<>();

  static {
    addPropEntry(propertyNames, CarddavTags.addressData);

  }

  /** Place holder for status
   *
   * @param sysi system interface
   * @param status from exception
   * @param uri of resource
   */
  public CarddavCardNode(final SysIntf sysi,
                         final int status,
                         final String uri) {
    super(true, sysi, uri);
    setStatus(status);
    this.uri = uri;
  }

  /** Constructor
   *
   * @param cdURI referencing resource
   * @param sysi system interface
   */
  public CarddavCardNode(final CarddavURI cdURI,
                         final SysIntf sysi) {
    super(cdURI, sysi);

    col = cdURI.getCol();
    collection = false;
    allowsGet = true;
    entityName = cdURI.getEntityName();

    exists = cdURI.getExists();

    card = cdURI.getEntity();
  }

  @Override
  public void init(final boolean content) {
    //if (!content) {
    //  return;
    //}
  }

  @Override
  public CarddavCollection getWdCollection() {
    return col;
  }

  @Override
  public AccessPrincipal getOwner() {
    if (col == null) {
      return null;
    }

    if (col.getOwner() != null) {
      return col.getOwner();
    }

    return null;
  }

  /**
   * @param val string version
   */
  public void setVcardVersion(final String val) {
    vcardVersion = val;
  }

  @Override
  public boolean removeProperty(final Element val,
                                final SetPropertyResult spr) {
    warn("Unimplemented - removeProperty");

    return false;
  }

  @Override
  public boolean setProperty(final Element val,
                             final SetPropertyResult spr) {
    if (super.setProperty(val, spr)) {
      return true;
    }

    return false;
  }

  @Override
  public void update() {
    getSysi().updateCard(col.getPath(), card);
  }

  /**
   * @return String
   */
  public String getEntityName() {
    return entityName;
  }

  @Override
  public boolean trailSlash() {
    return false;
  }

  /* ====================================================================
   *                   Property methods
   * ==================================================================== */

  @Override
  public boolean knownProperty(final QName tag) {
    if (propertyNames.get(tag) != null) {
      return true;
    }

    // Not ours
    return super.knownProperty(tag);
  }

@Override
public boolean generatePropertyValue(final QName tag,
                                      final WebdavNsIntf intf,
                                      final boolean allProp) {
    //PropVal pv = new PropVal();
    //XmlEmit xml = intf.getXmlEmit();

    if (propertyNames.get(tag) == null) {
      // Not ours
      return super.generatePropertyValue(tag, intf, allProp);
    }

    try {

      return false;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public Collection<PropertyTagEntry> getPropertyNames() {
    final Collection<PropertyTagEntry> res = new ArrayList<>();

    res.addAll(super.getPropertyNames());
    res.addAll(propertyNames.values());

    return res;
  }

  /* UNUSED
  @Override
  public Collection<WebdavProperty> getProperties(final String ns) {
    init(true);
    ArrayList<WebdavProperty> al = new ArrayList<WebdavProperty>();

    /* Default property calendar-data returns all of the object * /
    al.add(new AddressData(CarddavTags.addressData, debug));

    return al;
  }
  */

  @Override
  public String getContentString(final String contentType) {
    return card.outputVcard(vcardVersion);
  }

  /* ====================================================================
   *                   Overridden property methods
   * ==================================================================== */

  @Override
  public String writeContent(final XmlEmit xml,
                             final Writer wtr,
                             final String contentType) {
    try {
      if ("application/vcard+json".equals(contentType)) {
        if (xml == null) {
          wtr.write(card.outputJson(debug(), vcardVersion));
        } else {
          xml.cdataValue(card.outputJson(debug(), vcardVersion));
        }
        return contentType;
      }

      if (xml == null) {
        wtr.write(card.outputVcard(vcardVersion));
        return "text/vcard";
      }

      xml.cdataValue(card.outputVcard(vcardVersion));
      return "application/vcard+xml";
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public CurrentAccess getCurrentAccess() {
    if (col == null) {
      return null;
    }

    return getSysi().checkAccess(col, PrivilegeDefs.privAny, true);
  }

  @Override
  public String getEtagValue(final boolean strong) {
    init(true);

    if (card == null) {
      return null;
    }

    return makeEtag(card.getLastmod());
  }

  /**
   * @param strong true for strong etag
   * @return etag before changes
   */
  public String getPrevEtagValue(final boolean strong) {
    init(true);

    if (card == null) {
      return null;
    }

    return makeEtag(card.getPrevLastmod());
  }

  private String makeEtag(final String lastmod) {
    return new StringBuilder()
            .append("\"")
            .append(lastmod)
            .append("\"")
            .toString();
  }

  @Override
  public String toString() {
    return new ToString(this)
            .append("path", getPath())
            .append("entityName", String.valueOf(entityName))
            .toString();
  }

  /* ==============================================================
   *                   Required webdav properties
   * ============================================================== */

  @Override
  public String getContentLang() {
    return "en";
  }

  @Override
  public long getContentLen() {
    if (card != null) {
      return card.outputVcard(vcardVersion).length();
    }

    return 0;
  }

  @Override
  public String getContentType() {
    return "text/vcard; version=\"4.0\"; charset=UTF-8";
  }

  @Override
  public String getCreDate() {
    init(false);

    if (card == null) {
      return null;
    }

    //return card.getCreated();
    return null;
  }

  @Override
  public String getDisplayname() {
    return getEntityName();
  }

  @Override
  public String getLastmodDate() {
    init(false);

    if (card == null) {
      return null;
    }

    try {
      // return DateTimeUtil.fromISODateTimeUTCtoRfc822(card.getLastmod());
      return card.getLastmod();
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  /**
   * @param val Card
   */
  public void setCard(final Card val) {
    card = val;
  }

  /**
   * @return VCard
   */
  public Card getCard() {
    return card;
  }

  /* ==============================================================
   *                   Private methods
   * ============================================================== */

  //private void addProp(Collection<WebdavProperty> c, QName tag, Object val) {
  //  if (val != null) {
  //    c.add(new WebdavProperty(tag, String.valueOf(val)));
  //  }
  //}
}
