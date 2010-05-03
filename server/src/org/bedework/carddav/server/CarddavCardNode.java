/* **********************************************************************
    Copyright 2008 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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

package org.bedework.carddav.server;

import org.bedework.carddav.vcard.Card;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.PrivilegeDefs;
import edu.rpi.cmt.access.Acl.CurrentAccess;
import edu.rpi.sss.util.xml.tagdefs.CarddavTags;

import org.w3c.dom.Element;

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

  private String entityName;

  private CarddavCollection col;

  private final static HashMap<QName, PropertyTagEntry> propertyNames =
    new HashMap<QName, PropertyTagEntry>();

  static {
    addPropEntry(propertyNames, CarddavTags.addressData);

  }

  /** Place holder for status
   *
   * @param sysi
   * @param status
   * @param uri
   * @param debug
   */
  public CarddavCardNode(final SysIntf sysi, final int status, final String uri, final boolean debug) {
    super(true, sysi, uri, debug);
    setStatus(status);
    this.uri = uri;
  }

  /** Constructor
   *
   * @param cdURI
   * @param sysi
   * @param debug
   * @throws WebdavException
   */
  public CarddavCardNode(final CarddavURI cdURI,
                         final SysIntf sysi, final boolean debug) throws WebdavException {
    super(cdURI, sysi, debug);

    col = cdURI.getCol();
    collection = false;
    allowsGet = true;
    entityName = cdURI.getEntityName();

    exists = cdURI.getExists();

    card = cdURI.getEntity();
  }

  @Override
  public void init(final boolean content) throws WebdavException {
    if (!content) {
      return;
    }

  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.CarddavNode#getWdCollection()
   */
  @Override
  public CarddavCollection getWdCollection() throws WebdavException {
    return col;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getOwner()
   */
  @Override
  public AccessPrincipal getOwner() throws WebdavException {
    if (col == null) {
      return null;
    }

    if (col.getOwner() != null) {
      return col.getOwner();
    }

    return null;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#removeProperty(org.w3c.dom.Element)
   */
  @Override
  public boolean removeProperty(final Element val,
                                final SetPropertyResult spr) throws WebdavException {
    warn("Unimplemented - removeProperty");

    return false;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#setProperty(org.w3c.dom.Element)
   */
  @Override
  public boolean setProperty(final Element val,
                             final SetPropertyResult spr) throws WebdavException {
    if (super.setProperty(val, spr)) {
      return true;
    }

    return false;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#update()
   */
  @Override
  public void update() throws WebdavException {
    getSysi().updateCard(col.getPath(), card);
  }

  /**
   * @return String
   */
  public String getEntityName() {
    return entityName;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#trailSlash()
   */
  @Override
  public boolean trailSlash() {
    return false;
  }

  /* ====================================================================
   *                   Property methods
   * ==================================================================== */

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#knownProperty(edu.rpi.sss.util.xml.QName)
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
 * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#generatePropertyValue(edu.rpi.sss.util.xml.QName, edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf, boolean)
 */
@Override
public boolean generatePropertyValue(final QName tag,
                                      final WebdavNsIntf intf,
                                      final boolean allProp) throws WebdavException {
    //PropVal pv = new PropVal();
    //XmlEmit xml = intf.getXmlEmit();

    if (propertyNames.get(tag) == null) {
      // Not ours
      return super.generatePropertyValue(tag, intf, allProp);
    }

    try {

      return false;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getPropertyNames()
   */
  @Override
  public Collection<PropertyTagEntry> getPropertyNames() throws WebdavException {
    Collection<PropertyTagEntry> res = new ArrayList<PropertyTagEntry>();

    res.addAll(super.getPropertyNames());
    res.addAll(propertyNames.values());

    return res;
  }

  /* UNUSED
  @Override
  public Collection<WebdavProperty> getProperties(final String ns) throws WebdavException {
    init(true);
    ArrayList<WebdavProperty> al = new ArrayList<WebdavProperty>();

    /* Default property calendar-data returns all of the object * /
    al.add(new AddressData(CarddavTags.addressData, debug));

    return al;
  }
  */

  @Override
  public String getContentString() throws WebdavException {
    return card.output();
  }

  /* ====================================================================
   *                   Overridden property methods
   * ==================================================================== */

  @Override
  public CurrentAccess getCurrentAccess() throws WebdavException {
    if (col == null) {
      return null;
    }

    return getSysi().checkAccess(col, PrivilegeDefs.privAny, true);
  }

  @Override
  public String getEtagValue(final boolean strong) throws WebdavException {
    init(true);

    if (card == null) {
      return null;
    }

    return makeEtag(card.getLastmod());
  }

  /**
   * @param strong
   * @return etag before changes
   * @throws WebdavException
   */
  public String getPrevEtagValue(final boolean strong) throws WebdavException {
    init(true);

    if (card == null) {
      return null;
    }

    return makeEtag(card.getPrevLastmod());
  }

  private String makeEtag(final String lastmod) {
    StringBuilder val = new StringBuilder();
    val.append("\"");
    val.append(lastmod);
    val.append("\"");

    return val.toString();
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("CaldavComponentNode{");
    sb.append("path=");
    sb.append(getPath());
    sb.append(", entityName=");
    sb.append(String.valueOf(entityName));
    sb.append("}");

    return sb.toString();
  }

  /* ====================================================================
   *                   Required webdav properties
   * ==================================================================== */

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentLang()
   */
  @Override
  public String getContentLang() throws WebdavException {
    return "en";
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentLen()
   */
  @Override
  public long getContentLen() throws WebdavException {
    if (card != null) {
      return card.output().length();
    }

    return 0;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentType()
   */
  @Override
  public String getContentType() throws WebdavException {
    return "text/directory; charset=UTF-8";
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getCreDate()
   */
  @Override
  public String getCreDate() throws WebdavException {
    init(false);

    if (card == null) {
      return null;
    }

    //return card.getCreated();
    return null;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getDisplayname()
   */
  @Override
  public String getDisplayname() throws WebdavException {
    return getEntityName();
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getLastmodDate()
   */
  @Override
  public String getLastmodDate() throws WebdavException {
    init(false);

    if (card == null) {
      return null;
    }

    try {
      // return DateTimeUtil.fromISODateTimeUTCtoRfc822(card.getLastmod());
      return card.getLastmod();
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /**
   * @param val
   * @throws WebdavException
   */
  public void setCard(final Card val) throws WebdavException {
    card = val;
  }

  /**
   * @return VCard
   * @throws WebdavException
   */
  public Card getCard() throws WebdavException {
    return card;
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  //private void addProp(Collection<WebdavProperty> c, QName tag, Object val) {
  //  if (val != null) {
  //    c.add(new WebdavProperty(tag, String.valueOf(val)));
  //  }
  //}
}
