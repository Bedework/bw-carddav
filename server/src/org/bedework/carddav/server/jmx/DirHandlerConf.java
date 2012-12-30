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

import edu.rpi.cmt.config.ConfigurationType;

/**
 * @author douglm
 *
 */
public class DirHandlerConf extends ConfBase implements DirHandlerConfMBean {
  private String serviceName;

  private DirHandlerConfig cfg;

  /**
   * @param serviceName
   * @param cfg
   * @param configName
   * @param configDir
   */
  public void init(final String serviceName,
                   final DirHandlerConfig cfg,
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

  @Override
  public String getName() {
    return serviceName;
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  public void setPathPrefix(final String val) {
    getConf().setPathPrefix(val);
  }

  public String getPathPrefix() {
    return getConf().getPathPrefix();
  }

  public void setCardPathPrefix(final String val) {
    getConf().setCardPathPrefix(val);
  }

  public String getCardPathPrefix() {
    return getConf().getCardPathPrefix();
  }

  public void setCardPathPrefixes(final String val) {
    getConf().setCardPathPrefixes(val);
  }

  public String getCardPathPrefixes() {
    return getConf().getCardPathPrefixes();
  }

  public void setAddressBook(final boolean val) {
    getConf().setAddressBook(val);
  }

  public boolean getAddressBook() {
    return getConf().getAddressBook();
  }

  public void setClassName(final String val) {
    getConf().setClassName(val);
  }

  public String getClassName() {
    return getConf().getClassName();
  }

  public void setOwnerHref(final String val) {
    getConf().setOwnerHref(val);
  }

  public String getOwnerHref() {
    return getConf().getOwnerHref();
  }

  public void setCardKind(final String val) {
    getConf().setCardKind(val);
  }

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
