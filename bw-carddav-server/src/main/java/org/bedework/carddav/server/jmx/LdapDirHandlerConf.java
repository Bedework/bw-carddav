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

import org.bedework.carddav.util.LdapDirHandlerConfig;

/**
 * @author douglm
 *
 */
public class LdapDirHandlerConf extends DirHandlerConf implements LdapDirHandlerConfMBean {
  /* ========================================================================
   * Attributes
   * ======================================================================== */

  public void setInitialContextFactory(final String val)  {
    getConf().setInitialContextFactory(val);
  }

  public String getInitialContextFactory()  {
    return getConf().getInitialContextFactory();
  }

  public void setSecurityAuthentication(final String val)  {
    getConf().setSecurityAuthentication(val);
  }

  public String getSecurityAuthentication()  {
    return getConf().getSecurityAuthentication();
  }

  public void setSecurityProtocol(final String val)  {
    getConf().setSecurityProtocol(val);
  }

  public String getSecurityProtocol()  {
    return getConf().getSecurityProtocol();
  }

  public void setProviderUrl(final String val)  {
    getConf().setProviderUrl(val);
  }

  public String getProviderUrl()  {
    return getConf().getProviderUrl();
  }

  public void setBaseDn(final String val)  {
    getConf().setBaseDn(val);
  }

  public String getBaseDn()  {
    return getConf().getBaseDn();
  }

  public void setQueryLimit(final int val)  {
    getConf().setQueryLimit(val);
  }

  public int getQueryLimit()  {
    return getConf().getQueryLimit();
  }

  public void setAttrIds(final String val)  {
    getConf().setAttrIds(val);
  }

  public String getAttrIds()  {
    return getConf().getAttrIds();
  }

  public void setFolderObjectClass(final String val)  {
    getConf().setFolderObjectClass(val);
  }

  public String getFolderObjectClass()  {
    return getConf().getFolderObjectClass();
  }

  public void setAddressbookObjectClass(final String val)  {
    getConf().setAddressbookObjectClass(val);
  }

  public String getAddressbookObjectClass()  {
    return getConf().getAddressbookObjectClass();
  }

  public void setAddressbookEntryObjectClass(final String val)  {
    getConf().setAddressbookEntryObjectClass(val);
  }

  public String getAddressbookEntryObjectClass()  {
    return getConf().getAddressbookEntryObjectClass();
  }

  public void setPrincipalIdAttr(final String val)  {
    getConf().setPrincipalIdAttr(val);
  }

  public String getPrincipalIdAttr()  {
    return getConf().getPrincipalIdAttr();
  }

  public void setFolderIdAttr(final String val)  {
    getConf().setFolderIdAttr(val);
  }

  public String getFolderIdAttr()  {
    return getConf().getFolderIdAttr();
  }

  public void setAddressbookIdAttr(final String val)  {
    getConf().setAddressbookIdAttr(val);
  }

  public String getAddressbookIdAttr()  {
    return getConf().getAddressbookIdAttr();
  }

  public void setAddressbookEntryIdAttr(final String val)  {
    getConf().setAddressbookEntryIdAttr(val);
  }

  public String getAddressbookEntryIdAttr()  {
    return getConf().getAddressbookEntryIdAttr();
  }

  public void setGroupMemberAttr(final String val)  {
    getConf().setGroupMemberAttr(val);
  }

  public String getGroupMemberAttr()  {
    return getConf().getGroupMemberAttr();
  }

  public void setAuthDn(final String val)  {
    getConf().setAuthDn(val);
  }

  public String getAuthDn()  {
    return getConf().getAuthDn();
  }

  public void setAuthPw(final String val)  {
    getConf().setAuthPw(val);
  }

  public String getAuthPw()  {
    return getConf().getAuthPw();
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /*
  @Override
  public List<String> getUserInfo(final String cua) {
    return new ArrayList<String>();
  }
  */

  /* ====================================================================
   *                   Non-mbean methods
   * ==================================================================== */

  /**
   * @return current state of config
   */
  @Override
  public LdapDirHandlerConfig getConf() {
    return (LdapDirHandlerConfig)super.getConf();
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  /* ========================================================================
   * Lifecycle
   * ======================================================================== */

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */
}
