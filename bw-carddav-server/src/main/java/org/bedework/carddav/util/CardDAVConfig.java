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

import edu.rpi.sss.util.ToString;
import edu.rpi.sss.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

/** This class defines the various properties we need for a carddav server
 *
 * @author Mike Douglass
 */
public class CardDAVConfig extends ConfigBase<CardDAVConfig> {
  private final static QName confElement = new QName(ns, "bwcarddav");

  /* Default vcard version */
  private static final QName defaultVcardVersion = new QName(ns,
      "defaultVcardVersion");

  /* System interface implementation */
  private static final QName sysintfImpl = new QName(ns,
      "sysintfImpl");

  /* Web address service uri - null for no web address service */
  private static final QName webaddrServiceURI = new QName(ns,
      "webaddrServiceURI");

  private static final QName webaddrServicePropertiesList = new QName(ns,
      "webaddrServicePropertiesList");

  /* Path prefix for public searches */
  private static final QName webaddrPublicAddrbook = new QName(ns,
      "webaddrPublicAddrbook");

  private static final QName directoryBrowsingDisallowed = new QName(ns,
      "directoryBrowsingDisallowed");

  private static final QName defaultAddressbook = new QName(ns,
      "defaultAddressbook");

  private static final QName addressBookHandlerPrefix = new QName(ns,
      "addressBookHandlerPrefix");

  private static final QName userHomeRoot = new QName(ns,
      "userHomeRoot");

  private static final QName principalRoot = new QName(ns,
                                                       "principalRoot");

  private static final QName userPrincipalRoot = new QName(ns,
                                                           "userPrincipalRoot");

  private static final QName groupPrincipalRoot = new QName(ns,
                                                            "groupPrincipalRoot");

  private static final QName resourcePrincipalRoot = new QName(ns,
                                                               "resourcePrincipalRoot");

  private static final QName venuePrincipalRoot = new QName(ns,
                                                            "venuePrincipalRoot");

  private static final QName ticketPrincipalRoot = new QName(ns,
                                                             "ticketPrincipalRoot");

  private static final QName hostPrincipalRoot = new QName(ns,
                                                           "hostPrincipalRoot");

  private Set<DirHandlerConfig> handlerConfigs;

  @Override
  public QName getConfElement() {
    return confElement;
  }

  /** Set the default vcard version
   *
   * @param val    String
   */
  public void setDefaultVcardVersion(final String val) {
    setProperty(defaultVcardVersion, val);
  }

  /** get the default vcard version
   *
   * @return String
   */
  public String getDefaultVcardVersion() {
    return getPropertyValue(defaultVcardVersion);
  }

  /** Set the System interface implementation
   *
   * @param val    String
   */
  public void setSysintfImpl(final String val) {
    setProperty(sysintfImpl, val);
  }

  /** get the System interface implementation
   *
   * @return String
   */
  public String getSysintfImpl() {
    return getPropertyValue(sysintfImpl);
  }

  /** Set the web address service uri - null for no web address service
   *
   * @param val    String
   */
  public void setWebaddrServiceURI(final String val) {
    setProperty(webaddrServiceURI, val);
  }

  /** get the web address service uri - null for no web address service
   *
   * @return String
   */
  public String getWebaddrServiceURI() {
    return getPropertyValue(webaddrServiceURI);
  }

  /** Set the comma separated list of web addr book searchable properties
   *
   * @param val    String
   */
  public void setWebaddrServicePropertiesList(final String val) {
    setProperty(webaddrServicePropertiesList, val);
  }

  /**
   * @return comma separated list of web addr book searchable properties
   */
  public String getWebaddrServicePropertiesList() {
    return getPropertyValue(webaddrServicePropertiesList);
  }

  /**
   *
   * @param val    String
   */
  public void setWebaddrPublicAddrbook(final String val) {
    setProperty(webaddrPublicAddrbook, val);
  }

  /**
   *
   * @return String
   */
  public String getWebaddrPublicAddrbook() {
    return getPropertyValue(webaddrPublicAddrbook);
  }

  /**
   * @param val
   */
  public void setDirectoryBrowsingDisallowed(final boolean val) {
    setBooleanProperty(directoryBrowsingDisallowed, val);
  }

  /**
   * @return boolean
   */
  public boolean getDirectoryBrowsingDisallowed() {
    return getBooleanPropertyValue(directoryBrowsingDisallowed);
  }

  /** Set the default addressbook name
   *
   * @param val    String
   */
  public void setDefaultAddressbook(final String val) {
    setProperty(defaultAddressbook, val);
  }

  /** get the default addressbook name
   *
   * @return String
   */
  public String getDefaultAddressbook() {
    return getPropertyValue(defaultAddressbook);
  }

  /** Handler prefix for address books
   *
   * @param val    String
   */
  public void setAddressBookHandlerPrefix(final String val) {
    setProperty(addressBookHandlerPrefix, val);
  }

  /** Handler prefix for address books
   *
   * @return String
   */
  public String getAddressBookHandlerPrefix() {
    return getPropertyValue(addressBookHandlerPrefix);
  }

  /** Set the user home root e.g. "/user"
   *
   * @param val    String
   */
  public void setUserHomeRoot(final String val) {
    setProperty(userHomeRoot, val);
  }

  /** Set the user home root e.g. "/user"
   *
   * @return String
   */
  public String getUserHomeRoot() {
    return getPropertyValue(userHomeRoot);
  }

  /**
   * @return List derived from webaddrServicePropertiesList
   */
  public List<String> getWebaddrServiceProperties() {
    List<String> webaddrServiceProperties = new ArrayList<String>();

    for (String s: getWebaddrServicePropertiesList().split(",")) {
      webaddrServiceProperties.add(s.trim());
    }
    return webaddrServiceProperties;
  }

  /** Set the principal root e.g. "/principals"
   *
   * @param val    String
   */
  public void setPrincipalRoot(final String val) {
    setProperty(principalRoot, val);
  }

  /** get the principal root e.g. "/principals"
   *
   * @return String
   */
  public String getPrincipalRoot() {
    return getPropertyValue(principalRoot);
  }

  /** Set the user principal root e.g. "/principals/users"
   *
   * @param val    String
   */
  public void setUserPrincipalRoot(final String val) {
    setProperty(userPrincipalRoot, val);
  }

  /** get the principal root e.g. "/principals/users"
   *
   * @return String
   */
  public String getUserPrincipalRoot() {
    return getPropertyValue(userPrincipalRoot);
  }

  /** Set the group principal root e.g. "/principals/groups"
   *
   * @param val    String
   */
  public void setGroupPrincipalRoot(final String val) {
    setProperty(groupPrincipalRoot, val);
  }

  /** get the group principal root e.g. "/principals/groups"
   *
   * @return String
   */
  public String getGroupPrincipalRoot() {
    return getPropertyValue(groupPrincipalRoot);
  }

  /** Set the resource principal root e.g. "/principals/resources"
   *
   * @param val    String
   */
  public void setResourcePrincipalRoot(final String val) {
    setProperty(resourcePrincipalRoot, val);
  }

  /** get the resource principal root e.g. "/principals/resources"
   *
   * @return String
   */
  public String getResourcePrincipalRoot() {
    return getPropertyValue(resourcePrincipalRoot);
  }

  /** Set the venue principal root e.g. "/principals/locations"
   *
   * @param val    String
   */
  public void setVenuePrincipalRoot(final String val) {
    setProperty(venuePrincipalRoot, val);
  }

  /** get the venue principal root e.g. "/principals/locations"
   *
   * @return String
   */
  public String getVenuePrincipalRoot() {
    return getPropertyValue(venuePrincipalRoot);
  }

  /** Set the ticket principal root e.g. "/principals/tickets"
   *
   * @param val    String
   */
  public void setTicketPrincipalRoot(final String val) {
    setProperty(ticketPrincipalRoot, val);
  }

  /** get the ticket principal root e.g. "/principals/tickets"
   *
   * @return String
   */
  public String getTicketPrincipalRoot() {
    return getPropertyValue(ticketPrincipalRoot);
  }

  /** Set the host principal root e.g. "/principals/hosts"
   *
   * @param val    String
   */
  public void setHostPrincipalRoot(final String val) {
    setProperty(hostPrincipalRoot, val);
  }

  /** get the host principal root e.g. "/principals/hosts"
   *
   * @return String
   */
  public String getHostPrincipalRoot() {
    return getPropertyValue(hostPrincipalRoot);
  }

  /** Add our stuff to the StringBuilder
   *
   * @param sb    StringBuilder for result
   * @param indent
   */
  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("sysintf", getSysintfImpl());
  }

  /**
   * @return true if we already added the dir handler configs.
   */
  public boolean dirHandlersConfigured() {
    return !Util.isEmpty(handlerConfigs);
  }

  /**
   * @param dhc
   */
  public void addDirhandler(final DirHandlerConfig dhc) {
    if (handlerConfigs == null) {
      handlerConfigs = new TreeSet<DirHandlerConfig>();
    }

    handlerConfigs.add(dhc);
  }

  /** Find a directory handler for a given path. Each directory handler is
   * configured with a path prefix. It is require that there be a directory
   * handler to match the principalPath defined above.
   *
   * @param path
   * @return DirHandlerConfig or null
   */
  public DirHandlerConfig findDirhandler(final String path) {
    DirHandlerConfig conf = null;
    int matchLen = 0;

    if (handlerConfigs == null) {
      return null;
    }

    for (DirHandlerConfig c: handlerConfigs) {
      String prefix = c.getPathPrefix();
      int plen = prefix.length();

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
