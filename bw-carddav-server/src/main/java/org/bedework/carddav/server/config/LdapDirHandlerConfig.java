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
package org.bedework.carddav.server.config;

import org.bedework.carddav.common.config.DirHandlerConfig;
import org.bedework.util.config.ConfInfo;


/** This class defines the various properties we need to make a connection
 * and retrieve a group and user information via ldap.
 *
 * @author Mike Douglass
 */
@ConfInfo(elementName = "bwcarddav-dirhandler")
public class LdapDirHandlerConfig extends
        DirHandlerConfig<LdapDirHandlerConfig> {
  private String initialContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";

  private String securityAuthentication = "simple";

  private String securityProtocol = "NONE";

  private String providerUrl;

  private String baseDn;

  private int queryLimit;

  private String attrIds;

  private String folderObjectClass;

  private String addressbookObjectClass;

  private String addressbookEntryObjectClass;

  private String principalIdAttr;

  private String folderIdAttr = "ou";

  private String addressbookIdAttr = "ou";

  private String addressbookEntryIdAttr = "cn";

  private String groupMemberAttr;

  private String authDn;

  private String authPw;

  /* Not config */
  private String[] attrIdList = null;

  /**
   * @param val
   */
  public void setInitialContextFactory(final String val)  {
    initialContextFactory  = val;
  }

  /**
   * @return String
   */
  public String getInitialContextFactory()  {
    return initialContextFactory;
  }

  /**
   * @param val
   */
  public void setSecurityAuthentication(final String val)  {
    securityAuthentication  = val;
  }

  /**
   * @return String
   */
  public String getSecurityAuthentication()  {
    return securityAuthentication;
  }

  /** e.g. "ssl"
  *
  * @param val
  */
  public void setSecurityProtocol(final String val)  {
    securityProtocol = val;
  }

  /** e.g "ssl"
  *
  * @return String val
  */
  public String getSecurityProtocol()  {
    return securityProtocol;
  }

  /** URL of ldap server
   *
   * @param val
   */
  public void setProviderUrl(final String val)  {
    providerUrl = val;
  }

  /** URL of ldap server
   *
   * @return String val
   */
  public String getProviderUrl()  {
    return providerUrl;
  }

  /**
   *
   * @param val
   */
  public void setBaseDn(final String val)  {
    baseDn = val;
  }

  /**
   *
   * @return String val
   */
  public String getBaseDn()  {
    return baseDn;
  }

  /** Set the query limit - 0 for no limit
   *
   * @param val
   */
  public void setQueryLimit(final int val)  {
    queryLimit = val;
  }

  /**
   *
   * @return int val
   */
  public int getQueryLimit()  {
    return queryLimit;
  }

  /**
   *
   * @param val
   */
  public void setAttrIds(final String val)  {
    attrIds = val;

    attrIdList = attrIds.split("[,\\s]");
  }

  /**
   *
   * @return String val
   */
  public String getAttrIds()  {
    return attrIds;
  }

  /**
   *
   * @param val
   */
  public void setFolderObjectClass(final String val)  {
    folderObjectClass = val;
  }

  /**
   *
   * @return String val
   */
  public String getFolderObjectClass()  {
    return folderObjectClass;
  }

  /**
   *
   * @param val
   */
  public void setAddressbookObjectClass(final String val)  {
    addressbookObjectClass = val;
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookObjectClass()  {
    return addressbookObjectClass;
  }

  /**
   *
   * @param val
   */
  public void setAddressbookEntryObjectClass(final String val)  {
    addressbookEntryObjectClass = val;
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookEntryObjectClass()  {
    return addressbookEntryObjectClass;
  }

  /** Attribute we search for to get a principal
   *
   * @param val
   */
  public void setPrincipalIdAttr(final String val)  {
    principalIdAttr = val;
  }

  /** Attribute we search for to get a group
   *
   * @return String val
   */
  public String getPrincipalIdAttr()  {
    return principalIdAttr;
  }

  /**
   *
   * @param val
   */
  public void setFolderIdAttr(final String val)  {
    folderIdAttr = val;
  }

  /**
   *
   * @return String val
   */
  public String getFolderIdAttr()  {
    return folderIdAttr;
  }

  /**
   *
   * @param val
   */
  public void setAddressbookIdAttr(final String val)  {
    addressbookIdAttr = val;
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookIdAttr()  {
    return addressbookIdAttr;
  }

  /**
   *
   * @param val
   */
  public void setAddressbookEntryIdAttr(final String val)  {
    addressbookEntryIdAttr = val;
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookEntryIdAttr()  {
    return addressbookEntryIdAttr;
  }

  /** Attribute we want back identifying a member
   *
   * @param val
   */
  public void setGroupMemberAttr(final String val)  {
    groupMemberAttr = val;
  }

  /** Attribute we want back identifying a member
   *
   * @return String val
   */
  public String getGroupMemberAttr()  {
    return groupMemberAttr;
  }

  /** If we need an id to authenticate this is it.
   *
   * @param val
   */
  public void setAuthDn(final String val)  {
    authDn = val;
  }

  /** If we need an id to authenticate this is it.
   *
   * @return String val
   */
  public String getAuthDn()  {
    return authDn;
  }

  /** If we need an id to authenticate this is the pw.
   *
   * @param val
   */
  public void setAuthPw(final String val)  {
    authPw = val;
  }

  /** If we need an id to authenticate this is it.
   *
   * @return String val
   */
  public String getAuthPw()  {
    return authPw;
  }

  /**
   *
   * @return String[] val
   */
  @ConfInfo(dontSave = true)
  public String[] getAttrIdList()  {
    return attrIdList;
  }
}
