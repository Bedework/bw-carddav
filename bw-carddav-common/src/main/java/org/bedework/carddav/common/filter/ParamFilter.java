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
package org.bedework.carddav.common.filter;

import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;

/** Represent a param filter
 *
 * @author Mike Douglass
 *
 */
public class ParamFilter implements Logged {
  private String name;

  private boolean isNotDefined;

  private TextMatch match;

  /** Constructor
   *
   * @param name of filter
   * @param isNotDefined
   */
  public ParamFilter(final String name, final boolean isNotDefined) {
    this.name = name;
    this.isNotDefined = isNotDefined;
  }

  /** Constructor
   *
   * @param name of filter
   * @param match a text match
   */
  public ParamFilter(final String name, final TextMatch match) {
    this.name = name;
    this.match = match;
  }

  /**
   * @param val name of filter
   */
  public void setName(final String val) {
    name = val;
  }

  /**
   * @return String name
   */
  public String getName() {
    return name;
  }

  /**
   * @param val
   */
  public void setIsNotDefined(final boolean val) {
    isNotDefined = val;
  }

  /**
   * @return boolean isdefined value
   */
  public boolean getIsNotDefined() {
    return isNotDefined;
  }

  /**
   * @param val a text match
   */
  public void setMatch(final TextMatch val) {
    match = val;
  }

  /**
   * @return TextMatch
   */
  public TextMatch getMatch() {
    return match;
  }

  /** Debug
   *
   * @param indent amount
   */
  public void dump(final String indent) {
    final StringBuilder sb = new StringBuilder(indent);

    sb.append("<param-filter name=\"");
    sb.append(name);
    sb.append(">\n");
    debug(sb.toString());

    if (isNotDefined) {
      debug(indent + "  " + "<is-not-defined/>\n");
    } else {
      match.dump(indent + "  ");
    }

    debug(indent + "</param-filter>");
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

