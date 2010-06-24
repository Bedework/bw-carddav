/* **********************************************************************
    Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
package org.bedework.carddav.server.dirHandlers.db;

import org.bedework.carddav.server.access.SharedEntity;

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
   * @param val
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
   * @param val
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
   * @param val
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
   * @param val
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
   * @param val
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
   * @param val
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

  /* ====================================================================
   *                   Action methods
   * ==================================================================== */

  /** Add a deleted entity - these may appear as a result of updates.
   * A null parameter is a noop.
   *
   * @param val
   */
  public void addDeletedEntity(final DbEntity<?> val) {
    if ((val == null) || val.unsaved()) {
      return;
    }

    if (deletedEntities == null) {
      deletedEntities = new ArrayList<DbEntity<?>>();
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

  /**
   * @param sb
   */
  @Override
  public void toStringSegment(final StringBuilder sb) {
    super.toStringSegment(sb);

    sb.append(", \n   ownerHref=");
    sb.append(getOwnerHref());
    sb.append(", parentPath=");
    sb.append(getParentPath());
    sb.append(", created=");
    sb.append(getCreated());
    sb.append(",\n   acl=");
    sb.append(getAccess());
  }
}
