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
package org.bedework.carddav.server.dirHandlers.ldap;

import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.server.SysIntf.GetLimits;
import org.bedework.carddav.server.SysIntf.GetResult;
import org.bedework.carddav.server.dirHandlers.AbstractDirHandler;
import org.bedework.carddav.server.dirHandlers.ldap.LdapMapping.AttrPropertyMapping;
import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.server.filter.PropFilter;
import org.bedework.carddav.server.filter.TextMatch;
import org.bedework.carddav.util.CardDAVContextConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.util.LdapDirHandlerConfig;
import org.bedework.carddav.vcard.Card;
import org.bedework.carddav.vcard.PropertyBuilder;
import org.bedework.util.misc.Util;

import edu.rpi.cct.webdav.servlet.shared.UrlHandler;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.Ace;

import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.property.Kind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
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

  protected DirContext ctx;

  private NamingEnumeration<SearchResult> sresult;

  private String[] attrIdList;

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.dirHandlers.AbstractDirHandler#init(org.bedework.carddav.util.CardDAVConfig, org.bedework.carddav.util.DirHandlerConfig, edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler)
   */
  @Override
  public void init(final CardDAVContextConfig cdConfig,
                   final DirHandlerConfig dhConfig,
                   final UrlHandler urlHandler) throws WebdavException {
    super.init(cdConfig, dhConfig, urlHandler);

    ldapConfig = (LdapDirHandlerConfig)dhConfig;
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#getCard(java.lang.String, java.lang.String)
   */
  public Card getCard(final String path, final String name) throws WebdavException {
    verifyPath(path);

    try {
      openContext();

      String fullPath = Util.buildPath(false, path, "/", name);

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
  public GetResult getCards(final String path,
                            final Filter filter,
                            final GetLimits limits) throws WebdavException {
    verifyPath(path);

    try {
      String ldapFilter = makeFilter(filter);

      openContext();

      GetResult res = searchChildren(path, ldapFilter, limits, true);

      if (!res.entriesFound) {
        return res;
      }

      res.cards = new ArrayList<Card>();

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
  public CarddavCollection getCollection(final String path) throws WebdavException {
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
  public GetResult getCollections(final String path,
                                  final GetLimits limits)
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

  protected GetResult search(final String base,
                             final String filter,
                             final GetLimits limits,
                             final int scope) throws WebdavException {
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

      constraints.setReturningAttributes(getAttrIdList());

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

    Card card;
  }

  protected CardObject nextCard(final String path, final boolean fullPath) throws WebdavException {
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

  protected CollectionObject nextCdCollection(final String path,
                                              final boolean fullPath) throws WebdavException {
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

  protected CarddavCollection makeCdCollection(final String path,
                                               final boolean fullPath,
                                               final Attributes attrs) throws WebdavException {
    CarddavCollection cdc = new CarddavCollection();

    if (ldapConfig.getDirectory()) {
      /* This prefix is flagged as a potentially large directory.
       * This tells clients not to try to download the whole thing.
       */
      cdc.setDirectory(true);
    }

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
      cdc.setPath(Util.buildPath(true, path, "/", cdc.getName()));
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

    if (dhConfig.getOwnerHref() != null) {
      cdc.setOwner(getPrincipal(dhConfig.getOwnerHref()));
    } else {
      /* XXX This is incorrect but might get us going
       * I'm assuming the path starts with our path prefix.
       * The next element should be the account. Make that into an owner principal.
       *
       * Really we need an owner attribute of some kind - allowing for group ownership
       * or paths which don't have an owner - e.g. public
       */

      String[] elements = path.split("/");

      //String[] splitRoot = userRoot.split("/");
      int accountIndex = 2; // splitRoot.length;

      String pref = makePrincipalHref(elements[2], Ace.whoTypeUser);

      cdc.setOwner(getPrincipal(pref));
    }

    return cdc;
  }

  private static class LdapObject {
    boolean limitExceeded;

    Attributes attrs;
    String name;

    LdapObject() {
    }

    LdapObject(final Attributes attrs, final String name) {
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
  protected Card makeVcard(final String path,
                            final boolean fullPath,
                            final Attributes attrs) throws WebdavException {
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

    Card card;
    try {
      card = new Card();

      card.setCreated(makeIsoDatetime(stringAttr(attrs, "createTimestamp")));

      String lastMod = stringAttr(attrs, "modifyTimestamp");

      if (lastMod != null) {
        card.setLastmod(makeIsoDatetime(lastMod));
      } else {
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
        source = Util.buildPath(false, path, "/", card.getName());
      }
      source = urlHandler.prefix(source);

      simpleProp(card, "SOURCE", source);
      // XXX Use the source as the uid as well.
      simpleProp(card, "UID", source);

      /* The kind for the card either comes from a custom attribute in the
       * directory or from an explicitly defined kind in the configuration.
       */

      String attrId = LdapMapping.getKindAttrId();
      Attribute attr = null;

      if (attrId != null) {
        attr = attrs.get(attrId);
      }

      if ((attr != null) && (attr.get() != null)) {
        simpleProp(card, "KIND", (String)attr.get());
      } else if (ldapConfig.getCardKind() != null) {
        simpleProp(card, "KIND", ldapConfig.getCardKind());
      }

      Kind kind = (Kind)card.findProperty(Property.Id.KIND);

      if (kind == null) {
        // Default is individual
        kind = Kind.INDIVIDUAL;
      }

      AttrPropertyMapping kindMapping = LdapMapping.getKindMapping();

      for (LdapMapping lm: LdapMapping.attrToVcardProperty.values()) {
        if (!(lm instanceof AttrPropertyMapping)) {
          continue;
        }

        AttrPropertyMapping apm = (AttrPropertyMapping)lm;

        if ((kindMapping != null) &&
            kindMapping.equals(apm)) {
          // Already done kind
          continue;
        }

        /* Skip this property mapping if it isn't for this kind of vcard */
        if (!apm.getKinds().isEmpty() && !apm.getKinds().contains(kind)) {
          continue;
        }

        if ((apm.getGroup() == null) &&
            (apm.getParameterValue() == null)) {
          simpleProp(card, apm.getPropertyName(), attrs, apm.getAttrId());
        } else {
          paramProp(card, apm.getGroup(), apm.getPropertyName(),
                    apm.getParameterName(), apm.getParameterValue(),
                    attrs, apm.getAttrId());
        }
      }

      /*
      simpleProp(card, "FN", attrs, "cn");
      simpleProp(card, "N", attrs, "sn");
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
      */

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

  private String makeIsoDatetime(final String val) {
    if (val == null) {
      return null;
    }

    if ((val.length() == 16) &&
        (val.charAt(8) == 'T')) {
      return val;
    }

    if (val.length() == 15) {
      return val.substring(0, 8) + "T" + val.substring(8);
    }

    return val;
  }

  private String makeFilter(final Filter filter) {
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

  private String makePropFilterExpr(final PropFilter filter) {
    TextMatch tm = filter.getMatch();

    if (tm == null) {
      return null;
    }

    String name = filter.getName();

    int cpos = name.indexOf(',');

    if ((cpos < 0) && (Util.isEmpty(filter.getParamFilters()))) {
      // No group - no params - single attribute
      String attrId = LdapMapping.simplePropertyToLdapAttr(name);

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

    Collection<String> anames = LdapMapping.toLdapAttrNoGroup.get(name);
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

  private String makePropFilterExpr(final String attrId, final TextMatch tm) {
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

  private void simpleProp(final Card card,
                          final String propname,
                          final String value) throws WebdavException {
    Property p = PropertyBuilder.getProperty(propname, value);

    card.addProperty(p);
  }

  private void simpleProp(final Card card, final String propname,
                          final Attributes attrs,
                          final String attrId) throws WebdavException {
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

  private void paramProp(final Card card, final String group,
                         final String propname,
                         final String paramName,
                         final String paramValue,
                         final Attributes attrs,
                         final String attrId) throws WebdavException {
    String s = stringAttr(attrs, attrId);

    if (s == null) {
      return;
    }

    Property p = PropertyBuilder.getProperty(group,
                                             paramName, paramValue,
                                             propname, s);

    card.addProperty(p);
  }

  private String stringAttr(final Attributes attrs,
                           final String attrId) throws WebdavException {
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
   */
  protected Attributes getObject(final String path,
                             final boolean isCollection) throws WebdavException {
    try {
      AccessPrincipal ap = null;
      try {
        ap = getPrincipal(path);
      } catch (Throwable t) {
      }

      String dn;

      if ((ap != null) && (ap.getKind() == Ace.whoTypeUser)) {
        // Do principals

        dn = makePrincipalDn(ap.getAccount());
      } else {
        // Not principals - split the path after the suffix and turn into a series
        // of folders

        dn = makeAddrbookDn(path, isCollection);
      }

      return ctx.getAttributes(dn, getAttrIdList());
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
  protected GetResult searchChildren(final String path,
                                     final String filter,
                                     final GetLimits limits,
                                     final boolean cards) throws WebdavException {
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

      AccessPrincipal ap = null;
      try {
        ap = getPrincipal(path);
      } catch (Throwable t) {
      }

      if (ap != null) {
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

  protected String makePrincipalDn(final String account) {
    return ldapConfig.getPrincipalIdAttr() + account + "," +
                       ldapConfig.getBaseDn();
  }

  protected String makeAddrbookDn(final String path,
                                 final boolean isCollection) throws WebdavException {
    String remPath;

    if (path.endsWith("/")) {
      remPath = path.substring(0, path.length() - 1);
    } else {
      remPath = path;
    }

    if (dhConfig.getPathPrefix().equals(remPath)) {
      return ldapConfig.getBaseDn();
    }

    remPath = path.substring(dhConfig.getPathPrefix().length() + 1);
    if (remPath.endsWith(".vcf")) {
      remPath = remPath.substring(0, remPath.length() - 4);
    }

    String[] elements = remPath.split("/");

    StringBuilder sb = new StringBuilder();
    int i = elements.length - 1;

    if (i >= 0) {
      if (isCollection) {
        sb.append(ldapConfig.getFolderIdAttr());
      } else {
        sb.append(ldapConfig.getAddressbookEntryIdAttr());
      }

      sb.append("=");
      sb.append(dnEscape(elements[i]));
      i--;

      while (i > 0) {
        sb.append(",");
        sb.append(ldapConfig.getFolderIdAttr());
        sb.append("=");
        sb.append(dnEscape(elements[i]));
        i--;
      }

      sb.append(",");
    }

    sb.append(ldapConfig.getBaseDn());

    return sb.toString();
  }

  protected String dnEscape(final String val) {
    if (val.indexOf(",") < 0) {
      return val;
    }

    StringBuilder sb = new StringBuilder();

    int pos = 0;
    while (pos < val.length()) {
      int nextPos = val.indexOf(",", pos);

      if (nextPos < 0) {
        sb.append(val.substring(pos));
        break;
      }

      sb.append(val.substring(pos, nextPos));
      sb.append("\\,");
      pos = nextPos + 1;
    }

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

  protected String[] getAttrIdList() {
    if (attrIdList != null) {
      return attrIdList;
    }

    List<String> a = new ArrayList<String>(LdapMapping.defaultAttrIdList);

    if (ldapConfig.getAttrIdList() != null) {
      a.addAll(Arrays.asList(ldapConfig.getAttrIdList()));
    }
    attrIdList = a.toArray(new String[0]);

    return attrIdList;
  }
}
