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

import org.bedework.carddav.server.CarddavBWIntf.QueryResult;
import org.bedework.carddav.server.SysIntf.GetLimits;

import edu.rpi.cct.webdav.servlet.shared.WdCollection;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode;
import edu.rpi.sss.util.xml.XmlEmit;
import edu.rpi.sss.util.xml.tagdefs.CarddavTags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.xml.namespace.QName;

/** Class to represent a caldav node.
 *
 *   @author Mike Douglass   douglm - rpi.edu
 */
public abstract class CarddavNode extends WebdavNsNode {
  protected CarddavCollection col;

  private final static HashMap<QName, PropertyTagEntry> propertyNames =
    new HashMap<QName, PropertyTagEntry>();

  private final static Collection<QName> supportedReports = new ArrayList<QName>();

  static {
    addPropEntry(propertyNames, CarddavTags.addressbookHomeSet);

    supportedReports.add(CarddavTags.addressbookMultiget); // Calendar access
    supportedReports.add(CarddavTags.addressbookQuery);    // Calendar access
  }

  /* for accessing calendars */
  private SysIntf sysi;

  CarddavNode(CarddavURI cdURI, SysIntf sysi,
              boolean debug) throws WebdavException {
    super(sysi.getUrlHandler(), cdURI.getPath(), cdURI.isCollection(),
          cdURI.getUri(), debug);

    //this.cdURI = cdURI;
    this.sysi = sysi;

    if (cdURI != null) {
      uri = cdURI.getUri();
    }
  }

  CarddavNode(boolean collection, SysIntf sysi, String uri,boolean debug) {
    super(sysi.getUrlHandler(), null, collection, uri, debug);

    //this.cdURI = cdURI;
    this.sysi = sysi;
  }

  /* ====================================================================
   *                         Public methods
   * ==================================================================== */

  public WdCollection getCollection(boolean deref) throws WebdavException {
    return col;
  }

  /**
   * @return WdCollection containing or represented by this entity
   * @throws WebdavException
   */
  public abstract CarddavCollection getWdCollection() throws WebdavException ;

  /** Return a collection of children objects.
   *
   * <p>Default is to return null
   *
   * @param limits
   * @return Collection
   * @throws WebdavException
   */
  public QueryResult getChildren(GetLimits limits) throws WebdavException {
    return null;
  }

  /**
   * @return CalSvcI
   */
  public SysIntf getSysi() {
    return sysi;
  }

  /** Return a set of Qname defining reports this node supports.
   *
   * @return Collection of QName
   * @throws WebdavException
   */
  public Collection<QName> getSupportedReports() throws WebdavException {
    Collection<QName> res = new ArrayList<QName>();
    res.addAll(super.getSupportedReports());
    res.addAll(supportedReports);

    return res;
  }

  /* ====================================================================
   *                   Required webdav properties
   * ==================================================================== */

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentBinary()
   */
  public boolean getContentBinary() throws WebdavException {
    return false;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getChildren()
   */
  public Collection getChildren() throws WebdavException {
    return null;
  }

  /* ====================================================================
   *                   Property methods
   * ==================================================================== */

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#knownProperty(edu.rpi.sss.util.xml.QName)
   */
  public boolean knownProperty(QName tag) {
    if (propertyNames.get(tag) != null) {
      return true;
    }

    // Not ours
    return super.knownProperty(tag);
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#generatePropertyValue(edu.rpi.sss.util.xml.QName, edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf, boolean)
   */
  public boolean generatePropertyValue(QName tag,
                                       WebdavNsIntf intf,
                                       boolean allProp) throws WebdavException {
    XmlEmit xml = intf.getXmlEmit();

    try {
      // Not known - try higher
      return super.generatePropertyValue(tag, intf, allProp);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  public String toString() {
    StringBuffer sb = new StringBuffer(this.getClass().getName());

    sb.append("{");
    sb.append("path=");
    sb.append(getPath());
    sb.append("}");

    return sb.toString();
  }
}
