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

import java.util.List;

/** Run the carddav service
 *
 * @author douglm
 */
public interface CardDavContextMBean extends ConfBaseMBean {
  /* ========================================================================
   * Attributes
   * ======================================================================== */

  /** Set the web address service uri - null for no web address service
   *
   * @param val    String
   */
  void setWebaddrServiceURI(String val);

  /** get the web address service uri - null for no web address service
   *
   * @return String
   */
  @MBeanInfo("The web address service uri - null for no web address service")
  String getWebaddrServiceURI();

  /** Set the comma separated list of web addr book searchable properties
   *
   * @param val    String
   */
  void setWebaddrServicePropertiesList(String val);

  /**
   * @return comma separated list of web addr book searchable properties
   */
  @MBeanInfo("comma separated list of web addr book searchable properties")
  String getWebaddrServicePropertiesList();

  /**
   *
   * @param val    String
   */
  void setWebaddrPublicAddrbook(String val);

  /**
   *
   * @return String
   */
  @MBeanInfo("Principal path for public addressbook")
  String getWebaddrPublicAddrbook();

  /**
   * @param val true to disbale browsing
   */
  void setDirectoryBrowsingDisallowed(boolean val);

  /**
   * @return boolean
   */
  @MBeanInfo("Is directory browsing disallowed")
  boolean getDirectoryBrowsingDisallowed();

  /** Handler prefix for address books
   *
   * @param val    String
   */
  void setAddressBookHandlerPrefix(String val);

  /** Handler prefix for address books
   *
   * @return String
   */
  @MBeanInfo("Handler prefix for address books")
  String getAddressBookHandlerPrefix();

  /* ==============================================================
   * Operations
   * ============================================================== */

  /** Get info for a user
   *
   * @param cua user address
   * @return List of info lines
   */
  @MBeanInfo("Return information for the given principal or cua")
  List<String> getUserInfo(@MBeanInfo("cua") String cua);
}
