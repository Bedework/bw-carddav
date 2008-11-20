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

import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.sss.util.xml.XmlUtil;
import edu.rpi.sss.util.xml.tagdefs.CarddavTags;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/** Class to represent a calendar-query filter
 *  <pre>
10.5.  CARDDAV:filter XML Element
   Name:  filter
   Namespace:  urn:ietf:params:xml:ns:carddav
   Purpose:  Determines which matching objects are returned.

   Description:  The "filter" element specifies the search filter used
      to match address objects that should be returned by a report.  The
      "test" attribute specifies whether any (logical OR) or all
      (logical AND) of the prop-filter tests needs to match in order for
      the overall filter to match.

   Definition:
       <!ELEMENT filter (prop-filter*)>

       <!ATTLIST filter test (anyof | allof) "anyof">
       <!-- test value:
                 anyof  logical OR for prop-filter matches
                 allof  logical AND for prop-filter matches -->

10.5.1.  CARDDAV:prop-filter XML Element
   Name:  prop-filter
   Namespace:  urn:ietf:params:xml:ns:carddav
   Purpose:  Limits the search to specific properties.

   Description:  The CARDDAV:prop-filter XML element specifies a search
      criteria on a specific vCard property (e.g., NICKNAME).  An
      address object is said to match a CARDDAV:prop-filter if:

      *  A property of the type specified by the "name" attribute
         exists, and the CARDDAV:prop-filter is empty, or it matches the
         CARDDAV:text-match conditions if specified, and that CARDDAV:
         param-filter child elements also match.  The "test" attribute
         specifies whether any (logical OR) or all (logical AND) of the
         text-filter and param-filter tests needs to match in order for
         the overall filter to match.

      or:

      *  A property of the type specified by the "name" attribute does
         not exist, and the CARDAV:is-not-defined element is specified.

   Definition:
       <!ELEMENT prop-filter (is-not-defined |
                              (text-match?, param-filter*))>

       <!ATTLIST prop-filter name CDATA #REQUIRED
                             test (anyof | allof) "anyof">
       <!-- name value: a vCard property name (e.g., "NICKNAME")
         test value:
             anyof  logical OR for text-match/param-filter matches
             allof  logical AND for text-match/param-filter matches -->

10.5.2.  CARDDAV:param-filter XML Element
   Name:  param-filter
   Namespace:  urn:ietf:params:xml:ns:carddav
   Purpose:  Limits the search to specific parameter values.

   Description:  The CARDDAV:param-filter XML element specifies a search
      criteria on a specific vCard property parameter (e.g., TYPE) in
      the scope of a given CARDDAV:prop-filter.  A vCard property is
      said to match a CARDDAV:param-filter if:

      *  A parameter of the type specified by the "name" attribute
         exists, and the CARDDAV:param-filter is empty, or it matches
         the CARDDAV:text-match conditions if specified.

      or:

      *  A parameter of the type specified by the "name" attribute does
         not exist, and the CARDDAV:is-not-defined element is specified.

   Definition:
       <!ELEMENT param-filter (is-not-defined | text-match)?>

       <!ATTLIST param-filter name CDATA #REQUIRED>
       <!-- name value: a property parameter name (e.g., "TYPE") -->

10.5.3.  CARDDAV:is-not-defined XML Element
   Name:  is-not-defined
   Namespace:  urn:ietf:params:xml:ns:carddav
   Purpose:  Specifies that a match should occur if the enclosing
      property or parameter does not exist.

   Description:  The CARDDAV:is-not-defined XML element specifies that a
      match occurs if the enclosing property or parameter value
      specified in an address book REPORT request does not exist in the
      address data being tested.

   Definition:
       <!ELEMENT is-not-defined EMPTY>

10.5.4.  CARDDAV:text-match XML Element
   Name:  text-match
   Namespace:  urn:ietf:params:xml:ns:carddav
   Purpose:  Specifies a substring match on a property or parameter
      value.

   Description:  The CARDDAV:text-match XML element specifies text used
      for a substring match against the property or parameter value
      specified in an address book REPORT request.

      The "collation" attribute is used to select the collation that the
      server MUST use for character string matching.  In the absence of
      this attribute the server MUST use the "i;unicode-casemap"
      collation.

      The "negate-condition" attribute is used to indicate that this
      test returns a match if the text matches, when the attribute value
      is set to "no", or return a match if the text does not match, if
      the attribute value is set to "yes".  For example, this can be
      used to match components with a CATEGORIES property not set to
      PERSON.

      The "match-type" attribute is used to indicate the type of match
      operation to use.  Possible choices are:

         "equals" - an exact match to the target string

         "contains" - a substring match, matching anywhere within the
         target string

         "starts-with" - a substring match, matching only at the start
         of the target string

         "ends-with" - a substring match, matching only at the end of
         the target string

   Definition:
       <!ELEMENT text-match (#PCDATA)>
       <!-- PCDATA value: string -->

       <!ATTLIST text-match
          collation        CDATA "i;unicode-casemap"
          negate-condition (yes | no) "no"
          match-type (equals|contains|starts-with|ends-with) "contains">

 * </pre>
 *
 *   @author Mike Douglass   douglm @ rpi.edu
 */
public class CarddavFilter {
  /** */
  public static int testAnyOf = 0;

  /** */
  public static int testAllOf = 1;

  protected static int testAllAny;

  protected boolean debug;

  protected transient Logger log;

  private Collection<PropFilter> propFilters;

  /** Constructor
   *
   * @param debug
   */
  public CarddavFilter(boolean debug) {
    this.debug = debug;
  }

  /** Given a caldav like xml filter parse it
   *
   * @param xmlStr
   * @throws WebdavException
   */
  public void parse(String xmlStr) throws WebdavException {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);

      DocumentBuilder builder = factory.newDocumentBuilder();

      Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));

      parse(doc.getDocumentElement());
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /** The given node must be the Filter element
   *
   * @param nd
   * @throws WebdavException
   */
  public void parse(Node nd) throws WebdavException {
    /* We expect 0 or more prop-filter children. */

    Element[] children = getChildren(nd);

    if (children.length == 0) {
      // Empty
      return;
    }

    /* prop-filter* */

    try {
      for (int i = 0; i < children.length; i++) {
        Node curnode = children[i];

        if (debug) {
          trace("filter element: " +
              curnode.getNamespaceURI() + ":" +
              curnode.getLocalName());
        }

        if (XmlUtil.nodeMatches(curnode, CarddavTags.propFilter)) {
          PropFilter chpf = parsePropFilter(curnode);

          addPropFilter(chpf);
        } else {
          throw new WebdavBadRequest();
        }
      }
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavBadRequest(t.getMessage());
    }
  }

  /* The given node must be a prop-filter element
   *    <!ELEMENT prop-filter (is-not-defined | time-range | text-match)?
   *                            param-filter*>
   *
   *    <!ATTLIST prop-filter name CDATA #REQUIRED>
   */
  private PropFilter parsePropFilter(Node nd) throws WebdavException {
    try {
      PropFilter pf = new PropFilter(getOnlyAttrVal(nd, "name"));

      Element[] children = getChildren(nd);

      if (children.length == 0) {
        // Presence filter
        return pf;
      }

      int i = 0;
      Node curnode = children[i];

      if (XmlUtil.nodeMatches(curnode, CarddavTags.isNotDefined)) {
        pf.setIsNotDefined(true);
        i++;
      } else if (XmlUtil.nodeMatches(curnode, CarddavTags.textMatch)) {
        pf.setMatch(parseTextMatch(curnode));
        i++;
      }

      if (debug) {
        trace("propFilter element: " +
              curnode.getNamespaceURI() + " " +
              curnode.getLocalName());
      }

      while (i < children.length) {
        curnode = children[i];
        if (debug) {
          trace("propFilter element: " +
                curnode.getNamespaceURI() + " " +
                curnode.getLocalName());
        }

        // Can only have param-filter*
        if (!XmlUtil.nodeMatches(curnode, CarddavTags.paramFilter)) {
          throw new WebdavBadRequest();
        }

        ParamFilter parf = parseParamFilter(curnode);

        pf.addParamFilter(parf);
        i++;
      }

      return pf;
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavBadRequest(t.getMessage());
    }
  }

  /* The given node must be a param-filter element
   *    <!ELEMENT param-filter (is-not-defined | text-match) >
   *
   *    <!ATTLIST param-filter name CDATA #REQUIRED>
   */
  private ParamFilter parseParamFilter(Node nd) throws WebdavException {
    String name = getOnlyAttrVal(nd, "name");

    // Only one child - either is-defined | text-match
    Element child = getOnlyChild(nd);

    if (debug) {
      trace("paramFilter element: " +
            child.getNamespaceURI() + " " +
            child.getLocalName());
    }

    if (XmlUtil.nodeMatches(child, CarddavTags.isNotDefined)) {
      return new ParamFilter(name, true);
    }

    if (XmlUtil.nodeMatches(child, CarddavTags.textMatch)) {
      TextMatch match = parseTextMatch(child);

      return new ParamFilter(name, match);
    }

    throw new WebdavBadRequest();
  }

  /* The given node must be a text-match element
   *  <!ELEMENT text-match #PCDATA>
   *
   *  <!ATTLIST text-match caseless (yes|no)>
   */
  private TextMatch parseTextMatch(Node nd) throws WebdavException {
    //int numAttrs = XmlUtil.numAttrs(nd);
    int numValid = 0;

    Boolean caseless = null;
    caseless = yesNoAttr(nd, "caseless");
    if (caseless != null) {
      numValid++;
    }

    Boolean tempBool = null;
    boolean negated = false;
    tempBool = yesNoAttr(nd, "negate-condition");
    if (tempBool != null) {
      numValid++;
      negated = tempBool;
    }

    int matchType = TextMatch.matchTypeContains;
    String mtype = getAttrVal(nd, "match-type");

    if (mtype != null) {
      matchType = -1;
      for (int i = 1; i < TextMatch.matchTypes.length; i++) {
        if (TextMatch.matchTypes[i].equals(mtype)) {
          matchType = i;
          break;
        }
      }

      if (matchType < 0) {
        throw new WebdavBadRequest("Bad match-type attribute " + mtype);
      }
    }
    /*
    if (numAttrs != numValid) {
      throw new WebdavBadRequest();
    }
    */

    try {
      return new TextMatch(caseless, matchType, negated,
                           XmlUtil.getReqOneNodeVal(nd));
    } catch (Throwable t) {
      throw new WebdavBadRequest();
    }
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
   * @return Collection of prop filter
   */
  public Collection<PropFilter> getPropFilters() {
    if (propFilters == null) {
      propFilters = new ArrayList<PropFilter>();
    }

    return propFilters;
  }

  /**
   * @return boolean true if we have prop filters
   */
  public boolean hasPropFilters() {
    return ((propFilters == null) || (propFilters.size() != 0));
  }

  /**
   * @param pf
   */
  public void addPropFilter(PropFilter pf) {
    getPropFilters().add(pf);
  }

  private Element[] getChildren(Node nd) throws WebdavException {
    try {
      return XmlUtil.getElementsArray(nd);
    } catch (Throwable t) {
      if (debug) {
        getLogger().error("<filter>: parse exception: ", t);
      }

      throw new WebdavBadRequest();
    }
  }

  private Element getOnlyChild(Node nd) throws WebdavException {
    try {
      return XmlUtil.getOnlyElement(nd);
    } catch (Throwable t) {
      if (debug) {
        getLogger().error("<filter>: parse exception: ", t);
      }

      throw new WebdavBadRequest();
    }
  }

  private String getAttrVal(Node nd, String name) throws WebdavException {
    NamedNodeMap nnm = nd.getAttributes();

    if (nnm == null) {
      return null;
    }

    return XmlUtil.getAttrVal(nnm, name);
  }

  private String getOnlyAttrVal(Node nd, String name) throws WebdavException {
    NamedNodeMap nnm = nd.getAttributes();

    if ((nnm == null) || (nnm.getLength() != 1)) {
      throw new WebdavBadRequest("Missing or multiple attribute " + name);
    }

    String res = XmlUtil.getAttrVal(nnm, name);
    if (res == null) {
      throw new WebdavBadRequest("Missing or multiple attribute " + name);
    }

    return res;
  }

  private Boolean yesNoAttr(Node nd, String name) throws WebdavException {
    NamedNodeMap nnm = nd.getAttributes();

    if ((nnm == null) || (nnm.getLength() == 0)) {
      return null;
    }

    try {
      return XmlUtil.getYesNoAttrVal(nnm, name);
    } catch (Throwable t) {
      throw new WebdavBadRequest(t.getMessage());
    }
  }

  /** ===================================================================
   *                   Dump methods
   *  =================================================================== */

  public void dump() {
    trace("<filter>");

    if (propFilters != null) {
      for (PropFilter pf: propFilters) {
        pf.dump(log, "  ");
      }
    }

    trace("</filter>");
  }

  /** ===================================================================
   *                   Logging methods
   *  =================================================================== */

  /**
   * @return Logger
   */
  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  protected void debugMsg(String msg) {
    getLogger().debug(msg);
  }

  protected void error(Throwable t) {
    getLogger().error(this, t);
  }

  protected void logIt(String msg) {
    getLogger().info(msg);
  }

  protected void trace(String msg) {
    getLogger().debug(msg);
  }
}

