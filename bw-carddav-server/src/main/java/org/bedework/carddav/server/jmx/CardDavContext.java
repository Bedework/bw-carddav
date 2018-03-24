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

import org.bedework.carddav.server.config.CardDAVContextConfig;
import org.bedework.util.config.ConfigurationStore;
import org.bedework.util.jmx.ConfBase;

import java.util.ArrayList;
import java.util.List;

/**
 * @author douglm
 *
 */
public class CardDavContext extends ConfBase<CardDAVContextConfig> implements CardDavContextMBean {
  /**
   * @param configStore for configs
   * @param serviceName of this service
   * @param configName of the config
   */
  public CardDavContext(final ConfigurationStore configStore,
                        final String serviceName,
                        final String configName) {
    super(serviceName);
    setStore(configStore);

    setConfigName(configName);
  }

  @Override
  public String loadConfig() {
    return loadConfig(CardDAVContextConfig.class);
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  @Override
  public void setWebaddrServiceURI(final String val) {
    getConf().setWebaddrServiceURI(val);
  }

  @Override
  public String getWebaddrServiceURI() {
    return getConf().getWebaddrServiceURI();
  }

  @Override
  public void setWebaddrServicePropertiesList(final String val) {
    getConf().setWebaddrServicePropertiesList(val);
  }

  @Override
  public String getWebaddrServicePropertiesList() {
    return getConf().getWebaddrServicePropertiesList();
  }

  @Override
  public void setWebaddrPublicAddrbook(final String val) {
    getConf().setWebaddrPublicAddrbook(val);
  }

  @Override
  public String getWebaddrPublicAddrbook() {
    return getConf().getWebaddrPublicAddrbook();
  }

  @Override
  public void setDirectoryBrowsingDisallowed(final boolean val) {
    getConf().setDirectoryBrowsingDisallowed(val);
  }

  @Override
  public boolean getDirectoryBrowsingDisallowed() {
    return getConf().getDirectoryBrowsingDisallowed();
  }

  @Override
  public void setAddressBookHandlerPrefix(final String val) {
    getConf().setAddressBookHandlerPrefix(val);
  }

  @Override
  public String getAddressBookHandlerPrefix() {
    return getConf().getAddressBookHandlerPrefix();
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

  @Override
  public List<String> getUserInfo(final String cua) {
    return new ArrayList<>();
  }

  /* ====================================================================
   *                   Non-mbean methods
   * ==================================================================== */

  /**
   * @return current state of config
   */
  public synchronized CardDAVContextConfig getConf() {
    try {
      if (cfg != null) {
        return cfg;
      }

      cfg = getConfigInfo(getConfigName(), CardDAVContextConfig.class);

      return cfg;
    } catch (final Throwable t) {
      error(t);
      cfg = new CardDAVContextConfig();
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
