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
package org.bedework.carddav.common;

import org.bedework.webdav.servlet.shared.WdCollectionBase;

import javax.xml.namespace.QName;

/**
 * @author douglm
 *
 */
public class CarddavCollection extends WdCollectionBase<CarddavCollection> {
  private CarddavCollection parent;

  private boolean addressBook;

  private boolean directory;

  /** UTC datetime */
  private String lastmod;

  /** Ensure uniqueness - lastmod only down to second.
   */
  private int sequence;

  /** UTC datetime */
  private String prevLastmod;

  /** Ensure uniqueness - lastmod only down to second.
   */
  private int prevSequence;

  /**
   */
  public CarddavCollection() {
    super();
  }

  /**
   * @param val CarddavCollection
   */
  public void setParent(final CarddavCollection val) {
    parent = val;
  }

  /**
   * @return CarddavCollection
   */
  public CarddavCollection getParent() {
    return parent;
  }

  @Override
  public boolean getCanShare() {
    return false;
  }

  @Override
  public boolean getCanPublish() {
    return false;
  }

  @Override
  public CarddavCollection resolveAlias(final boolean resolveSubAlias) {
    return this;
  }

  @Override
  public void setProperty(final QName name, final String val) {
  }

  @Override
  public String getProperty(final QName name) {
    return null;
  }

  /**
   * @param val true for addressbook
   */
  public void setAddressBook(final boolean val) {
    addressBook = val;
  }

  /**
   * @return boolean
   */
  public boolean getAddressBook() {
    return addressBook;
  }

  /** True if this represents a directory. This is part of the gateway
   * spec. A directory should be treated as potentially very large.
   *
   * @param val True if this represents a directory.
   */
  public void setDirectory(final boolean val) {
    directory = val;
  }

  /** Is this a directory?
  *
  * @return boolean val
  */
  public boolean getDirectory() {
    return directory;
  }

  @Override
  public void setLastmod(final String val) {
    lastmod = val;
  }

  @Override
  public String getLastmod() {
    return lastmod;
  }

  /** Set the sequence
   *
   * @param val    sequence number
   */
  public void setSequence(final int val) {
    sequence = val;
  }

  /** Get the sequence
   *
   * @return int    the sequence
   */
  public int getSequence() {
    return sequence;
  }

  /** Prev lastmod is the saved lastmod before any changes.
   *
   * @param val UTC lastmod
   */
  public void setPrevLastmod(final String val) {
    prevLastmod = val;
  }

  /**
   * @return String lastmod
   */
  public String getPrevLastmod() {
    return prevLastmod;
  }

  /** Set the sequence
   *
   * @param val    sequence number
   */
  public void setPrevSequence(final int val) {
    prevSequence = val;
  }

  /** Get the sequence
   *
   * @return int    the sequence
   */
  public int getPrevSequence() {
    return prevSequence;
  }

  @Override
  public String getEtag() {
    return "\"" + getLastmod() + "-" +
           getSequence() +
           "\"";
  }

  @Override
  public String getPreviousEtag() {
    return "\"" + getPrevLastmod() +
           getPrevSequence() +
           "\"";
  }
}
