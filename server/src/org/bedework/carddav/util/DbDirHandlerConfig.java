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


/** This class defines the various properties we need to make a connection
 * and retrieve a group and user information via ldap.
 *
 * @author Mike Douglass
 */
public class DbDirHandlerConfig extends DirHandlerConfig {
  private String moduleType;

  private String rootAccess;

  private String rootOwner;

  private int queryLimit;

  /** Used by configuration tools
   *
   * @param val
   */
  public void setModuleType(final String val)  {
    moduleType  = val;
  }

  /**
   * @return String
   */
  public String getRootAccess()  {
    return rootAccess;
  }

  /**
   *
   * @param val
   */
  public void setRootAccess(final String val)  {
    rootAccess  = val;
  }

  /**
   * @return String
   */
  public String getRootOwner()  {
    return rootOwner;
  }

  /**
   *
   * @param val
   */
  public void setRootOwner(final String val)  {
    rootOwner  = val;
  }

  /**
   * @return String
   */
  public String getModuleType()  {
    return moduleType;
  }

  /** Set the query limit - 0 for no limit
   *
   * @param val
   */
  public void setQueryLimit(final int val)  {
    queryLimit = val;
  }

  /**
   *
   * @return int val
   */
  public int getQueryLimit()  {
    return queryLimit;
  }
}
