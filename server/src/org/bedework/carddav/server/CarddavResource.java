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

import edu.rpi.cct.webdav.servlet.shared.WebdavUserNode;
import edu.rpi.sss.util.DateTimeUtil;
import edu.rpi.sss.util.Util;

import java.io.Serializable;
import java.util.Date;

/** Represent a resource stored within the system, e.g an attachment or an
 * image. The actual content is stored in a BwResourceContent object to allow us
 * to put off retrieval of content - or maybe even store outside of the db.
 *
 *  @author Mike Douglass   douglm - rpi.edu
 */
public class CarddavResource implements Comparable<CarddavResource>, Serializable {
  private WebdavUserNode owner;

  private CarddavCollection parent;

  private String name;

  /** UTC datetime */
  private String created;

  /** UTC datetime */
  private String lastmod;

  /** Ensure uniqueness - lastmod only down to second.
   */
  private int sequence;

  private String contentType;

  private String encoding;  /* Always binary */

  private int contentLength;  /* Always binary */

  /* ====================================================================
   *                  Non-db fields - should be in a wrapper
   * ==================================================================== */

  private String prevLastmod;

  private int prevSeq;

  private CarddavResourceContent content;

  /** Constructor
   *
   */
  public CarddavResource() {
    super();

    Date dt = new Date();
    setLastmod(DateTimeUtil.isoDateTimeUTC(dt));
    setCreated(DateTimeUtil.isoDateTimeUTC(dt));
  }

  /* ====================================================================
   *                      Bean methods
   * ==================================================================== */

  /**
   * @param val
   */
  public void setOwner(WebdavUserNode val) {
    owner = val;
  }

  /**
   * @return WebdavUserNode
   */
  public WebdavUserNode getOwner() {
    return owner;
  }

  /**
   * @param val
   */
  public void setParent(CarddavCollection val) {
    parent = val;
  }

  /**
   * @return CarddavCollection
   */
  public CarddavCollection getParent() {
    return parent;
  }

  /** Set the name
   *
   * @param val    String name
   */
  public void setName(String val) {
    name = val;
  }

  /** Get the name
   *
   * @return String   name
   */
  public String getName() {
    return name;
  }

  /**
   * @param val
   */
  public void setCreated(String val) {
    created = val;
  }

  /**
   * @return String created
   */
  public String getCreated() {
    return created;
  }

  /**
   * @param val
   */
  public void setLastmod(String val) {
    lastmod = val;
  }

  /**
   * @return String lastmod
   */
  public String getLastmod() {
    return lastmod;
  }

  /** Set the sequence
   *
   * @param val    sequence number
   */
  public void setSequence(int val) {
    sequence = val;
  }

  /** Get the sequence
   *
   * @return int    the sequence
   */
  public int getSequence() {
    return sequence;
  }

  /** Set the contentType - may be null for unknown
   *
   *  @param  val   String contentType
   */
  public void setContentType(String val) {
    contentType = val;
  }

  /** Get the valueType
   *
   *  @return String     contentType
   */
  public String getContentType() {
    return contentType;
  }

  /** Set the encoding
   *
   *  @param  val   String encoding
   */
  public void setEncoding(String val) {
    encoding = val;
  }

  /** Get the encoding
   *
   *  @return String     encoding
   */
  public String getEncoding() {
    return encoding;
  }

  /** Set the length
   *
   *  @param  val   int
   */
  public void setContentLength(int val) {
    contentLength = val;
  }

  /** Get the length
   *
   *  @return int     length
   */
  public int getContentLength() {
    return contentLength;
  }

  /* ====================================================================
   *                   Other non-db methods
   * ==================================================================== */

  /** Update last mod fields
   */
  public void updateLastmod() {
    setLastmod(DateTimeUtil.isoDateTimeUTC(new Date()));
    setSequence(getSequence() + 1);
  }

  /** Set the resource's previous lastmod - used to allow if none match
   *
   *  @param val     lastmod
   */
  public void setPrevLastmod(String val) {
    prevLastmod = val;
  }

  /** Get the resource's previous lastmod - used to allow if none match
   *
   * @return the event's lastmod
   */
  public String getPrevLastmod() {
    return prevLastmod;
  }

  /** Set the event's previous seq - used to allow if none match
   *
   *  @param val     sequence number
   */
  public void setPrevSeq(int val) {
    prevSeq = val;
  }

  /** Get the event's previous seq - used to allow if none match
   *
   * @return the event's seq
   */
  public int getPrevSeq() {
    return prevSeq;
  }

  /** Set the content
   *
   *  @param  val   BwResourceContent
   */
  public void setContent(CarddavResourceContent val) {
    content = val;
  }

  /** Get the content
   *
   *  @return BwResourceContent     content
   */
  public CarddavResourceContent getContent() {
    return content;
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  public int hashCode() {
    return getParent().getPath().hashCode() * getName().hashCode();
  }

  public int compareTo(CarddavResource that)  {
    if (this == that) {
      return 0;
    }

    int res = Util.cmpObjval(getParent(), that.getParent());
    if (res != 0) {
      return res;
    }

    return Util.cmpObjval(getName(), that.getName());
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("BwAttachment{");

    sb.append("name=");
    sb.append(getName());
    sb.append(", getContentType=");
    sb.append(getContentType());
    sb.append(", encoding=");
    sb.append(getEncoding());
    sb.append(", length=");
    sb.append(getContentLength());

    sb.append("}");

    return sb.toString();
  }
}

