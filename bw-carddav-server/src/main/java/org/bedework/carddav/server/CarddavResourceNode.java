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
import org.bedework.util.timezones.DateTimeUtil;
import org.bedework.util.xml.XmlEmit;
import org.bedework.webdav.servlet.shared.WebdavException;

import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Writer;

import javax.xml.namespace.QName;

/** Class to represent a resource such as a file.
 *
 *   @author Mike Douglass   douglm rpi.edu
 */
public class CarddavResourceNode extends CarddavNode {
  private CarddavResource resource;

  private AccessPrincipal owner;

  private String entityName;

  private CarddavCollection col;

  private CurrentAccess currentAccess;

  /** Place holder for status
   *
   * @param sysi
   * @param status
   * @param uri
   */
  public CarddavResourceNode(final SysIntf sysi,
                             final int status,
                             final String uri) {
    super(true, sysi, uri);
    setStatus(status);
    this.uri = uri;
  }

  /** Constructor
   *
   * @param cdURI
   * @param sysi
   */
  public CarddavResourceNode(final CarddavURI cdURI,
                             final SysIntf sysi) {
    super(cdURI, sysi);

    resource = cdURI.getResource();
    col = cdURI.getCol();
    collection = false;
    allowsGet = true;
    entityName = cdURI.getEntityName();
    exists = cdURI.exists;

    if (resource != null) {
      resource.setPrevLastmod(resource.getLastmod());
      resource.setPrevSeq(resource.getPrevSeq());
    }
  }

  @Override
  public void init(final boolean content) {
    if (!content) {
      return;
    }

    if ((resource == null) && exists) {
      if (entityName == null) {
        exists = false;
      }
    }
  }

  @Override
  public CarddavCollection getWdCollection() {
    return col;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getOwner()
   */
  @Override
  public AccessPrincipal getOwner() {
    if (owner == null) {
      if (resource == null) {
        return null;
      }

      owner = resource.getOwner();
    }

    return owner;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#removeProperty(org.w3c.dom.Element)
   */
  @Override
  public boolean removeProperty(final Element val,
                                final SetPropertyResult spr) {
    warn("Unimplemented - removeProperty");

    return false;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#setProperty(org.w3c.dom.Element)
   */
  @Override
  public boolean setProperty(final Element val,
                             final SetPropertyResult spr) {
    if (super.setProperty(val, spr)) {
      return true;
    }

    return false;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#update()
   */
  @Override
  public void update() {
    if (resource != null) {
      getSysi().updateFile(resource, true);
    }
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
    // Not ours
    return super.knownProperty(tag);
  }

  /**
   * @param val
   */
  public void setResource(final CarddavResource val) {
    resource = val;
  }

  /** Returns the resource object
   *
   * @return BwResource
   */
  public CarddavResource getResource() {
    init(true);

    return resource;
  }

  /* ====================================================================
   *                   Overridden property methods
   * ==================================================================== */

  @Override
  public CurrentAccess getCurrentAccess() {
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

  @Override
  public String getEtagValue(final boolean strong) {
    init(true);

    if (resource == null) {
      return null;
    }

    return makeEtag(resource.getLastmod(), 0, strong);
  }

  /**
   * @param strong
   * @return etag before changes
   */
  public String getPrevEtagValue(final boolean strong) {
    init(true);

    if (resource == null) {
      return null;
    }

    return makeEtag(resource.getPrevLastmod(), resource.getPrevSeq(), strong);
  }

  private String makeEtag(final String lastmod, final int seq, final boolean strong) {
    StringBuilder val = new StringBuilder();
    if (!strong) {
      val.append("W");
    }

    val.append("\"");
    val.append(lastmod);
    val.append("-");
    val.append(seq);
    val.append("\"");

    return val.toString();
  }

  @Override
  public String toString() {
    StringBuffer sb = new StringBuffer();

    sb.append("CaldavResourceNode{");
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

  @Override
  public String writeContent(final XmlEmit xml,
                             final Writer wtr,
                             final String contentType) {
    return null;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentBinary()
   */
  @Override
  public boolean getContentBinary() {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentString()
   */
  @Override
  public String getContentString(String contentType) {
    init(true);
    throw new WebdavException("binary content");
  }

  /** Return binary content
  *
  * @return InputStream       content.
  * @throws WebdavException
  */
  public InputStream getBinaryContent() {
    init(true);

    if ((resource == null) || (resource.getContent() == null)) {
      return null;
    }

    return new ByteArrayInputStream(resource.getContent().getValue());
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentLang()
   */
  @Override
  public String getContentLang() {
    return "en";
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentLen()
   */
  @Override
  public long getContentLen() {
    init(true);

    if (resource == null) {
      return 0;
    }

    return resource.getContentLength();
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getContentType()
   */
  @Override
  public String getContentType() {
    if (resource == null) {
      return null;
    }

    return resource.getContentType();
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getCreDate()
   */
  @Override
  public String getCreDate() {
    init(false);

    if (resource == null) {
      return null;
    }

    return resource.getCreated();
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getDisplayname()
   */
  @Override
  public String getDisplayname() {
    return getEntityName();
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsNode#getLastmodDate()
   */
  @Override
  public String getLastmodDate() {
    init(false);

    if (resource == null) {
      return null;
    }

    try {
      return DateTimeUtil.fromISODateTimeUTCtoRfc822(resource.getLastmod());
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }
}
