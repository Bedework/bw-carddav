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

import org.bedework.carddav.util.CardDAVConfig;

import edu.rpi.cmt.config.ConfigurationFileStore;
import edu.rpi.cmt.config.ConfigurationType;
import edu.rpi.sss.util.OptionsI;

import java.util.ArrayList;
import java.util.List;

/**
 * @author douglm
 *
 */
public class CardDavContext extends ConfBase implements CardDavContextMBean {
  private String serviceName;

  private CardDAVConfig cfg;

  /**
   * @param serviceName
   * @param cfg
   * @param configName
   * @param configDir
   */
  public CardDavContext(final String serviceName,
                     final CardDAVConfig cfg,
                     final String configName,
                     final String configDir) {
    this.serviceName = serviceName;
    this.cfg = cfg;
    setConfigName(configName);
    setConfigDir(configDir);
  }

  @Override
  public ConfigurationType getConfigObject() {
    return getConf().getConfig();
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  public void setDefaultVcardVersion(final String val) {
    getConf().setDefaultVcardVersion(val);
  }

  public String getDefaultVcardVersion() {
    return getConf().getDefaultVcardVersion();
  }

  public void setSysintfImpl(final String val) {
    getConf().setSysintfImpl(val);
  }

  public String getSysintfImpl() {
    return getConf().getSysintfImpl();
  }

  public void setWebaddrServiceURI(final String val) {
    getConf().setWebaddrServiceURI(val);
  }

  public String getWebaddrServiceURI() {
    return getConf().getWebaddrServiceURI();
  }

  public void setWebaddrServicePropertiesList(final String val) {
    getConf().setWebaddrServicePropertiesList(val);
  }

  public String getWebaddrServicePropertiesList() {
    return getConf().getWebaddrServicePropertiesList();
  }

  public void setWebaddrPublicAddrbook(final String val) {
    getConf().setWebaddrPublicAddrbook(val);
  }

  public String getWebaddrPublicAddrbook() {
    return getConf().getWebaddrPublicAddrbook();
  }

  public void setDirectoryBrowsingDisallowed(final boolean val) {
    getConf().setDirectoryBrowsingDisallowed(val);
  }

  public boolean getDirectoryBrowsingDisallowed() {
    return getConf().getDirectoryBrowsingDisallowed();
  }

  public void setDefaultAddressbook(final String val) {
    getConf().setDefaultAddressbook(val);
  }

  public String getDefaultAddressbook() {
    return getConf().getDefaultAddressbook();
  }

  public void setAddressBookHandlerPrefix(final String val) {
    getConf().setAddressBookHandlerPrefix(val);
  }

  public String getAddressBookHandlerPrefix() {
    return getConf().getAddressBookHandlerPrefix();
  }

  public void setUserHomeRoot(final String val) {
    getConf().setUserHomeRoot(val);
  }

  public String getUserHomeRoot() {
    return getConf().getUserHomeRoot();
  }

  public void setPrincipalRoot(final String val) {
    getConf().setPrincipalRoot(val);
  }

  public String getPrincipalRoot() {
    return getConf().getPrincipalRoot();
  }

  public void setUserPrincipalRoot(final String val) {
    getConf().setUserPrincipalRoot(val);
  }

  public String getUserPrincipalRoot() {
    return getConf().getUserPrincipalRoot();
  }

  public void setGroupPrincipalRoot(final String val) {
    getConf().setGroupPrincipalRoot(val);
  }

  public String getGroupPrincipalRoot() {
    return getConf().getGroupPrincipalRoot();
  }

  public void setResourcePrincipalRoot(final String val) {
    getConf().setResourcePrincipalRoot(val);
  }

  public String getResourcePrincipalRoot() {
    return getConf().getResourcePrincipalRoot();
  }

  public void setVenuePrincipalRoot(final String val) {
    getConf().setVenuePrincipalRoot(val);
  }

  public String getVenuePrincipalRoot() {
    return getConf().getVenuePrincipalRoot();
  }

  public void setTicketPrincipalRoot(final String val) {
    getConf().setTicketPrincipalRoot(val);
  }

  public String getTicketPrincipalRoot() {
    return getConf().getTicketPrincipalRoot();
  }

  public void setHostPrincipalRoot(final String val) {
    getConf().setHostPrincipalRoot(val);
  }

  public String getHostPrincipalRoot() {
    return getConf().getHostPrincipalRoot();
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

  @Override
  public List<String> getUserInfo(final String cua) {
    return new ArrayList<String>();
  }

  /* ====================================================================
   *                   Non-mbean methods
   * ==================================================================== */

  /**
   * @return current state of config
   */
  public synchronized CardDAVConfig getConf() {
    try {
      if (cfg == null) {
        /* Try to load it */
        cfg = new CardDAVConfig();

        ConfigurationFileStore cfs = new ConfigurationFileStore(getConfigDir());

        ConfigurationType config = cfs.getConfig(getConfigName());

        if (config == null) {
          /* XXX For the time being try to load it from the options.
           * This is just to allow a migration from the old 3.8 system to the
           * later releases.
           */
          OptionsI opts = CardDavSvc.getOptions();
          cfg = (CardDAVConfig)opts.getAppProperty(getConfigName());

          /* Now save it for next time */
          //saveConfig(); - done at create
        } else {
          cfg.setConfig(config);
          cfg.setAppName(getConfigName());
        }
      }

      return cfg;
    } catch (Throwable t) {
      error(t);
      cfg = new CardDAVConfig();
      return cfg;
    }
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
