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

import org.bedework.carddav.common.CarddavCollection;
import org.bedework.carddav.common.GetLimits;
import org.bedework.carddav.server.CarddavBWIntf.QueryResult;
import org.bedework.util.misc.ToString;
import org.bedework.util.xml.tagdefs.CarddavTags;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WdEntity;
import org.bedework.webdav.servlet.shared.WebdavException;
import org.bedework.webdav.servlet.shared.WebdavNsIntf;
import org.bedework.webdav.servlet.shared.WebdavNsNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.function.Supplier;

import javax.xml.namespace.QName;

/** Class to represent a caldav node.
 *
 *   @author Mike Douglass   douglm - rpi.edu
 */
public abstract class CarddavNode extends WebdavNsNode {
  protected CarddavCollection col;

  private final static HashMap<QName, PropertyTagEntry> propertyNames =
    new HashMap<>();

  private final static Collection<QName> supportedReports = new ArrayList<>();

  static {
    addPropEntry(propertyNames, CarddavTags.addressbookHomeSet);

    supportedReports.add(CarddavTags.addressbookMultiget); // Calendar access
    supportedReports.add(CarddavTags.addressbookQuery);    // Calendar access
  }

  /* for accessing calendars */
  private SysIntf sysi;

  CarddavNode(final CarddavURI cdURI,
              final SysIntf sysi) throws WebdavException {
    super(sysi, sysi.getUrlHandler(), cdURI.getPath(),
          cdURI.isCollection(),
          cdURI.getUri());

    //this.cdURI = cdURI;
    this.sysi = sysi;

    if (cdURI != null) {
      uri = cdURI.getUri();
    }
  }

  CarddavNode(final boolean collection,
              final SysIntf sysi,
              final String uri) {
    super(sysi, sysi.getUrlHandler(), null, collection, uri);

    //this.cdURI = cdURI;
    this.sysi = sysi;
  }

  /* ====================================================================
   *                         Public methods
   * ==================================================================== */

  @Override
  public WdCollection getCollection(final boolean deref) throws WebdavException {
    return col;
  }

  @Override
  public WdCollection getImmediateTargetCollection() throws WebdavException {
    return col.resolveAlias(false); // False => don't resolve all subaliases
  }

  /**
   * @return WdCollection containing or represented by this entity
   * @throws WebdavException on fatal error
   */
  public abstract CarddavCollection getWdCollection() throws WebdavException ;

  /** Return a collection of children objects.
   *
   * <p>Default is to return null
   *
   * @param limits to limit fetch
   * @return Collection
   * @throws WebdavException on fatal error
   */
  public QueryResult getChildren(final GetLimits limits) throws WebdavException {
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
   * @throws WebdavException on fatal error
   */
  @Override
  public Collection<QName> getSupportedReports() throws WebdavException {
    Collection<QName> res = new ArrayList<>();
    res.addAll(super.getSupportedReports());
    res.addAll(supportedReports);

    return res;
  }

  @Override
  public boolean allowsSyncReport() throws WebdavException {
    return false;
  }

  @Override
  public boolean getDeleted() throws WebdavException {
    return false;
  }

  @Override
  public String getSyncToken() throws WebdavException {
    return null;
  }

  /* ====================================================================
   *                   Required webdav properties
   * ==================================================================== */

  @Override
  public boolean getContentBinary() throws WebdavException {
    return false;
  }

  @Override
  public Collection<? extends WdEntity> getChildren(
          final Supplier<Object> filterGetter) throws WebdavException {
    return null;
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
                                       final boolean allProp) throws WebdavException {
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

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    ts.append("path", getPath());

    return ts.toString();
  }
}
