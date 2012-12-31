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

import javax.xml.namespace.QName;


/** This class defines the various properties we need to make a connection
 * and retrieve a group and user information via ldap.
 *
 * @author Mike Douglass
 */
public class LdapDirHandlerConfig extends DirHandlerConfig {
  private static final QName initialContextFactory = new QName(ns, "initialContextFactory");

  private static final QName securityAuthentication = new QName(ns, "securityAuthentication");

  private static final QName securityProtocol = new QName(ns, "securityProtocol");

  private static final QName providerUrl = new QName(ns, "providerUrl");

  private static final QName baseDn = new QName(ns, "baseDn");

  private static final QName queryLimit = new QName(ns, "queryLimit");

  private static final QName attrIds = new QName(ns, "attrIds");

  private static final QName folderObjectClass = new QName(ns, "folderObjectClass");

  private static final QName addressbookObjectClass = new QName(ns, "addressbookObjectClass");

  private static final QName addressbookEntryObjectClass = new QName(ns, "addressbookEntryObjectClass");

  private static final QName principalIdAttr = new QName(ns, "principalIdAttr");

  private static final QName folderIdAttr = new QName(ns, "folderIdAttr");

  private static final QName addressbookIdAttr = new QName(ns, "addressbookIdAttr");

  private static final QName addressbookEntryIdAttr = new QName(ns, "addressbookEntryIdAttr");

  private static final QName groupMemberAttr = new QName(ns, "groupMemberAttr");

  private static final QName authDn = new QName(ns, "authDn");

  private static final QName authPw = new QName(ns, "authPw");

  private String[] attrIdList = null;

  /**
   * @param val
   */
  public void setInitialContextFactory(final String val)  {
    setProperty(initialContextFactory, val);
  }

  /**
   * @return String
   */
  public String getInitialContextFactory()  {
    return getPropertyValue(initialContextFactory);
  }

  /**
   * @param val
   */
  public void setSecurityAuthentication(final String val)  {
    setProperty(securityAuthentication, val);
  }

  /**
   * @return String
   */
  public String getSecurityAuthentication()  {
    return getPropertyValue(securityAuthentication);
  }

  /** e.g. "ssl"
  *
  * @param val
  */
  public void setSecurityProtocol(final String val)  {
    setProperty(securityProtocol, val);
  }

  /** e.g "ssl"
  *
  * @return String val
  */
  public String getSecurityProtocol()  {
    return getPropertyValue(securityProtocol);
  }

  /** URL of ldap server
   *
   * @param val
   */
  public void setProviderUrl(final String val)  {
    setProperty(providerUrl, val);
  }

  /** URL of ldap server
   *
   * @return String val
   */
  public String getProviderUrl()  {
    return getPropertyValue(providerUrl);
  }

  /**
   *
   * @param val
   */
  public void setBaseDn(final String val)  {
    setProperty(baseDn, val);
  }

  /**
   *
   * @return String val
   */
  public String getBaseDn()  {
    return getPropertyValue(baseDn);
  }

  /** Set the query limit - 0 for no limit
   *
   * @param val
   */
  public void setQueryLimit(final int val)  {
    setIntegerProperty(queryLimit, val);
  }

  /**
   *
   * @return int val
   */
  public int getQueryLimit()  {
    return getIntegerPropertyValue(queryLimit);
  }

  /**
   *
   * @param val
   */
  public void setAttrIds(final String val)  {
    setProperty(attrIds, val);
  }

  /**
   *
   * @return String val
   */
  public String getAttrIds()  {
    return getPropertyValue(attrIds);
  }

  /**
   *
   * @param val
   */
  public void setAddressbookObjectClass(final String val)  {
    setProperty(addressbookObjectClass, val);
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookObjectClass()  {
    return getPropertyValue(addressbookObjectClass);
  }

  /**
   *
   * @param val
   */
  public void setAddressbookEntryObjectClass(final String val)  {
    setProperty(addressbookEntryObjectClass, val);
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookEntryObjectClass()  {
    return getPropertyValue(addressbookEntryObjectClass);
  }

  /** Attribute we search for to get a principal
   *
   * @param val
   */
  public void setPrincipalIdAttr(final String val)  {
    setProperty(principalIdAttr, val);
  }

  /** Attribute we search for to get a group
   *
   * @return String val
   */
  public String getPrincipalIdAttr()  {
    return getPropertyValue(principalIdAttr);
  }

  /**
   *
   * @param val
   */
  public void setFolderObjectClass(final String val)  {
    setProperty(folderObjectClass, val);
  }

  /** The objectclass used to represent a folder or collection. usually
   * "organizationalUnit". Required.
   *
   * @return String val
   */
  public String getFolderObjectClass()  {
    return getPropertyValue(folderObjectClass);
  }

  /**
   *
   * @param val
   */
  public void setFolderIdAttr(final String val)  {
    setProperty(folderIdAttr, val);
  }

  /** The attribute used to designate the equivalent of a folder or collection,
   * usually "ou". Required.
   *
   * @return String val
   */
  public String getFolderIdAttr()  {
    return getPropertyValue(folderIdAttr);
  }

  /**
   *
   * @param val
   */
  public void setAddressbookIdAttr(final String val)  {
    setProperty(addressbookIdAttr, val);
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookIdAttr()  {
    return getPropertyValue(addressbookIdAttr);
  }

  /**
   *
   * @param val
   */
  public void setAddressbookEntryIdAttr(final String val)  {
    setProperty(addressbookEntryIdAttr, val);
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookEntryIdAttr()  {
    return getPropertyValue(addressbookEntryIdAttr);
  }

  /** Attribute we want back identifying a member
   *
   * @param val
   */
  public void setGroupMemberAttr(final String val)  {
    setProperty(groupMemberAttr, val);
  }

  /** Attribute we want back identifying a member
   *
   * @return String val
   */
  public String getGroupMemberAttr()  {
    return getPropertyValue(groupMemberAttr);
  }

  /** If we need an id to authenticate this is it.
   *
   * @param val
   */
  public void setAuthDn(final String val)  {
    setProperty(authDn, val);
  }

  /** If we need an id to authenticate this is it.
   *
   * @return String val
   */
  public String getAuthDn()  {
    return getPropertyValue(authDn);
  }

  /** If we need an id to authenticate this is the pw.
   *
   * @param val
   */
  public void setAuthPw(final String val)  {
    setProperty(authPw, val);
  }

  /** If we need an id to authenticate this is it.
   *
   * @return String val
   */
  public String getAuthPw()  {
    return getPropertyValue(authPw);
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
