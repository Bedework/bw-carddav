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
import org.bedework.access.Acl.CurrentAccess;
import org.bedework.access.PrivilegeDefs;
import org.bedework.carddav.server.CarddavBWIntf.QueryResult;
import org.bedework.carddav.server.SysIntf.GetLimits;
import org.bedework.carddav.server.SysIntf.GetResult;
import org.bedework.carddav.vcard.Card;
import org.bedework.util.xml.XmlEmit;
import org.bedework.util.xml.XmlUtil;
import org.bedework.util.xml.tagdefs.AppleServerTags;
import org.bedework.util.xml.tagdefs.CarddavTags;
import org.bedework.util.xml.tagdefs.WebdavTags;

import edu.rpi.cct.webdav.servlet.shared.WdCollection;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;

import org.w3c.dom.Element;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

/** Class to represent a collection in carddav.
 *
 *   @author Mike Douglass   douglm@bedework.edu
 */
public class CarddavColNode extends CarddavNode {
  private AccessPrincipal owner;

  private CurrentAccess currentAccess;

  private final static HashMap<QName, PropertyTagEntry> propertyNames =
    new HashMap<QName, PropertyTagEntry>();

  static {
    addPropEntry(propertyNames, CarddavTags.addressbookDescription);
    addPropEntry(propertyNames, CarddavTags.maxResourceSize);
    addPropEntry(propertyNames, CarddavTags.supportedAddressData);
    addPropEntry(propertyNames, AppleServerTags.getctag);
  }

  /** Place holder for status
   *
   * @param sysi
   * @param status
   * @param uri
   */
  public CarddavColNode(final SysIntf sysi,
                        final int status,
                        final String uri) {
    super(true, sysi, uri);
    setStatus(status);
    this.uri = uri;
  }

  /**
   * @param cdURI
   * @param sysi
   * @throws WebdavException
   */
  public CarddavColNode(final CarddavURI cdURI,
                        final SysIntf sysi) throws WebdavException {
    super(cdURI, sysi);

    col = cdURI.getCol();
    collection = true;
    allowsGet = false;

    exists = cdURI.getExists();
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.CarddavNode#getWdCollection()
   */
  @Override
  public CarddavCollection getWdCollection() {
    return col;
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getOwner()
   */
  @Override
  public AccessPrincipal getOwner() throws WebdavException {
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
  public void init(final boolean content) throws WebdavException {
    if (!content) {
      return;
    }
  }

  @Override
  public String getEtagValue(final boolean strong) throws WebdavException {
    WdCollection c = getWdCollection(); // Unalias

    if (c == null) {
      return null;
    }

    String val = c.getLastmod();

    if (strong) {
      return "\"" + val + "\"";
    }

    return "W/\"" + val + "\"";
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#setDefaults(javax.xml.namespace.QName)
   */
  @Override
  public void setDefaults(final QName methodTag) throws WebdavException {
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.CarddavNode#getChildren(org.bedework.carddav.server.SysIntf.GetLimits)
   */
  @Override
  public QueryResult getChildren(final GetLimits limits) throws WebdavException {
    /* For the moment we're going to do this the inefficient way.
       We really need to have calendar defs that can be expressed as a search
       allowing us to retrieve all the ids of objects within a calendar.
       */

    try {
      QueryResult res = new QueryResult();

      CarddavCollection c = getWdCollection(); // Unalias

      GetResult gr = getSysi().getCollections(c, limits);

      if (gr.collections != null) {
        for (CarddavCollection wdc: gr.collections) {
          res.nodes.add(new CarddavColNode(new CarddavURI(wdc, true),
                                           getSysi()));
        }
      }

      if (!c.getAddressBook()) {
        return res;
      }

      gr = getSysi().getCards(c, null, limits);

      if (gr.cards != null) {
        for (Card card: gr.cards) {
          res.nodes.add(new CarddavCardNode(new CarddavURI(c, card,
                                                           card.getName(),
                                                           true),
                                            getSysi()));
        }
      }

      return res;
    } catch (WebdavException we) {
      throw we;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getContentString()
   */
  @Override
  public String getContentString() throws WebdavException {
    return null;
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#update()
   */
  @Override
  public void update() throws WebdavException {
    // ALIAS probably not unaliasing here
    if (col != null) {
      getSysi().updateCollection(col);
    }
  }

  /* ====================================================================
   *                   Required webdav properties
   * ==================================================================== */

  @Override
  public String writeContent(final XmlEmit xml,
                             final Writer wtr,
                             final String contentType) throws WebdavException {
    return null;
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getContentLang()
   */
  @Override
  public String getContentLang() throws WebdavException {
    return "en";
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getContentLen()
   */
  @Override
  public long getContentLen() throws WebdavException {
    return 0;
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getContentType()
   */
  @Override
  public String getContentType() throws WebdavException {
    return null;
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getCreDate()
   */
  @Override
  public String getCreDate() throws WebdavException {
    return col.getCreated();
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getDisplayname()
   */
  @Override
  public String getDisplayname() throws WebdavException {
    if (col == null) {
      return null;
    }

    return col.getName();
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#getLastmodDate()
   */
  @Override
  public String getLastmodDate() throws WebdavException {
    init(false);
    if (col == null) {
      return null;
    }

    try {
      //return DateTimeUtil.fromISODateTimeUTCtoRfc822(col.getLastmod());
      return col.getLastmod();
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* ====================================================================
   *                   Abstract methods
   * ==================================================================== */

  @Override
  public CurrentAccess getCurrentAccess() throws WebdavException {
    if (currentAccess != null) {
      return currentAccess;
    }

    try {
      currentAccess = getSysi().checkAccess(col, PrivilegeDefs.privAny, true);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }

    return currentAccess;
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#trailSlash()
   */
  @Override
  public boolean trailSlash() {
    return true;
  }

  /* ====================================================================
   *                   Property methods
   * ==================================================================== */

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#removeProperty(org.w3c.dom.Element)
   */
  @Override
  public boolean removeProperty(final Element val,
                                final SetPropertyResult spr) throws WebdavException {
    warn("Unimplemented - removeProperty");

    return false;
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#setProperty(org.w3c.dom.Element)
   */
  @Override
  public boolean setProperty(final Element val,
                             final SetPropertyResult spr) throws WebdavException {
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
        Collection<Element> propVals = XmlUtil.getElements(val);

        for (Element pval: propVals) {
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
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#knownProperty(edu.bedework.sss.util.xml.QName)
   */
  @Override
  public boolean knownProperty(final QName tag) {
    if (propertyNames.get(tag) != null) {
      return true;
    }

    // Not ours
    return super.knownProperty(tag);
  }

  /* (non-Javadoc)
   * @see edu.bedework.cct.webdav.servlet.shared.WebdavNsNode#generatePropertyValue(edu.bedework.sss.util.xml.QName, edu.bedework.cct.webdav.servlet.shared.WebdavNsIntf, boolean)
   */
  @Override
  public boolean generatePropertyValue(final QName tag,
                                       final WebdavNsIntf intf,
                                       final boolean allProp) throws WebdavException {
    XmlEmit xml = intf.getXmlEmit();

    try {
      if (tag.equals(WebdavTags.resourcetype)) {
        // dav 13.9
        xml.openTag(WebdavTags.resourcetype);
        xml.emptyTag(WebdavTags.collection);
        if (debug) {
          debugMsg("generatePropResourcetype for " + col);
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
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /** Return a set of PropertyTagEntry defining properties this node supports.
   *
   * @return Collection of PropertyTagEntry
   * @throws WebdavException
   */
  @Override
  public Collection<PropertyTagEntry> getPropertyNames()throws WebdavException {
    Collection<PropertyTagEntry> res = new ArrayList<PropertyTagEntry>();

    res.addAll(super.getPropertyNames());
    res.addAll(propertyNames.values());

    return res;
  }

  /** Return a set of Qname defining reports this node supports.
   *
   * @return Collection of QName
   * @throws WebdavException
   */
  @Override
  public Collection<QName> getSupportedReports() throws WebdavException {
    Collection<QName> res = new ArrayList<QName>();

    res.addAll(super.getSupportedReports());

    return res;
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("CarddavColNode{cduri=");
    sb.append("path=");
    sb.append(getPath());
    sb.append("}");

    return sb.toString();
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private boolean checkCalForSetProp(final SetPropertyResult spr) {
    if (col != null) {
      return true;
    }

    spr.status = HttpServletResponse.SC_NOT_FOUND;
    spr.message = "Not found";
    return false;
  }
}
