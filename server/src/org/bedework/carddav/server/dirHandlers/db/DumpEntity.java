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
package org.bedework.carddav.server.dirHandlers.db;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.sss.util.xml.XmlEmit;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import javax.xml.namespace.QName;

/** An entity which can be dumped..
 *
 * @author Mike Douglass
 * @version 1.0
 *
 * @param <T>
 */
public class DumpEntity<T> {
  private transient Logger log;

  /** We're dumping the entire object */
  public enum DumpType {
    /** We're dumping the entire object */
    def,

    /** We're dumping a compound type */
    compound,

    /** We're dumping enough to refer to an entity */
    reference
  }

  /** Override this if we want to optionally suppress the dump based on some
   * attributes. This allows us to skip empty objects which occassionally turn
   * up.
   *
   * @return boolean true to continue with dump.
   * @throws WebdavException
   */
  @NoWrap
  public boolean hasDumpValue() throws WebdavException {
    return true;
  }

  /** Dump this entity as xml.
   *
   * @param xml
   * @param dtype
   * @throws WebdavException
   */
  @NoWrap
  public void dump(final XmlEmit xml, final DumpType dtype) throws WebdavException {
    dump(xml, dtype, false);
  }

  /** Dump this entity as xml.
   *
   * @param xml
   * @param dtype
   * @param fromCollection  true if the value is a member of a collection
   * @throws WebdavException
   */
  @NoWrap
  public void dump(final XmlEmit xml, final DumpType dtype,
                   final boolean fromCollection) throws WebdavException {
    if (!hasDumpValue()) {
      return;
    }

    NoDump ndCl = getClass().getAnnotation(NoDump.class);
    Dump dCl = getClass().getAnnotation(Dump.class);

    boolean dumpKeyFields = dtype == DumpType.reference;

    ArrayList<String> noDumpMethods = null;
    ArrayList<String> firstMethods = null;

    try {
      if (ndCl != null) {
        if (ndCl.value().length == 0) {
          return;
        }

        noDumpMethods = new ArrayList<String>();
        for (String m: ndCl.value()) {
          noDumpMethods.add(m);
        }
      }

      if (!dumpKeyFields && (dCl != null) && (dCl.firstFields().length != 0)) {
        firstMethods = new ArrayList<String>();
        for (String f: dCl.firstFields()) {
          firstMethods.add(methodName(f));
        }
      }

      QName qn = null;

      if (fromCollection || (dtype != DumpType.compound)) {
        qn = startElement(xml, getClass(), dCl);
      }

      Collection<ComparableMethod> ms = findGetters(dCl, dtype);

      if (firstMethods != null) {
        doFirstMethods:
        for (String methodName: firstMethods) {
          for (ComparableMethod cm: ms) {
            Method m = cm.m;

            if (methodName.equals(m.getName())) {
              Dump d = m.getAnnotation(Dump.class);

              dumpValue(xml, m, d, m.invoke(this, (Object[])null), fromCollection);

              continue doFirstMethods;
            }
          }

          error("Listed first field has no corresponding getter: " + methodName);
        }
      }

      for (ComparableMethod cm: ms) {
        Method m = cm.m;

        if ((noDumpMethods != null) &&
            noDumpMethods.contains(fieldName(m.getName()))) {
          continue;
        }

        if ((firstMethods != null) &&
            firstMethods.contains(m.getName())) {
          continue;
        }

        Dump d = m.getAnnotation(Dump.class);

        dumpValue(xml, m, d, m.invoke(this, (Object[])null), fromCollection);
      }

      if (qn != null) {
        closeElement(xml, qn);
      }
    } catch (WebdavException cfe) {
      throw cfe;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private boolean dumpValue(final XmlEmit xml, final Method m, final Dump d,
                            final Object methVal,
                            final boolean fromCollection) throws Throwable {
    /* We always open the methodName or elementName tag if this is the method
     * value.
     *
     * If this is an element from a collection we generally don't want a tag.
     *
     * We do open a tag if the annottaion specifies a collectionElementName
     */
    if (methVal instanceof DumpEntity) {
      DumpEntity de = (DumpEntity)methVal;

      if (!de.hasDumpValue()) {
        return false;
      }

      boolean compound = (d!= null) && d.compound();

      QName mqn = startElement(xml, m, d, fromCollection);

      DumpType dt;
      if (compound) {
        dt = DumpType.compound;
      } else {
        dt = DumpType.reference;
      }

      de.dump(xml, dt);

      if (mqn != null) {
        closeElement(xml, mqn);
      }

      return true;
    }

    if (methVal instanceof Collection) {
      Collection c = (Collection)methVal;

      if (c.isEmpty()) {
        return false;
      }

      QName mqn = null;

      for (Object o: c) {
        if ((o instanceof DumpEntity) &&
            (!((DumpEntity)o).hasDumpValue())) {
          continue;
        }

        if (mqn == null) {
          mqn = startElement(xml, m, d, fromCollection);
        }

        dumpValue(xml, m, d, o, true);
      }

      if (mqn != null) {
        closeElement(xml, mqn);
      }

      return true;
    }

    property(xml, m, d, methVal, fromCollection);

    return true;
  }

  private QName startElement(final XmlEmit xml, final Class c, final Dump d) throws WebdavException {
    try {
      QName qn;

      if (d == null) {
        qn = new QName(c.getName());
      } else {
        qn = new QName(d.elementName());
      }

      xml.openTag(qn);
      return qn;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private QName startElement(final XmlEmit xml, final Method m, final Dump d,
                             final boolean fromCollection) throws WebdavException {
    try {
      QName qn = getTag(m, d, fromCollection);

      if (qn != null) {
        xml.openTag(qn);
      }

      return qn;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private QName getTag(final Method m, final Dump d,
                       final boolean fromCollection) {
    String tagName = null;

    if (d != null) {
      if (!fromCollection) {
        if (d.elementName().length() > 0) {
          tagName = d.elementName();
        }
      } else if (d.collectionElementName().length() > 0) {
        tagName = d.collectionElementName();
      }
    }

    if ((tagName == null) && !fromCollection) {
      tagName = fieldName(m.getName());
    }

    if (tagName == null) {
      return null;
    }

    return new QName(tagName);
  }

  private void property(final XmlEmit xml, final Method m,
                        final Dump d, final Object p,
                        final boolean fromCollection) throws WebdavException {
    if (p == null) {
      return;
    }

    try {
      QName qn = getTag(m, d, fromCollection);

      if (qn == null) {
        /* Collection and no collection element name specified */
        qn = new QName(p.getClass().getName());
      }

      String sval;

      if (p instanceof char[]) {
        sval = new String((char[])p);
      } else {
        sval = String.valueOf(p);
      }

      if ((sval.indexOf('&') < 0) && (sval.indexOf('<') < 0)) {
        xml.property(qn, sval);
      } else {
        xml.cdataProperty(qn, sval);
      }
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private void closeElement(final XmlEmit xml, final QName qn) throws WebdavException {
    try {
      xml.closeTag(qn);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private static class ComparableMethod implements Comparable<ComparableMethod> {
    Method m;

    ComparableMethod(final Method m) {
      this.m = m;
    }

    public int compareTo(final ComparableMethod that) {
      return this.m.getName().compareTo(that.m.getName());
    }
  }

  private Collection<ComparableMethod> findGetters(final Dump d,
                                                   final DumpType dt) throws WebdavException {
    Method[] meths = getClass().getMethods();
    Collection<ComparableMethod> getters = new TreeSet<ComparableMethod>();
    Collection<String> keyMethods = null;

    if (dt == DumpType.reference) {
      if ((d == null) || (d.keyFields().length == 0)) {
        error("No key fields defined for class " + getClass().getCanonicalName());
        throw new WebdavException("noKeyFields");
      }
      keyMethods = new ArrayList<String>();
      for (String f: d.keyFields()) {
        keyMethods.add(methodName(f));
      }
    }

    for (Method m : meths) {
      String mname = m.getName();

      if (mname.length() < 4) {
        continue;
      }

      /* Name must start with get */
      if (!mname.startsWith("get")) {
        continue;
      }

      /* Don't want getClass */
      if (mname.equals("getClass")) {
        continue;
      }

      /* No parameters */
      Class[] parClasses = m.getParameterTypes();
      if (parClasses.length != 0) {
        continue;
      }

      /* Not annotated with NoDump */
      if (m.getAnnotation(NoDump.class) != null) {
        continue;
      }

      /* If we have a list of key methods it must be in that list */
      if ((keyMethods != null) && !keyMethods.contains(mname)) {
        continue;
      }

      getters.add(new ComparableMethod(m));
    }

    return getters;
  }

  private String methodName(final String val) {
    String m = "get" + val.substring(0, 1).toUpperCase();
    if (val.length() > 1) {
      m += val.substring(1);
    }

    return m;
  }

  private String fieldName(final String val) {
    if (val.length() < 4) {
      return null;
    }

    return val.substring(3, 4).toLowerCase() + val.substring(4);
  }

  protected Logger getLog() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  protected void error(final String msg) {
    getLog().error(msg);
  }
}
