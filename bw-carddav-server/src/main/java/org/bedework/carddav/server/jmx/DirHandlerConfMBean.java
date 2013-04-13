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

import edu.rpi.cmt.jmx.ConfBaseMBean;
import edu.rpi.cmt.jmx.MBeanInfo;

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
  void setPathPrefix(final String val);

  /** Get the pathPrefix
   *
   * @return String
   */
  @MBeanInfo("The pathPrefix which defines the paths for which we call this handler.")
  String getPathPrefix();

  /** Set the cardPathPrefix which defines the prefix for principal cards.
   *
   * @param val    String
   */
  void setCardPathPrefix(final String val);

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
  void setCardPathPrefixes(final String val);

  /** Get the cardPathPrefixes
   *
   * @return prefixes
   */
  @MBeanInfo("The cardPathPrefixes which defines the prefixes for principal " +
  		"cards based on an account prefix.")
  String getCardPathPrefixes();

  /** True if this prefix represents an addressbook.
   * @param val
   */
  void setAddressBook(final boolean val);

  /** True if this prefix represents an addressbook.
   * @return boolean
   */
  @MBeanInfo("True if this prefix represents an addressbook.")
  boolean getAddressBook();

  /** True if this prefix represents a directory.
   * @param val
   */
  void setDirectory(final boolean val);

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
  void setClassName(final String val);

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
  void setOwnerHref(final String val);

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
  void setCardKind(final String val);

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
