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

import org.bedework.access.AccessPrincipal;
import org.bedework.base.ToString;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

/** Value object to represent a principal to allow testing of the access suite
 *
 *   @author Mike Douglass douglm   rpi.edu
 *  @version 1.0
 */
public abstract class Principal implements Comparator<Principal>,
                                           Comparable<Principal>,
                                           AccessPrincipal {
  /** Account for the principal
   */
  private String account;  // null for guest

  private String principalRef;  // null for guest

  /* groups of which this user is a member */
  protected Collection<Principal> groups;

  // Derived from the groups.
  protected Collection<String> groupNames;

  // For acl evaluation
  protected AccessPrincipal aclPrincipal;

  private String description;

  /* ==============================================================
   *                   Constructors
   * ============================================================== */

  /** Create a guest principal
   */
  public Principal() {
    this(null);
  }

  /** Create a principal for an account
   *
   * @param  account            String account name
   */
  public Principal(final String account) {
    this.account = account;
  }

  /* ==============================================================
   *                   Bean methods
   * ============================================================== */

  /**
   * @return int kind
   */
  public abstract int getKind();

  /** Set the unauthenticated state.
   *
   * @param val true for unauthenticated
   */
  public void setUnauthenticated(final boolean val) {
    if (val) {
      setAccount(null);
    }
  }

  /**
   * @return  boolean authenticated state
   */
  public boolean getUnauthenticated() {
    return getAccount() == null;
  }

  /**
   * @param val the account
   */
  public void setAccount(final String val) {
    account = val;
  }

  @Override
  public String getAccount() {
    return account;
  }

  @Override
  public String getAclAccount() {
    return account;
  }

  public void setPrincipalRef(final String val) {
    principalRef = val;
  }

  /**
   * @return  String principal reference
   */
  public String getPrincipalRef() {
    return principalRef;
  }

  /** Set of groups of which principal is a member
   *
   * @param val        Collection of Principal
   */
  public void setGroups(final Collection<Principal> val) {
    groupNames = null;
    groups = val;
  }

  /** Get the groups of which principal is a member.
   *
   * @return Collection    of Principal
   */
  public Collection<Principal> getGroups() {
    if (groups == null) {
      groups = new TreeSet<>();
    }

    return groups;
  }

  @Override
  public void setDescription(final String val) {
    description = val;
  }

  @Override
  public String getDescription() {
    return description;
  }

  /* ====================================================================
   *                   Convenience methods
   * ==================================================================== */

  /**
   * @param val Principal
   */
  public void addGroup(final Principal val) {
    getGroups().add(val);
  }

  /**
   * @return boolean true for a guest principal
   */
  public boolean isUnauthenticated() {
    return account == null;
  }

  /** Set of groupNames of which principal is a member
   *
   * @param val        Set of String
   */
  public void setGroupNames(final Collection<String> val) {
    groupNames = val;
  }

  /** Get the group names of which principal is a member.
   *
   * @return Set    of String
   */
  public Collection<String> getGroupNames() {
    if (groupNames == null) {
      groupNames = new TreeSet<>();
      for (final Principal group: getGroups()) {
        groupNames.add(group.getAccount());
      }
    }
    return groupNames;
  }

  protected void toStringSegment(final ToString ts) {
    ts.append("account", account);
    ts.append("kind", getKind());
  }

  /** Compare two strings. null is less than any non-null string.
   *
   * @param s1       first string.
   * @param s2       second string.
   * @return int     0 if the s1 is equal to s2;
   *                 <0 if s1 is lexicographically less than s2;
   *                 >0 if s1 is lexicographically greater than s2.
   */
  public static int compareStrings(final String s1, final String s2) {
    if (s1 == null) {
      if (s2 != null) {
        return -1;
      }

      return 0;
    }

    if (s2 == null) {
      return 1;
    }

    return s1.compareTo(s2);
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  public int compareTo(final Principal o) {
    return compare(this, o);
  }

  public int compare(final Principal p1, final Principal p2) {
    /*
    if (p1.getKind() < p2.getKind()) {
      return -1;
    }

    if (p1.getKind() > p2.getKind()) {
      return 1;
    }*/

    return compareStrings(p1.getPrincipalRef(), p2.getPrincipalRef());
  }

  @Override
  public int hashCode() {
    int hc = 7 * (getKind() + 1);

    if (account != null) {
      hc = account.hashCode();
    }

    return hc;
  }

  @Override
  public boolean equals(final Object o) {
    return compareTo((Principal)o) == 0;
  }

  @Override
  public String toString() {
    final ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
