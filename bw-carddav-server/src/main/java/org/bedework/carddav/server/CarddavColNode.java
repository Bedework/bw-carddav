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
import org.bedework.carddav.common.GetLimits;
import org.bedework.carddav.common.GetResult;
import org.bedework.carddav.common.vcard.Card;
import org.bedework.carddav.server.CarddavBWIntf.QueryResult;
import org.bedework.base.ToString;
import org.bedework.util.xml.XmlEmit;
import org.bedework.util.xml.XmlUtil;
import org.bedework.util.xml.tagdefs.AppleServerTags;
import org.bedework.util.xml.tagdefs.CarddavTags;
import org.bedework.util.xml.tagdefs.WebdavTags;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WebdavException;
import org.bedework.webdav.servlet.shared.WebdavNsIntf;

import org.w3c.dom.Element;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import jakarta.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

/** Class to represent a collection in carddav.
 *
 *   @author Mike Douglass   douglm@rpi.edu
 */
public class CarddavColNode extends CarddavNode {
  private AccessPrincipal owner;

  private CurrentAccess currentAccess;

  private final static HashMap<QName, PropertyTagEntry> propertyNames =
    new HashMap<>();

  static {
    addPropEntry(propertyNames, CarddavTags.addressbookDescription);
    addPropEntry(propertyNames, CarddavTags.maxResourceSize);
    addPropEntry(propertyNames, CarddavTags.supportedAddressData);
    addPropEntry(propertyNames, AppleServerTags.getctag);
  }

  /** Place holder for status
   *
   * @param sysi system interface
   * @param status from exception
   * @param uri of resource
   */
  public CarddavColNode(final SysIntf sysi,
                        final int status,
                        final String uri) {
    super(true, sysi, uri);
    setStatus(status);
    this.uri = uri;
  }

  /**
   * @param cdURI referencing resource
   * @param sysi system interface
   */
  public CarddavColNode(final CarddavURI cdURI,
                        final SysIntf sysi) {
    super(cdURI, sysi);

    col = cdURI.getCol();
    collection = true;
    allowsGet = false;

    exists = cdURI.getExists();
  }

  @Override
  public CarddavCollection getWdCollection() {
    return col;
  }

  @Override
  public AccessPrincipal getOwner() {
    if (owner == null) {
      if (col == null) {
        return null;
      }

      owner = col.getOwner();
    }

    if (owner != null) {
      return owner;
    }

    return null;
  }

  @Override
  public void init(final boolean content) {
    //if (!content) {
    //  return;
    //}
  }

  @Override
  public String getEtagValue(final boolean strong) {
    final WdCollection<?> c = getWdCollection(); // Unalias

    if (c == null) {
      return null;
    }

    final String val = c.getLastmod();

    if (strong) {
      return "\"" + val + "\"";
    }

    return "W/\"" + val + "\"";
  }

  @Override
  public QueryResult getChildren(final GetLimits limits) {
    /* For the moment we're going to do this the inefficient way.
       We really need to have calendar defs that can be expressed as a search
       allowing us to retrieve all the ids of objects within a calendar.
       */

    try {
      final QueryResult res = new QueryResult();

      final CarddavCollection c = getWdCollection(); // Unalias

      GetResult gr = getSysi().getCollections(c, limits);

      if (gr.collections != null) {
        for (final CarddavCollection wdc: gr.collections) {
          res.nodes.add(new CarddavColNode(new CarddavURI(wdc, true),
                                           getSysi()));
        }
      }

      if (!c.getAddressBook()) {
        return res;
      }

      gr = getSysi().getCards(c, null, limits);

      if (gr.cards != null) {
        for (final Card card: gr.cards) {
          res.nodes.add(new CarddavCardNode(new CarddavURI(c, card,
                                                           card.getName(),
                                                           true),
                                            getSysi()));
        }
      }

      return res;
    } catch (final WebdavException we) {
      throw we;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void update() {
    // ALIAS probably not unaliasing here
    if (col != null) {
      getSysi().updateCollection(col);
    }
  }

  /* ==============================================================
   *                   Required webdav properties
   * ============================================================== */

  @Override
  public String writeContent(final XmlEmit xml,
                             final Writer wtr,
                             final String contentType) {
    return null;
  }

  @Override
  public String getContentLang() {
    return "en";
  }

  @Override
  public long getContentLen() {
    return 0;
  }

  @Override
  public String getContentType() {
    return null;
  }

  @Override
  public String getCreDate() {
    return col.getCreated();
  }

  @Override
  public String getDisplayname() {
    if (col == null) {
      return null;
    }

    return col.getName();
  }

  @Override
  public String getLastmodDate() {
    init(false);
    if (col == null) {
      return null;
    }

    try {
      //return DateTimeUtil.fromISODateTimeUTCtoRfc822(col.getLastmod());
      return col.getLastmod();
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* ==============================================================
   *                   Abstract methods
   * ============================================================== */

  @Override
  public CurrentAccess getCurrentAccess() {
    if (currentAccess != null) {
      return currentAccess;
    }

    try {
      currentAccess = getSysi().checkAccess(col, PrivilegeDefs.privAny, true);
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }

    return currentAccess;
  }

  @Override
  public boolean trailSlash() {
    return true;
  }

  /* ==============================================================
   *                   Property methods
   * ============================================================== */

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

    try {
      if (XmlUtil.nodeMatches(val, WebdavTags.description)) {
        if (checkCalForSetProp(spr)) {
          col.setDescription(XmlUtil.getElementContent(val));
        }
        return true;
      }

      if (XmlUtil.nodeMatches(val, CarddavTags.addressbookDescription)) {
        if (checkCalForSetProp(spr)) {
          col.setDescription(XmlUtil.getElementContent(val));
        }
        return true;
      }

      if (XmlUtil.nodeMatches(val, WebdavTags.displayname)) {
        if (checkCalForSetProp(spr)) {
          col.setDisplayName(XmlUtil.getElementContent(val));
        }
        return true;
      }

      if (XmlUtil.nodeMatches(val, WebdavTags.resourcetype)) {
        final Collection<Element> propVals = XmlUtil.getElements(val);

        for (final Element pval: propVals) {
          if (XmlUtil.nodeMatches(pval, WebdavTags.collection)) {
            // Fine
            continue;
          }

          if (XmlUtil.nodeMatches(pval, CarddavTags.addressbook)) {
            col.setAddressBook(true);
          }
        }

        return true;
      }

      return false;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

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
    final XmlEmit xml = intf.getXmlEmit();

    try {
      if (tag.equals(WebdavTags.resourcetype)) {
        // dav 13.9
        xml.openTag(WebdavTags.resourcetype);
        xml.emptyTag(WebdavTags.collection);
        if (debug()) {
          debug("generatePropResourcetype for " + col);
        }

        if (col.getAddressBook()) {
          xml.emptyTag(CarddavTags.addressbook);
        }
        xml.closeTag(WebdavTags.resourcetype);

        return true;
      }

      if (tag.equals(AppleServerTags.getctag)) {
        xml.property(tag, col.getLastmod());

        return true;
      }

      if (tag.equals(CarddavTags.addressbookDescription)) {
        xml.property(tag, col.getDescription());

        return true;
      }

      if (tag.equals(CarddavTags.supportedAddressData)) {
        /* e.g.
         *        <C:supported-address-data
         *           xmlns:C="urn:ietf:params:xml:ns:carddav">
         *          <C:address-data-type content-type="text/vcard" version="3.0"/>
         *        </C:supported-address-data>
         */
        xml.openTag(tag);
        xml.startTag(CarddavTags.addressDataType);
        xml.attribute("content-type", "text/vcard");
        xml.attribute("version", "4.0");
        xml.endEmptyTag();
        xml.newline();
        xml.closeTag(tag);
        return true;
      }

      // Not known - try higher
      return super.generatePropertyValue(tag, intf, allProp);
    } catch (final WebdavException wde) {
      throw wde;
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

  @Override
  public Collection<QName> getSupportedReports() {
    return new ArrayList<>(super.getSupportedReports());
  }

  /* ==============================================================
   *                   Object methods
   * ============================================================== */

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    ts.append("cduri", getUri());
    ts.append("path", getPath());

    return ts.toString();
  }

  /* ==============================================================
   *                   Private methods
   * ============================================================== */

  private boolean checkCalForSetProp(final SetPropertyResult spr) {
    if (col != null) {
      return true;
    }

    spr.status = HttpServletResponse.SC_NOT_FOUND;
    spr.message = "Not found";
    return false;
  }
}
