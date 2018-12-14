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

package org.bedework.carddav.server.query;

import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

/**
 * @author Mike Douglass douglm   rpi.edu
 */
public class Prop implements Logged {
  /* Name of property */
  private String name;

  private boolean novalue;

  /** Constructor
   * @param name
   */
  public Prop(String name) {
    this.name = name;
  }

  /**
   * @param val
   */
  public void setName(String val) {
    name = val;
  }

  /**
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * @param val
   */
  public void setNovalue(boolean val) {
    novalue = val;
  }

  /**
   * @return boolean
   */
  public boolean getNovalue() {
    return novalue;
  }

  /**
   * @param indent
   */
  public void dump(String indent) {
    StringBuffer sb = new StringBuffer(indent);

    sb.append("<calddav:prop name=");
    sb.append(name);
    sb.append(" novalue=");
    sb.append(novalue);
    sb.append("/>");

    debug(sb.toString());
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}

