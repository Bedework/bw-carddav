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
package org.bedework.carddav.common.config;

import org.bedework.util.config.ConfInfo;
import org.bedework.util.config.OrmConfigBase;
import org.bedework.base.ToString;

/** Class defining the configuration for a directory handler.
 *
 * @author douglm
 * @param <T>
 */
@ConfInfo(elementName = "bwcarddav-dirhandler")
public abstract class DirHandlerConfig<T extends DirHandlerConfig<?>>
        extends OrmConfigBase<T> {
  private String confClass;

  private String confBeanClass;

  private String pathPrefix;

  private String principalPrefix;

  private String cardPathPrefix;

  private String cardPathPrefixes;

  private boolean addressBook;

  private boolean directory;

  private String className;

  private String ownerHref;

  private String cardKind;

  /** set the class for the conf wrapper object class
   *
   * @param val the class for the conf wrapper object class
   */
  public void setConfClass(final String val) {
    confClass = val;
  }

  /** Get the class for the conf wrapper object class
   *
   * @return String   className
   */
  public String getConfClass() {
    return confClass;
  }

  /** set the class for the jmx conf bean
   *
   * @param val the class for the jmx conf bean
   */
  public void setConfBeanClass(final String val) {
    confBeanClass = val;
  }

  /** Get the class for the jmx conf bean
   *
   * @return String   path
   */
  public String getConfBeanClass() {
    return confBeanClass;
  }

  /** Set the pathPrefix which defines the paths for which we call this handler.
   *
   * <p>For example we might put the enterprise directory on a path like
   * <pre>
   *   /public/rpi.edu
   * <pre>
   * while we put users personal directories on a path like
   * <pre>
   *   /user/
   * <pre>
   *
   * <p>This allows us to register directories for multiple systems.
   *
   * @param val    String path
   */
  public void setPathPrefix(final String val) {
    pathPrefix = val;
  }

  /** Get the pathPrefix
   *
   * @return String   path
   */
  public String getPathPrefix() {
    return pathPrefix;
  }

  /** Set the prefix for principals which correspond to cards within
   * this hierarchy
   *
   * <p>For example "/principals.users" might be a principal prefix
   * handled by "/directory/users"
   *
   * @param val    String path
   */
  public void setPrincipalPrefix(final String val) {
    principalPrefix = val;
  }

  /** Get the pathPrefix
   *
   * @return String   path
   */
  public String getPrincipalPrefix() {
    return principalPrefix;
  }

  /** Set the cardPathPrefix which defines the prefix for principal cards.
   *
   * <p>For example, if the pathPrefix is "/principals/locations" and the
   * cardPathPrefix is "/public/locations" then given a principal
   * <pre>
   *   /principals/locations/vcc309
   * <pre>
   * we create a card path by replacing one prefix with another and appending
   * ".vcf" to give
   * <pre>
   *   /public/locations/vcc309.vcf
   * <pre>
   *
   * <p>This is probably insufficient. We almost certainly need to define a
   * property the value of which replaces the name part of the path.
   *
   * @param val    String path
   */
  public void setCardPathPrefix(final String val) {
    cardPathPrefix = val;
  }

  /** Get the cardPathPrefix
   *
   * @return String   path
   */
  public String getCardPathPrefix() {
    return cardPathPrefix;
  }

  /** Set the cardPathPrefixes which defines the prefixes for principal cards based
   * on an account prefix.
   *
   * <p>For example, if the principal is /principals/users/loc_resource01
   * and the cardPathPrefixes is "/public/people,loc_:/public/locations" then
   * we create a card path
   * <pre>
   *   /public/locations/resource01.vcf
   * <pre>
   *
   * @param val    String
   */
  public void setCardPathPrefixes(final String val) {
    cardPathPrefixes = val;
  }

  /** Get the cardPathPrefixes
   *
   * @return String
   */
  public String getCardPathPrefixes() {
    return cardPathPrefixes;
  }

  /** True if this prefix represents an addressbook. Only required if we have no
   * way of adding objectClasses or attributes to the directory itself.
   *
   * @param val True if this prefix represents an addressbook.
   */
  public void setAddressBook(final boolean val)  {
    addressBook = val;
  }

  /** Is this an addressbook?
   *
   * @return boolean val
   */
  public boolean getAddressBook() {
    return addressBook;
  }

  /** True if this prefix represents a directory. This is part of the gateway
   * spec. A directory should be treated as potentially very large.
   *
   * @param val True if this prefix represents a directory.
   */
  public void setDirectory(final boolean val)  {
    directory = val;
  }

  /** Is this a directory?
   *
   * @return boolean val
   */
  public boolean getDirectory() {
    return directory;
  }

  /** Set the interface implementation
   *
   * @param val    String
   */
  public void setClassName(final String val) {
    className = val;
  }

  /** get the interface implementation
   *
   * @return String
   */
  public String getClassName() {
    return className;
  }

  /** Set the href for the owner
   *
   * @param val    String
   */
  public void setOwnerHref(final String val) {
    ownerHref = val;
  }

  /** get the href for the owner
   *
   * @return String
   */
  public String getOwnerHref() {
    return ownerHref;
  }

  /** If set defines the default kind in this directory
   *
   * @param val the default kind in this directory
   */
  public void setCardKind(final String val)  {
    cardKind = val;
  }

  /** If set defines the default kind in this directory
   *
   * @return String val
   */
  public String getCardKind()  {
    return cardKind;
  }

  /** Add our stuff
   *
   * @param ts    for result
   */
  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("confClass", getConfClass());
    ts.append("confBeanClass", getConfBeanClass());
    ts.append("pathPrefix", getPathPrefix());
    ts.append("principalPrefix", getPrincipalPrefix());
    ts.append("cardPathPrefix", getCardPathPrefix());
    ts.append("cardPathPrefixes", getCardPathPrefixes());
    ts.append("addressBook", getAddressBook());
    ts.append("directory", getDirectory());
    ts.append("className", getClassName());
    ts.append("ownerHref", getOwnerHref());
    ts.append("cardKind", getCardKind());
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public int compareTo(final T that) {
    return getPathPrefix().compareTo(that.getPathPrefix());
  }

  @Override
  public int hashCode() {
    return getPathPrefix().hashCode();
  }
}
