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

import edu.rpi.cmt.config.ConfigException;
import edu.rpi.cmt.config.ConfigurationType;
import edu.rpi.sss.util.OptionsFactory;
import edu.rpi.sss.util.OptionsI;

import org.apache.activemq.broker.jmx.AnnotatedMBean;
import org.apache.activemq.broker.jmx.ManagementContext;
import org.apache.log4j.Logger;
import org.jboss.mx.util.MBeanServerLocator;

import java.io.File;
import java.io.FileInputStream;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.management.ObjectName;

/**
 * @author douglm
 *
 */
public class CardDavSvc extends ConfBase implements CardDavSvcMBean {
  private boolean started;

  private static volatile String optionsPath;

  private static volatile String configDir;

  /* Options class if we've already been called for the options */
  private static volatile OptionsI opts;

  private static final String optionsOuterTag = "bedework-options";

  /** Global properties have this prefix.
   */
  private static final String optionsGlobalPrefix = "org.bedework.global.";

  /** App properties have this prefix.
   */
  private static final String optionsAppPrefix = "org.bedework.app.";

  private static CardDav cd;
  private static ManagementContext managementContext;
  private static Set<ObjectName> registeredMBeans = new CopyOnWriteArraySet<ObjectName>();

  @Override
  public ConfigurationType getConfigObject() {
    return null;
  }

  public void setOptionsPath(final String val) {
    optionsPath = val;
  }

  public String getOptionsPath() {
    return optionsPath;
  }

  @Override
  public void setConfigDir(final String val) {
    configDir = val;
  }

  @Override
  public String getConfigDir() {
    return configDir;
  }

  /**
   * @return path to configurations.
   */
  public static String getConfigDirPath() {
    return configDir;
  }

  /**
   * @return options
   * @throws Throwable
   */
  public static OptionsI getOptions() throws Throwable {
    if (opts != null) {
      return opts;
    }

    File f = new File(optionsPath);

    if (!f.exists()) {
      throw new Exception("Unable to access options file " + optionsPath);
    }

    if (!f.isFile()) {
      throw new ConfigException(optionsPath + " is not a file");
    }

    opts = OptionsFactory.fromStream(optionsGlobalPrefix,
                                     optionsAppPrefix,
                                     optionsOuterTag,
                                     new FileInputStream(f));
    return opts;
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /* ========================================================================
   * Lifecycle
   * ======================================================================== */

  @Override
  public void create() {
    try {
      /* Register the carddav mbean and load the configs. */

      getManagementContext().start();

      cd = new CardDav(getConfigDir());
      register("conf", "conf", cd);
      cd.loadConfigs();
    } catch (Throwable t) {
      error("Failed to create service");
      error(t);
    }
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
   *                   Private methods
   * ==================================================================== */

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */

  /* ====================================================================
   *                   JMX methods
   * ==================================================================== */

  /**
   * @param key
   * @param bean
   * @throws Exception
   */
  public static void register(final ObjectName key,
                              final Object bean) throws Exception {
    try {
      AnnotatedMBean.registerMBean(getManagementContext(), bean, key);
      registeredMBeans.add(key);
    } catch (Throwable e) {
      Logger l = Logger.getLogger(CardDavSvc.class);
      l.warn("Failed to register MBean: " + key);
      if (l.isDebugEnabled()) {
        l.error("Failure reason: " + e, e);
      }
    }
  }

  /**
   * @param key
   * @throws Exception
   */
  public static void unregister(final ObjectName key) throws Exception {
    if (registeredMBeans.remove(key)) {
      try {
        getManagementContext().unregisterMBean(key);
      } catch (Throwable e) {
        Logger l = Logger.getLogger(CardDavSvc.class);
        l.warn("Failed to unregister MBean: " + key);
        if (l.isDebugEnabled()) {
          l.error("Failure reason: " + e, e);
        }
      }
    }
  }

  /**
   * @return the management context.
   */
  public static ManagementContext getManagementContext() {
    if (managementContext == null) {
      managementContext = new ManagementContext();
      managementContext.setCreateConnector(false);

      managementContext.setMBeanServer(MBeanServerLocator.locateJBoss());
    }
    return managementContext;
  }
}
