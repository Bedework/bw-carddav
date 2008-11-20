/* **********************************************************************
    Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
package org.bedework.webdav;

import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.sss.util.DateTimeUtil;
import edu.rpi.sss.util.Util;

import java.util.Date;

/**
 * @author douglm
 *
 */
public class WdCollection implements Comparable<WdCollection> {
  /** The internal name of the collection
   */
  private String name;

  private String displayName;

  /* The path up to and including this object */
  private String path;

  private AccessPrincipal owner;

  /** UTC datetime */
  private String created;

  /** UTC datetime */
  private String lastmod;

  /** Ensure uniqueness - lastmod only down to second.
   */
  private int sequence;

  private String description;

  /** Constructor
   *
   */
  public WdCollection() {
    super();

    Date dt = new Date();
    setLastmod(DateTimeUtil.isoDateTimeUTC(dt));
    setCreated(DateTimeUtil.isoDateTimeUTC(dt));
  }

  /* ====================================================================
   *                      Bean methods
   * ==================================================================== */

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

  /** Set the display name
   *
   * @param val    String display name
   */
  public void setDisplayName(String val) {
    displayName = val;
  }

  /** Get the display name
   *
   * @return String   display name
   */
  public String getDisplayName() {
    return displayName;
  }

  /** Set the path to this collection
   *
   * @param val    String path
   */
  public void setPath(String val) {
    path = val;
  }

  /** Get the path
   *
   * @return String   path
   */
  public String getPath() {
    return path;
  }

  /**
   * @param val
   */
  public void setOwner(AccessPrincipal val) {
    owner = val;
  }

  /**
   * @return AccessPrincipal
   */
  public AccessPrincipal getOwner() {
    return owner;
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

  /** Set the description
   *
   * @param val    String description
   */
  public void setDescription(String val) {
    description = val;
  }

  /** Get the description
   *
   * @return String   description
   */
  public String getDescription() {
    return description;
  }

  public int hashCode() {
    return getPath().hashCode() * getName().hashCode();
  }

  public int compareTo(WdCollection that)  {
    if (this == that) {
      return 0;
    }

    int res = Util.cmpObjval(getPath(), that.getPath());
    if (res != 0) {
      return res;
    }

    return Util.cmpObjval(getName(), that.getName());
  }

}
