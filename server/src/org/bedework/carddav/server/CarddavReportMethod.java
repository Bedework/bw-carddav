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

package org.bedework.carddav.server;

import org.bedework.carddav.server.calquery.AddressData;
import org.bedework.carddav.server.filter.Filter;

import edu.rpi.cct.webdav.servlet.common.ReportMethod;
import edu.rpi.cct.webdav.servlet.common.PropFindMethod.PropRequest;
import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode;
import edu.rpi.cct.webdav.servlet.shared.WebdavProperty;
import edu.rpi.cct.webdav.servlet.shared.WebdavStatusCode;
import edu.rpi.sss.util.xml.XmlUtil;
import edu.rpi.sss.util.xml.tagdefs.CarddavTags;
import edu.rpi.sss.util.xml.tagdefs.WebdavTags;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Class called to handle OPTIONS. We should determine what the current
 * url refers to and send a response which shows the allowable methods on that
 * resource.
 *
 *   @author Mike Douglass   douglm@rpi.edu
 */
public class CarddavReportMethod extends ReportMethod {
  /* The parsed results go here. We see:
   *  1. Free-busy request
   *  2. Query - optional props + filter
   *  3. Multi-get - optional props + one or more hrefs
   */

  private Filter filter;
  private ArrayList<String> hrefs;

  AddressData caldata;

  // ENUM
  private final static int reportTypeQuery = 0;
  private final static int reportTypeMultiGet = 1;

  private int reportType;

  /** Called at each request
   */
  public void init() {
  }

  /* We process the parsed document and produce a response
   *
   * @param doc
   * @throws WebdavException
   */
  protected void process(Document doc,
                         int depth,
                         HttpServletRequest req,
                         HttpServletResponse resp) throws WebdavException {
    reportType = getCaldavReportType(doc);

    if (reportType < 0) {
      super.process(doc, depth, req, resp);
      return;
    }

    processDoc(doc);

    processResp(req, resp, depth);
  }

  /** See if we recognize this report type and return an index.
   *
   * @param doc
   * @return index or <0 for unknown.
   * @throws WebdavException
   */
  protected int getCaldavReportType(Document doc) throws WebdavException {
    try {
      Element root = doc.getDocumentElement();

      if (XmlUtil.nodeMatches(root, CarddavTags.addressbookQuery)) {
        return reportTypeQuery;
      }

      if (XmlUtil.nodeMatches(root, CarddavTags.addressbookMultiget)) {
        return reportTypeMultiGet;
      }

      return -1;
    } catch (Throwable t) {
      System.err.println(t.getMessage());
      if (debug) {
        t.printStackTrace();
      }

      throw new WebdavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  /* We process the parsed document and produce a Collection of request
   * objects to process.
   *
   * @param doc
   * @throws WebdavException
   */
  private void processDoc(Document doc) throws WebdavException {
    try {
      Element root = doc.getDocumentElement();

      Collection<Element> children = getChildren(root);

      /* Two possibilities:
               <!ELEMENT addressbook-multiget ((DAV:allprop |
                                      DAV:propname |
                                      DAV:prop)?, DAV:href+)>

               <!ELEMENT addressbook-query ((DAV:allprop |
                                    DAV:propname |
                                    DAV:prop)?, filter)>
       */

      if (children.isEmpty()) {
        throw new WebdavBadRequest();
      }

      Iterator<Element> chiter = children.iterator();

      /* First try for a property request */
      preq = pm.tryPropRequest(chiter.next());

      if (preq != null) {
        if (debug) {
          trace("REPORT: preq not null");
        }

        if (preq.reqType == PropRequest.ReqType.prop) {
          // Look for a calendar-data property
          for (WebdavProperty prop: preq.props) {
            if (prop instanceof AddressData) {
              caldata = (AddressData)prop;
            }
          }
        }
      }

      if (!chiter.hasNext()) {
        throw new WebdavBadRequest();
      }

      Element curnode = chiter.next();

      if (reportType == reportTypeQuery) {
        // Filter required next

        if (!XmlUtil.nodeMatches(curnode, CarddavTags.filter)) {
          throw new WebdavBadRequest("Expected filter");
        }

        // Delay parsing until we see if we have a timezone
        Element filterNode = curnode;

        if (chiter.hasNext()) {
          throw new WebdavBadRequest("Unexpected elements");
        }

        filter = new Filter(debug);
        filter.carddavParse(filterNode);

        if (debug) {
          trace("REPORT: query");
          filter.dump();
        }

        return;
      }

      if (reportType == reportTypeMultiGet) {
        // One or more hrefs

        for (;;) {
          if (!XmlUtil.nodeMatches(curnode, WebdavTags.href)) {
            throw new WebdavBadRequest("Expected href");
          }

          String href = XmlUtil.getElementContent(curnode);

          if (href != null) {
            String decoded;
            try {
              decoded = URLDecoder.decode(href, "UTF8");
            } catch (Throwable t) {
              throw new WebdavBadRequest("bad href: " + href);
            }

            href = decoded;
          }

          if ((href == null) || (href.length() == 0)) {
            throw new WebdavBadRequest("Bad href");
          }

          if (hrefs == null) {
            hrefs = new ArrayList<String>();
          }

          hrefs.add(href);
          if (!chiter.hasNext()) {
            break;
          }
          curnode = chiter.next();
        }

        if (hrefs == null) {
          // need at least 1
          throw new WebdavBadRequest("Expected href");
        }

        if (debug) {
          trace("REPORT: multi-get");

          for (String href: hrefs) {
            trace("    <DAV:href>" + href + "</DAV:href>");
          }
        }

        return;
      }

      if (debug) {
        trace("REPORT: unexpected element " + curnode.getNodeName() +
              " with type " + curnode.getNodeType());
      }
      throw new WebdavBadRequest("REPORT: unexpected element " + curnode.getNodeName() +
                                 " with type " + curnode.getNodeType());
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      error(t);
      throw new WebdavException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * @param req
   * @param resp
   * @param depth
   * @throws WebdavException
   */
  public void processResp(HttpServletRequest req,
                          HttpServletResponse resp,
                          int depth) throws WebdavException {
    resp.setStatus(WebdavStatusCode.SC_MULTI_STATUS);
    resp.setContentType("text/xml; charset=UTF-8");

    startEmit(resp);

    String resourceUri = getResourceUri(req);

    CarddavBWIntf intf = (CarddavBWIntf)getNsIntf();
    WebdavNsNode node = intf.getNode(resourceUri,
                                     WebdavNsIntf.existanceMust,
                                     WebdavNsIntf.nodeTypeUnknown);

    openTag(WebdavTags.multistatus);

    int status = HttpServletResponse.SC_OK;

    Collection<WebdavNsNode> nodes = null;

    if (reportType == reportTypeQuery) {
      nodes = (Collection<WebdavNsNode>)doNodeAndChildren(node, 0, defaultDepth(depth, 0));
    } else if (reportType == reportTypeMultiGet) {
      nodes = new ArrayList<WebdavNsNode>();

      if (hrefs != null) {
        for (String hr: hrefs) {
          try {
            nodes.add(intf.getNode(intf.getUri(hr),
                                   WebdavNsIntf.existanceMust,
                                   WebdavNsIntf.nodeTypeUnknown));
          } catch (WebdavException we) {
            if (hr.endsWith("/")) {
              nodes.add((WebdavNsNode)new CarddavColNode(intf.getSysi(),
                                                        we.getStatusCode(),
                                                        intf.getUri(hr), debug));
            } else {
              nodes.add((WebdavNsNode)new CarddavCardNode(intf.getSysi(),
                                                              we.getStatusCode(),
                                                              intf.getUri(hr), debug));
            }
          }
        }
      }
    }

    if (status != HttpServletResponse.SC_OK) {
      if (debug) {
        trace("REPORT status " + status);
      }
      // Entire request failed.
      node.setStatus(status);
      doNodeProperties(node);
    } else if (nodes != null) {
      for (WebdavNsNode curnode: nodes) {
        doNodeProperties(curnode);
      }
    }

    closeTag(WebdavTags.multistatus);

    flush();
  }

  private Collection<WebdavNsNode> getNodes(WebdavNsNode node)
          throws WebdavException {
    if (debug) {
      trace("getNodes: " + node.getUri());
    }

    CarddavBWIntf intf = (CarddavBWIntf)getNsIntf();

    return intf.query(node, filter);
  }

  private Collection<WebdavNsNode> doNodeAndChildren(WebdavNsNode node,
                                       int curDepth,
                                       int maxDepth) throws WebdavException {
    if (node instanceof CarddavCardNode) {
      // Targetted directly at component
      Collection<WebdavNsNode> nodes = new ArrayList<WebdavNsNode>();

      nodes.add(node);
      return nodes;
    }

    if (!(node instanceof CarddavColNode)) {
      throw new WebdavBadRequest();
    }

    if (debug) {
      trace("doNodeAndChildren: curDepth=" + curDepth +
            " maxDepth=" + maxDepth + " uri=" + node.getUri());
    }

    CarddavColNode colnode = (CarddavColNode)node;

    if (colnode.getWdCollection().getAddressBook()) {
      return getNodes(node);
    }

    Collection<WebdavNsNode> nodes = new ArrayList<WebdavNsNode>();

    curDepth++;

    if (curDepth > maxDepth) {
      return nodes;
    }

    for (WebdavNsNode child: getNsIntf().getChildren(node)) {
      nodes.addAll(doNodeAndChildren(child, curDepth, maxDepth));
    }

    return nodes;
  }
}

