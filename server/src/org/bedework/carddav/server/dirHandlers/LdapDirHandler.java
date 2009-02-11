/* **********************************************************************
    Copyright 2008 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
package org.bedework.carddav.server.dirHandlers;

import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.server.SysIntf.GetLimits;
import org.bedework.carddav.server.SysIntf.GetResult;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.server.filter.PropFilter;
import org.bedework.carddav.server.filter.TextMatch;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.util.LdapDirHandlerConfig;
import org.bedework.carddav.vcard.Param;
import org.bedework.carddav.vcard.Property;
import org.bedework.carddav.vcard.Vcard;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler;
import edu.rpi.cmt.access.PrincipalInfo;
import edu.rpi.sss.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;


/** Provide some common methods for ldap based directory handlers.
 *
 * @author douglm
 *
 */
public abstract class LdapDirHandler extends AbstractDirHandler {
  protected LdapDirHandlerConfig ldapConfig;

  private DirContext ctx;

  private NamingEnumeration<SearchResult> sresult;

  /* The data for this should probably come from the dirhandler config */

  /* These mappers provide the mapping for simple one atttribute per property
   * More complex mappings have to be dealt with in another manner.
   */

  protected static class VcardProperty {
    String group;
    String name;
    String type;

    VcardProperty(String name) {
      this(null, name, null);
    }

    VcardProperty(String group, String name, String type) {
      this.group = group;
      this.name = name;
      this.type = type;
    }

    public int hashCode() {
      int hc = 0;

      if (group != null) {
        hc = group.hashCode();
      }

      if (type != null) {
        hc*= type.hashCode();
      }

      return hc * name.hashCode();
    }

    public boolean equals(Object o) {
      if (!(o instanceof VcardProperty)) {
        return false;
      }

      VcardProperty that = (VcardProperty)o;

      return (Util.compareStrings(group, that.group) == 0) &&
             (Util.compareStrings(type, that.type) == 0) &&
             (Util.compareStrings(name, that.name) == 0);
    }
  }

  protected static final Map<VcardProperty, String> toLdapAttr =
    new HashMap<VcardProperty, String>();

  protected static final Map<String, Collection<String>> toLdapAttrNoGroup =
    new HashMap<String, Collection<String>>();

  protected static final Map<String, VcardProperty> toVcardProperty =
    new HashMap<String, VcardProperty>();

  static {
    //      SOURCE                         ldap url
    //      NAME                           some sort of name
    //      KIND                           "individual" for a single person,
    //                                     "group" for a group of people,
    //                                     "org" for an organization,
    //                                     "location" */
    addPropertyAttrMapping("FN", "cn");
    //        N                              sn; Given Names; Honorific Prefixes; Honorific Suffixes
    addPropertyAttrMapping("NICKNAME", "displayName");
    //      PHOTO
    //      BDAY
    //      DDAY
    //      BIRTH
    //      DEATH
    //      GENDER
    //      ADR                            po box; apartment or suite; street;
    //                                     locality (e.g., city);
    //                                     region (e.g., state or province);
    //                                     postal code; country
    //      LABEL
    addPropertyAttrMapping("HOME", "TEL", "voice", "homePhone");
    //HOME  TEL         TYPE=msg
    //HOME  TEL         TYPE=fax
    addPropertyAttrMapping("HOME", "TEL", "cell", "mobile");
    //HOME  TEL         TYPE=video
    //HOME  TEL         TYPE=pager
    addPropertyAttrMapping("WORK", "TEL", "voice", "telephoneNumber");
    //WORK  TEL         TYPE=msg
    addPropertyAttrMapping("WORK", "TEL", "fax", "facsimileTelephoneNumber");
    //WORK  TEL         TYPE=cell
    //WORK  TEL         TYPE=video
    addPropertyAttrMapping("WORK", "TEL", "pager", "pager");
    addPropertyAttrMapping("EMAIL", "mail");
    addPropertyAttrMapping("IMPP", "IM");
    //      LANG
    //      TZ
    //      GEO
    addPropertyAttrMapping("TITLE", "title");
    //      ROLE
    //      LOGO
    //      ORG                            organization name;
    //                                     one or more levels of org unit names
    //      MEMBER                         urls of group members
    //      RELATED
    //      CATEGORIES
    addPropertyAttrMapping("NOTE", "description");
    //      PRODID
    //      REV
    //      SORT-STRING
    //      SOUND
    //      UID                            Use the source for the moment.
    //      URL
    //      VERSION                        Value="4.0"
    //      CLASS
    //      KEY
    //      FBURL
    //      CALADRURI
    //      CALURI
  }

  private static void addPropertyAttrMapping(String pname, String aname) {
    addPropertyAttrMapping(null, pname, null, aname);
  }

  //private static void addPropertyAttrMapping(String group,
  //                                           String pname,
  //                                           String aname) {
  //  addPropertyAttrMapping(group, pname, null, aname);
  //}

  private static void addPropertyAttrMapping(String group,
                                             String pname,
                                             String type,
                                             String aname) {
    VcardProperty vcp = new VcardProperty(group, pname, type);
    toLdapAttr.put(vcp, aname);

    Collection<String> anames = toLdapAttrNoGroup.get(pname);
    if (anames == null) {
      anames = new ArrayList<String>();
      toLdapAttrNoGroup.put(pname, anames);
    }
    anames.add(aname);

    toVcardProperty.put(aname, vcp);
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.dirHandlers.AbstractDirHandler#init(org.bedework.carddav.util.CardDAVConfig, org.bedework.carddav.util.DirHandlerConfig, edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler)
   */
  public void init(CardDAVConfig cdConfig,
                   DirHandlerConfig dhConfig,
                   UrlHandler urlHandler) throws WebdavException {
    super.init(cdConfig, dhConfig, urlHandler);

    ldapConfig = (LdapDirHandlerConfig)dhConfig;
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCard(java.lang.String, java.lang.String)
   */
  public Vcard getCard(String path, String name) throws WebdavException {
    verifyPath(path);

    try {
      openContext();

      String fullPath = path + "/" + name;

      Attributes attrs = getObject(fullPath, false);

      if (attrs == null) {
        return null;
      }

      return makeVcard(fullPath, true, attrs);
    } finally {
      closeContext();
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCards(java.lang.String, org.bedework.carddav.server.filter.Filter, org.bedework.carddav.server.SysIntf.GetLimits)
   */
  public GetResult getCards(String path,
                            Filter filter,
                            GetLimits limits) throws WebdavException {
    verifyPath(path);

    try {
      String ldapFilter = makeFilter(filter);

      openContext();

      GetResult res = searchChildren(path, ldapFilter, limits, true);

      if (!res.entriesFound) {
        return res;
      }

      res.cards = new ArrayList<Vcard>();

      for (;;) {
        CardObject co = nextCard(path, false);

        if (co == null) {
          break;
        }

        if (co.limitExceeded) {
          res.overLimit = true;
          break;
        }

        res.cards.add(co.card);
      }

      return res;
    } finally {
      closeContext();
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCollection(java.lang.String)
   */
  public CarddavCollection getCollection(String path) throws WebdavException {
    verifyPath(path);

    /* We're fetching a collection entity with a fully specified path */
    try {
      openContext();

      Attributes attrs = getObject(path, true);

      if (attrs == null) {
        return null;
      }

      return makeCdCollection(path, true, attrs);
    } finally {
      closeContext();
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCollections(java.lang.String, org.bedework.carddav.server.SysIntf.GetLimits)
   */
  public GetResult getCollections(String path,
                                  GetLimits limits)
         throws WebdavException {
    verifyPath(path);

    try {
      openContext();

      GetResult res = searchChildren(path, null, limits, false);

      if (!res.entriesFound) {
        return res;
      }

      res.collections = new ArrayList<CarddavCollection>();
      for (;;) {
        CollectionObject co = nextCdCollection(path, false);

        if (co == null) {
          break;
        }

        if (co.limitExceeded) {
          res.overLimit = true;
          break;
        }

        res.collections.add(co.col);
      }

      return res;
    } finally {
      closeContext();
    }
  }

  /* ====================================================================
   *  Protected methods.
   * ==================================================================== */

  protected GetResult search(String base,
                             String filter,
                             GetLimits limits,
                             int scope) throws WebdavException {
    GetResult res = new GetResult();

    try {
      if (debug) {
        trace("About to search: base=" + base + " filter=" + filter +
              " scope=" + scope);
      }

      SearchControls constraints = new SearchControls();

      constraints.setSearchScope(scope);

      int limit = 0;
      if ((limits != null) && (limits.limit != 0)) {

        if (limits.curCount == limits.limit) {
          // Called in error
          res.overLimit = true;
          return res;
        }
        limit = limits.limit - limits.curCount;
      } else {
        limit = ldapConfig.getQueryLimit();
      }

      if (limit != 0) {
        constraints.setCountLimit(limit);
      }

      constraints.setReturningAttributes(ldapConfig.getAttrIdList());

      try {
        sresult = ctx.search(base, filter, constraints);

        if ((sresult != null) && sresult.hasMore()) {
          res.entriesFound = true;
        }

        if (debug) {
          trace("About to return from search with " + res.entriesFound);
        }
      } catch (NameNotFoundException e) {
        // Allow that one.
        if (debug) {
          trace("NameNotFoundException: return with null");
        }
        sresult = null;
      }

      return res;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private static class CardObject {
    boolean limitExceeded;

    Vcard card;
  }

  protected CardObject nextCard(String path, boolean fullPath) throws WebdavException {
    LdapObject ldo = nextObject();

    if (ldo == null) {
      return null;
    }

    CardObject co = new CardObject();
    if (ldo.limitExceeded) {
      co.limitExceeded = true;
      return co;
    }

    co.card = makeVcard(path, fullPath, ldo.attrs);

    return co;
  }

  private static class CollectionObject {
    boolean limitExceeded;

    CarddavCollection col;
  }

  protected CollectionObject nextCdCollection(String path,
                                              boolean fullPath) throws WebdavException {
    LdapObject ldo = nextObject();

    if (ldo == null) {
      return null;
    }

    CollectionObject co = new CollectionObject();
    if (ldo.limitExceeded) {
      co.limitExceeded = true;
      return co;
    }

    co.col = makeCdCollection(path, fullPath, ldo.attrs);

    return co;
  }

  protected CarddavCollection makeCdCollection(String path,
                                               boolean fullPath,
                                               Attributes attrs) throws WebdavException {
    CarddavCollection cdc = new CarddavCollection();

    if (ldapConfig.getAddressBook()) {
      /* This prefix is flagged as an address book. */
      cdc.setAddressBook(true);
    } else {
      /* Look for our special object class */
      Attribute oc = attrs.get("objectClass");
      if (oc == null) {
        throw new WebdavException("Need object class attribute");
      }

      try {
        NamingEnumeration<? extends Object> ocs = oc.getAll();
        while (ocs.hasMore()) {
          String soc = (String)ocs.next();
          if (soc.equals(ldapConfig.getAddressbookObjectClass())) {
            cdc.setAddressBook(true);
            break;
          }
        }
      } catch (NamingException ne) {
        throw new WebdavException(ne);
      }
    }

    cdc.setCreated(stringAttr(attrs, "createTimestamp"));
    cdc.setLastmod(stringAttr(attrs, "modifyTimestamp"));

    if (cdc.getLastmod() == null) {
      cdc.setLastmod(cdc.getCreated());
    }

    /* The name of this card comes from the attribute specified in the
     * config - addressbookEntryIdAttr
     */

    cdc.setName(stringAttr(attrs, ldapConfig.getFolderIdAttr()));
    cdc.setDisplayName(stringAttr(attrs, ldapConfig.getFolderIdAttr()));

    if (fullPath) {
      cdc.setPath(path);
//      simpleProp(card, "SOURCE", path);
    } else {
//      String cardName = stringAttr(attrs,
//                                   ldapConfig.getAddressbookEntryIdAttr());
//      simpleProp(card, "SOURCE",
//                 urlHandler.prefix(path + cardName + ".vcf"));
      cdc.setPath(path + "/" + cdc.getName());
    }

    //private String path;

    //private AccessPrincipal owner;

    /** UTC datetime */
    //private String created;

    /** UTC datetime */
    //private String lastmod;

    /** Ensure uniqueness - lastmod only down to second.
     */
    //private int sequence;

    cdc.setDescription(stringAttr(attrs, "description"));

    // parent          CarddavCollection
    // addressBook     boolean

    return cdc;
  }

  private static class LdapObject {
    boolean limitExceeded;

    Attributes attrs;
    String name;

    LdapObject() {
    }

    LdapObject(Attributes attrs, String name) {
      this.attrs = attrs;
      this.name = name;
      if ((name != null) && (name.length() == 0)) {
        this.name = null;
      }
    }
  }

  private LdapObject nextObject() throws WebdavException {
    try {
      SearchResult s = null;

      if (sresult == null) {
        throw new WebdavException("null search result");
      }

      if (!sresult.hasMore()) {
        if (debug) {
          trace("nextItem: no more");
        }

        sresult = null;
        return null;
      }

      try {
        s = sresult.next();
      } finally {
        if (s == null) {
          try {
            sresult.close();
          } catch (Exception e) {};

          sresult = null;
        }
      }

      if (s == null) {
        return null;
      }

      return new LdapObject(s.getAttributes(), s.getName());
    } catch (SizeLimitExceededException slee) {
      LdapObject le = new LdapObject();

      le.limitExceeded = true;
      return le;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /**
   * @param path path to this card
   * @param fullPath - path includes card name
   * @param attrs
   * @return
   * @throws WebdavException
   */
  protected Vcard makeVcard(String path,
                            boolean fullPath,
                            Attributes attrs) throws WebdavException {
    /* Map ldap attributes onto vcard. The following represents a best guess
     *
     * Group Attr        Param              ldap attr
             SOURCE                         ldap url
             NAME                           some sort of name
             KIND                           "individual" for a single person,
                                            "group" for a group of people,
                                            "org" for an organization,
                                            "location"
             FN                             cn
             N                              sn; Given Names; Honorific Prefixes; Honorific Suffixes
             NICKNAME                       displayName
             PHOTO
             BDAY
             DDAY
             BIRTH
             DEATH
             GENDER
             ADR                            po box; apartment or suite; street;
                                            locality (e.g., city);
                                            region (e.g., state or province);
                                            postal code; country
             LABEL
       HOME  TEL         TYPE=voice         homePhone
       HOME  TEL         TYPE=msg
       HOME  TEL         TYPE=fax
       HOME  TEL         TYPE=cell          mobile
       HOME  TEL         TYPE=video
       HOME  TEL         TYPE=pager
       WORK  TEL         TYPE=voice         telephoneNumber
       WORK  TEL         TYPE=msg
       WORK  TEL         TYPE=fax           facsimileTelephoneNumber
       WORK  TEL         TYPE=cell
       WORK  TEL         TYPE=video
       WORK  TEL         TYPE=pager         pager
             EMAIL                          mail
             IMPP                           IM url
             LANG
             TZ
             GEO
             TITLE                          title
             ROLE
             LOGO
             ORG                            organization name;
                                            one or more levels of org unit names
             MEMBER                         urls of group members
             RELATED
             CATEGORIES
             NOTE                           description
             PRODID
             REV
             SORT-STRING
             SOUND
             UID                            Use the source for the moment.
             URL
             VERSION                        Value="4.0"
             CLASS
             KEY
             FBURL
             CALADRURI                      use "mailto: + mail is no value supplied elsewhere
             CALURI
     */

    Vcard card;
    try {
      card = new Vcard();

      card.setCreated(stringAttr(attrs, "createTimestamp"));
      card.setLastmod(stringAttr(attrs, "modifyTimestamp"));

      if (card.getLastmod() == null) {
        card.setLastmod(card.getCreated());
      }

      /* The name of this card comes from the attribute specified in the
       * config - addressbookEntryIdAttr
       */

      card.setName(stringAttr(attrs, ldapConfig.getAddressbookEntryIdAttr()) + ".vcf");
      //card.setDisplayName(stringAttr(attrs, ldapConfig.getAddressbookEntryIdAttr()));

      /* The name of this card comes from the attribute specified in the
       * config - addressbookEntryIdAttr
       */

      String source;
      if (fullPath) {
        source = path;
      } else {
        source = path + "/" + card.getName();
      }
      source = urlHandler.prefix(source);

      simpleProp(card, "SOURCE", source);
      // XXX Use the source as th euid as well.
      simpleProp(card, "UID", source);

      /* The kind for the card either comes from a custom attribute in the
       * directory or from an explicitly defined kind in the configuration.
       *
       * We probably need some sort of objectClass to kind mapper.
       */

      if (ldapConfig.getCardKind() != null) {
        simpleProp(card, "KIND", ldapConfig.getCardKind());
      } else {
        //simpleProp(card, "KIND", attrs, "cardkind");
      }

      simpleProp(card, "FN", attrs, "cn");
      //N                              sn; Given Names; Honorific Prefixes; Honorific Suffixes
      simpleProp(card, "NICKNAME", attrs, "displayName");

      //ADR                            po box; apartment or suite; street;
      //                               locality (e.g., city);
      //                               region (e.g., state or province);
      //                               postal code; country

      paramProp(card, "HOME", "TEL", "TYPE", "voice", attrs, "homePhone");
      //HOME  TEL         TYPE=msg
      //HOME  TEL         TYPE=fax
      paramProp(card, "HOME", "TEL", "TYPE", "cell", attrs, "mobile");
      //HOME  TEL         TYPE=video
      //HOME  TEL         TYPE=pager
      paramProp(card, "WORK", "TEL", "TYPE", "voice", attrs, "telephoneNumber");
      //WORK  TEL         TYPE=msg
      paramProp(card, "WORK", "TEL", "TYPE", "fax", attrs, "facsimileTelephoneNumber");
      //WORK  TEL         TYPE=cell
      //WORK  TEL         TYPE=video
      paramProp(card, "WORK", "TEL", "TYPE", "pager", attrs, "pager");

      simpleProp(card, "EMAIL", attrs, "mail");

      String mail = stringAttr(attrs, "mail");
      if (mail != null) {
        simpleProp(card, "CALADRURI", "mailto:" + mail);
      }

      //ORG                            organization name;
      //                               one or more levels of org unit names

      simpleProp(card, "NOTE", attrs, "description");

      /* If the groupMemberAttr is defined in the config try to fetch it.
       * If we succeed add the values to the member property converted into
       * carddav uris if possible.
       */

      if (ldapConfig.getGroupMemberAttr() != null) {
        Attribute mbrAttr = attrs.get(ldapConfig.getGroupMemberAttr());

        if (mbrAttr != null) {
          NamingEnumeration<? extends Object> mbrs = mbrAttr.getAll();
          while (mbrs.hasMore()) {
            String mbr = (String)mbrs.next();
          }
        }
      }

      return card;
    } catch (NamingException ne) {
      throw new WebdavException(ne);
    }
  }

  private String makeFilter(Filter filter) {
    if (filter == null) {
      return null;
    }

    Collection<PropFilter> pfilters = filter.getPropFilters();

    if ((pfilters == null) || pfilters.isEmpty()) {
      return null;
    }

    int testAllAnyProps = filter.getTestAllAny();

    StringBuilder sb = new StringBuilder();

    boolean first = true;

    for (PropFilter pfltr: pfilters) {
      String ptest = makePropFilterExpr(pfltr);

      if (ptest == null) {
        continue;
      }

      sb.append(ptest);

      if (first) {
        first = false;
        continue;
      }

      sb.append(")");

      if (testAllAnyProps == Filter.testAllOf) {
        sb.insert(0, "(&");
      } else {
        sb.insert(0, "(|");
      }
    }

    return sb.toString();
  }

  private String makePropFilterExpr(PropFilter filter) {
    TextMatch tm = filter.getMatch();

    if (tm == null) {
      return null;
    }

    String name = filter.getName();

    int cpos = name.indexOf(',');

    if ((cpos < 0) && (Util.isEmpty(filter.getParamFilters()))) {
      // No group - no params - single attribute
      String attrId = toLdapAttr.get(new VcardProperty(name));

      if (attrId == null) {
        return null;
      }

      return makePropFilterExpr(attrId, tm);
    }

    if (cpos > 0) {
      if (name.endsWith(",")) {
        // Don't do that
        return null;
      }

      name = name.substring(cpos + 1);
    }

    Collection<String> anames = toLdapAttrNoGroup.get(name);
    if (Util.isEmpty(anames)) {
      return null;
    }

    int testAllAnyProps = filter.getTestAllAny();

    StringBuilder sb = new StringBuilder();

    boolean first = true;

    for (String attrId: anames) {
      String ptest = makePropFilterExpr(attrId, tm);

      if (ptest == null) {
        continue;
      }

      sb.append(ptest);

      if (first) {
        first = false;
        continue;
      }

      sb.append(")");

      if (testAllAnyProps == Filter.testAllOf) {
        sb.insert(0, "(&");
      } else {
        sb.insert(0, "(|");
      }
    }

    return sb.toString();
  }

  private String makePropFilterExpr(String attrId, TextMatch tm) {
    StringBuilder sb = new StringBuilder();

    sb.append("(");
    sb.append(attrId);
    sb.append("=");

    int mt = tm.getMatchType();
    if ((mt == TextMatch.matchTypeContains) ||
        (mt == TextMatch.matchTypeEndsWith)) {
      sb.append("*");
    }

    sb.append(tm.getVal());

    if ((mt == TextMatch.matchTypeContains) ||
        (mt == TextMatch.matchTypeStartsWith)) {
      sb.append("*");
    }

    sb.append(")");

    return sb.toString();
  }

  private void simpleProp(Vcard card, String propname,
                          String value) throws WebdavException {
    Property p = new Property(propname, value);

    card.addProperty(p);
  }

  private void simpleProp(Vcard card, String propname,
                          Attributes attrs,
                          String attrId) throws WebdavException {
    try {
      Attribute attr = attrs.get(attrId);

      if (attr == null) {
        return;
      }

      simpleProp(card, propname, String.valueOf(attr.get()));
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private void paramProp(Vcard card, String group, String propname,
                         String paramName, String paramValue,
                         Attributes attrs,
                         String attrId) throws WebdavException {
    String s = stringAttr(attrs, attrId);

    if (s == null) {
      return;
    }

    Property p = new Property(group, propname, s);
    p.addParam(new Param(paramName, paramValue));

    card.addProperty(p);
  }

  private String stringAttr(Attributes attrs,
                           String attrId) throws WebdavException {
    try {
      Attribute attr = attrs.get(attrId);

      if (attr == null) {
        return null;
      }

      return String.valueOf(attr.get());
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* Do the search for a single object in the directory
\   */
  protected Attributes getObject(String path,
                             boolean isCollection) throws WebdavException {
    try {
      PrincipalInfo pi = null;
      try {
        pi = getPrincipalInfo(path);
      } catch (Throwable t) {
      }

      String dn;

      if ((pi != null) && pi.valid) {
        // Do principals

        dn = makePrincipalDn(pi.who);
      } else {
        // Not principals - split the path after the suffix and turn into a series
        // of folders

        dn = makeAddrbookDn(path, isCollection);
      }

      return ctx.getAttributes(dn, ldapConfig.getAttrIdList());
    } catch (NameNotFoundException nnfe) {
      return null;
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* Search for children of the given path.
   */
  protected GetResult searchChildren(String path,
                                     String filter,
                                     GetLimits limits,
                                     boolean cards) throws WebdavException {
    try {
      StringBuilder sb = new StringBuilder();

      if (cards) {
        sb.append("(objectClass=");
        sb.append(ldapConfig.getAddressbookEntryObjectClass());
        sb.append(")");
      } else if (ldapConfig.getFolderObjectClass() == null) {
        sb.append("(objectClass=");
        sb.append(ldapConfig.getAddressbookObjectClass());
        sb.append(")");
      } else {
        sb.append("(|(objectClass=");
        sb.append(ldapConfig.getFolderObjectClass());
        sb.append(")(objectClass=");
        sb.append(ldapConfig.getAddressbookObjectClass());
        sb.append("))");
      }

      String ldapFilter = sb.toString();

      if (filter != null) {
        ldapFilter = "(&" + ldapFilter + filter + ")";
      }

      PrincipalInfo pi = null;
      try {
        pi = getPrincipalInfo(path);
      } catch (Throwable t) {
      }

      if ((pi != null) && pi.valid) {
        // Do principals
        throw new WebdavException("unimplemented");  // browse principals
      }

      return search(makeAddrbookDn(path, true),
                    ldapFilter, limits, SearchControls.ONELEVEL_SCOPE);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  protected String makePrincipalDn(String account) {
    return ldapConfig.getPrincipalIdAttr() + account + "," +
                       ldapConfig.getBaseDn();
  }

  protected String makeAddrbookDn(String path,
                                 boolean isCollection) throws WebdavException {
    if (dhConfig.getPathPrefix().equals(path)) {
      return ldapConfig.getBaseDn();
    }

    String remPath = path.substring(dhConfig.getPathPrefix().length() + 1);
    if (remPath.endsWith(".vcf")) {
      remPath = remPath.substring(0, remPath.length() - 4);
    }

    String[] elements = remPath.split("/");

    StringBuilder sb = new StringBuilder();
    int i = elements.length - 1;

    if (isCollection) {
      sb.append(ldapConfig.getFolderIdAttr());
    } else {
      sb.append(ldapConfig.getAddressbookEntryIdAttr());
    }

    sb.append("=");
    sb.append(elements[i]);
    i--;

    while (i >= 0) {
      sb.append(",");
      sb.append(ldapConfig.getFolderIdAttr());
      sb.append("=");
      sb.append(elements[i]);
      i--;
    }

    sb.append(",");
    sb.append(ldapConfig.getBaseDn());

    return sb.toString();
  }

  protected void openContext() throws WebdavException {
    try {
      Properties pr = new Properties();

      pr.put(Context.PROVIDER_URL, ldapConfig.getProviderUrl());
      pr.put(Context.INITIAL_CONTEXT_FACTORY, ldapConfig.getInitialContextFactory());

      if (ldapConfig.getAuthDn() != null) {
        pr.put(Context.SECURITY_AUTHENTICATION, "simple");
        pr.put(Context.SECURITY_PRINCIPAL, ldapConfig.getAuthDn());
        pr.put(Context.SECURITY_CREDENTIALS, ldapConfig.getAuthPw());
      }

      // Make simple authentication the default
      pr.put(Context.SECURITY_AUTHENTICATION, "simple");

      if (debug) {
        trace("Directory: get new context for " +
              pr.get(Context.PROVIDER_URL));
      }
      ctx = new InitialDirContext(pr);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  protected void closeContext() {
    if (ctx != null) {
      try {
        ctx.close();
      } catch (Throwable t) {}
    }
  }
}
