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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import org.bedework.carddav.vcard.Property;
import org.bedework.carddav.vcard.Vcard;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;

/**
 *
 *   @author Mike Douglass
 */
public class PropFilter {
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
  public PropFilter(String name) {
    this.name = name;
  }

  /** Constructor
   *
   * @param name
   * @param isNotDefined
   */
  public PropFilter(String name, boolean isNotDefined) {
    this.name = name;
    this.isNotDefined = isNotDefined;
  }

  /** Constructor
   *
   * @param name
   * @param match
   */
  public PropFilter(String name, TextMatch match) {
    this.name = name;
    this.match = match;
  }

  /**
   * @param val
   */
  public void setName(String val) {
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
  public void setIsNotDefined(boolean val) {
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
  public void setTestAllAny(int val) {
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
  public void setMatch(TextMatch val) {
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
  public void addParamFilter(ParamFilter pf) {
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
  public boolean filter(Vcard c) throws WebdavException {
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
   * @param log
   * @param indent
   */
  public void dump(Logger log, String indent) {
    StringBuffer sb = new StringBuffer(indent);

    sb.append("<prop-filter name=\"");
    sb.append(name);
    sb.append("\">\n");
    log.debug(sb.toString());

    if (isNotDefined) {
      log.debug(indent + "  " + "<is-not-defined/>\n");
    } else if (match != null) {
      match.dump(log, indent + "  ");
    }

    if (paramFilters != null) {
      for (ParamFilter pf: paramFilters) {
        pf.dump(log, indent + "  ");
      }
    }

    log.debug(indent + "</prop-filter>");
  }
}

