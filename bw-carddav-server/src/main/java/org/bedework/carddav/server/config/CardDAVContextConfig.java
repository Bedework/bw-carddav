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

import org.bedework.util.config.ConfInfo;
import org.bedework.util.config.ConfigBase;
import org.bedework.util.misc.ToString;

import java.util.ArrayList;
import java.util.List;

/** This class defines the various properties we need for a carddav server
 *
 * @author Mike Douglass
 */
@ConfInfo(elementName = "bwcarddav")
public class CardDAVContextConfig extends ConfigBase<CardDAVContextConfig> {
  private boolean directoryBrowsingDisallowed;

  private String addressBookHandlerPrefix;

  /* Web address service uri - null for no web address service */
  private String webaddrServiceURI;

  private String webaddrServicePropertiesList;

  /* Path prefix for public searches */
  private String webaddrPublicAddrbook;

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

  /** Add our stuff to the StringBuilder
   *
   * @param ts    ToString for result
   */
  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("directoryBrowsingDisallowed", getDirectoryBrowsingDisallowed());
    ts.append("addressBookHandlerPrefix", getAddressBookHandlerPrefix());
    ts.append("webaddrServiceURI", getWebaddrServiceURI());
    ts.append("webaddrServicePropertiesList", getWebaddrServicePropertiesList());
    ts.append("webaddrPublicAddrbook", getWebaddrPublicAddrbook());
  }

  /* ====================================================================
   *                   Non-attribute methods
   * ==================================================================== */

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

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */
}
