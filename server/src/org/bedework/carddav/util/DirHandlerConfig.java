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

/** Class defining the configuration for a directory handler.
 *
 * @author douglm
 */
public class DirHandlerConfig {
  private String pathPrefix;

  private String cardPathPrefix;

  private String cardPathPrefixes;

  private boolean addressBook;

  private String className;

  private String ownerHref;

  private String cardKind;

  private boolean debug;

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
   * @param val
   */
  public void setAddressBook(final boolean val)  {
    addressBook = val;
  }

  /** Is debugging on?
   *
   * @return boolean val
   */
  public boolean getAddressBook()  {
    return addressBook;
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
  * @param val
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

 /**
  * @param val
  */
 public void setDebug(final boolean val)  {
   debug = val;
 }

 /** Is debugging on?
  *
  * @return boolean val
  */
 public boolean getDebug()  {
   return debug;
 }
}
