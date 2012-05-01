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
package org.bedework.carddav.util;


/** This class defines the various properties we need to make a connection
 * and retrieve a group and user information via ldap.
 *
 * @author Mike Douglass
 */
public class LdapDirHandlerConfig extends DirHandlerConfig {
  private static final String initialContextFactoryPname = "initialContextFactory";
  private static final String securityAuthenticationPname = "securityAuthentication";

  private static final String securityProtocolPname = "securityProtocol";

  private static final String providerUrlPname = "providerUrl";

  private static final String baseDnPname = "baseDn";

  private static final String queryLimitPname = "queryLimit";

  private static final String attrIdsPname = "attrIds";

  private static final String folderObjectClassPname = "folderObjectClass";

  private static final String addressbookObjectClassPname = "addressbookObjectClass";

  private static final String addressbookEntryObjectClassPname = "addressbookEntryObjectClass";

  private static final String principalIdAttrPname = "principalIdAttr";

  private static final String folderIdAttrPname = "folderIdAttr";

  private static final String addressbookIdAttrPname = "addressbookIdAttr";

  private static final String addressbookEntryIdAttrPname = "addressbookEntryIdAttr";

  private static final String groupMemberAttrPname = "groupMemberAttr";

  private static final String authDnPname = "authDn";

  private static final String authPwPname = "authPw";

  private String[] attrIdList = null;

  /**
   * @param val
   */
  public void setInitialContextFactory(final String val)  {
    setProperty(initialContextFactoryPname, val);
  }

  /**
   * @return String
   */
  public String getInitialContextFactory()  {
    return getPropertyValue(initialContextFactoryPname);
  }

  /**
   * @param val
   */
  public void setSecurityAuthentication(final String val)  {
    setProperty(securityAuthenticationPname, val);
  }

  /**
   * @return String
   */
  public String getSecurityAuthentication()  {
    return getPropertyValue(securityAuthenticationPname);
  }

  /** e.g. "ssl"
  *
  * @param val
  */
  public void setSecurityProtocol(final String val)  {
    setProperty(securityProtocolPname, val);
  }

  /** e.g "ssl"
  *
  * @return String val
  */
  public String getSecurityProtocol()  {
    return getPropertyValue(securityProtocolPname);
  }

  /** URL of ldap server
   *
   * @param val
   */
  public void setProviderUrl(final String val)  {
    setProperty(providerUrlPname, val);
  }

  /** URL of ldap server
   *
   * @return String val
   */
  public String getProviderUrl()  {
    return getPropertyValue(providerUrlPname);
  }

  /**
   *
   * @param val
   */
  public void setBaseDn(final String val)  {
    setProperty(baseDnPname, val);
  }

  /**
   *
   * @return String val
   */
  public String getBaseDn()  {
    return getPropertyValue(baseDnPname);
  }

  /** Set the query limit - 0 for no limit
   *
   * @param val
   */
  public void setQueryLimit(final int val)  {
    setProperty(queryLimitPname, String.valueOf(val));
  }

  /**
   *
   * @return int val
   */
  public int getQueryLimit()  {
    return getIntPropertyValue(queryLimitPname);
  }

  /**
   *
   * @param val
   */
  public void setAttrIds(final String val)  {
    setProperty(attrIdsPname, val);
  }

  /**
   *
   * @return String val
   */
  public String getAttrIds()  {
    return getPropertyValue(attrIdsPname);
  }

  /**
   *
   * @param val
   */
  public void setFolderObjectClass(final String val)  {
    setProperty(folderObjectClassPname, val);
  }

  /**
   *
   * @return String val
   */
  public String getFolderObjectClass()  {
    return getPropertyValue(folderObjectClassPname);
  }

  /**
   *
   * @param val
   */
  public void setAddressbookObjectClass(final String val)  {
    setProperty(addressbookObjectClassPname, val);
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookObjectClass()  {
    return getPropertyValue(addressbookObjectClassPname);
  }

  /**
   *
   * @param val
   */
  public void setAddressbookEntryObjectClass(final String val)  {
    setProperty(addressbookEntryObjectClassPname, val);
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookEntryObjectClass()  {
    return getPropertyValue(addressbookEntryObjectClassPname);
  }

  /** Attribute we search for to get a principal
   *
   * @param val
   */
  public void setPrincipalIdAttr(final String val)  {
    setProperty(principalIdAttrPname, val);
  }

  /** Attribute we search for to get a group
   *
   * @return String val
   */
  public String getPrincipalIdAttr()  {
    return getPropertyValue(principalIdAttrPname);
  }

  /**
   *
   * @param val
   */
  public void setFolderIdAttr(final String val)  {
    setProperty(folderIdAttrPname, val);
  }

  /**
   *
   * @return String val
   */
  public String getFolderIdAttr()  {
    return getPropertyValue(folderIdAttrPname);
  }

  /**
   *
   * @param val
   */
  public void setAddressbookIdAttr(final String val)  {
    setProperty(addressbookIdAttrPname, val);
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookIdAttr()  {
    return getPropertyValue(addressbookIdAttrPname);
  }

  /**
   *
   * @param val
   */
  public void setAddressbookEntryIdAttr(final String val)  {
    setProperty(addressbookEntryIdAttrPname, val);
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookEntryIdAttr()  {
    return getPropertyValue(addressbookEntryIdAttrPname);
  }

  /** Attribute we want back identifying a member
   *
   * @param val
   */
  public void setGroupMemberAttr(final String val)  {
    setProperty(groupMemberAttrPname, val);
  }

  /** Attribute we want back identifying a member
   *
   * @return String val
   */
  public String getGroupMemberAttr()  {
    return getPropertyValue(groupMemberAttrPname);
  }

  /** If we need an id to authenticate this is it.
   *
   * @param val
   */
  public void setAuthDn(final String val)  {
    setProperty(authDnPname, val);
  }

  /** If we need an id to authenticate this is it.
   *
   * @return String val
   */
  public String getAuthDn()  {
    return getPropertyValue(authDnPname);
  }

  /** If we need an id to authenticate this is the pw.
   *
   * @param val
   */
  public void setAuthPw(final String val)  {
    setProperty(authPwPname, val);
  }

  /** If we need an id to authenticate this is it.
   *
   * @return String val
   */
  public String getAuthPw()  {
    return getPropertyValue(authPwPname);
  }

  /**
   *
   * @return String[] val
   */
  public String[] getAttrIdList()  {
    if (attrIdList == null) {
      if (getAttrIds() != null) {
        attrIdList = getAttrIds().split("[,\\s]");
      }
    }
    return attrIdList;
  }
}
