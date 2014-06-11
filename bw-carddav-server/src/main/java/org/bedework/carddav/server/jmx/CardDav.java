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

import org.bedework.carddav.util.CardDAVContextConfig;
import org.bedework.carddav.util.DirHandlerConfig;

import org.bedework.util.config.ConfigurationStore;
import org.bedework.util.jmx.ConfBase;

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
public class CardDav extends ConfBase<CardDAVContextConfig> implements CardDavMBean {
  private static final String serviceName = "org.bedework.carddav:service=CardDav";

  /* Name of the property holding the location of the config data */
  private static final String confuriPname = "org.bedework.carddav.confuri";

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
  public CardDav() {
    super(serviceName);

    setConfigPname(confuriPname);
  }

  /**
   * @param configName
   * @return config for given service
   */
  public static CardDAVContextConfig getConf(final String configName) {
    for (ConfBase c: confBeans) {
      if ((c instanceof CardDavContext) &&
          (configName.equals(c.getConfigName()))) {
        return ((CardDavContext)c).getConf();
      }
    }

    return null;
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  /* ========================================================================
   * Operations
   * ======================================================================== */

  @Override
  public TabularData ListDirHandlersTable() {
    dhData.clear();

    for (DirHandlerConfig dhc: dirhandlerConfigs) {
      Object[] itemValues = {dhc.getPathPrefix(),
                             dhc.getName(),
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

  @Override
  public String ListDirHandlers() {
    StringBuilder res = new StringBuilder();

    for (DirHandlerConfig dhc: dirhandlerConfigs) {
      res.append(dhc.getPathPrefix());
      res.append("\t");
      res.append(dhc.getName());
      res.append("\t");
      res.append(dhc.getClassName());
      res.append("\n");
    }

    return res.toString();
  }

  @Override
  public String loadConfig() {
    try {
      /* Load up the end-point configs */

      ConfigurationStore cs = getStore();

      confBeans = new ArrayList<ConfBase>();

      /* register all our configurations */
      for (String c: configs) {
        ObjectName objectName = createObjectName("conf", c);

        CardDavContext cdc = new CardDavContext(cs,
                                                objectName.toString(),
                                                c);

        cdc.loadConfig();
        register("conf", c, cdc);
        confBeans.add(cdc);
      }

      /* Now do the dir handlers */

      /* First we need a list */

      cs = cs.getStore("dirHandlers");

      Collection<String> dirHandlerNames = cs.getConfigs();

      dirhandlerConfigs = new ArrayList<DirHandlerConfig>();

      for (String dhn: dirHandlerNames) {
        /* We have to load the config here to get the class of the bean. */
        DirHandlerConfig cfg = getDirHandlerConf(cs, dhn);

        if (cfg == null) {
          continue;
        }

        cfg.setName(dhn);
        dirhandlerConfigs.add(cfg);

        for (ConfBase c: confBeans) {
          if (c instanceof CardDavContext) {
            ((CardDavContext)c).getConf().addDirhandler(cfg);
          }
        }

        /* Create and register the mbean */
        ObjectName objectName = createObjectName("dirhandler", dhn);

        DirHandlerConf dhc = (DirHandlerConf)makeObject(cfg.getConfBeanClass());
        if (dhc == null) {
          continue;
        }

        dhc.init(cs, cfg, objectName.toString(), dhn);

        register("dirhandler", dhn, dhc);
        confBeans.add(dhc);
      }

      return "OK";
    } catch (Throwable t) {
      error("Failed to start management context");
      error(t);
      return "failed";
    }
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  /**
   * @return current state of config
   */
  private DirHandlerConfig getDirHandlerConf(final ConfigurationStore cfs,
                                             final String configName) {
    try {
      return (DirHandlerConfig)cfs.getConfig(configName);
    } catch (Throwable t) {
      error(t);
      return null;
    }
  }

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */
}
