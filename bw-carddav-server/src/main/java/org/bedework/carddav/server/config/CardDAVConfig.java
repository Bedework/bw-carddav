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

import org.bedework.carddav.common.config.CardDAVConfigI;
import org.bedework.carddav.common.config.DirHandlerConfig;
import org.bedework.util.config.ConfInfo;
import org.bedework.util.config.ConfigBase;
import org.bedework.util.misc.ToString;

import java.util.Set;
import java.util.TreeSet;

/** This class defines the various properties we need for a carddav server
 *
 * @author Mike Douglass
 */
@ConfInfo(elementName = "bwcarddav")
public class CardDAVConfig extends ConfigBase<CardDAVConfig>
        implements CardDAVConfigI {
  private String dataOut;

  /* Default vcard version */
  private String defaultVcardVersion;

  /* System interface implementation */
  private String sysintfImpl;

  private String defaultAddressbook;

  private String userHomeRoot;

  private String principalRoot;

  private String userPrincipalRoot;

  private String groupPrincipalRoot;

  private String resourcePrincipalRoot;

  private String venuePrincipalRoot;

  private String ticketPrincipalRoot;

  private String hostPrincipalRoot;

  private Set<CardDAVContextConfig> contextConfigs;

  private Set<DirHandlerConfig<?>> handlerConfigs;

  @Override
  public void setDataOut(final String val) {
    dataOut = val;
  }

  @Override
  public String getDataOut() {
    return dataOut;
  }

  @Override
  public void setDefaultVcardVersion(final String val) {
    defaultVcardVersion = val;
  }

  @Override
  public String getDefaultVcardVersion() {
    return defaultVcardVersion;
  }

  @Override
  public void setSysintfImpl(final String val) {
    sysintfImpl = val;
  }

  @Override
  public String getSysintfImpl() {
    return sysintfImpl;
  }

  @Override
  public void setDefaultAddressbook(final String val) {
    defaultAddressbook = val;
  }

  @Override
  public String getDefaultAddressbook() {
    return defaultAddressbook;
  }

  @Override
  public void setUserHomeRoot(final String val) {
    userHomeRoot = val;
  }

  @Override
  public String getUserHomeRoot() {
    return userHomeRoot;
  }

  @Override
  public void setPrincipalRoot(final String val) {
    principalRoot = val;
  }

  @Override
  public String getPrincipalRoot() {
    return principalRoot;
  }

  @Override
  public void setUserPrincipalRoot(final String val) {
    userPrincipalRoot = val;
  }

  @Override
  public String getUserPrincipalRoot() {
    return userPrincipalRoot;
  }

  @Override
  public void setGroupPrincipalRoot(final String val) {
    groupPrincipalRoot = val;
  }

  @Override
  public String getGroupPrincipalRoot() {
    return groupPrincipalRoot;
  }

  @Override
  public void setResourcePrincipalRoot(final String val) {
    resourcePrincipalRoot = val;
  }

  @Override
  public String getResourcePrincipalRoot() {
    return resourcePrincipalRoot;
  }

  @Override
  public void setVenuePrincipalRoot(final String val) {
    venuePrincipalRoot = val;
  }

  @Override
  public String getVenuePrincipalRoot() {
    return venuePrincipalRoot;
  }

  @Override
  public void setTicketPrincipalRoot(final String val) {
    ticketPrincipalRoot = val;
  }

  @Override
  public String getTicketPrincipalRoot() {
    return ticketPrincipalRoot;
  }

  @Override
  public void setHostPrincipalRoot(final String val) {
    hostPrincipalRoot = val;
  }

  @Override
  public String getHostPrincipalRoot() {
    return hostPrincipalRoot;
  }

  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("sysintf", getSysintfImpl());
  }

  /**
   * @return context config set
   */
  public Set<CardDAVContextConfig> getContextConfigs() {
    return contextConfigs;
  }

  /**
   * @param cc new config
   */
  public void addContext(final CardDAVContextConfig cc) {
    if (contextConfigs == null) {
      contextConfigs = new TreeSet<>();
    }

    contextConfigs.add(cc);
  }

  /**
   * @return config set
   */
  public Set<DirHandlerConfig<?>> getDirHandlerConfigs() {
    return handlerConfigs;
  }

  /**
   * @param dhc new config
   */
  public void addDirhandler(final DirHandlerConfig<?> dhc) {
    if (handlerConfigs == null) {
      handlerConfigs = new TreeSet<>();
    }

    handlerConfigs.add(dhc);
  }

  /** Find a directory handler for a given path. Each directory handler is
   * configured with a path prefix. It is require that there be a directory
   * handler to match the principalPath defined above.
   *
   * @param path prefix
   * @return DirHandlerConfig or null
   */
  public DirHandlerConfig<?> findDirhandler(final String path) {
    DirHandlerConfig<?> conf = null;
    int matchLen = 0;

    if (handlerConfigs == null) {
      return null;
    }

    for (final DirHandlerConfig<?> c: handlerConfigs) {
      final String prefix = c.getPathPrefix();
      final int plen = prefix.length();

      if ((plen < matchLen) || !path.startsWith(prefix)) {
        continue;
      }

      conf = c;
      matchLen = plen;

      if (plen == path.length()) {
        // Can't get better
        break;
      }
    }

    return conf;
  }

  /** Find a directory handler for a given principal href.
   *
   * @param principalHref the href
   * @return DirHandlerConfig or null
   */
  public DirHandlerConfig<?> findPrincipalDirhandler(final String principalHref) {
    if (handlerConfigs == null) {
      return null;
    }

    for (final DirHandlerConfig<?> c: handlerConfigs) {
      final String prefix = c.getPrincipalPrefix();

      if (prefix == null) {
        continue;
      }

      if (!principalHref.startsWith(prefix)) {
        continue;
      }

      return c;
    }

    return null;
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */
}
