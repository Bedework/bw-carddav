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
package org.bedework.carddav.server.dirHandlers.db;

import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.xml.XmlEmit;
import org.bedework.webdav.servlet.shared.WebdavException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

import javax.xml.namespace.QName;

/** An entity which can be dumped..
 *
 * @author Mike Douglass
 * @version 1.0
 *
 * @param <T>
 */
public class DumpEntity<T> implements Logged {
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
   */
  @NoWrap
  public boolean hasDumpValue() {
    return true;
  }

  /** Dump this entity as xml.
   *
   * @param xml emitter
   * @param dtype DumpType
   */
  @NoWrap
  public void dump(final XmlEmit xml, final DumpType dtype) {
    dump(xml, dtype, false);
  }

  /** Dump this entity as xml.
   *
   * @param xml emitter
   * @param dtype DumpType
   * @param fromCollection  true if the value is a member of a collection
   */
  @NoWrap
  public void dump(final XmlEmit xml, final DumpType dtype,
                   final boolean fromCollection) {
    if (!hasDumpValue()) {
      return;
    }

    final NoDump ndCl = getClass().getAnnotation(NoDump.class);
    final Dump dCl = getClass().getAnnotation(Dump.class);

    final boolean dumpKeyFields = dtype == DumpType.reference;

    ArrayList<String> noDumpMethods = null;
    ArrayList<String> firstMethods = null;

    try {
      if (ndCl != null) {
        if (ndCl.value().length == 0) {
          return;
        }

        noDumpMethods = new ArrayList<>();
        Collections.addAll(noDumpMethods, ndCl.value());
      }

      if (!dumpKeyFields && (dCl != null) && (dCl.firstFields().length != 0)) {
        firstMethods = new ArrayList<>();
        for (final String f: dCl.firstFields()) {
          firstMethods.add(methodName(f));
        }
      }

      QName qn = null;

      if (fromCollection || (dtype != DumpType.compound)) {
        qn = startElement(xml, getClass(), dCl);
      }

      final Collection<ComparableMethod> ms = findGetters(dCl, dtype);

      if (firstMethods != null) {
        doFirstMethods:
        for (final String methodName: firstMethods) {
          for (final ComparableMethod cm: ms) {
            final Method m = cm.m;

            if (methodName.equals(m.getName())) {
              final Dump d = m.getAnnotation(Dump.class);

              dumpValue(xml, m, d, m.invoke(this, (Object[])null), fromCollection);

              continue doFirstMethods;
            }
          }

          error("Listed first field has no corresponding getter: " + methodName);
        }
      }

      for (final ComparableMethod cm: ms) {
        final Method m = cm.m;

        if ((noDumpMethods != null) &&
            noDumpMethods.contains(fieldName(m.getName()))) {
          continue;
        }

        if ((firstMethods != null) &&
            firstMethods.contains(m.getName())) {
          continue;
        }

        final Dump d = m.getAnnotation(Dump.class);

        dumpValue(xml, m, d, m.invoke(this, (Object[])null), fromCollection);
      }

      if (qn != null) {
        closeElement(xml, qn);
      }
    } catch (final WebdavException cfe) {
      throw cfe;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* ==============================================================
   *                   Private methods
   * ============================================================== */

  private boolean dumpValue(final XmlEmit xml,
                            final Method m,
                            final Dump d,
                            final Object methVal,
                            final boolean fromCollection) {
    /* We always open the methodName or elementName tag if this is the method
     * value.
     *
     * If this is an element from a collection we generally don't want a tag.
     *
     * We do open a tag if the annottaion specifies a collectionElementName
     */
    if (methVal instanceof final DumpEntity<?> de) {

      if (!de.hasDumpValue()) {
        return false;
      }

      final boolean compound = (d!= null) && d.compound();

      final QName mqn = startElement(xml, m, d, fromCollection);

      final DumpType dt;
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

    if (methVal instanceof final Collection<?> c) {
      if (c.isEmpty()) {
        return false;
      }

      QName mqn = null;

      for (final Object o: c) {
        if ((o instanceof DumpEntity) &&
            (!((DumpEntity<?>)o).hasDumpValue())) {
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

  private QName startElement(final XmlEmit xml,
                             final Class<?> c,
                             final Dump d) {
    try {
      final QName qn;

      if (d == null) {
        qn = new QName(c.getName());
      } else {
        qn = new QName(d.elementName());
      }

      xml.openTag(qn);
      return qn;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  private QName startElement(final XmlEmit xml, final Method m, final Dump d,
                             final boolean fromCollection) {
    try {
      final QName qn = getTag(m, d, fromCollection);

      if (qn != null) {
        xml.openTag(qn);
      }

      return qn;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  private QName getTag(final Method m, final Dump d,
                       final boolean fromCollection) {
    String tagName = null;

    if (d != null) {
      if (!fromCollection) {
        if (!d.elementName().isEmpty()) {
          tagName = d.elementName();
        }
      } else if (!d.collectionElementName().isEmpty()) {
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
                        final boolean fromCollection) {
    if (p == null) {
      return;
    }

    try {
      QName qn = getTag(m, d, fromCollection);

      if (qn == null) {
        /* Collection and no collection element name specified */
        qn = new QName(p.getClass().getName());
      }

      final String sval;

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
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  private void closeElement(final XmlEmit xml, final QName qn) {
    try {
      xml.closeTag(qn);
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  private static class ComparableMethod
          implements Comparable<ComparableMethod> {
    Method m;

    ComparableMethod(final Method m) {
      this.m = m;
    }

    public int compareTo(final ComparableMethod that) {
      return this.m.getName().compareTo(that.m.getName());
    }
  }

  private Collection<ComparableMethod> findGetters(final Dump d,
                                                   final DumpType dt) {
    final Method[] meths = getClass().getMethods();
    final Collection<ComparableMethod> getters = new TreeSet<>();
    Collection<String> keyMethods = null;

    if (dt == DumpType.reference) {
      if ((d == null) || (d.keyFields().length == 0)) {
        error("No key fields defined for class " + getClass().getCanonicalName());
        throw new WebdavException("noKeyFields");
      }
      keyMethods = new ArrayList<>();
      for (final String f: d.keyFields()) {
        keyMethods.add(methodName(f));
      }
    }

    for (final Method m : meths) {
      final String mname = m.getName();

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
      final Class<?>[] parClasses = m.getParameterTypes();
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

  /* ==============================================================
   *                   Logged methods
   * ============================================================== */

  private final BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
