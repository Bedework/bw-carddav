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

import org.bedework.carddav.common.vcard.Card;

import java.util.ArrayList;
import java.util.Collection;

/** Result from getCards and getCollections
 *
 * User: mike Date: 12/15/15 Time: 16:20
 */
public class GetResult {
  /** Server truncated the query result */
  public boolean serverTruncated;

  /** Exceeded user limit */
  public boolean overLimit;

  /** The possibly truncated result from getCards */
  public Collection<Card> cards = new ArrayList<>();

  /** The possibly truncated result from getCollections */
  public Collection<CarddavCollection> collections;

  /** Something found with last search */
  public boolean entriesFound;
}
