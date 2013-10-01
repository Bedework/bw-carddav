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

import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.util.config.ConfigurationStore;
import org.bedework.util.jmx.ConfBase;

/**
 * @author douglm
 *
 */
public class DirHandlerConf extends ConfBase<DirHandlerConfig> implements DirHandlerConfMBean {
  /**
   * @param configStore
   * @param cfg
   * @param serviceName
   * @param configName
   */
  public void init(final ConfigurationStore configStore,
                   final DirHandlerConfig cfg,
                   final String serviceName,
                   final String configName) {
    setServiceName(serviceName);
    setStore(configStore);

    setConfigName(configName);

    this.cfg = cfg;
  }

  @Override
  public String loadConfig() {
    return loadConfig(DirHandlerConfig.class);
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  @Override
  public void setPathPrefix(final String val) {
    getConf().setPathPrefix(val);
  }

  @Override
  public String getPathPrefix() {
    return getConf().getPathPrefix();
  }

  @Override
  public void setCardPathPrefix(final String val) {
    getConf().setCardPathPrefix(val);
  }

  @Override
  public String getCardPathPrefix() {
    return getConf().getCardPathPrefix();
  }

  @Override
  public void setCardPathPrefixes(final String val) {
    getConf().setCardPathPrefixes(val);
  }

  @Override
  public String getCardPathPrefixes() {
    return getConf().getCardPathPrefixes();
  }

  @Override
  public void setAddressBook(final boolean val) {
    getConf().setAddressBook(val);
  }

  @Override
  public boolean getAddressBook() {
    return getConf().getAddressBook();
  }

  @Override
  public void setDirectory(final boolean val) {
    getConf().setDirectory(val);
  }

  @Override
  public boolean getDirectory() {
    return getConf().getDirectory();
  }

  @Override
  public void setClassName(final String val) {
    getConf().setClassName(val);
  }

  @Override
  public String getClassName() {
    return getConf().getClassName();
  }

  @Override
  public void setOwnerHref(final String val) {
    getConf().setOwnerHref(val);
  }

  @Override
  public String getOwnerHref() {
    return getConf().getOwnerHref();
  }

  @Override
  public void setCardKind(final String val) {
    getConf().setCardKind(val);
  }

  @Override
  public String getCardKind() {
    return getConf().getCardKind();
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
  public DirHandlerConfig getConf() {
    return cfg;
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
