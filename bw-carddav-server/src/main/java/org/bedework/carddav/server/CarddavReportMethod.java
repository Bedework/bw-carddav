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
package org.bedework.carddav.server;

import org.bedework.carddav.common.GetLimits;
import org.bedework.carddav.common.filter.Filter;
import org.bedework.carddav.server.CarddavBWIntf.QueryResult;
import org.bedework.carddav.server.query.AddressData;
import org.bedework.util.xml.XmlUtil;
import org.bedework.util.xml.tagdefs.CarddavTags;
import org.bedework.util.xml.tagdefs.WebdavTags;
import org.bedework.webdav.servlet.common.PropFindMethod.PropRequest;
import org.bedework.webdav.servlet.common.ReportMethod;
import org.bedework.webdav.servlet.shared.WebdavBadRequest;
import org.bedework.webdav.servlet.shared.WebdavException;
import org.bedework.webdav.servlet.shared.WebdavNsIntf;
import org.bedework.webdav.servlet.shared.WebdavNsNode;
import org.bedework.webdav.servlet.shared.WebdavProperty;
import org.bedework.webdav.servlet.shared.WebdavStatusCode;

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
 *   @author Mike Douglass   douglm  rpi.edu
 */
public class CarddavReportMethod extends ReportMethod {
  /* The parsed results go here. We see:
   *  1. Free-busy request
   *  2. Query - optional props + filter
   *  3. Multi-get - optional props + one or more hrefs
   */

  private Filter filter;
  private GetLimits limit;
  private ArrayList<String> hrefs;

  AddressData adrdata;

  // ENUM
  private final static int reportTypeQuery = 0;
  private final static int reportTypeMultiGet = 1;

  private int reportType;

  /** Called at each request
   */
  @Override
  public void init() {
  }

  /* We process the parsed document and produce a response
   *
   * @param doc
   * @throws WebdavException
   */
  @Override
  protected void process(final Document doc,
                         final int depth,
                         final HttpServletRequest req,
                         final HttpServletResponse resp) throws WebdavException {
    reportType = getCarddavReportType(doc);

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
  protected int getCarddavReportType(final Document doc) throws WebdavException {
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
  private void processDoc(final Document doc) throws WebdavException {
    try {
      Element root = doc.getDocumentElement();

      Collection<Element> children = getChildren(root);

      /* Two possibilities:
               <!ELEMENT addressbook-multiget ((DAV:allprop |
                                      DAV:propname |
                                      DAV:prop)?, DAV:href+)>

               <!ELEMENT addressbook-query ((DAV:allprop |
                                     DAV:propname |
                                     DAV:prop)?, filter, limit?)>
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
          // Look for an address-data property
          for (WebdavProperty prop: preq.props) {
            if (prop instanceof AddressData) {
              adrdata = (AddressData)prop;
            }
          }
        }
      }

      if (!chiter.hasNext()) {
        if (reportType == reportTypeQuery) {
          throw new WebdavBadRequest("Require filter element");
        }
        if (reportType == reportTypeMultiGet) {
          throw new WebdavBadRequest("Require one or more hrefs");
        }
        throw new WebdavBadRequest();
      }

      Element curnode = chiter.next();

      if (reportType == reportTypeQuery) {
        // Filter required next
        if (!XmlUtil.nodeMatches(curnode, CarddavTags.filter)) {
          throw new WebdavBadRequest("Expected filter");
        }

        filter = new Filter(debug);
        filter.carddavParse(curnode);

        if (debug) {
          trace("REPORT: query");
          filter.dump();
        }

        if (!chiter.hasNext()) {
          return; // no limit
        }

        curnode = chiter.next();

        // Only limit possible now
        if (!XmlUtil.nodeMatches(curnode, CarddavTags.limit)) {
          throw new WebdavBadRequest("Expected filter or limit");
        }

        if (chiter.hasNext()) {
          throw new WebdavBadRequest("Unexpected elements");
        }

        Collection<Element> limitChildren = getChildren(curnode);

        for (Element limNode: limitChildren) {
          if (!XmlUtil.nodeMatches(limNode, CarddavTags.nresults)) {
            throw new WebdavBadRequest("Bad limit element");
          }

          limit = new GetLimits();

          try {
            limit.limit = Integer.parseInt(XmlUtil.getElementContent(limNode));
          } catch (Throwable t) {
            throw new WebdavBadRequest("Bad limit nresults value");
          }
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
  public void processResp(final HttpServletRequest req,
                          final HttpServletResponse resp,
                          final int depth) throws WebdavException {
    resp.setStatus(WebdavStatusCode.SC_MULTI_STATUS);
    resp.setContentType("text/xml; charset=UTF-8");

    startEmit(resp);

    String resourceUri = getResourceUri(req);

    CarddavBWIntf intf = (CarddavBWIntf)getNsIntf();
    WebdavNsNode node = intf.getNode(resourceUri,
                                     WebdavNsIntf.existanceMust,
                                     WebdavNsIntf.nodeTypeUnknown,
                                     false);

    openTag(WebdavTags.multistatus);

    int status = HttpServletResponse.SC_OK;

    Collection<WebdavNsNode> nodes = null;

    if (reportType == reportTypeQuery) {
      QueryResult qr = doNodeAndChildren(node, 0, defaultDepth(depth, 0));

      if (qr.overLimit || qr.serverTruncated) {
        node.setStatus(507);
        doNodeProperties(node);
      }
      nodes = qr.nodes;
    } else if (reportType == reportTypeMultiGet) {
      nodes = new ArrayList<WebdavNsNode>();

      if (hrefs != null) {
        for (String hr: hrefs) {
          try {
            WebdavNsNode nd = intf.getNode(intf.getUri(hr),
                                           WebdavNsIntf.existanceMust,
                                           WebdavNsIntf.nodeTypeUnknown,
                                           false);

            if (nd instanceof CarddavCardNode) {
              ((CarddavCardNode)nd).setVcardVersion(getVcardVersion());
            }
            nodes.add(nd);
          } catch (WebdavException we) {
            if (hr.endsWith("/")) {
              nodes.add(new CarddavColNode(intf.getSysi(),
                                           we.getStatusCode(),
                                           intf.getUri(hr)));
            } else {
            	nodes.add(new CarddavCardNode(intf.getSysi(),
            	                              we.getStatusCode(),
            	                              intf.getUri(hr)));
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

  private String getVcardVersion() throws WebdavException {
    String reqv = null;
    if (adrdata != null) {
      reqv = adrdata.getVersion();
    }

    return ((CarddavBWIntf)getNsIntf()).getVcardVersion(reqv);
  }

  private QueryResult doNodeAndChildren(final WebdavNsNode node,
                                        int curDepth,
                                        final int maxDepth) throws WebdavException {
    if (node instanceof CarddavCardNode) {
      // Targeted directly at component
      QueryResult qr = new QueryResult();

      ((CarddavCardNode)node).setVcardVersion(getVcardVersion());

      qr.nodes.add(node);
      return qr;
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
      CarddavBWIntf intf = (CarddavBWIntf)getNsIntf();

      return intf.query(node, filter, limit, getVcardVersion());
    }

    curDepth++;

    QueryResult qr = new QueryResult();

    if (curDepth > maxDepth) {
      return qr;
    }

    for (WebdavNsNode child: getNsIntf().getChildren(node)) {
      int sz = qr.nodes.size();
      if ((limit != null) && (sz > limit.limit)) {
        qr.overLimit = true;
        break;
      }

      QueryResult subqr = doNodeAndChildren(child, curDepth, maxDepth);
      qr.nodes.addAll(subqr.nodes);

      if (subqr.overLimit) {
        qr.overLimit = true;
        break;
      }

      if (subqr.serverTruncated) {
        qr.serverTruncated = true;
        break;
      }
    }

    return qr;
  }
}

