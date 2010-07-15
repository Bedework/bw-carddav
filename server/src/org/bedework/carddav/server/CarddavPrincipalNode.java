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

import org.bedework.carddav.server.SysIntf.PrincipalInfo;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
import edu.rpi.cct.webdav.servlet.shared.WebdavPrincipalNode;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.sss.util.xml.XmlEmit;
import edu.rpi.sss.util.xml.tagdefs.CarddavTags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

/** Class to represent a principal in cardav.
 *
 *
 *   @author Mike Douglass   douglm  rpi.edu
 */
public class CarddavPrincipalNode extends WebdavPrincipalNode {

  /* for accessing calendars */
  private SysIntf sysi;

  /* Fetched for principal properties */
  private PrincipalInfo pinfo;

  private final static HashMap<QName, PropertyTagEntry> propertyNames =
    new HashMap<QName, PropertyTagEntry>();

  static {
    addPropEntry(propertyNames, CarddavTags.addressbookHomeSet);
    addPropEntry(propertyNames, CarddavTags.principalAddress);
  }

  /**
   * @param cdURI
   * @param sysi
   * @param ap
   * @param debug
   * @throws WebdavException
   */
  public CarddavPrincipalNode(final CarddavURI cdURI, final SysIntf sysi,
                             final AccessPrincipal ap,
                             final boolean debug) throws WebdavException {
    super(sysi.getUrlHandler(), cdURI.getPath(),
          ap,
          cdURI.isCollection(), cdURI.getUri(), debug);
    this.sysi = sysi;
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
    String ns = tag.getNamespaceURI();
    XmlEmit xml = intf.getXmlEmit();

    /* Deal with webdav properties */
    if (!ns.equals(CarddavTags.namespace)) {
      // Not ours
      return super.generatePropertyValue(tag, intf, allProp);
    }

    try {
      if (tag.equals(CarddavTags.addressbookHomeSet)) {
        String addrPath = getPinfo().defaultAddressbookPath;

        if (addrPath != null) {
          xml.openTag(tag);
          generateHref(xml, addrPath);
          xml.closeTag(tag);

          return true;
        }
      }

      if (tag.equals(CarddavTags.principalAddress)) {
        String cardPath = getPinfo().principalCardPath;
        if (cardPath != null) {
          xml.openTag(tag);
          generateHref(xml, cardPath);
          xml.closeTag(tag);

          return true;
        }
      }

      // Not known - try higher
      return super.generatePropertyValue(tag, intf, allProp);
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

  private PrincipalInfo getPinfo() throws WebdavException {
    if (pinfo != null) {
      return pinfo;
    }

    pinfo = sysi.getPrincipalInfo(getOwner(),
                                  true);

    if (pinfo == null) {
      // Fake one up
      pinfo = new PrincipalInfo(getOwner().getAccount(),
                                null,
                                null,
                                null,
                                null,
                                null);
    }
    return pinfo;
  }
}
