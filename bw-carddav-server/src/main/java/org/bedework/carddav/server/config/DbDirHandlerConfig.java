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

import org.bedework.carddav.common.config.DirHandlerConfig;
import org.bedework.util.config.ConfInfo;
import org.bedework.util.misc.ToString;

/** This class defines the various properties we need to make a connection
 * and retrieve a group and user information from a db.
 *
 * @author Mike Douglass
 */
@ConfInfo(elementName = "bwcarddav-dirhandler")
public class DbDirHandlerConfig extends
        DirHandlerConfig<DbDirHandlerConfig> {
  private String rootAccess;

  private String rootOwner;

  private int queryLimit;

  /**
   *
   * @param val access string
   */
  public void setRootAccess(final String val)  {
    rootAccess = val;
  }

  /**
   * @return String
   */
  public String getRootAccess()  {
    return rootAccess;
  }

  /**
   *
   * @param val principal
   */
  public void setRootOwner(final String val)  {
    rootOwner = val;
  }

  /**
   * @return String
   */
  public String getRootOwner()  {
    return rootOwner;
  }

  /**
   *
   * @param val the query limit - 0 for no limit
   */
  public void setQueryLimit(final int val)  {
    queryLimit = val;
  }

  /**
   *
   * @return int query limit - 0 for no limit
   */
  public int getQueryLimit()  {
    return queryLimit;
  }

  /** Add our stuff
   *
   * @param ts    StringBuilder for result
   */
  @Override
  public void toStringSegment(final ToString ts) {
    super.toStringSegment(ts);

    ts.append("rootAccess", getRootAccess());
    ts.append("rootOwner", getRootOwner());
    ts.append("queryLimit", getQueryLimit());
  }
}
