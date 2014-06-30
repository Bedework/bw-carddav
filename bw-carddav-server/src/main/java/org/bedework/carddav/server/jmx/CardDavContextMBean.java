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

  /** Set the default vcard version
   *
   * @param val    String
   */
  void setDefaultVcardVersion(final String val);

  /** get the default vcard version
   *
   * @return String
   */
  @MBeanInfo("The default vcard version")
  String getDefaultVcardVersion();

  /** Set the System interface implementation
   *
   * @param val    String
   */
  void setSysintfImpl(final String val);

  /** get the System interface implementation
   *
   * @return String
   */
  @MBeanInfo("The System interface implementation")
  String getSysintfImpl();

  /** Set the web address service uri - null for no web address service
   *
   * @param val    String
   */
  void setWebaddrServiceURI(final String val);

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
  void setWebaddrServicePropertiesList(final String val);

  /**
   * @return comma separated list of web addr book searchable properties
   */
  @MBeanInfo("comma separated list of web addr book searchable properties")
  String getWebaddrServicePropertiesList();

  /**
   *
   * @param val    String
   */
  void setWebaddrPublicAddrbook(final String val);

  /**
   *
   * @return String
   */
  @MBeanInfo("Principal path for public addressbook")
  String getWebaddrPublicAddrbook();

  /**
   * @param val
   */
  void setDirectoryBrowsingDisallowed(final boolean val);

  /**
   * @return boolean
   */
  @MBeanInfo("Is directory browsing disallowed")
  boolean getDirectoryBrowsingDisallowed();

  /** Set the default addressbook name
   *
   * @param val    String
   */
  void setDefaultAddressbook(final String val);

  /** get the default addressbook name
   *
   * @return String
   */
  @MBeanInfo("Default addressbook name")
  String getDefaultAddressbook();

  /** Handler prefix for address books
   *
   * @param val    String
   */
  void setAddressBookHandlerPrefix(final String val);

  /** Handler prefix for address books
   *
   * @return String
   */
  @MBeanInfo("Handler prefix for address books")
  String getAddressBookHandlerPrefix();

  /** Set the user home root e.g. "/user"
   *
   * @param val    String
   */
  void setUserHomeRoot(final String val);

  /** Set the user home root e.g. "/user"
   *
   * @return String
   */
  @MBeanInfo("The user home root e.g. \"/user\"")
  String getUserHomeRoot();

  /** Set the principal root e.g. "/principals"
   *
   * @param val    String
   */
  void setPrincipalRoot(final String val);

  /** get the principal root e.g. "/principals"
   *
   * @return String
   */
  @MBeanInfo("The principal root e.g. \"/principals\"")
  String getPrincipalRoot();

  /** Set the user principal root e.g. "/principals/users"
   *
   * @param val    String
   */
  void setUserPrincipalRoot(final String val);

  /** get the principal root e.g. "/principals/users"
   *
   * @return String
   */
  @MBeanInfo("The user principal root e.g. \"/principals/users\"")
  String getUserPrincipalRoot();

  /** Set the group principal root e.g. "/principals/groups"
   *
   * @param val    String
   */
  void setGroupPrincipalRoot(final String val);

  /** get the group principal root e.g. "/principals/groups"
   *
   * @return String
   */
  @MBeanInfo("The group principal root e.g. \"/principals/groups\"")
  String getGroupPrincipalRoot();

  /** Set the resource principal root e.g. "/principals/resources"
   *
   * @param val    String
   */
  void setResourcePrincipalRoot(final String val);

  /** get the resource principal root e.g. "/principals/resources"
   *
   * @return String
   */
  @MBeanInfo("The resource principal root e.g. \"/principals/resources\"")
  String getResourcePrincipalRoot();

  /** Set the venue principal root e.g. "/principals/locations"
   *
   * @param val    String
   */
  void setVenuePrincipalRoot(final String val);

  /** get the venue principal root e.g. "/principals/locations"
   *
   * @return String
   */
  @MBeanInfo("The ticket principal root e.g. \"/principals/locations\"")
  String getVenuePrincipalRoot();

  /** Set the ticket principal root e.g. "/principals/tickets"
   *
   * @param val    String
   */
  void setTicketPrincipalRoot(final String val);

  /** get the ticket principal root e.g. "/principals/tickets"
   *
   * @return String
   */
  @MBeanInfo("The host principal root e.g. \"/principals/tickets\"")
  String getTicketPrincipalRoot();

  /** Set the host principal root e.g. "/principals/hosts"
   *
   * @param val    String
   */
  void setHostPrincipalRoot(final String val);

  /** get the host principal root e.g. "/principals/hosts"
   *
   * @return String
   */
  @MBeanInfo("The host principal root e.g. \"/principals/hosts\"")
  String getHostPrincipalRoot();

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /** Get info for a user
   *
   * @param cua user address
   * @return List of info lines
   */
  @MBeanInfo("Return information for the given principal or cua")
  List<String> getUserInfo(@MBeanInfo("cua") String cua);
}
