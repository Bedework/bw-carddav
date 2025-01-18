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
import org.bedework.access.Ace;
import org.bedework.carddav.common.CarddavCollection;
import org.bedework.carddav.common.vcard.Card;
import org.bedework.base.ToString;
import org.bedework.util.misc.Util;

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
   * @param col the collection
   * @param exists        true if the referenced object exists
   */
  CarddavURI(final CarddavCollection col, final boolean exists) {
    init(col, null, null, null, exists);
  }

  /** Reference to a contained entity
   *
   * @param col the collection
   * @param entity contained object
   * @param entityName it's name
   * @param exists        true if the referenced object exists
   */
  CarddavURI(final CarddavCollection col, final Card entity,
             final String entityName,
             final boolean exists) {
    init(col, null, entity, entityName, exists);
  }

  /** Reference to a contained resource
   *
   * @param res the resource
   * @param exists        true if the referenced object exists
   */
  CarddavURI(final CarddavResource res, final boolean exists) {
    init(res.getParent(), res, null, res.getName(), exists);
    resourceUri = true;
  }

  CarddavURI(final AccessPrincipal principal) {
    exists = true;
    col = null;
    entityName = principal.getAccount();
    this.principal = principal;
  }

  private void init(final CarddavCollection col, final CarddavResource res,
                    final Card entity, final String name,
                    final boolean exists) {
    this.col = col;
    resource = res;
    this.entity = entity;
    entityName = name;
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
   */
  public String getColName() {
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
   */
  public String getPath() {
    if (principal != null) {
      return principal.getPrincipalRef();
    }

    if (entity != null) {
      return Util.buildPath(false, col.getPath(), "/", entity.getName());
    }

    return col.getPath();
  }

  /**
   * @return String
   */
  public String getUri() {
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
  public boolean sameName(final String entityName) {
    if ((entityName == null) && (getEntityName() == null)) {
      return true;
    }

    if ((entityName == null) || (getEntityName() == null)) {
      return false;
    }

    return entityName.equals(getEntityName());
  }

  @Override
  public String toString() {
    final var ts = new ToString(this);

    try {
      ts.append("path", getPath());
    } catch (final Throwable t) {
      ts.append(t);
    }
    ts.append("entityName", String.valueOf(entityName));

    return ts.toString();
  }

  @Override
  public int hashCode() {
    try {
      final int hc = entityName.hashCode();

      if (isUser()) {
        return hc * 7;
      }

      if (isGroup()) {
        return hc * 11;
      }

      return (hc * 13) + col.getPath().hashCode();
    } catch (final Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof final CarddavURI that)) {
      return false;
    }

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

