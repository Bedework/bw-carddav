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

import org.bedework.carddav.common.vcard.Card;
import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;
import org.bedework.util.vcard.VcardDefs;
import org.bedework.util.xml.XmlEmit;
import org.bedework.util.xml.XmlUtil;
import org.bedework.util.xml.tagdefs.CarddavTags;
import org.bedework.webdav.servlet.shared.WebdavBadRequest;
import org.bedework.webdav.servlet.shared.WebdavException;
import org.bedework.webdav.servlet.shared.WebdavNsNode;
import org.bedework.webdav.servlet.shared.WebdavProperty;

import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.property.Kind;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;

/** Class to represent a address-query address-data element
 *
 *   @author Mike Douglass   douglm   rpi.edu
 */
public class AddressData extends WebdavProperty implements Logged {
  /*
      <!ELEMENT address-data (allprop | prop*)>

      <!ELEMENT address-data (#PCDATA)>
         pcdata is for response

      <!ATTLIST address-data content-type CDATA "text/vcard"
                             version CDATA "4.0">

      <!ELEMENT allprop EMPTY>

      <!ELEMENT prop EMPTY>

      <!ATTLIST prop name CDATA #REQUIRED
                     novalue (yes|no) "no">
   */
  private String returnContentType; // null for defaulted

  private String version = "3.0"; // null for defaulted

  // true or look at props
  private boolean allprop;

  private Collection<Prop> props; // null for zero

  /** Constructor
   *
   * @param tag  QName name
   */
  public AddressData(final QName tag) {
    super(tag, null);
  }

  /**
   * @return String returnContentType
   */
  public String getReturnContentType() {
    return returnContentType;
  }

  /**
   * @return String version
   */
  public String getVersion() {
    return version;
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
      props = new ArrayList<>();
    }

    return props;
  }

  /** The given node must be the Filter element
   *
   * @param nd filter element
   */
  public void parse(final Node nd) {
    /* Either empty - show everything or
              comp + optional (expand-recurrence-set or
                               limit-recurrence-set)
     */
    final NamedNodeMap nnm = nd.getAttributes();

    if (nnm != null) {
      for (int nnmi = 0; nnmi < nnm.getLength(); nnmi++) {
        final Node attr = nnm.item(nnmi);

        switch (attr.getNodeName()) {
          case "content-type" -> {
            returnContentType = attr.getNodeValue();
            if (returnContentType == null) {
              throw new WebdavBadRequest();
            }
          }
          case "xmlns" -> {
          }
          case "version" -> {
            version = attr.getNodeValue();
            if ((version == null) || !VcardDefs.validVersions.contains(
                    version)) {
              throw new WebdavBadRequest("Bad version");
            }
          }
          default ->
            // Bad attribute(s)
                  throw new WebdavBadRequest();
        }
      }
    }

    final Element[] children = getChildren(nd);
    boolean hadProps = false;

    try {
      for (final Element curnode : children) {
        if (debug()) {
          debug("calendar-data node type: " +
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
            props = new ArrayList<>();
          }

          props.add(parseProp(curnode));
          hadProps = true;
        } else {
          throw new WebdavBadRequest();
        }
      }
    } catch (final WebdavBadRequest wbr) {
      throw wbr;
    } catch (final Throwable t) {
      throw new WebdavBadRequest();
    }
  }

  /** Given the WebdavNsNode, returns the transformed content.
   *
   * @param wdnode the node
   * @param xml output
   * @param contentType - first element from content type or null
   */
  public void process(final WebdavNsNode wdnode,
                      final XmlEmit xml,
                      final String contentType) {
    if (!(wdnode instanceof final CarddavCardNode node)) {
      return;
    }

    if (allprop || (props == null)) {
      node.writeContent(xml, null, contentType);
      return;
    }

    /* Ensure node exists */
    node.init(true);
    if (!node.getExists()) {
      throw new WebdavException(HttpServletResponse.SC_NOT_FOUND);
    }

    xml.cdataValue(transformVcard(node.getCard(), props));
  }

  /* Transform one or more VCARD objects based on a list of required
   * properties.
   */
  private String transformVcard(final Card card,
                                final Collection<Prop> props) {
    try {
      final Card ncard = new Card();

      /* Always set FN and KIND */
      boolean fnSet = false;
      boolean kindSet = false;

      for (final Prop pr: props) {
        final List<Property> ps = card.findProperties(pr.getName());
        if (Util.isEmpty(ps)) {
          continue;
        }

        for (final Property p: ps) {
          ncard.addProperty(p);

          if (p.getId() == Property.Id.KIND) {
            kindSet = true;
          } else if (p.getId() == Property.Id.FN) {
            fnSet = true;
          }
        }
      }

      if (!kindSet) {
        final Property p = card.findProperty(Property.Id.KIND);

        if (p != null) {
          ncard.addProperty(p);
        } else {
          ncard.addProperty(Kind.INDIVIDUAL);
        }
      }

      if (!fnSet) {
        final Property p = card.findProperty(Property.Id.FN);

        if (p != null) {
          ncard.addProperty(p);
        } else {
          // This is not valid
          // ncard.addProperty(Kind.INDIVIDUAL);
        }
      }

      return ncard.outputVcard(getVersion());
    } catch (final Throwable t) {
      if (debug()) {
        getLogger().error("transformVcard exception: ", t);
      }

      throw new WebdavBadRequest();
    }
  }

  /* ==============================================================
   *                   Private parsing methods
   * ============================================================== */

  private Prop parseProp(final Node nd) {
    final NamedNodeMap nnm = nd.getAttributes();

    if ((nnm == null) || (nnm.getLength() == 0)) {
      throw new WebdavBadRequest();
    }

    int attrCt = nnm.getLength();

    final String name = XmlUtil.getAttrVal(nnm, "name");
    if (name == null) {
      throw new WebdavBadRequest();
    }

    attrCt--;

    Boolean val;

    try {
      val = XmlUtil.getYesNoAttrVal(nnm, "novalue");
    } catch (final Throwable t) {
      throw new WebdavBadRequest();
    }

    final Prop pr = new Prop(name);

    if (val != null) {
      pr.setNovalue(val);
    }

    return pr;
  }

  private Element[] getChildren(final Node nd) {
    try {
      return XmlUtil.getElementsArray(nd);
    } catch (final Throwable t) {
      if (debug()) {
        error("<filter>: parse exception: ", t);
      }

      throw new WebdavBadRequest();
    }
  }

  /* ==============================================================
   *                   Dump methods
   * ============================================================== */

  /**
   *
   */
  public void dump() {
    final StringBuilder sb = new StringBuilder("  <address-data");

    if (returnContentType != null) {
      sb.append("  return-content-type=\"")
        .append(returnContentType)
        .append("\"");
    }

    if (version != null) {
      sb.append("  version=\"")
        .append(version)
        .append("\"");
    }
    sb.append(">");
    debug(sb.toString());

    debug("  </address-data>");
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

