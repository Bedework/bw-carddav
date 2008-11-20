/* **********************************************************************
    Copyright 2007 Rensselaer Polytechnic Institute. All worldwide rights reserved.

    Redistribution and use of this distribution in source and binary forms,
    with or without modification, are permitted provided that:
       The above copyright notice and this permission notice appear in all
        copies and supporting documentation;

        The name, identifiers, and trademarks of Rensselaer Polytechnic
        Institute are not used in advertising or publicity without the
        express prior written permission of Rensselaer Polytechnic Institute;

    DISCLAIMER: The software is distributed" AS IS" without any express or
    implied warranty, including but not limited to, any implied warranties
    of merchantability or fitness for a particular purpose or any warrant)'
    of non-infringement of any current or pending patent rights. The authors
    of the software make no representations about the suitability of this
    software for any particular purpose. The entire risk as to the quality
    and performance of the software is with the user. Should the software
    prove defective, the user assumes the cost of all necessary servicing,
    repair or correction. In particular, neither Rensselaer Polytechnic
    Institute, nor the authors of the software are liable for any indirect,
    special, consequential, or incidental damages related to the software,
    to the maximum extent the law permits.
*/
package org.bedework.carddav.util;

import java.util.ArrayList;
import java.util.Collection;


/** This class defines the various properties we need for a carddav server
 *
 * @author Mike Douglass
 */
public class CardDAVConfig /*extends ConfigCommon */{
  /* System interface implementation */
  private String sysintfImpl;

  /* Web address service uri - null for no web address service */
  private String webaddrServiceURI;

  /* Path prefix for public searches */
  private String webaddrPublicAddrbook;

  private boolean directoryBrowsingDisallowed;

  private String defaultAddressbook;

  /* Principals */
  private String principalRoot;
  private String userPrincipalRoot;
  private String groupPrincipalRoot;
  private String resourcePrincipalRoot;
  private String venuePrincipalRoot;
  private String ticketPrincipalRoot;
  private String hostPrincipalRoot;

  private Collection<DirHandlerConfig> handlerConfigs;

  /** Set the System interface implementation
   *
   * @param val    String
   */
  public void setSysintfImpl(String val) {
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
  public void setWebaddrServiceURI(String val) {
    webaddrServiceURI = val;
  }

  /** get the web address service uri - null for no web address service
   *
   * @return String
   */
  public String getWebaddrServiceURI() {
    return webaddrServiceURI;
  }

  /**
   *
   * @param val    String
   */
  public void setWebaddrPublicAddrbook(String val) {
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
   * @param val
   */
  public void setDirectoryBrowsingDisallowed(boolean val) {
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
  public void setDefaultAddressbook(String val) {
    defaultAddressbook = val;
  }

  /** get the default addressbook name
   *
   * @return String
   */
  public String getDefaultAddressbook() {
    return defaultAddressbook;
  }

  /** Set the principal root e.g. "/principals"
   *
   * @param val    String
   */
  public void setPrincipalRoot(String val) {
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
  public void setUserPrincipalRoot(String val) {
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
  public void setGroupPrincipalRoot(String val) {
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
  public void setResourcePrincipalRoot(String val) {
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
  public void setVenuePrincipalRoot(String val) {
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
  public void setTicketPrincipalRoot(String val) {
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
  public void setHostPrincipalRoot(String val) {
    hostPrincipalRoot = val;
  }

  /** get the host principal root e.g. "/principals/hosts"
   *
   * @return String
   */
  public String getHostPrincipalRoot() {
    return hostPrincipalRoot;
  }

  /**
   * @param dhc
   */
  public void addDirhandler(DirHandlerConfig dhc) {
    if (handlerConfigs == null) {
      handlerConfigs = new ArrayList<DirHandlerConfig>();
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
  public DirHandlerConfig findDirhandler(String path) {
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
}
