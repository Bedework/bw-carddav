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

import org.bedework.util.misc.Util;

import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.property.Kind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/** Define information to allow us to map between ldap attributes and vcard
 * properties. The mapping is not complete but should handle most cases
 *
 * <p>We might want to make use of the entryUUID attribute - see RFC4530</p>
 *
 * <p>Failing that - generate a uid for the vcard using the ldap scheme
 * and use the dn value</p>
 * @author douglm
 *
 */
public class LdapMapping {
  /* The following needs to be part of the config - which needs changes */

  private String attrId;


  /** Simple not-required attr<->property
   *
   * @param attrId
   */
  public LdapMapping(final String attrId) {
    this.attrId = attrId;
  }

  /**
   * @return name of attribute
   */
  public String getAttrId() {
    return attrId;
  }

  /** This represent a value we need to insert into an ldap entry as we create it.
   * These have no corresponding vcard property.
   *
   */
  public static class AttrValue extends LdapMapping {
    private String value;


    /** Simple not-required attr<->property
     *
     * @param attrId the id
     * @param value the value
     */
    public AttrValue(final String attrId,
                     final String value) {
      super(attrId);
      this.value = value;
    }

    /**
     * @return name of attribute
     */
    public String getValue() {
      return value;
    }

    @Override
    public int hashCode() {
      int hc = getAttrId().hashCode();

      if (value != null) {
        hc = value.hashCode();
      }

      return hc;
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof AttrValue)) {
        return false;
      }

      final AttrValue that = (AttrValue)o;

      return (Util.compareStrings(getAttrId(), that.getAttrId()) == 0) &&
             (Util.compareStrings(value, that.value) == 0);
    }
  }

  /** These represent vcard properties.
   */
  public static class AttrPropertyMapping extends LdapMapping {
    private String group;
    private String propertyName;
    private String parameterName;
    private String parameterValue;
    private boolean required;

    /* What kind of vcard - empty for any */
    private List<Kind> kinds = new ArrayList<>();

    /** Simple not-required attr<->property
     *
     * @param attrId
     * @param propertyName
     * @param kinds
     */
    public AttrPropertyMapping(final String attrId,
                               final String propertyName,
                               final Kind... kinds) {
      this(attrId, null, propertyName, null, null, false, kinds);
    }

    /** Not-required attr<->property+parameter
     *
     * @param attrId
     * @param propertyName
     * @param parameterName
     * @param parameterValue
     * @param kinds
     */
    public AttrPropertyMapping(final String attrId,
                               final String propertyName,
                               final String parameterName,
                               final String parameterValue,
                               final Kind... kinds) {
      this(attrId, null, propertyName, parameterName, parameterValue,
           false, kinds);
    }

    /** Possibly required attr<->property
     *
     * @param attrId
     * @param propertyName
     * @param required
     * @param kinds
     */
    public AttrPropertyMapping(final String attrId,
                               final String propertyName,
                               final boolean required,
                               final Kind... kinds) {
      this(attrId, null, propertyName, null, null, required, kinds);
    }

    /** Possibly required attr<->property+parameter
     *
     * @param attrId
     * @param group
     * @param propertyName
     * @param parameterName
     * @param parameterValue
     * @param required
     * @param kinds
     */
    public AttrPropertyMapping(final String attrId,
                               final String group,
                               final String propertyName,
                               final String parameterName,
                               final String parameterValue,
                               final boolean required,
                               final Kind... kinds) {
      super(attrId);
      this.group = group;
      this.propertyName = propertyName;
      this.parameterName = parameterName;
      this.parameterValue = parameterValue;
      this.required = required;

      for (Kind k: kinds) {
        this.kinds.add(k);
      }
    }

    /**
     * @return group value
     */
    public String getGroup() {
      return group;
    }

    /**
     * @return name of property
     */
    public String getPropertyName() {
      return propertyName;
    }

    /**
     * @return name of parameter
     */
    public String getParameterName() {
      return parameterName;
    }

    /**
     * @return name of parameter value
     */
    public String getParameterValue() {
      return parameterValue;
    }

    /**
     * @return true if attribute/property is required
     */
    public boolean getRequired() {
      return required;
    }

    /**
     * @return kinds for this property
     */
    public List<Kind> getKinds() {
      return kinds;
    }

    @Override
    public int hashCode() {
      int hc = getAttrId().hashCode();

      if (propertyName != null) {
        hc *= propertyName.hashCode();
      }

      if (group != null) {
        hc = group.hashCode();
      }

      if (parameterName != null) {
        hc *= parameterName.hashCode();
      }

      if (parameterValue != null) {
        hc *= parameterValue.hashCode();
      }

      return hc;
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof AttrPropertyMapping)) {
        return false;
      }

      AttrPropertyMapping that = (AttrPropertyMapping)o;

      return (Util.compareStrings(getAttrId(), that.getAttrId()) == 0) &&
             (Util.compareStrings(propertyName, that.propertyName) == 0) &&
             (Util.compareStrings(group, that.group) == 0) &&
             (Util.compareStrings(parameterName, that.parameterName) == 0) &&
             (Util.compareStrings(parameterValue, that.parameterValue) == 0);
    }
  }

  /** Mappings
   */
  public static final Map<String, LdapMapping> attrToVcardProperty =
    new HashMap<>();

  /** property name -> attribute names
   */
  public static final Map<String, Collection<String>> toLdapAttrNoGroup =
    new HashMap<>();

  /** Map property info to attribute
   */
  private static final Map<String, String> simplePropertyToLdapAttrMap =
    new HashMap<>();

  /** We have to explicitly fetch attributes to get system attributes. This is
   * the default list we obtain. The config can name extra attributes.
   */
  public static final List<String> defaultAttrIdList = new ArrayList<>();

  /** If non null this is the mapping for the KIND property */
  private static AttrPropertyMapping kindProperty = null;

  /** Object classes and KIND */
  private static Map<String, Kind> ocKindMapping = new HashMap<>();

  static {
    addOcKindMapping("person", Kind.INDIVIDUAL);
    addOcKindMapping("organizationalPerson", Kind.INDIVIDUAL);
    addOcKindMapping("inetOrgPerson", Kind.INDIVIDUAL);

    addOcKindMapping("groupofnames", Kind.GROUP);
    addOcKindMapping("groupofuniquenames", Kind.GROUP);

    addAttrValue("objectclass", "top");
    addAttrValue("objectclass", "person");
    addAttrValue("objectclass", "organizationalPerson");
    addAttrValue("objectclass", "inetOrgPerson");

    // Required
    addPropertyAttrMapping("cn", "FN", true);
    //addPropertyAttrMapping("sn", "N", true);
    //                          sn; Given Names; Honorific Prefixes; Honorific Suffixes

    addPropertyAttrMapping("source", "UID", true);

    // Common properties
    addPropertyAttrMapping("CalResourceKind", "KIND");
    //                                     "individual" for a single person,
    //                                     "group" for a group of people,
    //                                     "org" for an organization,
    //                                     "location" */
    addPropertyAttrMapping("displayName", "NICKNAME");
    addPropertyAttrMapping("mail", "EMAIL");
    addPropertyAttrMapping("description", "NOTE");

    addPropertyAttrMapping("homePhone", "HOME", "TEL",
                            "TYPE", "voice", false);
    addPropertyAttrMapping("mobile", "HOME", "TEL",
                            "TYPE", "cell", false);
    addPropertyAttrMapping("telephoneNumber", "WORK", "TEL",
                            "TYPE", "voice", false);
    addPropertyAttrMapping("facsimileTelephoneNumber", "WORK", "TEL",
                            "TYPE", "fax", false);
    addPropertyAttrMapping("pager", "WORK", "TEL", "TYPE", "pager", false);
    addPropertyAttrMapping("mail", "EMAIL");
    addPropertyAttrMapping("IM", "IMPP");
    addPropertyAttrMapping("title", "TITLE");
    addPropertyAttrMapping("description", "NOTE");
    addPropertyAttrMapping("calCAPURI", "CAPURI");
    addPropertyAttrMapping("calCalAdrURI", "CALADRURI");

    // Resources
    addPropertyAttrMapping("AccessabilityURL", "ACCESSABILITYINFO",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("AdmittanceURL", "ADMISSIONINFO",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("ApprovalInfoURL", "APPROVALINFO",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("Autoaccept", "AUTOACCEPT",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("BookingWindowEnd", "BOOKINGEND",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("BookingWindowStart", "BOOKINGSTART",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("Capacity", "CAPACITY",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("CostURL", "COSTINFO",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("Free", "FREE",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("InventoryList","INVENTORYLIST",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("InventoryURL","INVENTORYURL",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("MaxInstances", "MAXINSTANCES",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("Multiplebookings", "MULTIBOOK",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("ResourceManager", "RESOURCEMANAGERINFO",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("owner", "RESOURCEOWNERINFO",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("Restricted", "RESTRICTEDACCESS",
                           Kind.LOCATION, Kind.THING);
    addPropertyAttrMapping("SchedAdmin", "SCHEDADMININFO",
                           Kind.LOCATION, Kind.THING);

    //      SOURCE                         ldap url
    //      NAME                           some sort of name
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
    //      LANG
    //      TZ
    //      GEO
    //      ROLE
    //      LOGO
    //      ORG                            organization name;
    //                                     one or more levels of org unit names

    addPropertyAttrMapping("member", "MEMBER",
                           Kind.GROUP);
    //      RELATED
    //      CATEGORIES
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
    //HOME  TEL         TYPE=msg
    //HOME  TEL         TYPE=fax
    //HOME  TEL         TYPE=video
    //HOME  TEL         TYPE=pager
    //WORK  TEL         TYPE=msg
    //WORK  TEL         TYPE=cell
    //WORK  TEL         TYPE=video

    /* Add extra default attributes */
    addDefaultAttrId("sn");
    addDefaultAttrId("givenName");
    addDefaultAttrId("displayName");
    addDefaultAttrId("createTimestamp");
    addDefaultAttrId("modifyTimestamp");
    addDefaultAttrId("o");
    addDefaultAttrId("objectClass");
    addDefaultAttrId("org");
    addDefaultAttrId("ou");
    addDefaultAttrId("uid");
    addDefaultAttrId("employeeNumber");
    addDefaultAttrId("uniqueMember");
  }

  private static void addOcKindMapping(final String objectClass,
                                       final Kind k) {
    ocKindMapping.put(objectClass.toLowerCase(), k);
  }

  private static void addAttrValue(final String aname,
                                   final String pname) {
    final AttrValue av = new AttrValue(aname, pname);
    attrToVcardProperty.put(aname, av);
  }

  private static void addPropertyAttrMapping(final String aname,
                                             final String propertyName,
                                             final boolean required,
                                             final Kind... kinds) {
    addPropertyAttrMapping(aname, null, propertyName, null, null,
                           required, kinds);
  }

  private static void addPropertyAttrMapping(final String aname,
                                             final String propertyName,
                                             final Kind... kinds) {
    addPropertyAttrMapping(aname, null, propertyName, null, null, false, kinds);
  }

  private static void addPropertyAttrMapping(final String aname,
                                             final String group,
                                             final String propertyName,
                                             final String paramName,
                                             final String paramVal,
                                             final boolean required,
                                             final Kind... kinds) {
    final AttrPropertyMapping apm =
            new AttrPropertyMapping(aname, group, propertyName,
                                    paramName, paramVal,
                                    required, kinds);
    attrToVcardProperty.put(aname, apm);

    Collection<String> anames = toLdapAttrNoGroup.get(propertyName);
    if (anames == null) {
      anames = new ArrayList<>();
      toLdapAttrNoGroup.put(propertyName, anames);
    }
    anames.add(aname);

    if (group == null) {
      simplePropertyToLdapAttrMap.put(propertyName.toUpperCase(), aname);
    }

    if (propertyName.equalsIgnoreCase(Property.Id.KIND.toString())) {
      kindProperty = apm;
    }

    addDefaultAttrId(aname);
  }

  private static void addDefaultAttrId(final String aname) {
    if (!defaultAttrIdList.contains(aname)) {
      defaultAttrIdList.add(aname);
    }
  }

  /** Return the ldap attribute for a simple property name
   *
   * @param pname name of parameter
   * @return ldap attribute
   */
  public static String simplePropertyToLdapAttr(final String pname) {
    return simplePropertyToLdapAttrMap.get(pname.toUpperCase());
  }

  /**
   * @param val  object class
   * @return KIND for object class
   */
  public static Kind getOcKindMapping(final String val) {
    return ocKindMapping.get(val.toLowerCase());
  }

  /**
   * @return mapping for KIND or null if no mapping
   */
  public static AttrPropertyMapping getKindMapping() {
    return kindProperty;
  }

  /**
   * @return attrId or null if no mapping
   */
  public static String getKindAttrId() {
    if (kindProperty == null) {
      return null;
    }

    return kindProperty.getAttrId();
  }
}
