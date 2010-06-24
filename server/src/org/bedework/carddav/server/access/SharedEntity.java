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
package org.bedework.carddav.server.access;


/** Entities implementing this support access control as implemented by the access
 * utilities.
 *
 * <p>The owner href (ownerHref), encoded acl (access) and parent path must be
 * persisted.
 *
 *  <isCollection is usually derived.
 *
 * <p>path may be persisted or derived.
 *
 * <p>accessState is a transient object allowing us to calculate the access and
 * possibly cache that calculation.
 *
 *
 * @author Mike Douglass
 * @version 1.0
 *
 * @param <T>
 */
public interface SharedEntity {
  /** Href of owner
   *
   * @param val
   */
  void setOwnerHref(final String val);

  /** Href of owner
   *
   * @return String
   */
  String getOwnerHref();

  /** Encoded acl
   *
   * @param val
   */
  void setAccess(final String val);

  /** Encoded acl
   *
   * @return String
   */
  String getAccess();

  /** Access is inherited from the parent
   *
   * @param val
   */
  void setParentPath(final String val);

  /** Access is inherited from the parent
   *
   * @return parentPath.
   */
  String getParentPath();

  /**
   * @return the full path of this entity (parentPath + "/" + name)
   */
  String getPath();

  /** We only try to preserve access state in collections. There will be many more
   * entity objects (events, cards etc) than collections. In addition, most
   * entity objects don't have specific access set on them so the access is
   * essentially the parent collection access.
   *
   * @return boolean true for this entity being a collection
   */
  boolean isCollection();

  /** Set the access state
   *
   * @param val
   */
  void setAccessState(AccessState val);

  /**
   * @return current AccessState object or null
   */
  AccessState getAccessState();
}
