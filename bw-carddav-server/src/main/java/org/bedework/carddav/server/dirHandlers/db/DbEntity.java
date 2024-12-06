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
package org.bedework.carddav.server.dirHandlers.db;

import org.bedework.util.misc.ToString;
import org.bedework.webdav.servlet.access.SharedEntity;

import java.util.ArrayList;
import java.util.Collection;

/** Base type for a database entity. We require an id and the subclasses must
 * implement hashcode and compareTo.
 *
 * @author Mike Douglass
 * @version 1.0
 *
 * @param <T>
 */
public abstract class DbEntity<T> extends UnversionedDbentity<T> implements SharedEntity {
  /* Hibernate does not implicitly delete db entities during update or
   * save, except for those referenced as part of a Collection.
   *
   * These lists allows us to do explicit deletes when we delete or
   * update the entry.
   */

  private Collection<DbEntity<?>> deletedEntities;

  /* db version number */
  private Integer seq;

  /* For quota'd dbentities. */
  private int byteSize;

  private String ownerHref;

  private String creatorHref;

  private String access;

  private String parentPath;

  private String created;

  /** No-arg constructor
   *
   */
  public DbEntity() {
  }

  /** The last calculated byte size should be stored with the entity. On update
   * call calculateByteSize to get a new value and use the difference to adjust
   * the quota.
   *
   * @param val byte size
   */
  public void setByteSize(final int val) {
    byteSize = val;
  }

  /**
   * @return int last byte size
   */
  public int getByteSize() {
    return byteSize;
  }

  /** Set the seq for this entity
   *
   * @param val    int seq
   */
  public void setSeq(final Integer val) {
    seq = val;
  }

  /** Get the entity seq
   *
   * @return int    the entity seq
   */
  public Integer getSeq() {
    return seq;
  }

  /**
   * @param val owner href
   */
  public void setOwnerHref(final String val) {
    ownerHref = val;
  }

  /**
   * @return String
   */
  public String getOwnerHref() {
    return ownerHref;
  }

  /**
   * @param val  creatorHref
   */
  public void setCreatorHref(final String val) {
    creatorHref = val;
  }

  /**
   * @return String
   */
  public String getCreatorHref() {
    return creatorHref;
  }

  /**
   * @param val access
   */
  public void setAccess(final String val) {
    access = val;
  }

  /**
   * @return String
   */
  public String getAccess() {
    return access;
  }

  /**
   * @param val parent path
   */
  public void setParentPath(final String val) {
    parentPath = val;
  }

  /**
   * @return parentPath.
   */
  public String getParentPath() {
    return parentPath;
  }

  /**
   * @param val create date/time
   */
  public void setCreated(final String val) {
    created = val;
  }

  /**
   * @return String created
   */
  public String getCreated() {
    return created;
  }

  /* ==============================================================
   *                   Action methods
   * ============================================================== */

  /** Add a deleted entity - these may appear as a result of updates.
   * A null parameter is a noop.
   *
   * @param val a deleted entity
   */
  public void addDeletedEntity(final DbEntity<?> val) {
    if ((val == null) || val.unsaved()) {
      return;
    }

    if (deletedEntities == null) {
      deletedEntities = new ArrayList<>();
    }

    deletedEntities.add(val);
  }

  /**
   * @return deleted entities or null
   */
  @NoDump
  public Collection<DbEntity<?>> getDeletedEntities() {
    return deletedEntities;
  }

  /** Called when we are about to delete from the db
   *
   */
  public void beforeDeletion() {
  }

  /** Called after delete from the db
   *
   */
  public void afterDeletion() {
  }

  /** Called when we are about to update the object.
   *
   */
  public void beforeUpdate() {
  }

  /** Called when we are about to save the object. Default to calling before
   * update
   *
   */
  public void beforeSave() {
    beforeUpdate();
  }

  /** Size to use for quotas.
   * @return int
   */
  public int length() {
    return 8;  // overhead
  }

  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.newLine()
      .append("ownerHref", getOwnerHref())
      .append("parentPath", getParentPath())
      .append("created", getCreated())
      .newLine()
      .append("acl=", getAccess());
  }
}
