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

import org.apache.activemq.broker.jmx.MBeanInfo;

/** Configure a carddav service dir handler
 *
 * @author douglm
 */
public interface LdapDirHandlerConfMBean extends ConfBaseMBean {
  /* ========================================================================
   * Attributes
   * ======================================================================== */
  /**
   * @param val
   */
  void setInitialContextFactory(final String val);

  /**
   * @return String
   */
  @MBeanInfo("")
  String getInitialContextFactory();

  /**
   * @param val
   */
  void setSecurityAuthentication(final String val);

  /**
   * @return String
   */
  @MBeanInfo("security Authentication")
  String getSecurityAuthentication();

  /** e.g. "ssl"
  *
  * @param val
  */
  void setSecurityProtocol(final String val) ;

  /** e.g "ssl"
  *
  * @return String val
  */
  @MBeanInfo("e.g. \"ssl\"")
  String getSecurityProtocol();

  /** URL of ldap server
   *
   * @param val
   */
  void setProviderUrl(final String val);

  /** URL of ldap server
   *
   * @return String val
   */
  @MBeanInfo("URL of ldap server")
  String getProviderUrl();

  /**
   *
   * @param val
   */
  void setBaseDn(final String val);

  /**
   *
   * @return String val
   */
  @MBeanInfo("The base dn")
  String getBaseDn();

  /** Set the query limit - 0 for no limit
   *
   * @param val
   */
  void setQueryLimit(final int val);

  /**
   *
   * @return int val
   */
  @MBeanInfo("Max number of responses")
  int getQueryLimit();

  /**
   *
   * @param val
   */
  void setAttrIds(final String val);

  /**
   *
   * @return String val
   */
  @MBeanInfo("comman separated list of attribute ids")
  String getAttrIds();

  /**
   *
   * @param val
   */
  void setFolderObjectClass(final String val);

  /**
   *
   * @return String val
   */
  @MBeanInfo("The objectclass used to represent a folder or collection. " +
  		"usually \"organizationalUnit\". Required.")
  String getFolderObjectClass();

  /**
   *
   * @param val
   */
  void setAddressbookObjectClass(final String val);

  /**
   *
   * @return String val
   */
  @MBeanInfo("Addressbook ObjectClass")
  String getAddressbookObjectClass();

  /**
   *
   * @param val
   */
  void setAddressbookEntryObjectClass(final String val);

  /**
   *
   * @return String val
   */
  @MBeanInfo("Addressbook entry ObjectClass")
  String getAddressbookEntryObjectClass();

  /** Attribute we search for to get a principal
   *
   * @param val
   */
  void setPrincipalIdAttr(final String val);

  /** Attribute we search for to get a group
   *
   * @return String val
   */
  @MBeanInfo("Attribute we search for to get a group")
  String getPrincipalIdAttr();

  /**
   *
   * @param val
   */
  @MBeanInfo("")
  void setFolderIdAttr(final String val) ;

  /**
   *
   * @return String val
   */
  @MBeanInfo("The attribute used to designate the equivalent of a folder or " +
  		"collection, usually \"ou\". Required.")
  String getFolderIdAttr();

  /**
   *
   * @param val
   */
  void setAddressbookIdAttr(final String val);

  /**
   *
   * @return String val
   */
  @MBeanInfo("addressbook Id Attr")
  String getAddressbookIdAttr();

  /**
   *
   * @param val
   */
  void setAddressbookEntryIdAttr(final String val);

  /**
   *
   * @return String val
   */
  @MBeanInfo("Addressbook Entry Id Attribute")
  String getAddressbookEntryIdAttr();

  /** Attribute we want back identifying a member
   *
   * @param val
   */
  void setGroupMemberAttr(final String val);

  /** Attribute we want back identifying a member
   *
   * @return String val
   */
  @MBeanInfo("Attribute we want back identifying a member")
  String getGroupMemberAttr();

  /** If we need an id to authenticate this is it.
   *
   * @param val
   */
  void setAuthDn(final String val);

  /** If we need an id to authenticate this is it.
   *
   * @return String val
   */
  @MBeanInfo("If we need an id to authenticate this is it")
  String getAuthDn();

  /** If we need an id to authenticate this is the pw.
   *
   * @param val
   */
  void setAuthPw(final String val);

  /** If we need an id to authenticate this is the pw.
   *
   * @return String val
   */
  @MBeanInfo("If we need an id to authenticate this is the password.")
  String getAuthPw();

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
