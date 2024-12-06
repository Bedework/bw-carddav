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

import org.bedework.carddav.common.DirHandler;
import org.bedework.carddav.common.config.DirHandlerConfig;
import org.bedework.carddav.server.config.CardDAVConfig;
import org.bedework.carddav.server.config.CardDAVContextConfig;
import org.bedework.carddav.server.dirHandlers.DirHandlerFactory;
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
public class CardDav extends ConfBase<CardDAVConfig> implements CardDavMBean {
  /** */
  public static final String serviceName = "org.bedework.carddav:service=CardDav";

  /* Name of the directory holding the config data */
  private static final String confDirName = "carddav";

  private static final String[] dhListItemNames = {"path",
                                                   "name",
                                                   "class"};

  private static final String[] dhListItemDescriptions = {"Path sequence",
                                                          "Dir handler name",
                                                          "Dir handler class"};

  private static final OpenType<?>[] dhListItemTypes = {SimpleType.STRING,
                                                        SimpleType.STRING,
                                                        SimpleType.STRING};

  /* path must be unique */
  private static final String[] dhListIndexNames = {"path"};

  private static final TabularDataSupport dhData;

  private static final CompositeType dhType;
  static {
    try {
      dhType = new CompositeType("Dirhandlers",
                                 "Dirhandlers info",
                                 dhListItemNames,
                                 dhListItemDescriptions,
                                 dhListItemTypes);
      final TabularType dhTable = new TabularType("Dirhandlers",
                                                  "Dirhandlers info",
                                                  dhType,
                                                  dhListIndexNames);

      dhData = new TabularDataSupport(dhTable);
    } catch (final OpenDataException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   */
  public CardDav() {
    super(serviceName, confDirName, "config");
  }

  /**
   * @param configName name of context config
   * @return config for given service
   */
  public CardDAVContextConfig getConf(final String configName) {
    for (final CardDAVContextConfig c: getConfig().getContextConfigs()) {
      if (configName.equals(c.getName())) {
        return c;
      }
    }

    return null;
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  @Override
  public void setDataOut(final String val) {
    getConfig().setDataOut(val);
  }

  @Override
  public String getDataOut() {
    return getConfig().getDataOut();
  }

  @Override
  public void setDefaultVcardVersion(final String val) {
    getConfig().setDefaultVcardVersion(val);
  }

  @Override
  public String getDefaultVcardVersion() {
    return getConfig().getDefaultVcardVersion();
  }

  @Override
  public void setSysintfImpl(final String val) {
    getConfig().setSysintfImpl(val);
  }

  @Override
  public String getSysintfImpl() {
    return getConfig().getSysintfImpl();
  }

  @Override
  public void setDefaultAddressbook(final String val) {
    getConfig().setDefaultAddressbook(val);
  }

  @Override
  public String getDefaultAddressbook() {
    return getConfig().getDefaultAddressbook();
  }

  @Override
  public void setUserHomeRoot(final String val) {
    getConfig().setUserHomeRoot(val);
  }

  @Override
  public String getUserHomeRoot() {
    return getConfig().getUserHomeRoot();
  }

  @Override
  public void setPrincipalRoot(final String val) {
    getConfig().setPrincipalRoot(val);
  }

  @Override
  public String getPrincipalRoot() {
    return getConfig().getPrincipalRoot();
  }

  @Override
  public void setUserPrincipalRoot(final String val) {
    getConfig().setUserPrincipalRoot(val);
  }

  @Override
  public String getUserPrincipalRoot() {
    return getConfig().getUserPrincipalRoot();
  }

  @Override
  public void setGroupPrincipalRoot(final String val) {
    getConfig().setGroupPrincipalRoot(val);
  }

  @Override
  public String getGroupPrincipalRoot() {
    return getConfig().getGroupPrincipalRoot();
  }

  @Override
  public void setResourcePrincipalRoot(final String val) {
    getConfig().setResourcePrincipalRoot(val);
  }

  @Override
  public String getResourcePrincipalRoot() {
    return getConfig().getResourcePrincipalRoot();
  }

  @Override
  public void setVenuePrincipalRoot(final String val) {
    getConfig().setVenuePrincipalRoot(val);
  }

  @Override
  public String getVenuePrincipalRoot() {
    return getConfig().getVenuePrincipalRoot();
  }

  @Override
  public void setTicketPrincipalRoot(final String val) {
    getConfig().setTicketPrincipalRoot(val);
  }

  @Override
  public String getTicketPrincipalRoot() {
    return getConfig().getTicketPrincipalRoot();
  }

  @Override
  public void setHostPrincipalRoot(final String val) {
    getConfig().setHostPrincipalRoot(val);
  }

  @Override
  public String getHostPrincipalRoot() {
    return getConfig().getHostPrincipalRoot();
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

  @Override
  public TabularData ListDirHandlersTable() {
    dhData.clear();

    for (final DirHandlerConfig<?> dhc: getConfig().getDirHandlerConfigs()) {
      final Object[] itemValues = {dhc.getPathPrefix(),
                                   dhc.getName(),
                                   dhc.getClassName()
      };
      try {
        dhData.put(new CompositeDataSupport(dhType, dhListItemNames,
                                            itemValues));
      } catch (final OpenDataException e) {
        error(e);
      }
    }

    return dhData;
  }

  @Override
  public String ListDirHandlers() {
    final StringBuilder res = new StringBuilder();

    for (final DirHandlerConfig<?> dhc: getConfig().getDirHandlerConfigs()) {
      res.append(dhc.getPathPrefix())
         .append("\t")
         .append(dhc.getName())
         .append("\t")
         .append(dhc.getClassName())
         .append("\n");
    }

    return res.toString();
  }

  @Override
  public List<String> exportData() {
    final DirHandlerFactory dhf = new DirHandlerFactory(getConfig());
    final List<String> msgs = new ArrayList<>();

    try {
      for (final DirHandler dh: dhf.getHandlers("admin", null)) {
        dh.exportData(getConfig().getDataOut());
      }
    } catch (final Throwable t) {
      error(t);
      msgs.add(t.getLocalizedMessage());
    } finally {
      try {
        dhf.close();
      } catch (final Throwable t) {
        error(t);
        msgs.add(t.getLocalizedMessage());
      }
    }

    return msgs;
  }

  @Override
  public String loadConfig() {
    try {
      final String res = loadConfig(CardDAVConfig.class);
      if (!"OK".equals(res)) {
        return res;
      }

      /* Load up the context configs */

      final ConfigurationStore ctxcs = getStore().getStore("contexts");
      final Collection<String> contextNames = ctxcs.getConfigs();

      /* register all our configurations */
      for (final String c: contextNames) {
        final ObjectName objectName = createObjectName("conf", c);

        final CardDavContext cdc =
                new CardDavContext(ctxcs,
                                   objectName.toString(),
                                   c);

        cdc.loadConfig();
        register("conf", c, cdc);
        getConfig().addContext(cdc.getConfig());
      }

      /* Now do the dir handlers */

      /* First we need a list */

      final ConfigurationStore dhcs = getStore().getStore("dirHandlers");

      final Collection<String> dirHandlerNames = dhcs.getConfigs();

      for (final String dhn: dirHandlerNames) {
        /* We have to load the config here to get the class of the bean. */
        final DirHandlerConfig<?> cfg = getDirHandlerConf(dhcs, dhn);

        if (cfg == null) {
          continue;
        }

        cfg.setName(dhn);

        /* Create and register the mbean */
        final ObjectName objectName = createObjectName("dirhandler", dhn);

        final DirHandlerConf dhc =
                (DirHandlerConf)makeObject(
                        cfg.getConfBeanClass(),
                        objectName.toString(),
                        dhcs,
                        dhn);
        if (dhc == null) {
          continue;
        }

        dhc.setConfig(cfg);

        register("dirhandler", dhn, dhc);
        getConfig().addDirhandler(dhc.getConfig());
      }

      return "OK";
    } catch (final Throwable t) {
      error("Failed to start management context");
      error(t);
      return "failed";
    }
  }

  /* ==============================================================
   *                   Private methods
   * ============================================================== */

  /**
   * @return current state of config
   */
  private DirHandlerConfig<?> getDirHandlerConf(final ConfigurationStore cfs,
                                             final String configName) {
    try {
      return (DirHandlerConfig<?>)cfs.getConfig(configName);
    } catch (final Throwable t) {
      error(t);
      return null;
    }
  }
}
