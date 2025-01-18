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
package org.bedework.carddav.server.dirHandlers.db;

import org.bedework.base.ToString;

/** Base type for a database entity with a name.
 *
 * @author Mike Douglass
 * @version 1.0
 *
 * @param <T>
 */
public abstract class DbNamedEntity<T> extends DbEntity<T> {
  private String name;

  private String path;

  /** Set the name
   *
   * @param val    String name
   */
  public void setName(final String val) {
    name = val;
  }

  /** Get the name
   *
   * @return String   name
   */
  public String getName() {
    return name;
  }

  /**
   * @param val path
   */
  public void setPath(final String val) {
    path = val;
  }

  /**
   * @return path.
   */
  public String getPath() {
    return path;
  }

  /* ==============================================================
   *                   SharedEntity methods
   * ============================================================== */

  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.newLine().append("name", getName());
  }
}
