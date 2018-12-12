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

import org.bedework.carddav.common.vcard.Card;
import org.bedework.util.logging.Logged;
import org.bedework.webdav.servlet.shared.WebdavException;

import net.fortuna.ical4j.vcard.Property;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 *   @author Mike Douglass
 */
public class PropFilter implements Logged {
  /* Name of property */
  private String name;

  private boolean isNotDefined;

  protected int testAllAny = Filter.testAnyOf;

  private TextMatch match;

  private ArrayList<ParamFilter> paramFilters;

  /** Constructor
   *
   * @param name
   */
  public PropFilter(final String name) {
    this.name = name;
  }

  /** Constructor
   *
   * @param name
   * @param isNotDefined
   */
  public PropFilter(final String name, final boolean isNotDefined) {
    this.name = name;
    this.isNotDefined = isNotDefined;
  }

  /** Constructor
   *
   * @param name
   * @param match
   */
  public PropFilter(final String name, final TextMatch match) {
    this.name = name;
    this.match = match;
  }

  /**
   * @param val
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
   * @return boolean
   */
  public boolean getIsNotDefined() {
    return isNotDefined;
  }

  /**
   * @param val
   */
  public void setTestAllAny(final int val) {
    testAllAny = val;
  }

  /**
   * @return int
   */
  public int getTestAllAny() {
    return testAllAny;
  }

  /**
   * @param val
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

  /**
   * @return Collection
   */
  public Collection<ParamFilter> getParamFilters() {
    if (paramFilters == null) {
      paramFilters = new ArrayList<ParamFilter>();
    }

    return paramFilters;
  }

  /** Add a param filter
   *
   * @param pf
   */
  public void addParamFilter(final ParamFilter pf) {
    getParamFilters().add(pf);
  }

  /** Return true if the given component matches the property filter
   *
   * NOTE *********** Not handling params yet
   *
   * @param c
   * @return boolean true if the given component matches the property filter
   * @throws WebdavException
   */
  public boolean filter(final Card c) throws WebdavException {
    try {
      Property prop = c.findProperty(getName());

      if (prop == null) {
        return getIsNotDefined();
      }

      TextMatch match = getMatch();
      if (match != null) {
        return match.matches(prop.getValue());
      }

      return false;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /** Debug
   *
   * @param indent
   */
  public void dump(final String indent) {
    final StringBuffer sb = new StringBuffer(indent);

    sb.append("<prop-filter name=\"");
    sb.append(name);
    sb.append("\">\n");
    debug(sb.toString());

    if (isNotDefined) {
      debug(indent + "  " + "<is-not-defined/>\n");
    } else if (match != null) {
      match.dump(indent + "  ");
    }

    if (paramFilters != null) {
      for (ParamFilter pf: paramFilters) {
        pf.dump(indent + "  ");
      }
    }

    debug(indent + "</prop-filter>");
  }
}

