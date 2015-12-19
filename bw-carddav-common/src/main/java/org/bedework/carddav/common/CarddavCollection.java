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
import org.bedework.webdav.servlet.shared.WebdavException;

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
   * @throws WebdavException
   */
  public CarddavCollection() throws WebdavException {
    super();
  }

  /**
   * @param val
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
  public boolean getCanShare() throws WebdavException {
    return false;
  }

  @Override
  public boolean getCanPublish() throws WebdavException {
    return false;
  }

  @Override
  public CarddavCollection resolveAlias(final boolean resolveSubAlias) throws WebdavException {
    return this;
  }

  @Override
  public void setProperty(final QName name, final String val) throws WebdavException {
  }

  @Override
  public String getProperty(final QName name) throws WebdavException {
    return null;
  }

  /**
   * @param val
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
   * @param val
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

  /**
   * @param val
   * @throws WebdavException
   */
  @Override
  public void setLastmod(final String val) throws WebdavException {
    lastmod = val;
  }

  /**
   * @return String lastmod
   * @throws WebdavException
   */
  @Override
  public String getLastmod() throws WebdavException {
    return lastmod;
  }

  /** Set the sequence
   *
   * @param val    sequence number
   * @throws WebdavException
   */
  public void setSequence(final int val) throws WebdavException {
    sequence = val;
  }

  /** Get the sequence
   *
   * @return int    the sequence
   * @throws WebdavException
   */
  public int getSequence() throws WebdavException {
    return sequence;
  }

  /** Prev lastmod is the saved lastmod before any changes.
   *
   * @param val
   * @throws WebdavException
   */
  public void setPrevLastmod(final String val) throws WebdavException {
    prevLastmod = val;
  }

  /**
   * @return String lastmod
   * @throws WebdavException
   */
  public String getPrevLastmod() throws WebdavException {
    return prevLastmod;
  }

  /** Set the sequence
   *
   * @param val    sequence number
   * @throws WebdavException
   */
  public void setPrevSequence(final int val) throws WebdavException {
    prevSequence = val;
  }

  /** Get the sequence
   *
   * @return int    the sequence
   * @throws WebdavException
   */
  public int getPrevSequence() throws WebdavException {
    return prevSequence;
  }

  @Override
  public String getEtag() throws WebdavException {
    return "\"" + getLastmod() + "-" +
           getSequence() +
           "\"";
  }

  @Override
  public String getPreviousEtag() throws WebdavException {
    return "\"" + getPrevLastmod() +
           getPrevSequence() +
           "\"";
  }
}
