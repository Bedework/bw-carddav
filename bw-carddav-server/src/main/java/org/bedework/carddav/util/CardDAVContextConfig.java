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

import org.bedework.util.config.ConfInfo;
import org.bedework.util.config.ConfigBase;
import org.bedework.util.misc.ToString;
import org.bedework.util.misc.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** This class defines the various properties we need for a carddav server
 *
 * @author Mike Douglass
 */
@ConfInfo(elementName = "bwcarddav")
public class CardDAVContextConfig extends ConfigBase<CardDAVContextConfig> {
  private String dataOut;

  /* Default vcard version */
  private String defaultVcardVersion;

  /* System interface implementation */
  private String sysintfImpl;

  /* Web address service uri - null for no web address service */
  private String webaddrServiceURI;

  private String webaddrServicePropertiesList;

  /* Path prefix for public searches */
  private String webaddrPublicAddrbook;

  private boolean directoryBrowsingDisallowed;

  private String defaultAddressbook;

  private String addressBookHandlerPrefix;

  private String userHomeRoot;

  private String principalRoot;

  private String userPrincipalRoot;

  private String groupPrincipalRoot;

  private String resourcePrincipalRoot;

  private String venuePrincipalRoot;

  private String ticketPrincipalRoot;

  private String hostPrincipalRoot;

  private Set<DirHandlerConfig> handlerConfigs;

  /** data output directory name - full path. Used for data export
   *
   * @param val path
   */
  public void setDataOut(final String val) {
    dataOut = val;
  }

  /**
   * @return data output full path
   */
  public String getDataOut() {
    return dataOut;
  }

  /** Set the default vcard version
   *
   * @param val    String
   */
  public void setDefaultVcardVersion(final String val) {
    defaultVcardVersion = val;
  }

  /** get the default vcard version
   *
   * @return String
   */
  public String getDefaultVcardVersion() {
    return defaultVcardVersion;
  }

  /** Set the System interface implementation
   *
   * @param val    String
   */
  public void setSysintfImpl(final String val) {
    sysintfImpl = val;
  }

  /** get the System interface implementation
   *
   * @return String
   */
  public String getSysintfImpl() {
    return sysintfImpl;
  }

  /** Set the web address service uri - null for no web address service
   *
   * @param val    String
   */
  public void setWebaddrServiceURI(final String val) {
    webaddrServiceURI = val;
  }

  /** get the web address service uri - null for no web address service
   *
   * @return String
   */
  public String getWebaddrServiceURI() {
    return webaddrServiceURI;
  }

  /** Set the comma separated list of web addr book searchable properties
   *
   * @param val    String
   */
  public void setWebaddrServicePropertiesList(final String val) {
    webaddrServicePropertiesList = val;
  }

  /**
   * @return comma separated list of web addr book searchable properties
   */
  public String getWebaddrServicePropertiesList() {
    return webaddrServicePropertiesList;
  }

  /**
   *
   * @param val    String
   */
  public void setWebaddrPublicAddrbook(final String val) {
    webaddrPublicAddrbook = val;
  }

  /**
   *
   * @return String
   */
  public String getWebaddrPublicAddrbook() {
    return webaddrPublicAddrbook;
  }

  /**
   * @param val true to disallow browsing
   */
  public void setDirectoryBrowsingDisallowed(final boolean val) {
    directoryBrowsingDisallowed = val;
  }

  /**
   * @return boolean
   */
  public boolean getDirectoryBrowsingDisallowed() {
    return directoryBrowsingDisallowed;
  }

  /** Set the default addressbook name
   *
   * @param val    String
   */
  public void setDefaultAddressbook(final String val) {
    defaultAddressbook = val;
  }

  /** get the default addressbook name
   *
   * @return String
   */
  public String getDefaultAddressbook() {
    return defaultAddressbook;
  }

  /** Handler prefix for address books
   *
   * @param val    String
   */
  public void setAddressBookHandlerPrefix(final String val) {
    addressBookHandlerPrefix = val;
  }

  /** Handler prefix for address books
   *
   * @return String
   */
  public String getAddressBookHandlerPrefix() {
    return addressBookHandlerPrefix;
  }

  /** Set the user home root e.g. "/user"
   *
   * @param val    String
   */
  public void setUserHomeRoot(final String val) {
    userHomeRoot = val;
  }

  /** Set the user home root e.g. "/user"
   *
   * @return String
   */
  public String getUserHomeRoot() {
    return userHomeRoot;
  }

  /**
   * @return List derived from webaddrServicePropertiesList
   */
  @ConfInfo(dontSave = true)
  public List<String> getWebaddrServiceProperties() {
    final List<String> webaddrServiceProperties = new ArrayList<String>();

    for (final String s: getWebaddrServicePropertiesList().split(",")) {
      webaddrServiceProperties.add(s.trim());
    }
    return webaddrServiceProperties;
  }

  /** Set the principal root e.g. "/principals"
   *
   * @param val    String
   */
  public void setPrincipalRoot(final String val) {
    principalRoot = val;
  }

  /** get the principal root e.g. "/principals"
   *
   * @return String
   */
  public String getPrincipalRoot() {
    return principalRoot;
  }

  /** Set the user principal root e.g. "/principals/users"
   *
   * @param val    String
   */
  public void setUserPrincipalRoot(final String val) {
    userPrincipalRoot = val;
  }

  /** get the principal root e.g. "/principals/users"
   *
   * @return String
   */
  public String getUserPrincipalRoot() {
    return userPrincipalRoot;
  }

  /** Set the group principal root e.g. "/principals/groups"
   *
   * @param val    String
   */
  public void setGroupPrincipalRoot(final String val) {
    groupPrincipalRoot = val;
  }

  /** get the group principal root e.g. "/principals/groups"
   *
   * @return String
   */
  public String getGroupPrincipalRoot() {
    return groupPrincipalRoot;
  }

  /** Set the resource principal root e.g. "/principals/resources"
   *
   * @param val    String
   */
  public void setResourcePrincipalRoot(final String val) {
    resourcePrincipalRoot = val;
  }

  /** get the resource principal root e.g. "/principals/resources"
   *
   * @return String
   */
  public String getResourcePrincipalRoot() {
    return resourcePrincipalRoot;
  }

  /** Set the venue principal root e.g. "/principals/locations"
   *
   * @param val    String
   */
  public void setVenuePrincipalRoot(final String val) {
    venuePrincipalRoot = val;
  }

  /** get the venue principal root e.g. "/principals/locations"
   *
   * @return String
   */
  public String getVenuePrincipalRoot() {
    return venuePrincipalRoot;
  }

  /** Set the ticket principal root e.g. "/principals/tickets"
   *
   * @param val    String
   */
  public void setTicketPrincipalRoot(final String val) {
    ticketPrincipalRoot = val;
  }

  /** get the ticket principal root e.g. "/principals/tickets"
   *
   * @return String
   */
  public String getTicketPrincipalRoot() {
    return ticketPrincipalRoot;
  }

  /** Set the host principal root e.g. "/principals/hosts"
   *
   * @param val    String
   */
  public void setHostPrincipalRoot(final String val) {
    hostPrincipalRoot = val;
  }

  /** get the host principal root e.g. "/principals/hosts"
   *
   * @return String
   */
  public String getHostPrincipalRoot() {
    return hostPrincipalRoot;
  }

  /** Add our stuff to the StringBuilder
   *
   * @param ts    ToString for result
   */
  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("sysintf", getSysintfImpl());
  }

  /**
   * @return config set
   */
  public Set<DirHandlerConfig> getDirHandlerConfigs() {
    return handlerConfigs;
  }

  /**
   * @param dhc new config
   */
  public void addDirhandler(final DirHandlerConfig dhc) {
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
  public DirHandlerConfig findDirhandler(final String path) {
    DirHandlerConfig conf = null;
    int matchLen = 0;

    if (handlerConfigs == null) {
      return null;
    }

    for (final DirHandlerConfig c: handlerConfigs) {
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

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */
}
