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

import edu.rpi.cmt.config.ConfigurationElementType;
import edu.rpi.cmt.config.ConfigurationType;
import edu.rpi.sss.util.ToString;
import edu.rpi.sss.util.xml.tagdefs.BedeworkServerTags;

import java.io.Serializable;
import java.util.List;

import javax.xml.namespace.QName;

/** This class defines the various properties we need for a carddav server
 *
 * @author Mike Douglass
 * @param <T>
 */
public abstract class ConfigBase<T extends ConfigBase> implements Comparable<T>, Serializable {
  protected ConfigurationType config;

  protected final static String ns = BedeworkServerTags.bedeworkSystemNamespace;

  private String appName;

  /**
   * @return QName which acts as the outer element for the configuration.
   */
  public abstract QName getConfElement();

  /**
   * @param val
   */
  public void setAppName(final String val) {
    appName = val;
  }

  /**
   * @return String
   */
  public String getAppName() {
    return appName;
  }

  /* ====================================================================
   *                   Config methods
   * ==================================================================== */

  /**
   * @param val
   */
  public void setConfig(final ConfigurationType val) {
    config = val;
  }

  /**
   * @return config object
   */
  public synchronized ConfigurationType getConfig() {
    if (config != null) {
      return config;
    }

    config = new ConfigurationType(getConfElement());

    return config;
  }

  /**
   * @param name
   * @return properties with given name
   */
  public List<ConfigurationElementType> getProperties(final QName name) {
    try {
      return getConfig().findAll(name);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /* * Remove all with given name
   *
   * @param name
   * /
  public void removeProperties(final String name) {
    Set<CarddavProperty> ps = getProperties(name);

    if ((ps == null) || (ps.size() == 0)) {
      return;
    }

    for (CarddavProperty p: ps) {
      removeProperty(p);
    }
  }

  /**
   * @return int
   * /
  public int getNumProperties() {
    Collection<CarddavProperty> c = getProperties();
    if (c == null) {
      return 0;
    }

    return c.size();
  }

  /* *
   * @param name
   * @return property or null
   * /
  public CarddavProperty findProperty(final String name) {
    Collection<CarddavProperty> props = getProperties();

    if (props == null) {
      return null;
    }

    for (CarddavProperty prop: props) {
      if (name.equals(prop.getName())) {
        return prop;
      }
    }

    return null;
  }*/

  /** Set the single valued property
   *
   * @param name
   * @param value
   */
  public void setProperty(final QName name,
                          final String value) {
    try {
      getConfig().setProperty(name, value);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /** Add a property
   *
   * @param name
   * @param value
   */
  public void addProperty(final QName name,
                          final String value) {
    try {
      getConfig().addProperty(name, value);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * @param name
   * @return single value of valued property with given name
   */
  public String getPropertyValue(final QName name) {
    try {
      return getConfig().getPropertyValue(name);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /** Set the single valued property
  *
  * @param name
  * @param value
  */
  public void setBooleanProperty(final QName name,
                                 final Boolean value) {
    try {
      getConfig().setBooleanProperty(name, value);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * @param name
   * @return single value of valued property with given name
   */
  public Boolean getBooleanPropertyValue(final QName name) {
    try {
      return getConfig().getBooleanPropertyValue(name);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /** Set the single valued property
   *
   * @param name
   * @param value
   */
  public void setIntegerProperty(final QName name,
                                 final Integer value) {
    try {
      getConfig().setIntegerProperty(name, value);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /**
   * @param name
   * @return single value of valued property with given name
   */
  public Integer getIntegerPropertyValue(final QName name) {
    try {
      return getConfig().getIntegerPropertyValue(name);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  /* *
   * @param name
   * @return single value of valued property with given name
   * /
  public Long getLongPropertyValue(final String name) {
    String s = getPropertyValue(name);

    if (s == null) {
      return null;
    }

    return Long.valueOf(s);
  }

  /* *
   * @param val
   * /
  public void addProperty(final CarddavProperty val) {
    Set<CarddavProperty> c = getProperties();
    if (c == null) {
      c = new TreeSet<CarddavProperty>();
      setProperties(c);
    }

    if (!c.contains(val)) {
      c.add(val);
    }
  }

  /* *
   * @param val
   * @return boolean
   * /
  public boolean removeProperty(final CarddavProperty val) {
    Set<CarddavProperty> c = getProperties();
    if (c == null) {
      return false;
    }

    return c.remove(val);
  }

  /* *
   * @return set of CarddavProperty
   * /
  public Set<CarddavProperty> copyProperties() {
    if (getNumProperties() == 0) {
      return null;
    }
    TreeSet<CarddavProperty> ts = new TreeSet<CarddavProperty>();

    for (CarddavProperty p: getProperties()) {
      ts.add(p);
    }

    return ts;
  }

  /* *
   * @return set of CarddavProperty
   * /
  public Set<CarddavProperty> cloneProperties() {
    if (getNumProperties() == 0) {
      return null;
    }
    TreeSet<CarddavProperty> ts = new TreeSet<CarddavProperty>();

    for (CarddavProperty p: getProperties()) {
      ts.add((CarddavProperty)p.clone());
    }

    return ts;
  }*/

  /** Add our stuff to the StringBuilder
   *
   * @param ts    ToString for result
   * @param indent
   */
  public void toStringSegment(final ToString ts) {
    ts.append("appName", getAppName());
  }

  /* ====================================================================
   *                   Object methods
   * ==================================================================== */

  @Override
  public int compareTo(final ConfigBase that) {
    return getAppName().compareTo(that.getAppName());
  }

  @Override
  public int hashCode() {
    return getAppName().hashCode();
  }

  @Override
  public String toString() {
    ToString ts = new ToString(this);

    toStringSegment(ts);

    return ts.toString();
  }
}
