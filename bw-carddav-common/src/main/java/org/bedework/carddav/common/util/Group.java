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
package org.bedework.carddav.common.util;

import org.bedework.access.WhoDefs;
import org.bedework.base.ToString;

import java.util.Collection;
import java.util.TreeSet;

/** Group object for access suite tests.
 *
 *   @author Mike Douglass douglm   rpi.edu
 *  @version 1.0
 */
public class Group extends Principal {
  /** members of the group
   */
  private Collection<Principal> groupMembers;

  /* ==============================================================
   *                   Constructors
   * ============================================================== */

  /** Create a group
   */
  public Group() {
    super();
  }

  /** Create a group with a given name
   *
   * @param  account            String group account name
   */
  public Group(final String account) {
    super(account);
  }

  public int getKind() {
    return WhoDefs.whoTypeGroup;
  }

  /** Set the members of the group.
   *
   * @param   val     Collection of group members.
   */
  public void setGroupMembers(final Collection<Principal> val) {
    groupMembers = val;
  }

  /** Return the members of the group.
   *
   * @return Collection        group members
   */
  public Collection<Principal> getGroupMembers() {
    if (groupMembers == null) {
      groupMembers = new TreeSet<Principal>();
    }
    return groupMembers;
  }

  /* ==============================================================
   *                   Convenience methods
   * ============================================================== */

  /** Return true if the account name is in the group members.
   *
   * @param account to check
   * @param group     boolean true if we're testing for a group.
   * @return true if the account name is in the group members.
   */
  public boolean isMember(final String account,
                          final boolean group) {
    for (final Principal mbr: getGroupMembers()) {
      if (mbr.getAccount().equals(account)) {
        if (group == (mbr instanceof Group)) {
          return true;
        }
      }
    }

    return false;
  }

  /** Add a group member. Return true if is was added, false if it was in
   * the list
   *
   * @param mbr        Principal to add
   * @return boolean   true if added
   */
  public boolean addGroupMember(final Principal mbr) {
    return getGroupMembers().add(mbr);
  }

  /** Remove a group member. Return true if it was removed, false if it was
   * not in the list
   *
   * @param mbr        Principal to remove
   * @return boolean   true if removed
   */
  public boolean removeGroupMember(final Principal mbr) {
    return getGroupMembers().remove(mbr);
  }

  protected void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);
    ts.append("groupMembers", getGroupMembers());
  }
}
