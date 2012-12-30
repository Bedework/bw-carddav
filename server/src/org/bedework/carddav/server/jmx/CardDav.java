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

import org.bedework.carddav.server.CardOptionsFactory;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.util.DbDirHandlerConfig;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.util.LdapDirHandlerConfig;

import edu.rpi.cmt.config.ConfigurationFileStore;
import edu.rpi.cmt.config.ConfigurationStore;
import edu.rpi.cmt.config.ConfigurationType;
import edu.rpi.sss.util.OptionsI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenDataException;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.SimpleType;
import javax.management.openmbean.TabularData;
import javax.management.openmbean.TabularDataSupport;
import javax.management.openmbean.TabularType;

/**
 * @author douglm
 *
 */
public class CardDav extends ConfBase implements CardDavMBean {
  private static final String[] configs = {"usercarddav",
                                           "pubcarddav"};

  private static List<ConfBase> confBeans;

  private static List<DirHandlerConfig> dirhandlerConfigs;

  private static String[] dhListItemNames = {"path",
                                                     "name",
                                                     "class"};

  private static String[] dhListItemDescriptions = {"Path sequence",
                                                            "Dir handler name",
                                                            "Dir handler class"};

  private static OpenType[] dhListItemTypes = {SimpleType.STRING,
                                                       SimpleType.STRING,
                                                       SimpleType.STRING};

  /* path must be unique */
  private static String[] dhListIndexNames = {"path"};

  private static TabularType dhTable = null;
  private static CompositeType dhType = null;
  private static TabularDataSupport dhData;

  static {
    try {
      dhType = new CompositeType("Dirhandlers",
                                   "Dirhandlers info",
                                   dhListItemNames,
                                   dhListItemDescriptions,
                                   dhListItemTypes);
      dhTable = new TabularType("Dirhandlers",
                                        "Dirhandlers info",
                                        dhType,
                                        dhListIndexNames);

      dhData = new TabularDataSupport(dhTable);
    } catch (OpenDataException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   */
  public static final String serviceName = "org.bedework.carddav:service=CardDav";

  /**
   * @param configName
   * @return config for given service
   */
  public static CardDAVConfig getConf(final String configName) {
    for (ConfBase c: confBeans) {
      if ((c instanceof CardDavConf) &&
          (configName.equals(c.getConfigName()))) {
        return ((CardDavConf)c).getConf();
      }
    }

    return null;
  }

  @Override
  public ConfigurationType getConfigObject() {
    return null;
  }

  /**
   * @return name IDENTICAL to that defined for service.
   */
  @Override
  public String getName() {
    return serviceName;
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  /* ========================================================================
   * Operations
   * ======================================================================== */

  public TabularData ListDirHandlers() {
    dhData.clear();

    for (DirHandlerConfig dhc: dirhandlerConfigs) {
      Object[] itemValues = {dhc.getPathPrefix(),
                             dhc.getAppName(),
                             dhc.getClassName()
      };
      try {
        dhData.put(new CompositeDataSupport(dhType, dhListItemNames,
                                              itemValues));
      } catch (OpenDataException e) {
        e.printStackTrace();
      }
    }

    return dhData;
  }

  /*
   * public void addPageSizeResult(PageSizeResult pageSizeResult) {
     Object[] itemValues = {
        pageSizeResult.pageClass.getName(),
        pageSizeResult.sizeBeforeDetach,
        pageSizeResult.sizeAfterDetach
     };
     try {
        pageData.put(new CompositeDataSupport(pageType, itemNames,
           itemValues));
     } catch (OpenDataException e) {
        e.printStackTrace();
     }
  }
   */

  /* ========================================================================
   * Lifecycle
   * ======================================================================== */

  /* Only for loading from options object */
  private OptionsI opts;

  private OptionsI getOpts() throws Throwable {
    if (opts == null) {
      opts = CardOptionsFactory.getOptions();
    }

    return opts;
  }

  @Override
  public void create() {
    try {
      /* Load up the end-point configs */

      ConfigurationStore cfs = new ConfigurationFileStore(getConfigDir());

      confBeans = new ArrayList<ConfBase>();

      getManagementContext().start();
      /* register all our configurations */
      for (String c: configs) {
        ObjectName objectName = createObjectName("conf", c);

        CardDavConf cdc = new CardDavConf(objectName.toString(),
                                          getCardDAVConf(cfs, c),
                                          c,
                                          getConfigDir());

        register("conf", c, cdc);
        confBeans.add(cdc);

        cdc.saveConfig(); // Just to ensure we have it for next time
      }

      /* Now do the dir handlers */

      /* First we need a list */

      cfs = cfs.getStore("dirHandlers");

      Collection<String> dirHandlerNames = cfs.getConfigs();

      if (dirHandlerNames.isEmpty()) {
        // XXX temp - get from the options
        String dirHandlersElementName = "org.bedework.global.dirhandlers";
        dirHandlerNames = getOpts().getNames(dirHandlersElementName);
      }

      dirhandlerConfigs = new ArrayList<DirHandlerConfig>();

      for (String dhn: dirHandlerNames) {
        DirHandlerConfig cfg = getDirHandlerConf(cfs, dhn);

        if (cfg == null) {
          continue;
        }

        cfg.setAppName(dhn);
        dirhandlerConfigs.add(cfg);

        for (ConfBase c: confBeans) {
          if (c instanceof CardDavConf) {
            ((CardDavConf)c).getConf().addDirhandler(cfg);
          }
        }

        /* Create and register the mbean */
        ObjectName objectName = createObjectName("dirhandler", dhn);

        DirHandlerConf dhc = (DirHandlerConf)makeObject(cfg.getConfBeanClass());
        if (dhc == null) {
          continue;
        }

        dhc.init(objectName.toString(), cfg, dhn, cfs.getPath());

        register("dirhandler", dhn, dhc);
        confBeans.add(dhc);

        dhc.saveConfig(); // Just to ensure we have it for next time
      }
    } catch (Throwable t) {
      error("Failed to start management context");
      error(t);
    }
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  /**
   * @return current state of config
   */
  private synchronized CardDAVConfig getCardDAVConf(final ConfigurationStore cfs,
                                                   final String configName) {
    CardDAVConfig cfg;
    try {
      /* Try to load it */
      ConfigurationType config = cfs.getConfig(configName);

      if (config == null) {
        /* XXX For the time being try to load it from the options.
         * This is just to allow a migration from the old 3.8 system to the
         * later releases.
         */
        cfg = (CardDAVConfig)getOpts().getAppProperty(configName);
      } else {
        cfg = new CardDAVConfig();

        cfg.setConfig(config);
        cfg.setAppName(configName);
      }

      return cfg;
    } catch (Throwable t) {
      error(t);
      cfg = new CardDAVConfig();
      return cfg;
    }
  }

  /**
   * @return current state of config
   */
  private synchronized DirHandlerConfig getDirHandlerConf(final ConfigurationStore cfs,
                                                           final String configName) {
    try {
      /* Try to load it */

      ConfigurationType config = cfs.getConfig(configName);

      if (config != null) {
        DirHandlerConfig cfg =
            (DirHandlerConfig)makeObject(DirHandlerConfig.getConfClass(config));

        cfg.setConfig(config);

        return cfg;
      }

      /* XXX For the time being try to load it from the options.
       * This is just to allow a migration from the old 3.8 system to the
       * later releases.
       */
      String cname = "org.bedework.global.dirhandlers." + configName;
      DirHandlerConfig cfg = (DirHandlerConfig)getOpts().getProperty(cname);

      if (cfg == null) {
        warn("No config found for " + cname);
        return null;
      }

      cfg.setConfClass(cfg.getClass().getCanonicalName());

      /* Fix this up so that we have a class for the config bean. */

      if (cfg instanceof DbDirHandlerConfig) {
        cfg.setConfBeanClass(DbDirHandlerConf.class.getCanonicalName());
      } else if (cfg instanceof LdapDirHandlerConfig) {
        cfg.setConfBeanClass(LdapDirHandlerConf.class.getCanonicalName());
      } else {
        warn("No config bean for class " + cfg.getConfClass());
      }

      return cfg;
    } catch (Throwable t) {
      error(t);
      return null;
    }
  }

  private Object makeObject(final String className) {
    try {
      Object o = Class.forName(className).newInstance();

      if (o == null) {
        error("Class " + className + " not found");
        return null;
      }

      return o;
    } catch (Throwable t) {
      error(t);
      return null;
    }
  }

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */
}
