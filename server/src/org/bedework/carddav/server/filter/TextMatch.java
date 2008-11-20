/* **********************************************************************
    Copyright 2007 Rensselaer Polytechnic Institute. All worldwide rights reserved.

    Redistribution and use of this distribution in source and binary forms,
    with or without modification, are permitted provided that:
       The above copyright notice and this permission notice appear in all
        copies and supporting documentation;

        The name, identifiers, and trademarks of Rensselaer Polytechnic
        Institute are not used in advertising or publicity without the
        express prior written permission of Rensselaer Polytechnic Institute;

    DISCLAIMER: The software is distributed" AS IS" without any express or
    implied warranty, including but not limited to, any implied warranties
    of merchantability or fitness for a particular purpose or any warrant)'
    of non-infringement of any current or pending patent rights. The authors
    of the software make no representations about the suitability of this
    software for any particular purpose. The entire risk as to the quality
    and performance of the software is with the user. Should the software
    prove defective, the user assumes the cost of all necessary servicing,
    repair or correction. In particular, neither Rensselaer Polytechnic
    Institute, nor the authors of the software are liable for any indirect,
    special, consequential, or incidental damages related to the software,
    to the maximum extent the law permits.
*/
package org.bedework.carddav.server.filter;

import org.apache.log4j.Logger;

/**
 */
public class TextMatch {
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
   * @param val
   */
  public TextMatch(String val) {
    setMatchType(matchTypeContains);
    setVal(val);
  }

  /** Constructor
   *
   * @param caseless
   * @param matchType
   * @param negated
   * @param val
   */
  public TextMatch(Boolean caseless, int matchType, boolean negated, String val) {
    setCaseless(caseless);
    setMatchType(matchType);
    setNegated(negated);
    setVal(val);
  }

  /** Set the value
   * @param val
   */
  public void setVal(String val) {
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
  public void setCaseless(Boolean val) {
    caseless = val;

    upperMatch = (val != null) && (!val.booleanValue());

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

  /**
   *
   * @param val int
   */
  public void setMatchType(int val) {
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
  public void setNegated(boolean val) {
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
   * @param candidate
   * @return boolean true if matches
   */
  public boolean matches(String candidate) {
    if (candidate == null) {
      return false;
    }

    if (!upperMatch) {
      return candidate.startsWith(getVal());
    }

    return candidate.toUpperCase().startsWith(getVal());
  }

  /** Debug
   * @param log
   * @param indent
   */
  public void dump(Logger log, String indent) {
    StringBuffer sb = new StringBuffer(indent);

    sb.append("<text-match");
    if (caseless != null) {
      sb.append(" caseless=");
      sb.append(caseless);
    }

    sb.append(" match-type=");
    sb.append(matchTypes[getMatchType()]);

    sb.append(">");
    log.debug(sb.toString());

    log.debug(val);

    log.debug(indent + "</text-match>\n");
  }
}
