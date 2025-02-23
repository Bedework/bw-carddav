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
   * @param name Name of property
   */
  public Prop(final String name) {
    this.name = name;
  }

  /**
   * @param val property value
   */
  public void setName(final String val) {
    name = val;
  }

  /**
   * @return String
   */
  public String getName() {
    return name;
  }

  /**
   * @param val true for property with no value
   */
  public void setNovalue(final boolean val) {
    novalue = val;
  }

  /**
   * @return boolean
   */
  public boolean getNovalue() {
    return novalue;
  }

  /**
   * @param indent amount
   */
  public void dump(final String indent) {
    debug(new StringBuilder(indent)
                  .append("<calddav:prop name=")
                  .append(name)
                  .append(" novalue=")
                  .append(novalue)
                  .append("/>")
                  .toString());
  }

  /* ==============================================================
   *                   Logged methods
   * ============================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}

