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
package org.bedework.carddav.util;

import java.util.ArrayList;
import java.util.Arrays;

/** This class defines the various properties we need to make a connection
 * and retrieve a group and user information via ldap.
 *
 * @author Mike Douglass
 */
public class LdapDirHandlerConfig extends DirHandlerConfig {
  private String moduleType;

  private String initialContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
  private String securityAuthentication = "simple";

  private String securityProtocol = "NONE";

  private String providerUrl;

  private String baseDn;

  private int queryLimit;

  private String attrIds;

  private String[] defaultAttrIdList = {
        "cn",
        "createTimestamp",
        "description",
        "displayName",
        "facsimileTelephoneNumber",
        "homePhone",
        "mail",
        "mobile",
        "modifyTimestamp",
        "o",
        "objectClass",
        "org",
        "ou",
        "pager",
        "sn",
        "telephoneNumber",
        "uid",
        "uniqueMember"};

  private String[] attrIdList = defaultAttrIdList;

  private String cardKind;

  private String folderObjectClass;

  private String addressbookObjectClass;

  private String addressbookEntryObjectClass;

  private String principalIdAttr;

  private String folderIdAttr = "ou";

  private String addressbookIdAttr = "ou";

  private String addressbookEntryIdAttr = "cn";

  private String groupMemberAttr;

  private String authDn;

  private String authPw;

  /** Used by configuration tools
   *
   * @param val
   */
  public void setModuleType(String val)  {
    moduleType  = val;
  }

  /**
   * @return String
   */
  public String getModuleType()  {
    return moduleType;
  }

  /**
   * @param val
   */
  public void setInitialContextFactory(String val)  {
    initialContextFactory  = val;
  }

  /**
   * @return String
   */
  public String getInitialContextFactory()  {
    return initialContextFactory;
  }

  /**
   * @param val
   */
  public void setSecurityAuthentication(String val)  {
    securityAuthentication  = val;
  }

  /**
   * @return String
   */
  public String getSecurityAuthentication()  {
    return securityAuthentication;
  }

  /** e.g. "ssl"
  *
  * @param val
  */
  public void setSecurityProtocol(String val)  {
    securityProtocol = val;
  }

  /** e.g "ssl"
  *
  * @return String val
  */
  public String getSecurityProtocol()  {
    return securityProtocol;
  }

  /** URL of ldap server
   *
   * @param val
   */
  public void setProviderUrl(String val)  {
    providerUrl = val;
  }

  /** URL of ldap server
   *
   * @return String val
   */
  public String getProviderUrl()  {
    return providerUrl;
  }

  /**
   *
   * @param val
   */
  public void setBaseDn(String val)  {
    baseDn = val;
  }

  /**
   *
   * @return String val
   */
  public String getBaseDn()  {
    return baseDn;
  }

  /** Set the query limit - 0 for no limit
   *
   * @param val
   */
  public void setQueryLimit(int val)  {
    queryLimit = val;
  }

  /**
   *
   * @return int val
   */
  public int getQueryLimit()  {
    return queryLimit;
  }

  /**
   *
   * @param val
   */
  public void setAttrIds(String val)  {
    attrIds = val;

    String[] alist = attrIds.split("[,\\s]");

    ArrayList<String> al = new ArrayList<String>(Arrays.asList(defaultAttrIdList));

    for (int i = 0; i < alist.length; i++) {
      String a = alist[i].trim();

      if (a.length() > 0) {
        al.add(a);
      }
    }

    attrIdList = al.toArray(new String[0]);
  }

  /**
   *
   * @return String val
   */
  public String getAttrIds()  {
    return attrIds;
  }

  /**
   *
   * @return String[] val
   */
  public String[] getAttrIdList()  {
    return attrIdList;
  }

  /** The 'kind' property value
   *
   * @param val
   */
  public void setCardKind(String val)  {
    cardKind = val;
  }

  /** An object class which identifies an entry as a user
   *
   * @return String val
   */
  public String getCardKind()  {
    return cardKind;
  }

  /**
   *
   * @param val
   */
  public void setFolderObjectClass(String val)  {
    folderObjectClass = val;
  }

  /**
   *
   * @return String val
   */
  public String getFolderObjectClass()  {
    return folderObjectClass;
  }

  /**
   *
   * @param val
   */
  public void setAddressbookObjectClass(String val)  {
    addressbookObjectClass = val;
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookObjectClass()  {
    return addressbookObjectClass;
  }

  /**
   *
   * @param val
   */
  public void setAddressbookEntryObjectClass(String val)  {
    addressbookEntryObjectClass = val;
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookEntryObjectClass()  {
    return addressbookEntryObjectClass;
  }

  /** Attribute we search for to get a principal
   *
   * @param val
   */
  public void setPrincipalIdAttr(String val)  {
    principalIdAttr = val;
  }

  /** Attribute we search for to get a group
   *
   * @return String val
   */
  public String getPrincipalIdAttr()  {
    return principalIdAttr;
  }

  /**
   *
   * @param val
   */
  public void setFolderIdAttr(String val)  {
    folderIdAttr = val;
  }

  /**
   *
   * @return String val
   */
  public String getFolderIdAttr()  {
    return folderIdAttr;
  }

  /**
   *
   * @param val
   */
  public void setAddressbookIdAttr(String val)  {
    addressbookIdAttr = val;
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookIdAttr()  {
    return addressbookIdAttr;
  }

  /**
   *
   * @param val
   */
  public void setAddressbookEntryIdAttr(String val)  {
    addressbookEntryIdAttr = val;
  }

  /**
   *
   * @return String val
   */
  public String getAddressbookEntryIdAttr()  {
    return addressbookEntryIdAttr;
  }

  /** Attribute we want back identifying a member
   *
   * @param val
   */
  public void setGroupMemberAttr(String val)  {
    groupMemberAttr = val;
  }

  /** Attribute we want back identifying a member
   *
   * @return String val
   */
  public String getGroupMemberAttr()  {
    return groupMemberAttr;
  }

  /** If we need an id to authenticate this is it.
   *
   * @param val
   */
  public void setAuthDn(String val)  {
    authDn = val;
  }

  /** If we need an id to authenticate this is it.
   *
   * @return String val
   */
  public String getAuthDn()  {
    return authDn;
  }

  /** If we need an id to authenticate this is the pw.
   *
   * @param val
   */
  public void setAuthPw(String val)  {
    authPw = val;
  }

  /** If we need an id to authenticate this is it.
   *
   * @return String val
   */
  public String getAuthPw()  {
    return authPw;
  }
}
