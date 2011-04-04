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
   * @throws WebdavException
   */
  public CarddavPrincipalNode(final CarddavURI cdURI, final SysIntf sysi,
                             final AccessPrincipal ap) throws WebdavException {
    super(sysi.getUrlHandler(), cdURI.getPath(),
          ap,
          cdURI.isCollection(), cdURI.getUri());
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
