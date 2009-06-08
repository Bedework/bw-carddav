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

import org.bedework.carddav.server.SysIntf.GetLimits;
import org.bedework.carddav.server.SysIntf.GetResult;
import org.bedework.carddav.server.SysIntf.UserInfo;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.server.query.AddressData;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.util.Group;
import org.bedework.carddav.util.User;
import org.bedework.carddav.vcard.Vcard;

import edu.rpi.cct.webdav.servlet.common.AccessUtil;
import edu.rpi.cct.webdav.servlet.common.Headers;
import edu.rpi.cct.webdav.servlet.common.WebdavServlet;
import edu.rpi.cct.webdav.servlet.common.WebdavUtils;
import edu.rpi.cct.webdav.servlet.common.MethodBase.MethodInfo;
import edu.rpi.cct.webdav.servlet.shared.PrincipalPropertySearch;
import edu.rpi.cct.webdav.servlet.shared.WdCollection;
import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavForbidden;
import edu.rpi.cct.webdav.servlet.shared.WebdavNotFound;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode;
import edu.rpi.cct.webdav.servlet.shared.WebdavPrincipalNode;
import edu.rpi.cct.webdav.servlet.shared.WebdavProperty;
import edu.rpi.cct.webdav.servlet.shared.WebdavServerError;
import edu.rpi.cct.webdav.servlet.shared.WebdavUnauthorized;
import edu.rpi.cct.webdav.servlet.shared.WebdavUnsupportedMediaType;
import edu.rpi.cmt.access.AccessException;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.Ace;
import edu.rpi.cmt.access.AceWho;
import edu.rpi.cmt.access.Acl;
import edu.rpi.cmt.access.PrivilegeDefs;
import edu.rpi.cmt.access.WhoDefs;
import edu.rpi.cmt.access.AccessXmlUtil.AccessXmlCb;
import edu.rpi.sss.util.OptionsI;
import edu.rpi.sss.util.xml.XmlEmit;
import edu.rpi.sss.util.xml.XmlUtil;
import edu.rpi.sss.util.xml.tagdefs.CarddavTags;
import edu.rpi.sss.util.xml.tagdefs.WebdavTags;

import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;

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
 *   @author Mike Douglass   douglm @ rpi.edu
 */
public class CarddavBWIntf extends WebdavNsIntf {
  /** Namespace prefix based on the request url.
   */
  private String namespacePrefix;

  private AccessUtil accessUtil;

  /** Namespace based on the request url.
   */
  @SuppressWarnings("unused")
  private String namespace;

  SysIntf sysi;

  private CardDAVConfig config;

  /** We store CaldavURI objects here
   * /
  private HashMap<String, CaldavURI> uriMap = new HashMap<String, CaldavURI>();
  */

  /* ====================================================================
   *                     Interface methods
   * ==================================================================== */

  /** Called before any other method is called to allow initialisation to
   * take place at the first or subsequent requests
   *
   * @param servlet
   * @param req
   * @param props
   * @param debug
   * @param methods    HashMap   table of method info
   * @param dumpContent
   * @throws WebdavException
   */
  public void init(WebdavServlet servlet,
                   HttpServletRequest req,
                   Properties props,
                   boolean debug,
                   HashMap<String, MethodInfo> methods,
                   boolean dumpContent) throws WebdavException {
    super.init(servlet, req, props, debug, methods, dumpContent);

    try {
      HttpSession session = req.getSession();
      ServletContext sc = session.getServletContext();

      String appName = sc.getInitParameter("bwappname");

      if ((appName == null) || (appName.length() == 0)) {
        appName = "unknown-app-name";
      }

      namespacePrefix = WebdavUtils.getUrlPrefix(req);
      namespace = namespacePrefix + "/schema";

      OptionsI opts = CardOptionsFactory.getOptions(debug);
      config = (CardDAVConfig)opts.getAppProperty(appName);
      if (config == null) {
        config = new CardDAVConfig();
      }

      String dirHandlersElementName = "org.bedework.global.dirhandlers";
      Collection<String> dirHandlerNames = opts.getNames(dirHandlersElementName);

      for (String dhn: dirHandlerNames) {
        Object o = opts.getProperty(dirHandlersElementName + "." + dhn);

        if (debug) {
          debugMsg("dhn=" + dhn);
        }

        config.addDirhandler((DirHandlerConfig)o);
      }

      sysi = getSysIntf();
      sysi.init(req, account, config, debug);

      accessUtil = new AccessUtil(namespacePrefix, xml,
                                  new CalDavAccessXmlCb(sysi), debug);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /** See if we can reauthenticate. Use for real-time service which needs to
   * authenticate as a particular principal.
   *
   * @param req
   * @param account
   * @throws WebdavException
   */
  public void reAuth(HttpServletRequest req,
                     String account) throws WebdavException {
    try {
      this.account = account;

      sysi = getSysIntf();
      sysi.init(req, account, config, debug);

      accessUtil = new AccessUtil(namespacePrefix, xml,
                                  new CalDavAccessXmlCb(sysi), debug);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private SysIntf getSysIntf() throws WebdavException {
    String className = config.getSysintfImpl();

    Object o = null;
    try {
      o = Class.forName(className).newInstance();
    } catch (Throwable t) {
      o = null;
    }

    if (o == null) {
      throw new WebdavException("Class " + className + " not found");
    }

    if (!SysIntf.class.isInstance(o)) {
      throw new WebdavException("Class " + className +
                                   " is not a subclass of " +
                                   SysIntf.class.getName());
    }

    return (SysIntf)o;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getDavHeader(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode)
   */
  public String getDavHeader(WebdavNsNode node) throws WebdavException {
    return super.getDavHeader(node) + ", addressbook";
  }

  protected CardDAVConfig getConfig() {
    return config;
  }

  /**
   */
  private static class CalDavAccessXmlCb implements AccessXmlCb, Serializable {
    private SysIntf sysi;

    private QName errorTag;

    CalDavAccessXmlCb(SysIntf sysi) {
      this.sysi = sysi;
    }

    /* (non-Javadoc)
     * @see edu.rpi.cmt.access.AccessXmlUtil.AccessXmlCb#makeHref(java.lang.String, int)
     */
    public String makeHref(String id, int whoType) throws AccessException {
      try {
        AccessPrincipal p;
        if (whoType == Ace.whoTypeUser) {
          p = new User(id);
        } else {
          p = new Group(id);
        }
        return sysi.makeHref(p);
      } catch (Throwable t) {
        throw new AccessException(t);
      }
    }

    /* (non-Javadoc)
     * @see edu.rpi.cmt.access.AccessXmlUtil.AccessXmlCb#getPrincipal()
     */
    public AccessPrincipal getPrincipal() throws AccessException {
      try {
        return sysi.getPrincipal();
      } catch (Throwable t) {
        throw new AccessException(t);
      }
    }

    public AccessPrincipal getPrincipal(String href) throws AccessException {
      try {
        return sysi.getPrincipal(href);
      } catch (Throwable t) {
        throw new AccessException(t);
      }
    }

    /* (non-Javadoc)
     * @see edu.rpi.cmt.access.AccessXmlUtil.AccessXmlCb#setErrorTag(edu.rpi.sss.util.xml.QName)
     */
    public void setErrorTag(QName tag) throws AccessException {
      errorTag = tag;
    }

    /* (non-Javadoc)
     * @see edu.rpi.cmt.access.AccessXmlUtil.AccessXmlCb#getErrorTag()
     */
    public QName getErrorTag() throws AccessException {
      return errorTag;
    }
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getAccessUtil()
   */
  public AccessUtil getAccessUtil() throws WebdavException {
    return accessUtil;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#canPut(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode)
   */
  public boolean canPut(WebdavNsNode node) throws WebdavException {
    int access = PrivilegeDefs.privWriteContent;

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

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getDirectoryBrowsingDisallowed()
   */
  public boolean getDirectoryBrowsingDisallowed() throws WebdavException {
    return sysi.getDirectoryBrowsingDisallowed();
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#close()
   */
  public void close() throws WebdavException {
    sysi.close();
  }

  /**
   * @return SysIntf
   */
  public SysIntf getSysi() {
    return sysi;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getSupportedLocks()
   */
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

  public boolean getAccessControl() {
    return true;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#addNamespace(edu.rpi.sss.util.xml.XmlEmit)
   */
  public void addNamespace(XmlEmit xml) throws WebdavException {
    super.addNamespace(xml);

    try {
      xml.addNs(CarddavTags.namespace);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getNode(java.lang.String, int, int)
   */
  public WebdavNsNode getNode(String uri,
                              int existance,
                              int nodeType) throws WebdavException {
    return getNodeInt(uri, existance, nodeType, null, null, null);
  }

  private WebdavNsNode getNodeInt(String uri,
                                  int existance,
                                  int nodeType,
                                  CarddavCollection col,
                                  Vcard card,
                                  CarddavResource r) throws WebdavException {
    if (debug) {
      debugMsg("About to get node for " + uri);
    }

    if (uri == null)  {
      return null;
    }

    try {
      CarddavURI wi = findURI(uri, existance, nodeType, col, card, r);

      if (wi == null) {
        throw new WebdavNotFound(uri);
      }

      WebdavNsNode nd = null;

      if (wi.isUser() || wi.isGroup()) {
        AccessPrincipal ap;

        if (wi.isUser()) {
          ap = new User(wi.getEntityName());
          nd = new CarddavUserNode(wi, sysi, ap, debug);
        } else {
          ap = new Group(wi.getEntityName());
          nd = new CarddavGroupNode(wi, sysi, ap, debug);
        }

      } else if (wi.isCollection()) {
        nd = new CarddavColNode(wi, sysi, debug);
      } else if (wi.isResource()) {
        nd = new CarddavResourceNode(wi, sysi, debug);
      } else {
        nd = new CarddavCardNode(wi, sysi, debug);
      }

      return nd;
    } catch (WebdavException we) {
      throw we;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void putNode(WebdavNsNode node)
      throws WebdavException {
  }

  public void delete(WebdavNsNode node) throws WebdavException {
    try {
      CarddavNode cnode = getBwnode(node);

      if (cnode instanceof CarddavResourceNode) {
        CarddavResourceNode rnode = (CarddavResourceNode)cnode;

        CarddavResource r = rnode.getResource();

        sysi.deleteFile(r);
      } else if (cnode instanceof CarddavCardNode) {
        if (debug) {
          trace("About to delete card " + cnode);
        }
        sysi.deleteCard((CarddavCardNode)cnode);
      } else {
        if (!(cnode instanceof CarddavColNode)) {
          throw new WebdavUnauthorized();
        }

        sysi.deleteCollection(((CarddavColNode)cnode).getWdCollection());
      }
    } catch (WebdavException we) {
      throw we;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getChildren(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode)
   */
  public Collection<WebdavNsNode> getChildren(WebdavNsNode node) throws WebdavException {
    try {
      if (!node.isCollection()) {
        // Don't think we should have been called
        return new ArrayList<WebdavNsNode>();
      }

      if (debug) {
        debugMsg("About to get children for " + node.getUri());
      }

      Collection<WebdavNsNode> ch = null;

      if (node instanceof CarddavNode) {
        CarddavNode cdnode = getBwnode(node);

        // XXX We'd like to be applying limits here as well I guess
        ch = cdnode.getChildren(null).nodes;
      } else {
//        ch = node.getChildren().nodes;
      }

      if (ch == null) {
        return new ArrayList<WebdavNsNode>();
      }

      return ch;
    } catch (WebdavException we) {
      throw we;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  public WebdavNsNode getParent(WebdavNsNode node)
      throws WebdavException {
    return null;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getContent(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode)
   */
  public Reader getContent(WebdavNsNode node) throws WebdavException {
    try {
      if (!node.getAllowsGet()) {
        return null;
      }

      CarddavNode bwnode = getBwnode(node);

      return bwnode.getContent();
    } catch (WebdavException we) {
      throw we;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getBinaryContent(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode)
   */
  public InputStream getBinaryContent(WebdavNsNode node) throws WebdavException {
    try {
      if (!node.getAllowsGet()) {
        return null;
      }

      if (!(node instanceof CarddavResourceNode)) {
        throw new WebdavException("Unexpected node type");
      }

      CarddavResourceNode bwnode = (CarddavResourceNode)getBwnode(node);
      CarddavResource r = bwnode.getResource();

      if (r.getContent() == null) {
        sysi.getFileContent(r);
      }

      return bwnode.getContentStream();
    } catch (WebdavException we) {
      throw we;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#putContent(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode, java.lang.String, java.io.Reader, boolean, java.lang.String)
   */
  public PutContentResult putContent(WebdavNsNode node,
                                     String[] contentTypePars,
                                     Reader contentRdr,
                                     boolean create,
                                     String ifEtag) throws WebdavException {
    try {
      PutContentResult pcr = new PutContentResult();
      pcr.node = node;

      if (node instanceof CarddavResourceNode) {
        throw new WebdavException(HttpServletResponse.SC_PRECONDITION_FAILED);
      }

      CarddavCardNode bwnode = (CarddavCardNode)getBwnode(node);
      CarddavCollection col = bwnode.getWdCollection();

      boolean calContent = false;
      if ((contentTypePars != null) && contentTypePars.length > 0) {
        calContent = contentTypePars[0].equals("text/vcard");
      }

      if (!col.getAddressBook() || !calContent) {
        throw new WebdavForbidden(CarddavTags.supportedAddressData);
      }

      /** We can only put a single resource - that resource will be a vcard
       */

      Vcard card = new Vcard().parse(contentRdr);

      pcr.created = putCard(bwnode, card, create, ifEtag);

      return pcr;
    } catch (WebdavException we) {
      throw we;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#putBinaryContent(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode, java.lang.String, java.io.InputStream, boolean, java.lang.String)
   */
  public PutContentResult putBinaryContent(WebdavNsNode node,
                                           String[] contentTypePars,
                                           InputStream contentStream,
                                           boolean create,
                                           String ifEtag) throws WebdavException {
    try {
      PutContentResult pcr = new PutContentResult();
      pcr.node = node;

      if (!(node instanceof CarddavResourceNode)) {
        throw new WebdavException(HttpServletResponse.SC_PRECONDITION_FAILED);
      }

      CarddavResourceNode bwnode = (CarddavResourceNode)getBwnode(node);
      CarddavCollection col = bwnode.getWdCollection();

      if (col.getAddressBook()) {
        throw new WebdavException(HttpServletResponse.SC_PRECONDITION_FAILED);
      }

      CarddavResource r = bwnode.getResource();

      if (!bwnode.getExists()) {
        create = true;
      }

      String contentType = null;

      if ((contentTypePars != null) && contentTypePars.length > 0) {
        for (String c: contentTypePars) {
          if (contentType != null) {
            contentType += ";";
          }
          contentType += c;
        }
      }

      r.setContentType(contentType);

      // XXX Fix this
      int bufflen = 5000;
      byte[] buff = new byte[bufflen];
      byte[] res = null;

      for (;;) {
        int len = contentStream.read(buff, 0, bufflen);
        if (len < 0) {
          break;
        }

        if (res == null) {
          res = buff;
          buff = new byte[bufflen];
        } else {
          byte[] newres = new byte[res.length + len];
          for (int i = 0; i < res.length; i++) {
            newres[i] = res[i];
          }

          for (int i = 0; i < len; i++) {
            newres[res.length + i] = buff[i];
          }

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

      if (create) {
        sysi.putFile(col, r);
      } else {
        sysi.updateFile(r, true);
      }
      return pcr;
    } catch (WebdavException we) {
      throw we;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private boolean putCard(CarddavCardNode bwnode,
                          Vcard card,
                          boolean create,
                          String ifEtag) throws WebdavException {
    String entityName = bwnode.getEntityName();
    CarddavCollection col = bwnode.getWdCollection();
    boolean created = false;

    if (debug) {
      debugMsg("putContent: intf has card with name " + entityName);
    }

    Vcard oldCard = sysi.getCard(col.getPath(), entityName);

    if (oldCard == null) {
      created = true;
      card.setName(entityName);

      sysi.addCard(col.getPath(), card);

      bwnode.setCard(card);
    } else if (create) {
      /* Resource already exists */

      throw new WebdavException(HttpServletResponse.SC_PRECONDITION_FAILED);
    } else {
      if (!entityName.equals(card.getName())) {
        throw new WebdavBadRequest("Mismatched names");
      }

      if ((ifEtag != null) && (!ifEtag.equals(bwnode.getPrevEtagValue(true)))) {
        if (debug) {
          debugMsg("putContent: etag mismatch if=" + ifEtag +
                   "prev=" + bwnode.getPrevEtagValue(true));
        }
        throw new WebdavException(HttpServletResponse.SC_PRECONDITION_FAILED);
      }

      if (debug) {
        debugMsg("putContent: update event " + card);
      }
      sysi.updateCard(col.getPath(), card);
    }

    return created;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#create(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode)
   */
  public void create(WebdavNsNode node) throws WebdavException {
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#createAlias(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode)
   */
  public void createAlias(WebdavNsNode alias) throws WebdavException {
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#acceptMkcolContent(javax.servlet.http.HttpServletRequest)
   */
  public void acceptMkcolContent(HttpServletRequest req) throws WebdavException {
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
   * @throws WebdavException
   */
  public void makeCollection(HttpServletRequest req,
                             HttpServletResponse resp,
                             WebdavNsNode node) throws WebdavException {
    try {
      CarddavColNode bwnode = (CarddavColNode)getBwnode(node);

      /* The uri should have an entity name representing the new collection
       * and a calendar object representing the parent.
       *
       * A namepart of null means that the path already exists
       */

      CarddavCollection newCol = bwnode.getWdCollection();
      CarddavCollection parent = newCol.getParent();
      if (parent.getAddressBook()) {
        throw new WebdavForbidden(CarddavTags.addressbookCollectionLocationOk);
      }

      if (newCol.getName() == null) {
        throw new WebdavForbidden("Forbidden: Null name");
      }

      resp.setStatus(sysi.makeCollection(newCol,
                                         parent.getPath()));
    } catch (WebdavException we) {
      throw we;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void copyMove(HttpServletRequest req,
                       HttpServletResponse resp,
                       WebdavNsNode from,
                       WebdavNsNode to,
                       boolean copy,
                       boolean overwrite,
                       int depth) throws WebdavException {
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

  private void copyMoveCollection(HttpServletResponse resp,
                                  CarddavColNode from,
                                  WebdavNsNode to,
                                  boolean copy,
                                  boolean overwrite,
                                  int depth) throws WebdavException {
    if (!(to instanceof CarddavColNode)) {
      throw new WebdavBadRequest();
    }

    // Copy folder
    if ((depth != Headers.depthNone) && (depth != Headers.depthInfinity)) {
      throw new WebdavBadRequest();
    }

    CarddavColNode fromCalNode = from;
    CarddavColNode toCalNode = (CarddavColNode)to;

    if (toCalNode.getExists() && !overwrite) {
      resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

      return;
    }

    WdCollection fromCal = fromCalNode.getWdCollection();
    WdCollection toCal = toCalNode.getWdCollection();

    getSysi().copyMove(fromCal, toCal, copy, overwrite);
    if (toCalNode.getExists()) {
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      resp.setStatus(HttpServletResponse.SC_CREATED);
      Headers.makeLocation(resp, getLocation(to), debug);
    }
  }

  private void copyMoveComponent(HttpServletResponse resp,
                                 CarddavCardNode from,
                                 WebdavNsNode to,
                                 boolean copy,
                                 boolean overwrite) throws WebdavException {
    if (!(to instanceof CarddavCardNode)) {
      throw new WebdavBadRequest();
    }

    CarddavCardNode toNode = (CarddavCardNode)to;

    if (toNode.getExists() && !overwrite) {
      resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

      return;
    }

    Vcard fromCard = from.getCard();
    WdCollection toCol = toNode.getWdCollection();

    if (!getSysi().copyMove(fromCard, toCol, toNode.getEntityName(), copy,
                            overwrite)) {
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      resp.setStatus(HttpServletResponse.SC_CREATED);
      Headers.makeLocation(resp, getLocation(to), debug);
    }
  }

  private void copyMoveResource(HttpServletResponse resp,
                               CarddavResourceNode from,
                               WebdavNsNode to,
                               boolean copy,
                               boolean overwrite) throws WebdavException {
    if (!(to instanceof CarddavResourceNode)) {
      throw new WebdavBadRequest();
    }

    CarddavResourceNode toNode = (CarddavResourceNode)to;

    if (toNode.getExists() && !overwrite) {
      resp.setStatus(HttpServletResponse.SC_PRECONDITION_FAILED);

      return;
    }

    WdCollection toCal = toNode.getWdCollection();

    if (!getSysi().copyMoveFile(from.getResource(),
                            toCal, toNode.getEntityName(), copy,
                            overwrite)) {
      resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    } else {
      resp.setStatus(HttpServletResponse.SC_CREATED);
      Headers.makeLocation(resp, getLocation(to), debug);
    }
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#specialUri(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String)
   */
  public boolean specialUri(HttpServletRequest req,
                            HttpServletResponse resp,
                            String resourceUri) throws WebdavException {
    return SpecialUri.process(req, resp, resourceUri, getSysi(), config, debug);
  }

  /* ====================================================================
   *                  Access methods
   * ==================================================================== */

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getGroups(java.lang.String, java.lang.String)
   */
  public Collection<WebdavNsNode> getGroups(String resourceUri,
                                            String principalUrl)
          throws WebdavException {
    Collection<WebdavNsNode> res = new ArrayList<WebdavNsNode>();

    Collection<String> hrefs = getSysi().getGroups(resourceUri, principalUrl);
    for (String href: hrefs) {
      if (href.endsWith("/")) {
        href = href.substring(0, href.length());
      }

      AccessPrincipal ap = getSysi().getPrincipal(href);

      res.add(new WebdavPrincipalNode(getSysi().getUrlHandler(),
                                      ap.getPrincipalRef(),
                                      ap, false,
                                      ap.getPrincipalRef() + "/",
                                      debug));
    }

    return res;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getPrincipalCollectionSet(java.lang.String)
   */
  public Collection<String> getPrincipalCollectionSet(String resourceUri)
         throws WebdavException {
    ArrayList<String> al = new ArrayList<String>();

    for (String s: getSysi().getPrincipalCollectionSet(resourceUri)) {
      al.add(sysi.getUrlHandler().prefix(s));
    }

    return al;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getPrincipals(java.lang.String, edu.rpi.cct.webdav.servlet.shared.PrincipalPropertySearch)
   */
  public Collection<WebdavNsNode> getPrincipals(String resourceUri,
                                                PrincipalPropertySearch pps)
          throws WebdavException {
    ArrayList<WebdavNsNode> pnodes = new ArrayList<WebdavNsNode>();

    for (UserInfo cui: sysi.getPrincipals(resourceUri, pps)) {
      pnodes.add(new WebdavPrincipalNode(sysi.getUrlHandler(),
                                         cui.principalPathPrefix,
                                         new User(cui.account), true,
                                         cui.principalPathPrefix + "/" +
                                           cui.account + "/",
                                         debug));
    }

    return pnodes;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#makeUserHref(java.lang.String)
   */
  public String makeUserHref(String id) throws WebdavException {
    return getSysi().makeHref(new User(id));
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#updateAccess(edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf.AclInfo)
   */
  public void updateAccess(AclInfo info) throws WebdavException {
    CarddavNode node = (CarddavNode)getNode(info.what,
                                              WebdavNsIntf.existanceMust,
                                              WebdavNsIntf.nodeTypeUnknown);

    try {
      // May need a real principal hierarchy
      if (node instanceof CarddavColNode) {
        sysi.updateAccess((CarddavColNode)node, info.acl);
      } else if (node instanceof CarddavCardNode) {
        sysi.updateAccess((CarddavCardNode)node, info.acl);
      } else {
        throw new WebdavException(HttpServletResponse.SC_NOT_IMPLEMENTED);
      }
    } catch (WebdavException wi) {
      throw wi;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  public void emitAcl(WebdavNsNode node) throws WebdavException {
    try {
      Acl acl = node.getCurrentAccess().getAcl();

      if (acl != null) {
        accessUtil.emitAcl(acl, true);
      }
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#getAclPrincipalInfo(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode)
   */
  public Collection<String> getAclPrincipalInfo(WebdavNsNode node) throws WebdavException {
    try {
      TreeSet<String> hrefs = new TreeSet<String>();

      for (Ace ace: node.getCurrentAccess().getAcl().getAces()) {
        AceWho who = ace.getWho();

        if (who.getWhoType() == WhoDefs.whoTypeUser) {
          hrefs.add(accessUtil.makeUserHref(who.getWho()));
        } else if (who.getWhoType() == WhoDefs.whoTypeGroup) {
          hrefs.add(accessUtil.makeGroupHref(who.getWho()));
        }
      }

      return hrefs;
    } catch (AccessException ae) {
      if (debug) {
        error(ae);
      }
      throw new WebdavServerError();
    }
  }

  /* ====================================================================
   *                Property value methods
   * ==================================================================== */

  /** Override this to create namespace specific property objects.
   *
   * @param propnode
   * @return WebdavProperty
   * @throws WebdavException
   */
  public WebdavProperty makeProp(Element propnode) throws WebdavException {
    if (!XmlUtil.nodeMatches(propnode, CarddavTags.addressData)) {
      return super.makeProp(propnode);
    }

    /* Handle the calendar-data element */

    AddressData caldata = new AddressData(new QName(propnode.getNamespaceURI(),
                                                      propnode.getLocalName()),
                                                      debug);
    caldata.parse(propnode);

    return caldata;
  }

  /** Properties we can process */
  private static final QName[] knownProperties = {
    CarddavTags.addressData,
    CarddavTags.maxResourceSize,
  };

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#knownProperty(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode, edu.rpi.cct.webdav.servlet.shared.WebdavProperty)
   */
  public boolean knownProperty(WebdavNsNode node,
                               WebdavProperty pr) {
    QName tag = pr.getTag();

    for (int i = 0; i < knownProperties.length; i++) {
      if (tag.equals(knownProperties[i])) {
        return true;
      }
    }

    /* Try the node for a value */

    return super.knownProperty(node, pr);
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf#generatePropValue(edu.rpi.cct.webdav.servlet.shared.WebdavNsNode, edu.rpi.cct.webdav.servlet.shared.WebdavProperty, boolean)
   */
  public boolean generatePropValue(WebdavNsNode node,
                                   WebdavProperty pr,
                                   boolean allProp) throws WebdavException {
    QName tag = pr.getTag();
    String ns = tag.getNamespaceURI();

    try {
      /* Deal with anything but webdav properties */
      if (ns.equals(WebdavTags.namespace)) {
        // Not ours
        return super.generatePropValue(node, pr, allProp);
      }

      if (tag.equals(CarddavTags.addressData)) {
        // pr may be a CalendarData object - if not it's probably allprops
        if (!(pr instanceof AddressData)) {
          pr = new AddressData(tag, debug);
        }

        AddressData caldata = (AddressData)pr;
        String content = null;

        if (debug) {
          trace("do CalendarData for " + node.getUri());
        }

        int status = HttpServletResponse.SC_OK;
        try {
          content = caldata.process(node);
        } catch (WebdavException wde) {
          status = wde.getStatusCode();
          if (debug && (status != HttpServletResponse.SC_NOT_FOUND)) {
            error(wde);
          }
        }

        if (status != HttpServletResponse.SC_OK) {
          // XXX should be passing status back
          return false;
        }

        /* Output the (transformed) node.
         */

        xml.cdataProperty(CarddavTags.addressData, content);
        return true;
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
    } catch (WebdavException wie) {
      throw wie;
    } catch (Throwable t) {
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
    public Collection<WebdavNsNode> nodes = new ArrayList<WebdavNsNode>();;
  }

  /** Use the given query to return a collection of nodes. An exception will
   * be raised if the entire query fails for some reason (access, etc). An
   * empty collection will be returned if no objects match.
   *
   * @param wdnode    WebdavNsNode defining root of search
   * @param retrieveRecur  How we retrieve recurring events
   * @param fltr      Filter object defining search
   * @param limits    to limit resutl size
   * @return Collection of result nodes (empty for no result)
   * @throws WebdavException
   */
  public QueryResult query(WebdavNsNode wdnode,
                                        Filter fltr,
                                        GetLimits limits) throws WebdavException {
    QueryResult qr = new QueryResult();
    CarddavNode node = getBwnode(wdnode);

    GetResult res = fltr.query(node, limits);

    qr.overLimit = res.overLimit;
    qr.serverTruncated = res.serverTruncated;

    /* We now need to build a node for each of the cards in the collection.
       For each card we must determine what collection it's in. We then take the
       incoming uri, strip any collection names off it and append the collection
       name and card name to create the new uri.

       If there is no name for the card we just give it the default.
     */

    qr.nodes = new ArrayList<WebdavNsNode>();

    try {
      for (Vcard card: res.cards) {

        CarddavCollection col = node.getWdCollection();
        card.setParent(col);

        /* Was this - this code assumed a multi-depth search
        CarddavCollection col = card.getParent();*/
        String uri = col.getPath();

        /* If no name was assigned use the guid */
        String name = card.getName();
        if (name == null) {
          name = card.getUid() + ".ics";
        }

        String curi = uri + "/" + name;

        CarddavCardNode cnode = (CarddavCardNode)getNodeInt(curi,
                                                   WebdavNsIntf.existanceDoesExist,
                                                   WebdavNsIntf.nodeTypeEntity,
                                                   col, card, null);

        qr.nodes.add(cnode);
      }

      qr.nodes = fltr.postFilter(qr.nodes);

      return qr;
    } catch (WebdavException we) {
      throw we;
    } catch (Throwable t) {
      error(t);
      throw new WebdavServerError();
    }
  }

  /**
   * @param node
   * @return CaldavBwNode
   * @throws WebdavException
   */
  public CarddavNode getBwnode(WebdavNsNode node)
      throws WebdavException {
    if (!(node instanceof CarddavNode)) {
      throw new WebdavException("Not a valid node object " +
                                    node.getClass().getName());
    }

    return (CarddavNode)node;
  }

  /**
   * @param node
   * @param errstatus
   * @return CaldavCalNode
   * @throws WebdavException
   */
  public CarddavColNode getCalnode(WebdavNsNode node, int errstatus)
      throws WebdavException {
    if (!(node instanceof CarddavColNode)) {
      throw new WebdavException(errstatus);
    }

    return (CarddavColNode)node;
  }

  /* ====================================================================
   *                         Private methods
   * ==================================================================== */

  /** Find the named item by following down the path from the root.
   * This requires the names at each level to be unique (and present)
   *
   * I don't think the name.now has to have an ics suffix. Draft 7 goes as
   * far as saying it may have ".ics" or ".ifb"
   *
   * For the moment enforce one or the other
   *
   * <p>Uri is at least /user/user-id or <br/>
   *    /public
   * <br/>followed by one or more calendar path elements possibly followed by an
   * entity name.
   *
   * @param uri        String uri - just the path part
   * @param existance        Say's something about the state of existance
   * @param nodeType         Say's something about the type of node
   * @param col        Supplied WdCollection object if we already have it.
   * @param card
   * @param rsrc
   * @return CaldavURI object representing the uri
   * @throws WebdavException
   */
  private CarddavURI findURI(String uri,
                            int existance,
                            int nodeType,
                            CarddavCollection col,
                            Vcard card,
                            CarddavResource rsrc) throws WebdavException {
    try {
      if ((nodeType == WebdavNsIntf.nodeTypeUnknown) &&
          (existance != WebdavNsIntf.existanceMust)) {
        // We assume an unknown type must exist
        throw new WebdavServerError();
      }

      /* Normalize the uri - Remove all "." and ".." components */

//      boolean collectionUri = false;

      try {
        uri = new URI(null, null, uri, null).toString();

        uri = new URI(URLEncoder.encode(uri, "UTF-8")).normalize().getPath();

        uri = URLDecoder.decode(uri, "UTF-8");

        if (uri.endsWith("/")) {
//          collectionUri = true;
          uri = uri.substring(0, uri.length() - 1);
        }

        if (debug) {
          debugMsg("Normalized uri=" + uri);
        }
      } catch (Throwable t) {
        if (debug) {
          error(t);
        }
        throw new WebdavBadRequest("Bad uri: " + uri);
      }

      if (!uri.startsWith("/")) {
        return null;
      }

      CarddavURI curi = null;

      boolean isPrincipal = sysi.isPrincipal(uri);

      if ((nodeType == WebdavNsIntf.nodeTypePrincipal) && !isPrincipal) {
        throw new WebdavNotFound(uri);
      }

      if (isPrincipal) {
        return new CarddavURI(getSysi().getPrincipal(uri));
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

      if ((nodeType == WebdavNsIntf.nodeTypeCollection) ||
          (nodeType == WebdavNsIntf.nodeTypeUnknown)) {
        // For unknown we try the full path first as a calendar.
        if (debug) {
          debugMsg("search for collection uri \"" + uri + "\"");
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

          if (debug) {
            debugMsg("create collection uri - col=\"" + col.getPath() + "\"");
          }

          curi = new CarddavURI(col, true);

          return curi;
        }
      }

      // Entity or unknown

      /* Split name into parent path and entity name part */
      SplitResult split = splitUri(uri);

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
        CarddavCollection newCol = new CarddavCollection();

        newCol.setParent(col);
        newCol.setName(split.name);
        newCol.setPath(col.getPath() + "/" + newCol.getName());

        curi = new CarddavURI(newCol, false);

        return curi;
      }

      if (col.getAddressBook()) {
        if (debug) {
          debugMsg("find card - col=\"" + col.getPath() + "\" name=\"" +
                   split.name + "\"");
        }

        card = sysi.getCard(col.getPath(), split.name);

        if ((existance == WebdavNsIntf.existanceMust) && (card == null)) {
          throw new WebdavNotFound(uri);
        }

        curi = new CarddavURI(col, card, split.name, card != null);
      } else {
        if (debug) {
          debugMsg("find resource - col=\"" + col.getPath() + "\" name=\"" +
                   split.name + "\"");
        }

        /* Look for a resource */
        rsrc = sysi.getFile(col, split.name);

        if ((existance == WebdavNsIntf.existanceMust) && (rsrc == null)) {
          throw new WebdavNotFound(uri);
        }

        boolean exists = rsrc != null;

        if (!exists) {
          rsrc = new CarddavResource();
          rsrc.setName(split.name);
          rsrc.setParent(col);
        }

        curi = new CarddavURI(rsrc, exists);
      }

      //putUriPath(curi);

      return curi;
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private static class SplitResult {
    String path;
    String name;

    SplitResult(String path, String name) {
      this.path = path;
      this.name = name;
    }
  }

  /* Split the uri so that result.path is the path up to the name part result.name
   *
   * NormalizeUri was called previously so we have no trailing "/"
   */
  private SplitResult splitUri(String uri) throws WebdavException {
    int pos = uri.lastIndexOf("/");
    if (pos < 0) {
      // bad uri
      throw new WebdavBadRequest("Invalid uri: " + uri);
    }

    if (pos == 0) {
      return new SplitResult(uri, null);
    }

    return new SplitResult(uri.substring(0, pos), uri.substring(pos + 1));
  }
}
