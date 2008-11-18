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
import org.bedework.carddav.server.Vcard;
import org.bedework.carddav.server.Vcard.Param;
import org.bedework.carddav.server.Vcard.Property;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.util.LdapDirHandlerConfig;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler;
import edu.rpi.cmt.access.Ace;
import edu.rpi.cmt.access.PrincipalInfo;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
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

  private SearchControls constraints;
  private NamingEnumeration<SearchResult> sresult;

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.dirHandlers.AbstractDirHandler#init(org.bedework.carddav.util.CardDAVConfig, org.bedework.carddav.util.DirHandlerConfig, edu.rpi.cct.webdav.servlet.shared.WebdavNsNode.UrlHandler)
   */
  public void init(CardDAVConfig cdConfig,
                   DirHandlerConfig dhConfig,
                   UrlHandler urlHandler) throws WebdavException {
    super.init(cdConfig, dhConfig, urlHandler);

    ldapConfig = (LdapDirHandlerConfig)dhConfig;
  }

  /* ====================================================================
   *  Protected methods.
   * ==================================================================== */

  protected boolean search(String base,
                           String filter,
                           int scope) throws WebdavException {
    try {
      if (debug) {
        trace("About to search: base=" + base + " filter=" + filter +
              " scope=" + scope);
      }

      constraints.setSearchScope(scope);
      constraints.setCountLimit(1000);
      constraints.setReturningAttributes(ldapConfig.getAttrIdList());

      boolean entriesFound = false;

      try {
        sresult = ctx.search(base, filter, constraints);

        if ((sresult != null) && sresult.hasMore()) {
          entriesFound = true;
        }

        if (debug) {
          trace("About to return from search with " + entriesFound);
        }
      } catch (NameNotFoundException e) {
        // Allow that one.
        if (debug) {
          trace("NameNotFoundException: return with null");
        }
        sresult = null;
      }

      return entriesFound;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  protected Vcard nextCard(String path, boolean fullPath) throws WebdavException {
    LdapObject ldo = nextObject();

    if (ldo == null) {
      return null;
    }

    Vcard card = makeVcard(path, fullPath, ldo.attrs);

    return card;
  }

  protected CarddavCollection nextCdCollection(String path,
                                               boolean fullPath) throws WebdavException {
    LdapObject ldo = nextObject();

    if (ldo == null) {
      return null;
    }

    return makeCdCollection(path, fullPath, ldo.attrs);
  }

  protected CarddavCollection makeCdCollection(String path,
                                               boolean fullPath,
                                               Attributes attrs) throws WebdavException {
    CarddavCollection cdc = new CarddavCollection();

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
    Attributes attrs;
    String name;

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
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /**
   * @param path path to this card
   * @param fullPAth - path includes card name
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
             MEMBER
             RELATED
             CATEGORIES
             NOTE                           description
             PRODID
             REV
             SORT-STRING
             SOUND
             UID
             URL
             VERSION                        Value="4.0"
             CLASS
             KEY
             FBURL
             CALADRURI
             CALURI
     */

    Vcard card = new Vcard();

    //card.setCreated(stringAttr(attrs, "createTimestamp"));
    card.setLastmod(stringAttr(attrs, "modifyTimestamp"));

    /* The name of this card comes from the attribute specified in the
     * config - addressbookEntryIdAttr
     */

    if (fullPath) {
      simpleProp(card, "SOURCE", path);
    } else {
      String cardName = stringAttr(attrs,
                                   ldapConfig.getAddressbookEntryIdAttr());
      simpleProp(card, "SOURCE",
                 urlHandler.prefix(path + cardName + ".vcf"));
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

    //ORG                            organization name;
    //                               one or more levels of org unit names

    simpleProp(card, "NOTE", attrs, "description");

    return card;
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
      if (!path.startsWith(dhConfig.getPathPrefix() + "/")) {
        // Not ours
        throw new WebdavException("Invalid href for this handler");
      }

      PrincipalInfo pi = null;
      try {
        pi = getPrincipalInfo(path);
      } catch (Throwable t) {
      }

      String dn;

      if (pi != null) {
        // Do principals

        if (pi.whoType == Ace.whoTypeUser) {
          dn = makeUserDn(pi.who);
        } else if (pi.whoType == Ace.whoTypeGroup) {
          dn = makeGroupDn(pi.who);
        } else {
          throw new WebdavException("unimplemented");
        }
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
  protected boolean searchChildren(String path,
                                   String childClass) throws WebdavException {
    try {
      StringBuilder sb = new StringBuilder();

      sb.append("(objectClass=");
      sb.append(childClass);
      sb.append(")");

      String ldapFilter = sb.toString();

      PrincipalInfo pi = null;
      try {
        pi = getPrincipalInfo(path);
      } catch (Throwable t) {
      }

      if (pi != null) {
        // Do principals
        throw new WebdavException("unimplemented");  // browse principals
      }

      return search(makeAddrbookDn(path, true),
                    ldapFilter, SearchControls.ONELEVEL_SCOPE);
    } catch (WebdavException wde) {
      throw wde;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  protected String makeUserDn(String account) {
    return ldapConfig.getUserIdAttr() + account + "," +
                       ldapConfig.getUserBaseDn();
  }

  protected String makeGroupDn(String account) {
    return ldapConfig.getGroupIdAttr() + account + "," +
                       ldapConfig.getGroupBaseDn();
  }

  protected String makeAddrbookDn(String path,
                                 boolean isCollection) throws WebdavException {
    String remPath = path.substring(dhConfig.getPathPrefix().length() + 1);
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
    sb.append(ldapConfig.getHomeDn());

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
      constraints = new SearchControls();
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
