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
package org.bedework.carddav.common.vcard;

import org.bedework.access.AccessPrincipal;
import org.bedework.carddav.common.CarddavCollection;

import net.fortuna.ical4j.vcard.VCard;

/** A vcard and properties for cardDAV
 *
 * @author douglm
 *
 */
public class Card extends org.bedework.util.vcard.Card {
  private AccessPrincipal owner;

  private CarddavCollection parent;

  /** Create Card with a new embedded VCard
   *
   */
  public Card() {
    super();
  }

  /** Create card with supplied vcard
   *
   * @param vcard the card
   */
  public Card(final VCard vcard) {
    super(vcard);
  }

  /**
   * @param val owner principal
   */
  public void setOwner(final AccessPrincipal val) {
    owner = val;
  }

  /**
   * @return AccessPrincipal
   */
  public AccessPrincipal getOwner() {
    return owner;
  }

  /**
   * @param val parent collection
   */
  public void setParent(final CarddavCollection val) {
    parent = val;
  }

  /**
   * @return parent.
   */
  @SuppressWarnings(value = "unused")
  public CarddavCollection getParent() {
    return parent;
  }
}
