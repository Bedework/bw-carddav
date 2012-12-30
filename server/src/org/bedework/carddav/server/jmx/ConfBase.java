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
package org.bedework.carddav.server.jmx;

import edu.rpi.cmt.config.ConfigurationFileStore;
import edu.rpi.cmt.config.ConfigurationType;

import org.apache.activemq.broker.jmx.AnnotatedMBean;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.activemq.util.JMXSupport;
import org.apache.log4j.Logger;
import org.jboss.mx.util.MBeanServerLocator;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author douglm
 *
 */
public abstract class ConfBase implements ConfBaseMBean {
  private transient Logger log;

  private boolean started;

  private String configName;

  private String configRoot;

  /**
   * @return name IDENTICAL to that defined for service.
   */
  public abstract String getName();

  /**
   * @return the object we are managing
   */
  public abstract ConfigurationType getConfigObject();

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  @Override
  public void setConfigName(final String val) {
    configName = val;
  }

  public String getConfigName() {
    return configName;
  }

  public void setConfigDir(final String val) {
    configRoot = val;
  }

  public String getConfigDir() {
    return configRoot;
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

  public String saveConfig() {
    try {
      ConfigurationType config = getConfigObject();
      if (config == null) {
        return "No configuration to save";
      }

      ConfigurationFileStore cfs = new ConfigurationFileStore(getConfigDir());

      config.setName(configName);

      cfs.saveConfiguration(config);

      return "saved";
    } catch (Throwable t) {
      error(t);
      return t.getLocalizedMessage();
    }
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  /* ========================================================================
   * Lifecycle
   * ======================================================================== */

  @Override
  public void create() {
  }

  @Override
  public void start() {
    started = true;
  }

  @Override
  public void stop() {
    started = false;
  }

  @Override
  public boolean isStarted() {
    return started;
  }

  @Override
  public void destroy() {
    try {
      getManagementContext().stop();
    } catch (Throwable t) {
      error("Failed to stop management context");
      error(t);
    }
  }

  /* ====================================================================
   *                   JMX methods
   * ==================================================================== */

  /* */
  private ObjectName serviceObjectName;
  private ManagementContext managementContext;
  private Set<ObjectName> registeredMBeans = new CopyOnWriteArraySet<ObjectName>();

  protected void register(final String serviceType,
                       final String name,
                       final Object view) {
    try {
      ObjectName objectName = createObjectName(serviceType, name);
      register(objectName, view);
    } catch (Throwable t) {
      error("Failed to register " + serviceType + ":" + name);
      error(t);
    }
  }

  protected void unregister(final String serviceType,
                         final String name) {
    try {
      ObjectName objectName = createObjectName(serviceType, name);
      unregister(objectName);
    } catch (Throwable t) {
      error("Failed to unregister " + serviceType + ":" + name);
      error(t);
    }
  }

  protected void register(final ObjectName key,
                          final Object view) throws Exception {
    try {
      AnnotatedMBean.registerMBean(getManagementContext(), view, key);
      registeredMBeans.add(key);
    } catch (Throwable e) {
      warn("Failed to register MBean: " + key);
      debug("Failure reason: " + e);
      error(e);
    }
  }

  protected void unregister(final ObjectName key) throws Exception {
    if (registeredMBeans.remove(key)) {
      try {
        getManagementContext().unregisterMBean(key);
      } catch (Throwable e) {
        warn("Failed to unregister MBean: " + key);
        debug("Failure reason: " + e);
      }
    }
  }

  protected ObjectName getServiceObjectName() throws MalformedObjectNameException {
    if (serviceObjectName == null) {
      serviceObjectName = new ObjectName(getName());
    }

    return serviceObjectName;
  }

  protected ObjectName createObjectName(final String serviceType,
                                        final String name) throws MalformedObjectNameException {
    // Build the object name for the bean
    Map props = getServiceObjectName().getKeyPropertyList();
    ObjectName objectName = new ObjectName(getServiceObjectName().getDomain() + ":" +
        "service=" + props.get("service") + "," +
        "Type=" + JMXSupport.encodeObjectNamePart(serviceType) + "," +
        "Name=" + JMXSupport.encodeObjectNamePart(name));
    return objectName;
  }

  protected ManagementContext getManagementContext() {
    if (managementContext == null) {
      managementContext = new ManagementContext();
      managementContext.setCreateConnector(false);

      managementContext.setMBeanServer(MBeanServerLocator.locateJBoss());
    }
    return managementContext;
  }

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */

  protected void info(final String msg) {
    getLogger().info(msg);
  }

  protected void warn(final String msg) {
    getLogger().warn(msg);
  }

  protected void debug(final String msg) {
    getLogger().debug(msg);
  }

  protected void error(final Throwable t) {
    getLogger().error(this, t);
  }

  protected void error(final String msg) {
    getLogger().error(msg);
  }

  /* Get a logger for messages
   */
  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }
}
