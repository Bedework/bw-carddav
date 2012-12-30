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
package org.bedework.carddav.server.jmx;

import org.bedework.carddav.util.DbDirHandlerConfig;

/**
 * @author douglm
 *
 */
public class DbDirHandlerConf extends DirHandlerConf implements DbDirHandlerConfMBean {
  /* ========================================================================
   * Attributes
   * ======================================================================== */

  public void setRootAccess(final String val)  {
    getConf().setRootAccess(val);
  }

  public String getRootAccess()  {
    return getConf().getRootAccess();
  }

  public void setRootOwner(final String val)  {
    getConf().setRootOwner(val);
  }

  public String getRootOwner()  {
    return getConf().getRootOwner();
  }

  public void setQueryLimit(final int val)  {
    getConf().setQueryLimit(val);
  }

  public int getQueryLimit()  {
    return getConf().getQueryLimit();
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /*
  @Override
  public List<String> getUserInfo(final String cua) {
    return new ArrayList<String>();
  }
  */

  /* ====================================================================
   *                   Non-mbean methods
   * ==================================================================== */

  /**
   * @return current state of config
   */
  @Override
  public DbDirHandlerConfig getConf() {
    return (DbDirHandlerConfig)super.getConf();
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  /* ========================================================================
   * Lifecycle
   * ======================================================================== */

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */
}
