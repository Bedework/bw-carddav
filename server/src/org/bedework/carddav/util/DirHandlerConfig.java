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

import edu.rpi.cmt.config.ConfigurationStringValueType;
import edu.rpi.cmt.config.ConfigurationType;
import edu.rpi.sss.util.ToString;

import javax.xml.namespace.QName;

/** Class defining the configuration for a directory handler.
 *
 * @author douglm
 * @param <T>
 */
public abstract class DirHandlerConfig<T extends DirHandlerConfig> extends ConfigBase<T> {
  /** */
  public final static QName confElement = new QName(ns, "bwcarddav-dirhandler");

  /** */
  public static final QName confClass = new QName(ns, "confClass");

  private static final QName confBeanClass = new QName(ns, "confBeanClass");

  private static final QName pathPrefix = new QName(ns, "pathPrefix");

  private static final QName cardPathPrefix = new QName(ns, "cardPathPrefix");

  private static final QName cardPathPrefixes = new QName(ns, "cardPathPrefixes");

  private static final QName addressBook = new QName(ns, "addressBook");

  private static final QName className = new QName(ns, "className");

  private static final QName ownerHref = new QName(ns, "ownerHref");

  private static final QName cardKind = new QName(ns, "cardKind");

  @Override
  public QName getConfElement() {
    return confElement;
  }

  /** set the class for the conf wrapper object class
   *
   * @param val
   */
  public void setConfClass(final String val) {
    setProperty(confClass, val);
  }

  /** Get the class for the conf wrapper object class
   *
   * @return String   path
   */
  public String getConfClass() {
    return getPropertyValue(confClass);
  }

  /** Get the class for the conf wrapper object class given a configuration
   * object.
   *
   * @param conf
   * @return String   path
   */
  public static String getConfClass(final ConfigurationType conf) {
    try {
      return ((ConfigurationStringValueType)conf.findAll(confClass).get(0)).getValue();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /** set the class for the jmx conf bean
   *
   * @param val
   */
  public void setConfBeanClass(final String val) {
    setProperty(confBeanClass, val);
  }

  /** Get the class for the jmx conf bean
   *
   * @return String   path
   */
  public String getConfBeanClass() {
    return getPropertyValue(confBeanClass);
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
    setProperty(pathPrefix, val);
  }

  /** Get the pathPrefix
   *
   * @return String   path
   */
  public String getPathPrefix() {
    return getPropertyValue(pathPrefix);
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
    setProperty(cardPathPrefix, val);
  }

  /** Get the cardPathPrefix
   *
   * @return String   path
   */
  public String getCardPathPrefix() {
    return getPropertyValue(cardPathPrefix);
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
    setProperty(cardPathPrefixes, val);
  }

  /** Get the cardPathPrefixes
   *
   * @return String
   */
  public String getCardPathPrefixes() {
    return getPropertyValue(cardPathPrefixes);
  }

  /** True if this prefix represents areturn getPropertyValue(ssbook. Only required if we have no
   * way of adding objectClasses or attributes to the directory itself.
   *
   * @param val
   */
  public void setAddressBook(final boolean val)  {
    setBooleanProperty(addressBook, val);
  }

  /** Is debugging on?
   *
   * @return boolean val
   */
  public boolean getAddressBook() {
    return getBooleanPropertyValue(addressBook);
  }

  /** Set the interface implementation
   *
   * @param val    String
   */
  public void setClassName(final String val) {
    setProperty(className, val);
  }

  /** get the interface implementation
   *
   * @return String
   */
  public String getClassName() {
    return getPropertyValue(className);
  }

  /** Set the href for the owner
   *
   * @param val    String
   */
  public void setOwnerHref(final String val) {
    setProperty(ownerHref, val);
  }

  /** get the href for the owner
   *
   * @return String
   */
  public String getOwnerHref() {
    return getPropertyValue(ownerHref);
  }

  /** If set defines the default kind in this directory
   *
   * @param val
   */
  public void setCardKind(final String val)  {
    setProperty(cardKind, val);
  }

  /** If set defines the default kind in this directory
   *
   * @return String val
   */
  public String getCardKind()  {
    return getPropertyValue(cardKind);
  }

  /** Add our stuff to the StringBuilder
   *
   * @param sb    StringBuilder for result
   * @param indent
   */
  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("pathPrefix", getPathPrefix());
    ts.append("ownerHref", getOwnerHref());
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
