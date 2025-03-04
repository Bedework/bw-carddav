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

import org.bedework.access.AccessException;
import org.bedework.access.AccessPrincipal;
import org.bedework.access.AccessXmlUtil.AccessXmlCb;
import org.bedework.access.Ace;
import org.bedework.access.AceWho;
import org.bedework.access.Acl;
import org.bedework.access.PrivilegeDefs;
import org.bedework.access.WhoDefs;
import org.bedework.carddav.common.CarddavCollection;
import org.bedework.carddav.common.GetLimits;
import org.bedework.carddav.common.GetResult;
import org.bedework.carddav.common.filter.Filter;
import org.bedework.carddav.common.util.Group;
import org.bedework.carddav.common.util.User;
import org.bedework.carddav.common.vcard.Card;
import org.bedework.carddav.server.SysIntf.PrincipalInfo;
import org.bedework.carddav.server.config.CardDAVContextConfig;
import org.bedework.carddav.server.jmx.CardDav;
import org.bedework.carddav.server.query.AddressData;
import org.bedework.util.misc.Util;
import org.bedework.util.vcard.VcardDefs;
import org.bedework.util.xml.XmlEmit;
import org.bedework.util.xml.XmlEmit.NameSpace;
import org.bedework.util.xml.XmlUtil;
import org.bedework.util.xml.tagdefs.CarddavTags;
import org.bedework.util.xml.tagdefs.WebdavTags;
import org.bedework.webdav.servlet.common.AccessUtil;
import org.bedework.webdav.servlet.common.Headers;
import org.bedework.webdav.servlet.common.Headers.IfHeaders;
import org.bedework.webdav.servlet.common.MethodBase.MethodInfo;
import org.bedework.webdav.servlet.common.WebdavServlet;
import org.bedework.webdav.servlet.common.WebdavUtils;
import org.bedework.webdav.servlet.shared.PrincipalPropertySearch;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WdEntity;
import org.bedework.webdav.servlet.shared.WdSynchReport;
import org.bedework.webdav.servlet.shared.WdSysIntf;
import org.bedework.webdav.servlet.shared.WebdavBadRequest;
import org.bedework.webdav.servlet.shared.WebdavException;
import org.bedework.webdav.servlet.shared.WebdavForbidden;
import org.bedework.webdav.servlet.shared.WebdavNotFound;
import org.bedework.webdav.servlet.shared.WebdavNsIntf;
import org.bedework.webdav.servlet.shared.WebdavNsNode;
import org.bedework.webdav.servlet.shared.WebdavPrincipalNode;
import org.bedework.webdav.servlet.shared.WebdavProperty;
import org.bedework.webdav.servlet.shared.WebdavServerError;
import org.bedework.webdav.servlet.shared.WebdavUnauthorized;
import org.bedework.webdav.servlet.shared.WebdavUnsupportedMediaType;

import net.fortuna.ical4j.util.CompatibilityHints;
import org.w3c.dom.Element;

import java.io.CharArrayReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.function.Supplier;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.xml.namespace.QName;

/** This class implements a namespace interface for the webdav abstract
 * servlet. One of these interfaces is associated with each current session.
 *
 * <p>As a first pass we'll define webdav urls as starting with <br/>
 * /user/user-name/calendar-name/<br/>
 *
 * <p>uri resolution should be made part of the core calendar allowing all
 * such distinctions to be removed from this code.
 *
 * <p>The part following the above prefix probably determines exactly what is
 * delivered. We may want the entire calendar (or what we show by default) or
 * a single event from the calendar
 *
 *   @author Mike Douglass   douglm   rpi.edu
 */
public class CarddavBWIntf extends WebdavNsIntf {
  /** Namespace prefix based on the request url.
   */
  private String namespacePrefix;

  private AccessUtil accessUtil;

  private CardDav confBean;

  /** Namespace based on the request url.
   */
  @SuppressWarnings("unused")
  private String namespace;

  SysIntf sysi;

  private CardDAVContextConfig config;

  /* We store CaldavURI objects here
   * /
  private HashMap<String, CaldavURI> uriMap = new HashMap<String, CaldavURI>();
  */

  /* ==============================================================
   *                     Interface methods
   * ============================================================== */

  /** Called before any other method is called to allow initialisation to
   * take place at the first or subsequent requests
   *
   * @param servlet our servlet
   * @param req http request
   * @param methods    HashMap   table of method info
   * @param dumpContent if we want content trace
   */
  @Override
  public void init(final WebdavServlet servlet,
                   final HttpServletRequest req,
                   final HashMap<String, MethodInfo> methods,
                   final boolean dumpContent) {
    super.init(servlet, req, methods, dumpContent);

    final HttpSession session = req.getSession();
    final ServletContext sc = session.getServletContext();

    namespacePrefix = WebdavUtils.getUrlPrefix(req);
    namespace = namespacePrefix + "/schema";

    confBean = ((CarddavServlet)servlet).getConf();

    loadConfig(sc.getInitParameter("bwappname"));

    /* Set ical4j so that it allows some older constructs */
    CompatibilityHints.setHintEnabled(CompatibilityHints.KEY_RELAXED_PARSING,
                                      true);

    sysi = newSysIntf();
    sysi.init(req, account, confBean.getConfig(),
              config);

    accessUtil = new AccessUtil(namespacePrefix, xml,
                                new CardDavAccessXmlCb(sysi));
  }

  /** See if we can reauthenticate. Use for real-time service which needs to
   * authenticate as a particular principal.
   *
   * @param req http request
   * @param account id
   */
  public void reAuth(final HttpServletRequest req,
                     final String account) {
    this.account = account;

    sysi = newSysIntf();
    sysi.init(req, account, confBean.getConfig(),
              config);

    accessUtil = new AccessUtil(namespacePrefix, xml,
                                new CardDavAccessXmlCb(sysi));
  }

  private SysIntf newSysIntf() {
    final String className = confBean.getSysintfImpl();

    Object o;

    try {
      o = Class.forName(className).newInstance();
    } catch (final Throwable t) {
      error(t);
      o = null;
    }

    if (o == null) {
      throw new WebdavException("Class " + className + " not found");
    }

    if (!(o instanceof SysIntf)) {
      throw new WebdavException("Class " + className +
                                   " is not a subclass of " +
                                   SysIntf.class.getName());
    }

    return (SysIntf)o;
  }

  @Override
  public String getAddMemberSuffix() {
    return ";add-member";
  }

  @Override
  public String getDavHeader(final WebdavNsNode node) {
    return super.getDavHeader(node) + ", addressbook";
  }

  protected CardDAVContextConfig getConfig() {
    return config;
  }

  /**
   */
  private static class CardDavAccessXmlCb implements AccessXmlCb, Serializable {
    private final SysIntf sysi;

    private QName errorTag;
    private String errorMsg;

    CardDavAccessXmlCb(final SysIntf sysi) {
      this.sysi = sysi;
    }

    @Override
    public String makeHref(final String id, final int whoType) throws AccessException {
      try {
        final AccessPrincipal p;
        if (whoType == Ace.whoTypeUser) {
          p = new User(id);
        } else {
          p = new Group(id);
        }
        return sysi.makeHref(p);
      } catch (final Throwable t) {
        throw new AccessException(t);
      }
    }

    @Override
    public AccessPrincipal getPrincipal() throws AccessException {
      try {
        return sysi.getPrincipal();
      } catch (final Throwable t) {
        throw new AccessException(t);
      }
    }

    @Override
    public AccessPrincipal getPrincipal(final String href) throws AccessException {
      try {
        return sysi.getPrincipal(href);
      } catch (final Throwable t) {
        throw new AccessException(t);
      }
    }

    @Override
    public void setErrorTag(final QName tag) {
      errorTag = tag;
    }

    @Override
    public QName getErrorTag() {
      return errorTag;
    }

    @Override
    public void setErrorMsg(final String val) {
      errorMsg = val;
    }

    @Override
    public String getErrorMsg() {
      return errorMsg;
    }
  }

  @Override
  public AccessUtil getAccessUtil() {
    return accessUtil;
  }

  @Override
  public boolean canPut(final WebdavNsNode node) {
    final int access;

    if (node instanceof CarddavCardNode) {
      /* access comes from the parent at the moment
      CarddavCardNode comp = (CarddavCardNode)node;

      if (comp.getCard() != null) {
        ent = comp.getCard();
      } else {
        ent = comp.getWdCollection();
        access = PrivilegeDefs.privBind;
      } */
      access = PrivilegeDefs.privBind;

      return sysi.checkAccess(((CarddavCardNode)node).getWdCollection(),
                              access, true).getAccessAllowed();
    } else {
      return false;
    }
  }

  @Override
  public boolean getDirectoryBrowsingDisallowed() {
    return sysi.getDirectoryBrowsingDisallowed();
  }

  @Override
  public void rollback() {
    // No rollback capability at the moment
  }

  @Override
  public void close() {
    sysi.close();
  }

  @Override
  public WdSysIntf getSysIntf() {
    return sysi;
  }

  /**
   * @return SysIntf
   */
  public SysIntf getSysi() {
    return sysi;
  }

  @Override
  public String getSupportedLocks() {
    return null; // No locks
    /*
     return  "<DAV:lockentry>" +
             "  <DAV:lockscope>" +
             "    <DAV:exclusive/>" +
             "  </DAV:lockscope>" +
             "  <DAV:locktype><DAV:write/></DAV:locktype>" +
             "</DAV:lockentry>";
             */
  }

  @Override
  public boolean getAccessControl() {
    return true;
  }

  @Override
  public void addNamespace(final XmlEmit xml) {
    super.addNamespace(xml);

    xml.addNs(new NameSpace(CarddavTags.namespace, "C"), false);
  }

  @Override
  public WebdavNsNode getNode(final String uri,
                              final int existance,
                              final int nodeType,
                              final boolean addMember) {
    return getNodeInt(uri, existance, nodeType, null, null, null);
  }

  @Override
  public void putNode(final WebdavNsNode node) {
  }

  @Override
  public void delete(final WebdavNsNode node) {
    try {
      final CarddavNode cnode = getBwnode(node);

      if (cnode instanceof final CarddavResourceNode rnode) {
        final CarddavResource r = rnode.getResource();

        sysi.deleteFile(r);
      } else if (cnode instanceof CarddavCardNode) {
        if (debug()) {
          debug("About to delete card " + cnode);
        }
        sysi.deleteCard((CarddavCardNode)cnode);
      } else {
        if (!(cnode instanceof CarddavColNode)) {
          throw new WebdavUnauthorized();
        }

        sysi.deleteCollection(cnode.getWdCollection());
      }
    } catch (final WebdavException we) {
      throw we;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public Collection<WebdavNsNode> getChildren(
          final WebdavNsNode node,
          final Supplier<Object> filterGetter) {
    if (!node.isCollection()) {
      // Don't think we should have been called
      return new ArrayList<>();
    }

    if (debug()) {
      debug("About to get children for " + node.getUri());
    }

    Collection<? extends WdEntity<?>> children = null;

    if (node instanceof CarddavNode) {
      final CarddavNode cdnode = getBwnode(node);

      // XXX We'd like to be applying limits here as well I guess
      children = cdnode.getChildren(filterGetter);
    } else {
//        ch = node.getChildren().nodes;
    }

    if (children == null) {
      return new ArrayList<>();
    }

    // TODO - fix this
    return new ArrayList<>();
  }

  @Override
  public WebdavNsNode getParent(final WebdavNsNode node) {
    return null;
  }

  @Override
  public Content getContent(final HttpServletRequest req,
                            final HttpServletResponse resp,
                            final String contentType,
                            final WebdavNsNode node) {
    try {
      final String accept = req.getHeader("ACCEPT");
      String[] acceptPars = {};
      String requestedVersion = "3.0";

      if (accept != null) {
        acceptPars = accept.split(";");

        String reqv = null;
        for (final String s: acceptPars) {
          if (!s.toLowerCase().startsWith("version=")) {
            continue;
          }

          reqv = s.split("=")[1];
        }

        requestedVersion = getVcardVersion(reqv);
      }

      if (node.isCollection()) {
        if ((accept == null) || accept.contains("text/html")) {
          if (getDirectoryBrowsingDisallowed()) {
            throw new WebdavException(HttpServletResponse.SC_FORBIDDEN);
          }

          final Content c = new Content();

          final String content = generateHtml(req, node);
          c.rdr = new CharArrayReader(content.toCharArray());
          c.contentType = "text/html";
          c.contentLength = content.getBytes().length;

          return c;
        }
      }

      /* ===================  Try for address-book fetch ======================= */

      if (node.isCollection() && (acceptPars.length > 0) &&
          (acceptPars[0].trim().equals("text/vcard") ||
              acceptPars[0].trim().startsWith("application/json"))) {
        SpecialUri.process(req, resp, getResourceUri(req), getSysi(), config,
                           true, acceptPars[0], requestedVersion);

        final Content c = new Content();

        c.written = true; // set content to say it's done

        return c;
      }

      if (node.isCollection()) {
        return null;
      }

      if (!node.getAllowsGet()) {
        return null;
      }

      final Content c = new Content();
      c.written = true;

      /* Should be a card node */

      if (node instanceof CarddavCardNode) {
        ((CarddavCardNode)node).setVcardVersion(requestedVersion);
      }

      node.writeContent(null, resp.getWriter(), accept);

      return c;
    } catch (final WebdavException we) {
      throw we;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public Content getBinaryContent(final WebdavNsNode node) {
    try {
      if (!node.getAllowsGet()) {
        return null;
      }

      if (!(node instanceof CarddavResourceNode)) {
        throw new WebdavException("Unexpected node type");
      }

      final CarddavResourceNode bwnode = (CarddavResourceNode)getBwnode(node);
      final CarddavResource r = bwnode.getResource();

      if (r.getContent() == null) {
        sysi.getFileContent(r);
      }

      final Content c = new Content();

      c.stream = bwnode.getContentStream();
      c.contentType = node.getContentType();
      c.contentLength = node.getContentLen();

      return c;
    } catch (final WebdavException we) {
      throw we;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public String getAcceptContentType(final HttpServletRequest req) {
    final String accept = req.getHeader("Accept");

    if (accept != null) {
      return accept;
    }

    final String[] contentTypePars;
    final String contentType = req.getContentType();

    String ctype = null;

    if (contentType != null) {
      contentTypePars = contentType.split(";");
      ctype = contentTypePars[0];
    }

    if (ctype == null) {
      return ctype;
    }

    return "text/vcard";
  }

  @Override
  public PutContentResult putContent(final HttpServletRequest req,
                                     final HttpServletResponse resp,
                                     final WebdavNsNode node,
                                     final String[] contentTypePars,
                                     final Reader contentRdr,
                                     final IfHeaders ifHeaders) {
    try {
      final PutContentResult pcr = new PutContentResult();
      pcr.node = node;

      if (node instanceof CarddavResourceNode) {
        throw new WebdavException(HttpServletResponse.SC_PRECONDITION_FAILED);
      }

      final CarddavCardNode bwnode = (CarddavCardNode)getBwnode(node);
      final CarddavCollection col = bwnode.getWdCollection();

      boolean calContent = false;
      if ((contentTypePars != null) && (contentTypePars.length > 0)) {
        calContent = contentTypePars[0].equals("text/vcard");
      }

      if (!col.getAddressBook() || !calContent) {
        throw new WebdavForbidden(CarddavTags.supportedAddressData);
      }

      /* We can only put a single resource - that resource will be a vcard
       */

      final Card card = new Card();
      try {
        card.parse(contentRdr);
      } catch (final Throwable t) {
        if (debug()) {
          error(t);
        }
        throw new WebdavForbidden(CarddavTags.supportedAddressData,
                                  t.getMessage());
      }

      pcr.created = putCard(bwnode, card, ifHeaders);

      return pcr;
    } catch (final WebdavException we) {
      throw we;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public PutContentResult putBinaryContent(final HttpServletRequest req,
                                           final WebdavNsNode node,
                                           final String[] contentTypePars,
                                           final InputStream contentStream,
                                           final IfHeaders ifHeaders) {
    try {
      final PutContentResult pcr = new PutContentResult();
      pcr.node = node;

      if (!(node instanceof CarddavResourceNode)) {
        throw new WebdavException(HttpServletResponse.SC_PRECONDITION_FAILED);
      }

      final CarddavResourceNode bwnode = (CarddavResourceNode)getBwnode(node);
      final CarddavCollection col = bwnode.getWdCollection();

      if (col.getAddressBook()) {
        throw new WebdavException(HttpServletResponse.SC_PRECONDITION_FAILED);
      }

      final CarddavResource r = bwnode.getResource();

      if (!bwnode.getExists()) {
        ifHeaders.create = true;
      }

      String contentType = null;

      if ((contentTypePars != null) && (contentTypePars.length > 0)) {
        for (final String c: contentTypePars) {
          if (contentType != null) {
            contentType += ";";
          }
          contentType += c;
        }
      }

      r.setContentType(contentType);

      // XXX Fix this
      final int bufflen = 5000;
      byte[] buff = new byte[bufflen];
      byte[] res = null;

      for (;;) {
        final int len = contentStream.read(buff, 0, bufflen);
        if (len < 0) {
          break;
        }

        if (res == null) {
          res = buff;
          buff = new byte[bufflen];
        } else {
          final byte[] newres = new byte[res.length + len];
          System.arraycopy(res, 0, newres, 0, res.length);

          System.arraycopy(buff, 0, newres, res.length, len);

          res = newres;
        }
      }

      CarddavResourceContent rc = r.getContent();

      if (rc == null) {
        if (!bwnode.getExists()) {
          sysi.getFileContent(r);
          rc = r.getContent();
        }

        if (rc == null) {
          rc = new CarddavResourceContent();
          r.setContent(rc);
        }
      }

      rc.setValue(res);
      r.setContentLength(res.length);

      if (ifHeaders.create) {
        sysi.putFile(col, r);
      } else {
        sysi.updateFile(r, true);
      }
      return pcr;
    } catch (final WebdavException we) {
      throw we;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  private boolean putCard(final CarddavCardNode bwnode,
                          final Card card,
                          final IfHeaders ifHeaders) {
    final String entityName = bwnode.getEntityName();
    final CarddavCollection col = bwnode.getWdCollection();
    boolean created = false;

    if (debug()) {
      debug("putContent: intf has card with name " + entityName);
    }

    final Card oldCard = sysi.getCard(col.getPath(), entityName);

    card.setName(entityName);

    if (oldCard == null) {
      created = true;

      sysi.addCard(col.getPath(), card);

      bwnode.setCard(card);
    } else if (ifHeaders.create) {
      /* Resource already exists */

      throw new WebdavException(HttpServletResponse.SC_PRECONDITION_FAILED,
                                CarddavTags.noUidConflict);
    } else {
      if (!entityName.equals(oldCard.getName())) {
        throw new WebdavBadRequest("Mismatched names");
      }

      if ((ifHeaders.ifEtag != null) &&
          (!ifHeaders.ifEtag.equals(bwnode.getPrevEtagValue(true)))) {
        if (debug()) {
          debug("putContent: etag mismatch if=" + ifHeaders.ifEtag +
                   "prev=" + bwnode.getPrevEtagValue(true));
        }
        throw new WebdavException(HttpServletResponse.SC_PRECONDITION_FAILED);
      }

      if (debug()) {
        debug("putContent: update event " + card);
      }
      sysi.updateCard(col.getPath(), card);
    }

    return created;
  }

  @Override
  public void create(final WebdavNsNode node) {
  }

  @Override
  public void createAlias(final WebdavNsNode alias) {
  }

  @Override
  public void acceptMkcolContent(final HttpServletRequest req) {
    throw new WebdavUnsupportedMediaType();
  }

  /** Create an empty collection at the given location.
   *
   * <pre>
   *  201 (Created) - The calendar collection resource was created in its entirety.
   *  403 (Forbidden) - This indicates at least one of two conditions: 1) the
   *          server does not allow the creation of calendar collections at the
   *          given location in its namespace, or 2) the parent collection of the
   *          Request-URI exists but cannot accept members.
   *  405 (Method Not Allowed) - MKCALENDAR can only be executed on a null resource.
   *  409 (Conflict) - A collection cannot be made at the Request-URI until one
   *          or more intermediate collections have been created.
   *  415 (Unsupported Media Type)- The server does not support the request type
   *          of the body.
   *  507 (Insufficient Storage) - The resource does not have sufficient space
   *          to record the state of the resource after the execution of this method.
   *
   * @param req       HttpServletRequest
   * @param node             node to create
   */
  @Override
  public void makeCollection(final HttpServletRequest req,
                             final HttpServletResponse resp,
                             final WebdavNsNode node) {
    try {
      final CarddavColNode bwnode = (CarddavColNode)getBwnode(node);

      /* The uri should have an entity name representing the new collection
       * and a calendar object representing the parent.
       *
       * A namepart of null means that the path already exists
       */

      final CarddavCollection newCol = bwnode.getWdCollection();
      final CarddavCollection parent = newCol.getParent();
      if (parent.getAddressBook()) {
        throw new WebdavForbidden(CarddavTags.addressbookCollectionLocationOk);
      }

      if (newCol.getName() == null) {
        throw new WebdavForbidden("Forbidden: Null name");
      }

      resp.setStatus(sysi.makeCollection(newCol,
                                         parent.getPath()));
    } catch (final WebdavException we) {
      throw we;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void copyMove(final HttpServletRequest req,
                       final HttpServletResponse resp,
                       final WebdavNsNode from,
                       final WebdavNsNode to,
                       final boolean copy,
                       final boolean overwrite,
                       final int depth) {
    if (from instanceof CarddavColNode) {
      copyMoveCollection(resp, (CarddavColNode)from,
                         to, copy, overwrite, depth);

      return;
    }

    // Copy entity or resource
    if ((depth != Headers.depthNone) && (depth != 0)) {
      throw new WebdavBadRequest();
    }

    if (from instanceof CarddavCardNode) {
      copyMoveComponent(resp, (CarddavCardNode)from,
                        to, copy, overwrite);
      return;
    }

    if (from instanceof CarddavResourceNode) {
      copyMoveResource(resp, (CarddavResourceNode)from,
                       to, copy, overwrite);
      return;
    }

    throw new WebdavBadRequest();
  }

  private void copyMoveCollection(final HttpServletResponse resp,
                                  final CarddavColNode from,
                                  final WebdavNsNode to,
                                  final boolean copy,
                                  final boolean overwrite,
                                  final int depth) {
    if (!(to instanceof final CarddavColNode toCalNode)) {
      throw new WebdavBadRequest();
    }

    // Copy folder
    if ((depth != Headers.depthNone) && (depth != Headers.depthInfinity)) {
      throw new WebdavBadRequest();
    }

    if (toCalNode.getExists() && !overwrite) {
      resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

      return;
    }

    final WdCollection<?> fromCal = from.getWdCollection();
    final WdCollection<?> toCal = toCalNode.getWdCollection();

    getSysi().copyMove(fromCal, toCal, copy, overwrite);
    if (toCalNode.getExists()) {
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      resp.setStatus(HttpServletResponse.SC_CREATED);
      Headers.makeLocation(resp, getLocation(to));
    }
  }

  private void copyMoveComponent(final HttpServletResponse resp,
                                 final CarddavCardNode from,
                                 final WebdavNsNode to,
                                 final boolean copy,
                                 final boolean overwrite) {
    if (!(to instanceof final CarddavCardNode toNode)) {
      throw new WebdavBadRequest();
    }

    if (toNode.getExists() && !overwrite) {
      resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

      return;
    }

    final Card fromCard = from.getCard();
    final WdCollection<?> toCol = toNode.getWdCollection();

    if (!getSysi().copyMove(fromCard, toCol, toNode.getEntityName(), copy,
                            overwrite)) {
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      resp.setStatus(HttpServletResponse.SC_CREATED);
      Headers.makeLocation(resp, getLocation(to));
    }
  }

  private void copyMoveResource(final HttpServletResponse resp,
                               final CarddavResourceNode from,
                               final WebdavNsNode to,
                               final boolean copy,
                               final boolean overwrite) {
    if (!(to instanceof final CarddavResourceNode toNode)) {
      throw new WebdavBadRequest();
    }

    if (toNode.getExists() && !overwrite) {
      resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

      return;
    }

    final WdCollection<?> toCal = toNode.getWdCollection();

    if (!getSysi().copyMoveFile(from.getResource(),
                            toCal, toNode.getEntityName(), copy,
                            overwrite)) {
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      resp.setStatus(HttpServletResponse.SC_CREATED);
      Headers.makeLocation(resp, getLocation(to));
    }
  }

  @Override
  public boolean specialUri(final HttpServletRequest req,
                            final HttpServletResponse resp,
                            final String resourceUri) {
    return SpecialUri.process(req, resp, resourceUri, getSysi(), config,
                              false, null, null);
  }

  @Override
  public WdSynchReport getSynchReport(final String path,
                                      final String token,
                                      final int limit,
                                      final boolean recurse) {
    return null;
  }

  @Override
  public String getSyncToken(final String path) {
    return null;
  }

  /* ====================================================================
   *                  Access methods
   * ==================================================================== */

  @Override
  public Collection<WebdavNsNode> getGroups(final String resourceUri,
                                            final String principalUrl) {
    final Collection<WebdavNsNode> res = new ArrayList<>();

    final Collection<String> hrefs =
            getSysi().getGroups(resourceUri, principalUrl);
    for (String href: hrefs) {
      if (href.endsWith("/")) {
        href = href.substring(0, href.length() - 1);
      }

      final AccessPrincipal ap = getSysi().getPrincipal(href);

      res.add(new WebdavPrincipalNode(getSysi(),
                                      getSysi().getUrlHandler(),
                                      ap.getPrincipalRef(),
                                      ap, false,
                                      ap.getPrincipalRef()));
    }

    return res;
  }

  @Override
  public Collection<String> getPrincipalCollectionSet(final String resourceUri) {
    final ArrayList<String> al = new ArrayList<>();

    for (final String s: getSysi().getPrincipalCollectionSet(resourceUri)) {
      al.add(sysi.getUrlHandler().prefix(s));
    }

    return al;
  }

  @Override
  public Collection<WebdavNsNode> getPrincipals(final String resourceUri,
                                                final PrincipalPropertySearch pps) {
    final ArrayList<WebdavNsNode> pnodes = new ArrayList<>();

    for (final PrincipalInfo cui: sysi.getPrincipals(resourceUri, pps)) {
      pnodes.add(new WebdavPrincipalNode(sysi,
                                         sysi.getUrlHandler(),
                                         cui.principalPathPrefix,
                                         new User(cui.account), true,
                                         Util.buildPath(true,
                                                        cui.principalPathPrefix,
                                                        "/",
                                                        cui.account)));
    }

    return pnodes;
  }

  @Override
  public String makeUserHref(final String id) {
    return getSysi().makeHref(new User(id));
  }

  @Override
  public void updateAccess(final AclInfo info) {
    final CarddavNode node = (CarddavNode)getNode(info.what,
                                                  WebdavNsIntf.existanceMust,
                                                  WebdavNsIntf.nodeTypeUnknown,
                                                  false);

    // May need a real principal hierarchy
    if (node instanceof CarddavColNode) {
      sysi.updateAccess((CarddavColNode)node, info.acl);
    } else if (node instanceof CarddavCardNode) {
      sysi.updateAccess((CarddavCardNode)node, info.acl);
    } else {
      throw new WebdavException(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
  }

  @Override
  public void emitAcl(final WebdavNsNode node) {
    try {
      final Acl acl = node.getCurrentAccess().getAcl();

      if (acl != null) {
        accessUtil.emitAcl(acl, true);
      }
    } catch (final AccessException ae) {
      throw new WebdavException(ae);
    }
  }

  @Override
  public Collection<String> getAclPrincipalInfo(final WebdavNsNode node) {
    try {
      final TreeSet<String> hrefs = new TreeSet<>();

      for (final Ace ace: node.getCurrentAccess().getAcl().getAces()) {
        final AceWho who = ace.getWho();

        if (who.getWhoType() == WhoDefs.whoTypeUser) {
          hrefs.add(accessUtil.makeUserHref(who.getWho()));
        } else if (who.getWhoType() == WhoDefs.whoTypeGroup) {
          hrefs.add(accessUtil.makeGroupHref(who.getWho()));
        }
      }

      return hrefs;
    } catch (final AccessException ae) {
      if (debug()) {
        error(ae);
      }
      throw new WebdavServerError();
    }
  }

  /* ====================================================================
   *                Property value methods
   * ==================================================================== */

  @Override
  public WebdavProperty makeProp(final Element propnode) {
    if (!XmlUtil.nodeMatches(propnode, CarddavTags.addressData)) {
      return super.makeProp(propnode);
    }

    /* Handle the calendar-data element */

    final AddressData caldata =
            new AddressData(new QName(propnode.getNamespaceURI(),
                                      propnode.getLocalName()));
    caldata.parse(propnode);

    return caldata;
  }

  /** Properties we can process */
  private static final QName[] knownProperties = {
    CarddavTags.addressData,
    CarddavTags.maxResourceSize,
  };

  @Override
  public boolean knownProperty(final WebdavNsNode node,
                               final WebdavProperty pr) {
    final QName tag = pr.getTag();

    for (final QName knownPropertie : knownProperties) {
      if (tag.equals(knownPropertie)) {
        return true;
      }
    }

    /* Try the node for a value */

    return super.knownProperty(node, pr);
  }

  @Override
  public boolean generatePropValue(final WebdavNsNode node,
                                   WebdavProperty pr,
                                   final boolean allProp) {
    final QName tag = pr.getTag();
    final String ns = tag.getNamespaceURI();

    try {
      /* Deal with anything but webdav properties */
      if (ns.equals(WebdavTags.namespace)) {
        // Not ours
        return super.generatePropValue(node, pr, allProp);
      }

      if (tag.equals(CarddavTags.addressData)) {
        // pr may be an AddressData object - if not it's probably allprops
        if (!(pr instanceof AddressData)) {
          pr = new AddressData(tag);
        }

        final AddressData addrdata = (AddressData)pr;

        if (debug()) {
          debug("do AddressData for " + node.getUri());
        }

        final String contentType = addrdata.getReturnContentType();
        String[] contentTypePars = null;

        if (contentType != null) {
          contentTypePars = contentType.split(";");
        }

        String ctype = null;
        if (contentTypePars !=null) {
          ctype = contentTypePars[0];
        }

        try {
          /* Output the (transformed) node.
           */

          if (ctype != null) {
            xml.openTagNoNewline(CarddavTags.addressData,
                                 "content-type", ctype);
          } else {
            xml.openTagNoNewline(CarddavTags.addressData);
          }

          addrdata.process(node, xml, ctype);
          xml.closeTagSameLine(CarddavTags.addressData);

          return true;
        } catch (final WebdavException wde) {
          final int status = wde.getStatusCode();
          if (debug() && (status != HttpServletResponse.SC_NOT_FOUND)) {
            error(wde);
          }
          return false;
        }
      }

      if (tag.equals(CarddavTags.maxResourceSize)) {
        /* e.g.
         * <C:max-resource-size
         *    xmlns:C="urn:ietf:params:xml:ns:caldav">102400</C:max-resource-size>
         */
        xml.property(tag, String.valueOf(sysi.getMaxUserEntitySize()));
        return true;
      }

      if (node.generatePropertyValue(tag, this, allProp)) {
        // Generated by node
        return true;
      }

      // Not known
      return false;
    } catch (final WebdavException we) {
      throw we;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* ====================================================================
   *                         Carddav methods
   * ==================================================================== */

  /** Result from query */
  public static class QueryResult {
    /** Server truncted the query result */
    public boolean serverTruncated;

    /** Exceeded user limit */
    public boolean overLimit;

    /** The possibly truncated result */
    public Collection<WebdavNsNode> nodes = new ArrayList<>();
  }

  /** Use the given query to return a collection of nodes. An exception will
   * be raised if the entire query fails for some reason (access, etc). An
   * empty collection will be returned if no objects match.
   *
   * @param wdnode    WebdavNsNode defining root of search
   * @param fltr      Filter object defining search
   * @param limits    to limit result size
   * @param vcardVersion rfc version number as String
   * @return Collection of result nodes (empty for no result)
   */
  public QueryResult query(final WebdavNsNode wdnode,
                                        final Filter fltr,
                                        final GetLimits limits,
                                        final String vcardVersion) {
    try {
      final QueryResult qr = new QueryResult();
      final CarddavNode node = getBwnode(wdnode);

//      GetResult res = fltr.query(node, limits);
      final GetResult res =
              node.getSysi().getCards(node.getWdCollection(),
                                      fltr, limits);

      if (debug()) {
        if (!res.entriesFound || (res.cards == null)) {
          debug("Query returned nothing");
        } else {
          debug("Query returned " + res.cards.size());
        }
      }

      qr.overLimit = res.overLimit;
      qr.serverTruncated = res.serverTruncated;

      /* We now need to build a node for each of the cards in the collection.
       For each card we must determine what collection it's in. We then take the
       incoming uri, strip any collection names off it and append the collection
       name and card name to create the new uri.

       If there is no name for the card we just give it the default.
     */

      qr.nodes = new ArrayList<>();

      for (final Card card: res.cards) {
        final CarddavCollection col = node.getWdCollection();
        card.setParent(col);

        /* Was this - this code assumed a multi-depth search
        CarddavCollection col = card.getParent();*/
        final String uri = col.getPath();

        /* If no name was assigned use the guid */
        String name = card.getName();
        if (name == null) {
          name = makeName(card.getUid()) + ".vcf";
        }

        final CarddavCardNode cnode =
                (CarddavCardNode)getNodeInt(Util.buildPath(false, uri, "/", name),
                                            WebdavNsIntf.existanceDoesExist,
                                            WebdavNsIntf.nodeTypeEntity,
                                            col, card, null);

        cnode.setVcardVersion(vcardVersion);

        qr.nodes.add(cnode);
      }

      //qr.nodes = fltr.postFilter(qr.nodes);

      return qr;
    } catch (final WebdavException we) {
      throw we;
    } catch (final Throwable t) {
      error(t);
      throw new WebdavServerError();
    }
  }

  /**
   * @param node to check and return
   * @return CaldavBwNode
   */
  public CarddavNode getBwnode(final WebdavNsNode node) {
    if (!(node instanceof CarddavNode)) {
      throw new RuntimeException("Not a valid node object " +
                                         node.getClass().getName());
    }

    return (CarddavNode)node;
  }

  /**
   * @param node for type check
   * @param errstatus for exception if required
   * @return CaldavCalNode
   */
  public CarddavColNode getCalnode(final WebdavNsNode node,
                                   final int errstatus) {
    if (!(node instanceof CarddavColNode)) {
      throw new WebdavException("Not a valid node object " +
                                         errstatus);
    }

    return (CarddavColNode)node;
  }

  /**
   * @param requested version - may be null
   * @return a valid version
   * @throws WebdavException if version invalid
   */
  public String getVcardVersion(final String requested) {
    if (requested != null) {
      return checkVersion(requested);
    }

    if (confBean.getDefaultVcardVersion() != null) {
      return checkVersion(confBean.getDefaultVcardVersion());
    }

    return "3.0";
  }

  /* ====================================================================
   *                         Private methods
   * ==================================================================== */

  private String checkVersion(final String version) {
    if (!VcardDefs.validVersions.contains(version)) {
      throw new WebdavBadRequest("Bad version " + version);
    }

    return version;
  }

  private WebdavNsNode getNodeInt(final String uri,
                                  final int existance,
                                  final int nodeType,
                                  final CarddavCollection col,
                                  final Card card,
                                  final CarddavResource r) {
    if (debug()) {
      debug("About to get node for " + uri);
    }

    if (uri == null)  {
      return null;
    }

    try {
      final CarddavURI wi = findURI(uri,
                                    existance,
                                    nodeType,
                                    false,
                                    col, card, r);

      if (wi == null) {
        throw new WebdavNotFound(uri);
      }

      final WebdavNsNode nd;

      if (wi.isUser() || wi.isGroup()) {
        final AccessPrincipal ap;

        if (wi.isUser()) {
          ap = new User(wi.getEntityName());
          ap.setPrincipalRef(wi.getPath());
          nd = new CarddavUserNode(wi, sysi, ap);
        } else {
          ap = new Group(wi.getEntityName());
          ap.setPrincipalRef(wi.getPath());
          nd = new CarddavGroupNode(wi, sysi, ap);
        }

      } else if (wi.isCollection()) {
        nd = new CarddavColNode(wi, sysi);
      } else if (wi.isResource()) {
        nd = new CarddavResourceNode(wi, sysi);
      } else {
        nd = new CarddavCardNode(wi, sysi);
      }

      return nd;
    } catch (final WebdavException we) {
      throw we;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  private void loadConfig(String appName) {
    try {
      if ((appName == null) || (appName.isEmpty())) {
        appName = "unknown-app-name";
      }

      config = confBean.getConf(appName);
      if (config == null) {
        config = new CardDAVContextConfig();
      }
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  /** Find the named item by following down the path from the root.
   * This requires the names at each level to be unique (and present)
   *
   * <p>Uri is at least /user/user-id or <br/>
   *    /public
   * <br/>followed by one or more calendar path elements possibly followed by an
   * entity name.
   *
   * @param theUri           String uri - just the path part
   * @param existance        Say's something about the state of existance
   * @param nodeType         Say's something about the type of node
   * @param addMember        From POST
   * @param col        Supplied WdCollection object if we already have it.
   * @param card
   * @param rsrc
   * @return CaldavURI object representing the uri
   */
  private CarddavURI findURI(final String theUri,
                            final int existance,
                            final int nodeType,
                            final boolean addMember,
                            CarddavCollection col,
                            Card card,
                            CarddavResource rsrc) {
    try {
      if ((nodeType == WebdavNsIntf.nodeTypeUnknown) &&
          (existance != WebdavNsIntf.existanceMust)) {
        // We assume an unknown type must exist
        throw new WebdavServerError();
      }

      final String uri = Util.buildPath(theUri.endsWith("/"),
                                        normalizeUri(theUri));

      if (!uri.startsWith("/")) {
        return null;
      }

      final CarddavURI curi;

      final boolean isPrincipal = sysi.isPrincipal(uri);

      if ((nodeType == WebdavNsIntf.nodeTypePrincipal) && !isPrincipal) {
        throw new WebdavNotFound(uri);
      }

      if (isPrincipal) {
        final AccessPrincipal p = getSysi().getPrincipal(uri);

        if (p == null) {
          throw new WebdavNotFound(uri);
        }

        return new CarddavURI(p);
      }

      if (existance == WebdavNsIntf.existanceDoesExist) {
        // Provided with collection and entity if needed.

        if (card != null) {
          curi = new CarddavURI(col, card, card.getName(), true);
        } else if (rsrc != null) {
          curi = new CarddavURI(rsrc, true);
        } else {
          curi = new CarddavURI(col, null, null, true);
        }

        return curi;
      }

      if (uri.endsWith("/") &&
              ((nodeType == WebdavNsIntf.nodeTypeCollection) ||
                       (nodeType == WebdavNsIntf.nodeTypeUnknown))) {
        // For unknown we try the full path first as a calendar.
        if (debug()) {
          debug("search for collection uri \"" + uri + "\"");
        }
        col = sysi.getCollection(uri);

        if (col == null) {
          if ((nodeType == WebdavNsIntf.nodeTypeCollection) &&
              (existance != WebdavNsIntf.existanceNot) &&
              (existance != WebdavNsIntf.existanceMay)) {
            /* We asked for a collection and it doesn't exist */
            throw new WebdavNotFound(uri);
          }

          // We'll try as an entity for unknown
        } else {
          if (existance == WebdavNsIntf.existanceNot) {
            throw new WebdavForbidden(WebdavTags.resourceMustBeNull);
          }

          if (debug()) {
            debug("create collection uri - col=\"" + col.getPath() + "\"");
          }

          curi = new CarddavURI(col, true);

          return curi;
        }
      }

      // Entity or unknown

      /* Split name into parent path and entity name part */
      final SplitResult split = splitUri(uri);

      if (split.name == null) {
        // No name part
        throw new WebdavNotFound(uri);
      }

      /* Look for the parent */
      col = sysi.getCollection(split.path);

      if (col == null) {
        if (nodeType == WebdavNsIntf.nodeTypeCollection) {
          // Trying to create calendar/collection with no parent
          throw new WebdavException(HttpServletResponse.SC_CONFLICT);
        }

        throw new WebdavNotFound(uri);
      }

      if (nodeType == WebdavNsIntf.nodeTypeCollection) {
        // Trying to create calendar/collection
        final CarddavCollection newCol = new CarddavCollection();

        newCol.setParent(col);
        newCol.setName(split.name);
        newCol.setPath(Util.buildPath(true, col.getPath(), "/", newCol.getName()));

        curi = new CarddavURI(newCol, false);

        return curi;
      }

      if (col.getAddressBook()) {
        if (debug()) {
          debug("find card - col=\"" + col.getPath() + "\" name=\"" +
                   split.name + "\"");
        }

        card = sysi.getCard(col.getPath(), split.name);

        if ((existance == WebdavNsIntf.existanceMust) && (card == null)) {
          throw new WebdavNotFound(uri);
        }

        curi = new CarddavURI(col, card, split.name, card != null);
      } else {
        if (debug()) {
          debug("find resource - col=\"" + col.getPath() + "\" name=\"" +
                   split.name + "\"");
        }

        /* Look for a resource */
        rsrc = sysi.getFile(col, split.name);

        if ((existance == WebdavNsIntf.existanceMust) && (rsrc == null)) {
          throw new WebdavNotFound(uri);
        }

        final boolean exists = rsrc != null;

        if (!exists) {
          rsrc = new CarddavResource();
          rsrc.setName(split.name);
          rsrc.setParent(col);
        }

        curi = new CarddavURI(rsrc, exists);
      }

      //putUriPath(curi);

      return curi;
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  private static class SplitResult {
    String path;
    String name;

    SplitResult(final String path, final String name) {
      this.path = path;
      this.name = name;
    }
  }

  /* Split the uri so that result.path is the path up to the name part result.name
   *
   * NormalizeUri was called previously so we have no trailing "/"
   */
  private SplitResult splitUri(final String uri) {
    final int pos = uri.lastIndexOf("/");
    if (pos < 0) {
      // bad uri
      throw new WebdavBadRequest("Invalid uri: " + uri);
    }

    if (pos == 0) {
      return new SplitResult(uri, null);
    }

    return new SplitResult(uri.substring(0, pos + 1), uri.substring(pos + 1));
  }
}
