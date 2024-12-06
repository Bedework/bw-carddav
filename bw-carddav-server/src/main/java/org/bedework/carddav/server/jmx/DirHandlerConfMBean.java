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
package org.bedework.carddav.server.jmx;

import org.bedework.util.jmx.ConfBaseMBean;
import org.bedework.util.jmx.MBeanInfo;

/** Configure a carddav service dir handler
 *
 * @author douglm
 */
public interface DirHandlerConfMBean extends ConfBaseMBean {
  /* ========================================================================
   * Attributes
   * ======================================================================== */

  /** Set the pathPrefix which defines the paths for which we call this handler.
   *
   * @param val    String
   */
  void setPathPrefix(String val);

  /** Get the pathPrefix
   *
   * @return String
   */
  @MBeanInfo("The pathPrefix which defines the paths for which we call this handler.")
  String getPathPrefix();

  /** Set the prefix for principals which correspond to cards within
   * this hierarchy
   *
   * <p>For example "/principals.users" might be a principal prefix
   * handled by "/directory/users"
   *
   * @param val    String path
   */
  void setPrincipalPrefix(String val);

  /** Get the pathPrefix
   *
   * @return String   path
   */
  @MBeanInfo("The pathPrefix for principals which correspond to cards within" +
                     " this hierarchy.")
  String getPrincipalPrefix();

  /** Set the cardPathPrefix which defines the prefix for principal cards.
   *
   * @param val    String
   */
  void setCardPathPrefix(String val);

  /** Get the cardPathPrefix
   *
   * @return String
   */
  @MBeanInfo("The cardPathPrefix which defines the prefix for principal cards")
  String getCardPathPrefix();

  /** Set the cardPathPrefixes which defines the prefixes for principal cards based
   * on an account prefix.
   *
   * @param val    String
   */
  void setCardPathPrefixes(String val);

  /** Get the cardPathPrefixes
   *
   * @return prefixes
   */
  @MBeanInfo("The cardPathPrefixes which defines the prefixes for principal " +
  		"cards based on an account prefix.")
  String getCardPathPrefixes();

  /**
   * @param val True if this prefix represents an addressbook.
   */
  void setAddressBook(boolean val);

  /** True if this prefix represents an addressbook.
   * @return boolean
   */
  @MBeanInfo("True if this prefix represents an addressbook.")
  boolean getAddressBook();

  /**
   * @param val True if this prefix represents a directory.
   */
  void setDirectory(boolean val);

  /** True if this prefix represents a directory.
   * @return boolean
   */
  @MBeanInfo("True if this prefix represents a (potentially very large) directory. " +
  		"Tells clients not to try to download the whole thing")
  boolean getDirectory();

  /** Set the interface implementation
   *
   * @param val    String
   */
  void setClassName(String val);

  /** get the interface implementation
   *
   * @return String
   */
  @MBeanInfo("The interface implementation")
  String getClassName();

  /** The href for the owner
   *
   * @param val    String
   */
  void setOwnerHref(String val);

  /** The href for the owner
   *
   * @return String
   */
  @MBeanInfo("The href for the owner")
  String getOwnerHref();

  /** If set defines the default kind in this directory
   *
   * @param val    String
   */
  void setCardKind(String val);

  /** If set defines the default kind in this directory
   *
   * @return String
   */
  @MBeanInfo("If set defines the default kind in this directory")
  String getCardKind();

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /* * Get info for a user
   *
   * @param cua
   * @return List of info lines
   * /
  @MBeanInfo("Return information for the given principal or cua")
  List<String> getUserInfo(@MBeanInfo("cua") String cua);
  */
}
