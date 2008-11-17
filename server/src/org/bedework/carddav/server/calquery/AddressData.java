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
package org.bedework.carddav.server.calquery;

import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.carddav.server.Vcard;
import org.bedework.carddav.server.Vcard.Property;

import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode;
import edu.rpi.cct.webdav.servlet.shared.WebdavProperty;
import edu.rpi.sss.util.xml.XmlUtil;
import edu.rpi.sss.util.xml.tagdefs.CarddavTags;

import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/** Class to represent a calendar-query calendar-data element
 *
 *   @author Mike Douglass   douglm@rpi.edu
 */
public class AddressData extends WebdavProperty {
  /*
      <!ELEMENT address-data (allprop | prop*)>

      <!ELEMENT address-data (#PCDATA)>
         pcdata is for response

      <!ATTLIST address-data content-type CDATA "text/vcard"
                             version CDATA "3.0">

      <!ELEMENT allprop EMPTY>

      <!ELEMENT prop EMPTY>

      <!ATTLIST prop name CDATA #REQUIRED
                     novalue (yes|no) "no">
   */
  private boolean debug;

  protected transient Logger log;

  private String returnContentType; // null for defaulted

  // true or look at props
  private boolean allprop;

  private Collection<Prop> props; // null for zero

  /** Constructor
   *
   * @param tag  QName name
   * @param debug
   */
  public AddressData(QName tag,
                      boolean debug) {
    super(tag, null);
    this.debug = debug;
  }

  /**
   * @return String returnContentType
   */
  public String getReturnContentType() {
    return returnContentType;
  }

  /**
   * @return boolean true for all props
   */
  public boolean getAllprop() {
    return allprop;
  }

  /**
   * @return Vector of props
   */
  public Collection<Prop> getProps() {
    if (props == null) {
      props = new ArrayList<Prop>();
    }

    return props;
  }

  /** The given node must be the Filter element
   *
   * @param nd
   * @throws WebdavException
   */
  public void parse(Node nd) throws WebdavException {
    /* Either empty - show everything or
              comp + optional (expand-recurrence-set or
                               limit-recurrence-set)
     */
    NamedNodeMap nnm = nd.getAttributes();

    if (nnm != null) {
      for (int nnmi = 0; nnmi < nnm.getLength(); nnmi++) {
        Node attr = nnm.item(nnmi);

        if (attr.getNodeName().equals("content-type")) {
          returnContentType = attr.getNodeValue();
          if (returnContentType == null) {
            throw new WebdavBadRequest();
          }
        } else if (attr.getNodeName().equals("xmlns")) {
        } else {
          // Bad attribute(s)
          throw new WebdavBadRequest();
        }
      }
    }

    Element[] children = getChildren(nd);
    boolean hadProps = false;

    try {
      for (int i = 0; i < children.length; i++) {
        Node curnode = children[i];

        if (debug) {
          trace("calendar-data node type: " +
              curnode.getNodeType() + " name:" +
              curnode.getNodeName());
        }

        if (XmlUtil.nodeMatches(curnode, CarddavTags.allprop)) {
          if (hadProps) {
            throw new WebdavBadRequest();
          }

          allprop = true;
        } else if (XmlUtil.nodeMatches(curnode, CarddavTags.prop)) {
          if (allprop) {
            throw new WebdavBadRequest();
          }

          if (props == null) {
            props = new ArrayList<Prop>();
          }

          props.add(parseProp(curnode));
          hadProps = true;
        } else {
          throw new WebdavBadRequest();
        }
      }
    } catch (WebdavBadRequest wbr) {
      throw wbr;
    } catch (Throwable t) {
      throw new WebdavBadRequest();
    }
  }

  /** Given the CaldavBwNode, returns the transformed content.
   *
   * @param wdnode
   * @return String content
   * @throws WebdavException
   */
  public String process(WebdavNsNode wdnode) throws WebdavException {
    if (!(wdnode instanceof CarddavCardNode)) {
      return null;
    }

    CarddavCardNode node = (CarddavCardNode)wdnode;

    if (allprop || (props == null)) {
      return node.getContentString();
    }

    /** Ensure node exists */
    node.init(true);
    if (!node.getExists()) {
      throw new WebdavException(HttpServletResponse.SC_NOT_FOUND);
    }

    return transformVcard(node.getCard(), props);
  }

  /* Transform one or more VEVENT objects based on a list of required
   * properties.
   */
  private String transformVcard(Vcard card,
                                Collection<Prop> props)  throws WebdavException {
    try {
      Vcard ncard = new Vcard();

      for (Prop pr: props) {
        Property p = card.findProperty(pr.getName());

        if (p != null) {
          ncard.addProperty(p);
        }
      }

      return ncard.output();
    } catch (Throwable t) {
      if (debug) {
        getLogger().error("transformVcard exception: ", t);
      }

      throw new WebdavBadRequest();
    }
  }

  /* ====================================================================
   *                   Private parsing methods
   * ==================================================================== */

  private Prop parseProp(Node nd) throws WebdavException {
    NamedNodeMap nnm = nd.getAttributes();

    if ((nnm == null) || (nnm.getLength() == 0)) {
      throw new WebdavBadRequest();
    }

    int attrCt = nnm.getLength();

    String name = XmlUtil.getAttrVal(nnm, "name");
    if (name == null) {
      throw new WebdavBadRequest();
    }

    attrCt--;

    Boolean val = null;

    try {
      val = XmlUtil.getYesNoAttrVal(nnm, "novalue");
    } catch (Throwable t) {
      throw new WebdavBadRequest();
    }

    Prop pr = new Prop(name);

    if (val != null) {
      pr.setNovalue(val.booleanValue());
    }

    return pr;
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

  /* ====================================================================
   *                   Dump methods
   * ==================================================================== */

  /**
   *
   */
  public void dump() {
    StringBuilder sb = new StringBuilder("  <address-data");

    if (returnContentType != null) {
      sb.append("  return-content-type=\"");
      sb.append(returnContentType);
      sb.append("\"");
    }
    sb.append(">");
    trace(sb.toString());

    trace("  </address-data>");
  }

  /* ====================================================================
   *                   Logging methods
   * ==================================================================== */

  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  protected void debugMsg(String msg) {
    getLogger().debug(msg);
  }

  protected void logIt(String msg) {
    getLogger().info(msg);
  }

  protected void trace(String msg) {
    getLogger().debug(msg);
  }
}

