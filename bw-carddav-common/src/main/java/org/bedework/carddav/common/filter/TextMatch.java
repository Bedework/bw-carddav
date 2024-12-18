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

/**
 */
public class TextMatch implements Logged {
  /** "equals" - an exact match to the target string */
  public static final int matchTypeEquals = 0;

  /** "contains" - a substring match, matching anywhere within the target string */
  public static final int matchTypeContains = 1;

  /** "starts-with" - a substring match, matching only at the start of the target string */
  public static final int matchTypeStartsWith = 2;

  /** "ends-with"  private Boolean */
  public static final int matchTypeEndsWith = 3;

  /** valid match types */
  public final static String[] matchTypes = {
    "equals",
    "contains",
    "starts-with",
    "ends-with"
  };

  private int matchType = matchTypeContains;

  private Boolean caseless; // null for defaulted
  private boolean negated;
  private String val;

  private boolean upperMatch;

  /** Constructor
   *
   * @param val to match
   */
  public TextMatch(final String val) {
    setMatchType(matchTypeContains);
    setVal(val);
  }

  /** Constructor
   *
   * @param caseless true for any case
   * @param matchType
   * @param negated true for NOT
   * @param val to match
   */
  public TextMatch(final Boolean caseless,
                   final int matchType,
                   final boolean negated,
                   final String val) {
    setCaseless(caseless);
    setMatchType(matchType);
    setNegated(negated);
    setVal(val);
  }

  /** Set the value
   * @param val text value
   */
  public void setVal(final String val) {
    if (upperMatch) {
      this.val = val.toUpperCase();
    } else {
      this.val = val;
    }
  }

  /** Get the value
   * @return String
   */
  public String getVal() {
    return val;
  }

  /** Set caseless state
   *
   * @param val Boolean
   */
  public void setCaseless(final Boolean val) {
    caseless = val;

    upperMatch = (val != null) && (!val);

    if ((getVal() != null) && upperMatch) {
      setVal(getVal().toUpperCase());
    }
  }

  /** get caseless state
   *
   * @return Boolean
   */
  public Boolean getCaseless() {
    return caseless;
  }

  /** get caseless state
   *
   * @return boolean
   */
  public boolean testCaseless() {
    return (caseless != null) && caseless;
  }

  /**
   *
   * @param val int
   */
  public void setMatchType(final int val) {
    matchType = val;
  }

  /**
   *
   * @return int
   */
  public int getMatchType() {
    return matchType;
  }

  /** Set negated state
   *
   * @param val boolean
   */
  public void setNegated(final boolean val) {
    negated = val;
  }

  /** get negated state
   *
   * @return boolean
   */
  public boolean getNegated() {
    return negated;
  }

  /**
   * @param candidate string
   * @return boolean true if matches
   */
  public boolean matches(final String candidate) {
    if (candidate == null) {
      return false;
    }

    if (!upperMatch) {
      return candidate.startsWith(getVal());
    }

    return candidate.toUpperCase().startsWith(getVal());
  }

  /** Debug
   * @param indent amount
   */
  public void dump(final String indent) {
    final StringBuilder sb = new StringBuilder(indent);

    sb.append("<text-match");
    if (caseless != null) {
      sb.append(" caseless=");
      sb.append(caseless);
    }

    sb.append(" match-type=");
    sb.append(matchTypes[getMatchType()]);

    sb.append(">");
    debug(sb.toString());

    debug(val);

    debug(indent + "</text-match>\n");
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
