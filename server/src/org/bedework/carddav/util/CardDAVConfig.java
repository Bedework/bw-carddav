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

import edu.rpi.sss.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/** This class defines the various properties we need for a carddav server
 *
 * @author Mike Douglass
 */
public class CardDAVConfig extends DbItem<CardDAVConfig> {
  private String appName;

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

  private Set<CarddavProperty> properties;

  private Set<DirHandlerConfig> handlerConfigs;

  /* ---------------------------- Non db fields ----------------------------- */

  /* Principals - all saved as properties */
  private static final String principalRootPname = "principalRoot";

  private static final String userPrincipalRootPname = "userPrincipalRoot";

  private static final String groupPrincipalRootPname = "groupPrincipalRoot";

  private static final String resourcePrincipalRootPname = "resourcePrincipalRoot";

  private static final String venuePrincipalRootPname = "venuePrincipalRoot";

  private static final String ticketPrincipalRootPname = "ticketPrincipalRoot";

  private static final String hostPrincipalRootPname = "hostPrincipalRoot";

  /* For bedework build */
  private String appType;

  private boolean guestMode;

  private List<String> webaddrServiceProperties;

  /**
   * @param val
   */
  public void setAppName(final String val) {
    appName = val;
  }

  /**
   * @return String
   */
  public String getAppName() {
    return appName;
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

    webaddrServiceProperties = new ArrayList<String>();

    for (String s: val.split(",")) {
      webaddrServiceProperties.add(s.trim());
    }
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
   * @param val
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

  /**
   * @param val
   */
  public void setAppType(final String val) {
    appType = val;
  }

  /**
   * @return String
   */
  public String getAppType() {
    return appType;
  }

  /** True for a guest mode (non-auth) client.
   *
   * @param val
   */
  public void setGuestMode(final boolean val) {
    guestMode = val;
  }

  /**
   * @return boolean
   */
  public boolean getGuestMode() {
    return guestMode;
  }

  /**
   * @return List derived from webaddrServicePropertiesList
   */
  public List<String> getWebaddrServiceProperties() {
    return webaddrServiceProperties;
  }

  /* ====================================================================
   *                   Property methods
   * ==================================================================== */

  /**
   * @param val
   */
  public void setProperties(final Set<CarddavProperty> val) {
    properties = val;
  }

  /**
   * @return properties
   */
  public Set<CarddavProperty> getProperties() {
    return properties;
  }

  /**
   * @param name
   * @return properties with given name
   */
  public Set<CarddavProperty> getProperties(final String name) {
    TreeSet<CarddavProperty> ps = new TreeSet<CarddavProperty>();

    if (getNumProperties() == 0) {
      return ps;
    }

    for (CarddavProperty p: getProperties()) {
      if (p.getName().equals(name)) {
        ps.add(p);
      }
    }

    return ps;
  }

  /** Remove all with given name
   *
   * @param name
   */
  public void removeProperties(final String name) {
    Set<CarddavProperty> ps = getProperties(name);

    if ((ps == null) || (ps.size() == 0)) {
      return;
    }

    for (CarddavProperty p: ps) {
      removeProperty(p);
    }
  }

  /**
   * @return int
   */
  public int getNumProperties() {
    Collection<CarddavProperty> c = getProperties();
    if (c == null) {
      return 0;
    }

    return c.size();
  }

  /**
   * @param name
   * @return property or null
   */
  public CarddavProperty findProperty(final String name) {
    Collection<CarddavProperty> props = getProperties();

    if (props == null) {
      return null;
    }

    for (CarddavProperty prop: props) {
      if (name.equals(prop.getName())) {
        return prop;
      }
    }

    return null;
  }

  /** Set the single valued property
  *
  * @param name
  * @param value
  */
  public void setProperty(final String name,
                          final String value) {
    Set<CarddavProperty> ps = getProperties(name);

    if (ps.size() == 0) {
      addProperty(new CarddavProperty(name, value));
      return;
    }

    if (ps.size() > 1) {
      throw new RuntimeException("Multiple values for single valued property " + name);
    }

    CarddavProperty p = ps.iterator().next();

    if (!p.getValue().equals(value)) {
      p.setValue(value);
    }
  }

  /**
   * @param name
   * @return single value of valued property with given name
   */
  public String getPropertyValue(final String name) {
    Set<CarddavProperty> ps = getProperties(name);

    if (ps.size() == 0) {
      return null;
    }

    if (ps.size() > 1) {
      throw new RuntimeException("Multiple values for single valued property " + name);
    }

    return ps.iterator().next().getValue();
  }

  /**
   * @param name
   * @return single value of valued property with given name
   */
  public Integer getIntPropertyValue(final String name) {
    String s = getPropertyValue(name);

    if (s == null) {
      return null;
    }

    return Integer.valueOf(s);
  }

  /**
   * @param name
   * @return single value of valued property with given name
   */
  public Long getLongPropertyValue(final String name) {
    String s = getPropertyValue(name);

    if (s == null) {
      return null;
    }

    return Long.valueOf(s);
  }

  /**
   * @param val
   */
  public void addProperty(final CarddavProperty val) {
    Set<CarddavProperty> c = getProperties();
    if (c == null) {
      c = new TreeSet<CarddavProperty>();
      setProperties(c);
    }

    if (!c.contains(val)) {
      c.add(val);
    }
  }

  /**
   * @param val
   * @return boolean
   */
  public boolean removeProperty(final CarddavProperty val) {
    Set<CarddavProperty> c = getProperties();
    if (c == null) {
      return false;
    }

    return c.remove(val);
  }

  /**
   * @return set of CarddavProperty
   */
  public Set<CarddavProperty> copyProperties() {
    if (getNumProperties() == 0) {
      return null;
    }
    TreeSet<CarddavProperty> ts = new TreeSet<CarddavProperty>();

    for (CarddavProperty p: getProperties()) {
      ts.add(p);
    }

    return ts;
  }

  /**
   * @return set of CarddavProperty
   */
  public Set<CarddavProperty> cloneProperties() {
    if (getNumProperties() == 0) {
      return null;
    }
    TreeSet<CarddavProperty> ts = new TreeSet<CarddavProperty>();

    for (CarddavProperty p: getProperties()) {
      ts.add((CarddavProperty)p.clone());
    }

    return ts;
  }

  /** Set the principal root e.g. "/principals"
   *
   * @param val    String
   */
  public void setPrincipalRoot(final String val) {
    setProperty(principalRootPname, val);
  }

  /** get the principal root e.g. "/principals"
   *
   * @return String
   */
  public String getPrincipalRoot() {
    return getPropertyValue(principalRootPname);
  }

  /** Set the user principal root e.g. "/principals/users"
   *
   * @param val    String
   */
  public void setUserPrincipalRoot(final String val) {
    setProperty(userPrincipalRootPname, val);
  }

  /** get the principal root e.g. "/principals/users"
   *
   * @return String
   */
  public String getUserPrincipalRoot() {
    return getPropertyValue(userPrincipalRootPname);
  }

  /** Set the group principal root e.g. "/principals/groups"
   *
   * @param val    String
   */
  public void setGroupPrincipalRoot(final String val) {
    setProperty(groupPrincipalRootPname, val);
  }

  /** get the group principal root e.g. "/principals/groups"
   *
   * @return String
   */
  public String getGroupPrincipalRoot() {
    return getPropertyValue(groupPrincipalRootPname);
  }

  /** Set the resource principal root e.g. "/principals/resources"
   *
   * @param val    String
   */
  public void setResourcePrincipalRoot(final String val) {
    setProperty(resourcePrincipalRootPname, val);
  }

  /** get the resource principal root e.g. "/principals/resources"
   *
   * @return String
   */
  public String getResourcePrincipalRoot() {
    return getPropertyValue(resourcePrincipalRootPname);
  }

  /** Set the venue principal root e.g. "/principals/locations"
   *
   * @param val    String
   */
  public void setVenuePrincipalRoot(final String val) {
    setProperty(venuePrincipalRootPname, val);
  }

  /** get the venue principal root e.g. "/principals/locations"
   *
   * @return String
   */
  public String getVenuePrincipalRoot() {
    return getPropertyValue(venuePrincipalRootPname);
  }

  /** Set the ticket principal root e.g. "/principals/tickets"
   *
   * @param val    String
   */
  public void setTicketPrincipalRoot(final String val) {
    setProperty(ticketPrincipalRootPname, val);
  }

  /** get the ticket principal root e.g. "/principals/tickets"
   *
   * @return String
   */
  public String getTicketPrincipalRoot() {
    return getPropertyValue(ticketPrincipalRootPname);
  }

  /** Set the host principal root e.g. "/principals/hosts"
   *
   * @param val    String
   */
  public void setHostPrincipalRoot(final String val) {
    setProperty(hostPrincipalRootPname, val);
  }

  /** get the host principal root e.g. "/principals/hosts"
   *
   * @return String
   */
  public String getHostPrincipalRoot() {
    return getPropertyValue(hostPrincipalRootPname);
  }

  /** Add our stuff to the StringBuilder
   *
   * @param sb    StringBuilder for result
   * @param indent
   */
  public void toStringSegment(final StringBuilder sb,
                                 final String indent) {
    sb.append("appName = ");
    sb.append(getAppName());

    sb.append(", sysintf = ");
    sb.append(getSysintfImpl());
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public int compareTo(final CardDAVConfig that) {
    return getAppName().compareTo(that.getAppName());
  }

  @Override
  public int hashCode() {
    return getAppName().hashCode();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getSimpleName()).append("{");

    toStringSegment(sb, "  ");

    sb.append("}");
    return sb.toString();
  }
}
