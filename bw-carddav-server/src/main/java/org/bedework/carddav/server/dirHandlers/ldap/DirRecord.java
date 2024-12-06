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

import org.bedework.webdav.servlet.shared.WebdavException;

import java.io.Serializable;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.ModificationItem;

/** This class represents a directory record which may be built from a
    number of attributes, retrieved as a search result or read from some input
    stream.

    <p>We will implement some aspects of the ldif as defined in RFC 2849
    within this class. These will be related to the content vs change records.
    By default, a DirRecord object will be a content record. However, we will
    allow the creation of change type records. Having this defined here allows
    applications to pass around DirRecord objects without any idea of the
    underlying implementation.

    <p>Currently the following is implemented:<ul>
    <li><em>content records</em>  Consist of a dn with an associated set of
          attributes with
          values. No attribute options are implemented. Encoded strings may be
          converted incorrectly (need to check locale specifics)</li>
     <li><em>change records.</em><br/> <ul>
        <li><em>add:</em> Implemented. Look just like content records.</li>
        <li><em>delete:</em> Implemented. Consist of a dn only.</li>
        <li>all others are unimplemented.</li></ul>
     </li></ul>

  */
public abstract class DirRecord implements Serializable {
  /** @serial */
  private String name;

  /** @serial */
  private String dn;

  /** @serial */
  private boolean isContentRec = true;

  /** @serial */
  private int changeType = changeTypeInvalid;

  /** */
  public static final int changeTypeInvalid = 0;
  /** */
  public static final int changeTypeAdd = 1;
  /** */
  public static final int changeTypeDelete = 2;
  /** */
  public static final int changeTypeModify = 3;
  /** */
  public static final int changeTypeModdn = 4;

  /**
   * @return Attributes
   */
  public abstract Attributes getAttributes();

  /** Find the attribute for this record with the given name.
   *
   * @param attr name
   * @return  Attribute
   */
  public Attribute findAttr(final String attr) {
    return getAttributes().get(attr);
  }

  /** Set the attribute value in the table. Replaces any existing value(s)
      This does not write back to the directory
   *
   * @param attr name
   * @param val and value
   */
  public void setAttr(final String attr, final Object val) {
    getAttributes().put(attr, val);
  }

  /** Should be overriden to clear attributes.
   */
  public void clear() {
    dn = null;
    isContentRec = true;
    changeType = changeTypeInvalid;
  }

  /** Set the name for this record.
   *
   * @param val name
   */
  public void setName(final String val) {
    name = val;
  }

  /** return the name for this record.
   *
   * @return String
   */
  public String getName() {
    return name;
  }

  /** Set the dn for this record.
   *
   * @param val the dn for this record.
   */
  public void setDn(final String val) {
    dn = val;
  }

  /** return the dn for this record.
   *
   * @return String
   */
  public String getDn() {
    return dn;
  }

  /** Compare this with that. Return true if they are equal.
   *
   * @param that DirRecord
   * @return boolean
   */
  public boolean equals(final DirRecord that) {
    if (!dnEquals(that)) {
      return false;
    }

    final Attributes thisAttrs = getAttributes();
    final Attributes thatAttrs = that.getAttributes();

    if (thisAttrs == null) {
      if (thatAttrs == null) {
        return true;
      }
      return false;
    }

    if (thatAttrs == null) {
      return false;
    }

    return thisAttrs.equals(thatAttrs);
  }

  /** Compare this with that only looking at the named attributes.
      For this method, the dns must be equal and the values of the named
      attributes must be equal but their names may differ. The two arrays of
      attrIDs must be non-null and of the same length or an exception is raised.

      If there are no attributes in both records they are considered equal.
      If there are no attributes in only one record they are unequal.

      Zero length attrID lists means only the dn is compared.
   *
   * @param that
   * @param thisAttrIDs
   * @param thatAttrIDs
   * @return boolean
   */
  public boolean equals(final DirRecord that, final String[] thisAttrIDs,
                        final String[] thatAttrIDs) {
    if ((thisAttrIDs == null) || (thatAttrIDs == null)) {
      throw new WebdavException("DirectoryRecord: null attrID list");
    }

    if (thisAttrIDs.length != thatAttrIDs.length) {
      throw new WebdavException("DirectoryRecord: unequal length attrID lists");
    }

    if (!dnEquals(that)) {
      return false;
    }

    final int n = thisAttrIDs.length;

    if (n == 0) {
      return true;
    }

    final Attributes thisAttrs = getAttributes();
    final Attributes thatAttrs = that.getAttributes();

    if (thisAttrs == null) {
      if (thatAttrs == null) {
        return true;
      }
      return false;
    }

    if (thatAttrs == null) {
      return false;
    }

    for (int i = 0; i < n; i++) {
      final Attribute thisAttr = thisAttrs.get(thisAttrIDs[i]);
      final Attribute thatAttr = thatAttrs.get(thatAttrIDs[i]);

      if (thisAttr == null) {
        if (thatAttr == null) {
          return true;
        }
        return false;
      }

      if (thatAttr == null) {
        return false;
      }

      if (!attrEquals(thisAttr, thatAttr)) {
        return false;
      }
    }

    return true;
  }

  /** Simpler form of equals in which attributes have the same names in both
   *  records.
   *
   * @param that
   * @param attrIDs
   * @return boolean
   */
  public boolean equals(final DirRecord that, final String[] attrIDs) {
    return equals(that, attrIDs, attrIDs);
  }

  /** This compares all but the named attributes
      allbut true => All must be equal except those on the list
   *
   * @param that
   * @param attrIDs
   * @return boolean
   */
  public boolean equalsAllBut(final DirRecord that, final String[] attrIDs) {
    if (attrIDs == null) {
      throw new WebdavException("DirectoryRecord: null attrID list");
    }

    if (!dnEquals(that)) {
      return false;
    }

    final int n = attrIDs.length;

    if (n == 0) {
      return true;
    }

    final Attributes thisAttrs = getAttributes();
    final Attributes thatAttrs = that.getAttributes();

    if (thisAttrs == null) {
      if (thatAttrs == null) {
        return true;
      }
      return false;
    }

    if (thatAttrs == null) {
      return false;
    }

    /* We need to ensure that all attributes are checked.
        We init thatLeft to the number of attributes in the source.
        We decrement for each checked attribute.
        We then decrement for each ignored attribute present in that
        If the result is non-zero, then there are some extra attributes in that
        so we return unequal.
      */
    final int sz = thisAttrs.size();
    int thatLeft = sz;

    if ((sz == 0) && (thatAttrs.size() == 0)) {
      return true;
    }

    final NamingEnumeration<?> ne = thisAttrs.getAll();

    if (ne == null) {
      return false;
    }

    try {
      while (ne.hasMore()) {
        final Attribute attr = (Attribute)ne.next();
        final String id = attr.getID();
        boolean present = false;

        for (final String attrID: attrIDs) {
          if (id.equalsIgnoreCase(attrID)) {
            present = true;
            break;
          }
        }
        if (present) {
          // We don't compare
          if (thatAttrs.get(id) != null) {
            thatLeft--;
          }
        } else {
          final Attribute thatAttr = thatAttrs.get(id);
          if (thatAttr == null) {
            return false;
          }
          if (!thatAttr.contains(attr)) {
            return false;
          }
          thatLeft--;
        }
      }
    } catch (final NamingException ne1) {
      throw new WebdavException(ne1);
    }

    return (thatLeft == 0);
  }

  /**
   * @param thisA
   * @param that
   * @return boolean
   */
  public boolean attrEquals(final Attribute thisA, final Attribute that) {
    final int sz = thisA.size();

    if (sz != that.size()) {
      return false;
    }

    if (sz == 0) {
      return true;
    }

    try {
      final NamingEnumeration<?> ne = thisA.getAll();

      if (ne == null) {
        return false;
      }

      while (ne.hasMore()) {
        if (!that.contains(ne.next())) {
          return false;
        }
      }
    } catch (final NamingException ne) {
      throw new WebdavException(ne);
    }

    return true;
  }

  /** Compare the given single value with the attribute value(s).
   *
   * @param val
   * @param that
   * @param ignoreCase
   *  @return -2 for not equal or not present in multi-valued attribute
   *          -1 for val &lt; that
   *           0 for val = that
   *           1 for val &gt; that
   *           2 for val present in multi-valued attr
   */
  public int attrValCompare(final Object val, final Attribute that,
                            final boolean ignoreCase) {
    try {
      if (that.size() != 1) {
        final NamingEnumeration<?> ne = that.getAll();

        if (ne == null) {
          return -2;
        }

        while (ne.hasMore()) {
          final Object o = ne.next();
          if (val instanceof String) {
            if (compareVal(o, (String)val, ignoreCase) == 0) {
              return 2;
            }
          } else if (o.equals(val)) {
            return 2;
          }
        }
        return -2;
      }

      /* that is a single valued attribute.
       */
      final Object o = that.get();

      if (val instanceof String) {
        return compareVal(o, (String)val, ignoreCase);
      }

      if (o.equals(val)) {
        return 0;
      }
    } catch (final NamingException ne) {
      throw new WebdavException(ne);
    }

    return -2;
  }

  /** Extract the target attribute from this record then
   *  compare the given single value with the attribute value(s).
   *
   * @param val
   * @param attrName
   * @param ignoreCase
   *  @return -2 for not equal or not present in multi-valued attribute
   *          -1 for val &lt; that
   *           0 for val = that
   *           1 for val &gt; that
   *           2 for val present in multi-valued attr
   */
  public int attrValCompare(final Object val, final String attrName,
                            final boolean ignoreCase) {
    final Attribute a = findAttr(attrName);

    if (a == null) {
      return -2;
    }
    return attrValCompare(val, a, ignoreCase);
  }

  private int compareVal(final Object o, final String s, final boolean ignoreCase) {
    if (!(o instanceof String)) {
      return -2;
    }

    int c;
    if (ignoreCase) {
      c = s.compareToIgnoreCase((String) o);
    } else {
      c = s.compareTo((String) o);
    }

    if (c < 0) {
      c = -1;
    }
    if (c > 0) {
      c = 1;
    }

    return c;
  }

  /** Check dns for equality
   *
   * @param that other dn
   * @return boolean
   */
  public boolean dnEquals(final DirRecord that) {
    if (that == null) {
      throw new WebdavException("Null record for dnEquals");
    }

    final String thisDn = getDn();
    if (thisDn == null) {
      throw new WebdavException("No dn for this record");
    }

    final String thatDn = that.getDn();
    if (thatDn == null) {
      throw new WebdavException("That record has no dn");
    }

    return (thisDn.equals(thatDn));
  }

  /** Add the attribute value to the table. If an attribute already exists
   *  add it to the end of its values.
   *
   * @param   attr   String attribute name
   * @param   val    Object value
   */
  public void addAttr(final String attr, final Object val) {
//  System.out.println("addAttr " + attr);

    final Attribute a = findAttr(attr);

    if (a == null) {
      setAttr(attr, val);
    } else {
      a.add(val);
    }
  }

  /** getAttrVal - return first (or only) value for given attribute
   *  "dn" is treated as an attribute name.
   *
   * @param   attr   String attribute name
   * @return  Object attribute value
   */
  public Object getAttrVal(final String attr) {
    if (attr.equalsIgnoreCase("dn")) {
      return getDn();
    }

    final Attribute a = findAttr(attr);

    if (a == null) {
      return null;
    }

    try {
      return a.get();
    } catch (final NamingException ne) {
      throw new WebdavException(ne);
    }
  }

  /** Return true if the record contains all of the values of the given
   *  attribute.
   *
   * @param   attr    Attribute we're looking for
   * @return  boolean true if we found it
   */
  public boolean contains(final Attribute attr) {
    if (attr == null) {
      return false; // protect
    }

    final Attribute recAttr = getAttributes().get(attr.getID());

    if (recAttr == null) {
      return false;
    }

    try {
      final NamingEnumeration<?> ne = attr.getAll();

      while (ne.hasMore()) {
        if (!recAttr.contains(ne.next())) {
          return false;
        }
      }
    } catch (final NamingException ne) {
      throw new WebdavException(ne);
    }

    return true;
  }

  /**
   * Retrieve an enumeration of the named attribute's values.
   * The behaviour of this enumeration is unspecified
   * if the the attribute's values are added, changed,
   * or removed while the enumeration is in progress.
   * If the attribute values are ordered, the enumeration's items
   * will be ordered.
   * <p>
   * Each element of the enumeration is a possibly null Object. The object's
   * class is the class of the attribute value. The element is null
   * if the attribute's value is null.
   * If the attribute has zero values, an empty enumeration
   * is returned.
   *
   * @param attr
   * @return NamingEnumeration
   */
  public NamingEnumeration<?> attrElements(final String attr) {
    final Attribute a = findAttr(attr);

    if (a == null) {
      return null;
    }

    try {
      return a.getAll();
    } catch (final NamingException ne) {
      throw new WebdavException(ne);
    }
  }

  /** getAttrStr - return first (or only) string value for given attribute
   *
   * @param   attr   String attribute name
   * @return  String attribute value
   */
  public String getAttrStr(final String attr) {
    final Object o = getAttrVal(attr);

    if (o == null) {
      return null;
    }

    if (o instanceof String) {
      return (String)o;
    }

    return o.toString();
  }

  /**
   * @return boolean
   */
  public boolean getIsContent() {
    return isContentRec;
  }

  /**
   * @return int
   */
  public int getChangeType() {
    return changeType;
  }

  /**
   * @return ModificationItem[]
   */
  public ModificationItem[] getMods() {
    throw new WebdavException("Not a change record");
  }

  /**
   * @param val true if is content
   */
  public void setIsContent(final boolean val) {
    isContentRec = val;
  }

  /**
   * @param val
   */
  public void setChangeType(final int val) {
    changeType = val;
  }
}
