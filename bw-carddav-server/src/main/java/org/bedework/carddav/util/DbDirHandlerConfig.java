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
package org.bedework.carddav.util;

import java.util.List;

import javax.xml.namespace.QName;

/** This class defines the various properties we need to make a connection
 * and retrieve a group and user information from a db.
 *
 * @author Mike Douglass
 */
public class DbDirHandlerConfig extends DirHandlerConfig<DbDirHandlerConfig> {
  private static final QName rootAccess = new QName(ns, "rootAccess");

  private static final QName rootOwner = new QName(ns, "rootOwner");

  private static final QName queryLimit = new QName(ns, "queryLimit");

  private static final QName hibernateProperty = new QName(ns, "hibernateProperty");

  /**
   *
   * @param val
   */
  public void setRootAccess(final String val)  {
    setProperty(rootAccess, val);
  }

  /**
   * @return String
   */
  public String getRootAccess()  {
    return getPropertyValue(rootAccess);
  }

  /**
   *
   * @param val
   */
  public void setRootOwner(final String val)  {
    setProperty(rootOwner, val);
  }

  /**
   * @return String
   */
  public String getRootOwner()  {
    return getPropertyValue(rootOwner);
  }

  /** Set the query limit - 0 for no limit
   *
   * @param val
   */
  public void setQueryLimit(final int val)  {
    setIntegerProperty(queryLimit, val);
  }

  /**
   *
   * @return int val
   */
  public int getQueryLimit()  {
    return getIntegerPropertyValue(queryLimit);
  }

  /** Add a hibernate property
   *
   * @param name
   * @param val
   */
  public void addHibernateProperty(final String name,
                                   final String val) {
    addProperty(hibernateProperty, name + "=" + val);
  }

  /** Get a hibernate property
   *
   * @param val
   * @return value or null
   */
  public String getHibernateProperty(final String val) {
    List<String> ps = getHibernateProperties();

    String key = val + "=";
    for (String p: ps) {
      if (p.startsWith(key)) {
        return p.substring(key.length());
      }
    }

    return null;
  }

  /** Remove a hibernate property
   *
   * @param name
   */
  public void removeHibernateProperty(final String name) {
    try {
      String v = getHibernateProperty(name);

      if (v == null) {
        return;
      }

      getConfig().removeProperty(hibernateProperty, name + "=" + v);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /** Set a hibernate property
   *
   * @param name
   * @param val
   */
  public void setHibernateProperty(final String name,
                                   final String val) {
    try {
      removeHibernateProperty(name);
      addHibernateProperty(name, val);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   *
   * @return String val
   */
  public List<String> getHibernateProperties() {
    try {
      return getConfig().getAll(hibernateProperty);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
}
