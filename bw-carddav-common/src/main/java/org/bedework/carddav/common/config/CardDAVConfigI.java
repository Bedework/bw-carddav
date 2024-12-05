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
package org.bedework.carddav.common.config;

import org.bedework.util.config.ConfInfo;
import org.bedework.util.jmx.MBeanInfo;

/** This interface defines the various properties we need for a carddav server
 *
 * @author Mike Douglass
 */
@ConfInfo(elementName = "bwcarddav")
public interface CardDAVConfigI {
  /** data output directory name - full path. Used for data export
   *
   * @param val path
   */
  void setDataOut(String val);

  /**
   * @return data output full path
   */
  String getDataOut();

  /** Set the default vcard version
   *
   * @param val    String
   */
  void setDefaultVcardVersion(String val);

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
  void setSysintfImpl(String val);

  /** get the System interface implementation
   *
   * @return String
   */
  @MBeanInfo("The System interface implementation")
  String getSysintfImpl();

  /** Set the default addressbook name
   *
   * @param val    String
   */
  void setDefaultAddressbook(String val);

  /** get the default addressbook name
   *
   * @return String
   */
  @MBeanInfo("Default addressbook name")
  String getDefaultAddressbook();

  /** Set the user home root e.g. "/user"
   *
   * @param val    String
   */
  void setUserHomeRoot(String val);

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
  void setPrincipalRoot(String val);

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
  void setUserPrincipalRoot(String val);

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
  void setGroupPrincipalRoot(String val);

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
  void setResourcePrincipalRoot(String val);

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
  void setVenuePrincipalRoot(String val);

  /** get the venue principal root e.g. "/principals/locations"
   *
   * @return String
   */
  @MBeanInfo("The venue principal root e.g. \"/principals/venues\"")
  String getVenuePrincipalRoot();

  /** Set the ticket principal root e.g. "/principals/tickets"
   *
   * @param val    String
   */
  void setTicketPrincipalRoot(String val);

  /** get the ticket principal root e.g. "/principals/tickets"
   *
   * @return String
   */
  @MBeanInfo("The ticket principal root e.g. \"/principals/tickets\"")
  String getTicketPrincipalRoot();

  /** Set the host principal root e.g. "/principals/hosts"
   *
   * @param val    String
   */
  void setHostPrincipalRoot(String val);

  /** get the host principal root e.g. "/principals/hosts"
   *
   * @return String
   */
  @MBeanInfo("The host principal root e.g. \"/principals/hosts\"")
  String getHostPrincipalRoot();
}
