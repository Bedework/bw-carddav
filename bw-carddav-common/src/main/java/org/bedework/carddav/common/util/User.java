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
package org.bedework.carddav.common.util;

import org.bedework.access.WhoDefs;

/** Value object to represent a calendar user.
 *
 *   @author Mike Douglass douglm  rpi.edu
 *  @version 1.0
 */
public class User extends Principal {

  /* ==============================================================
   *                   Constructors
   * ============================================================== */

  /** Create a guest user
   */
  public User() {
    super();
  }

  /** Create a user object for an account name.
   *
   * @param  name            String user id for database
   */
  public User(final String name) {
    super(name);
  }

  public int getKind() {
    return WhoDefs.whoTypeUser;
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */
}
