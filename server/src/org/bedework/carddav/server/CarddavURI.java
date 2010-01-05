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

package org.bedework.carddav.server;

import org.bedework.carddav.vcard.Card;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.Ace;

/** We map uris onto an object which may be a calendar or an
 * entity contained within that calendar.
 *
 * <p>The response to webdav actions obviously depends upon the type of
 * referenced entity.
 *
 * <p>The server will have to determine whether a name represents a publicly
 * available calendar or a user and the access to a calendar will, of course,
 * depend upon the authentication state of the user.
 *
 *   @author Mike Douglass   douglm rpi.edu
 */
public class CarddavURI {
  boolean exists;

  /* For a resource or an entity, this is the containing collection
   */
  CarddavCollection col;

  CarddavResource resource;

  Card entity;

  String entityName;

  AccessPrincipal principal;

  boolean resourceUri; // entityname is resource

  /** Reference to a collection
   *
   * @param col
   * @param exists        true if the referenced object exists
   */
  CarddavURI(CarddavCollection col, boolean exists) {
    init(col, null, null, null, exists);
  }

  /** Reference to a contained entity
   *
   * @param col
   * @param entity
   * @param entityName
   * @param exists        true if the referenced object exists
   */
  CarddavURI(CarddavCollection col, Card entity,
             String entityName,
             boolean exists) {
    init(col, null, entity, entityName, exists);
  }

  /** Reference to a contained resource
   *
   * @param res
   * @param exists        true if the referenced object exists
   */
  CarddavURI(CarddavResource res, boolean exists) {
    init(res.getParent(), res, null, res.getName(), exists);
    resourceUri = true;
  }

  CarddavURI(AccessPrincipal principal) {
    exists = true;
    col = null;
    this.entityName = principal.getAccount();
    this.principal = principal;
  }

  private void init(CarddavCollection col, CarddavResource res,
                    Card entity, String name,
                    boolean exists) {
    this.col = col;
    this.resource = res;
    this.entity = entity;
    this.entityName = name;
    this.exists = exists;
  }

  /**
   * @return boolean
   */
  public boolean getExists() {
    return exists;
  }

  /**
   * @return WdCollection
   */
  public CarddavCollection getCol() {
    return col;
  }

  /**
   * @return BwResource
   */
  public CarddavResource getResource() {
    return resource;
  }

  /**
   * @return Object
   */
  public Card getEntity() {
    return entity;
  }

  /**
   * @return String
   * @throws WebdavException
   */
  public String getColName() throws WebdavException {
    return col.getName();
  }

  /**
   * @return String
   */
  public String getEntityName() {
    return entityName;
  }

  /* *
   * @return WebdavUserNode
   * /
  public WebdavPrincipalNode getOwner() {
    if (entity != null) {
      return col.getOwner();
    } else if (principal != null) {
      return principal;
    } else if (col != null) {
      return col.getOwner();
    } else if (resource != null) {
      return resource.getOwner();
    }

    return null;
  }*/

  /**
   * @return String
   * @throws WebdavException
   */
  public String getPath() throws WebdavException {
    if (principal != null) {
      return principal.getPrincipalRef();
    }

    if (entity != null) {
      return col.getPath() + "/" + entity.getName();
    }

    return col.getPath();
  }

  /**
   * @return String
   * @throws WebdavException
   */
  public String getUri() throws WebdavException {
//    if (entityName == null) {
//      return getPath();
//    }
//    return getPath() + "/" + entityName;
    return getPath();
  }

  /**
   * @return true if this represents a calendar
   */
  public boolean isResource() {
    return resourceUri;
  }

  /**
   * @return true if this represents a calendar
   */
  public boolean isCollection() {
    return entityName == null;
  }

  /**
   * @return true if this represents a user
   */
  public boolean isUser() {
    if (principal == null) {
      return false;
    }

    return principal.getKind() == Ace.whoTypeUser;
  }

  /**
   * @return true if this represents a group
   */
  public boolean isGroup() {
    if (principal == null) {
      return false;
    }

    return principal.getKind() == Ace.whoTypeGroup;
  }

  /**
   * @param entityName
   * @return true if has same name
   */
  public boolean sameName(String entityName) {
    if ((entityName == null) && (getEntityName() == null)) {
      return true;
    }

    if ((entityName == null) || (getEntityName() == null)) {
      return false;
    }

    return entityName.equals(getEntityName());
  }

  public String toString() {
    StringBuilder sb = new StringBuilder("CaldavURI{path=");

    try {
      sb.append(getPath());
    } catch (Throwable t) {
      sb.append("exception: ");
      sb.append(t.getMessage());
    }
    sb.append(", entityName=");
    sb.append(String.valueOf(entityName));
    sb.append("}");

    return sb.toString();
  }

  public int hashCode() {
    try {
      int hc = entityName.hashCode();

      if (isUser()) {
        return hc * 1;
      }

      if (isGroup()) {
        return hc * 2;
      }

      return hc * 3 + col.getPath().hashCode();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof CarddavURI)) {
      return false;
    }

    CarddavURI that = (CarddavURI)o;

    if (that.isUser() != isUser()) {
      return false;
    }

    if (that.isGroup() != isGroup()) {
      return false;
    }

    if (col == null) {
      if (that.col != null) {
        return false;
      }

      return true;
    }

    if (that.col == null) {
      return false;
    }

    if (!col.equals(that.col)) {
      return false;
    }

    return sameName(that.entityName);
  }
}

