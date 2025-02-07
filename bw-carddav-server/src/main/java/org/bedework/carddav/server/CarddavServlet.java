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
package org.bedework.carddav.server;

import org.bedework.carddav.server.jmx.CardDav;
import org.bedework.util.jmx.ConfBase;
import org.bedework.webdav.servlet.common.MethodBase.MethodInfo;
import org.bedework.webdav.servlet.common.WebdavServlet;
import org.bedework.webdav.servlet.shared.WebdavNsIntf;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.http.HttpServletRequest;

/** This class extends the webdav servlet class, implementing the abstract
 * methods and overriding others to extend/modify the behaviour.
 *
 * @author Mike Douglass   douglm  rpi . edu
 */
public class CarddavServlet extends WebdavServlet
    implements ServletContextListener {
  /* ==============================================================
   *                     Abstract servlet methods
   * ============================================================== */

  @Override
  protected void addMethods() {
    super.addMethods();

    // Replace methods
    methods.put("REPORT", new MethodInfo(CarddavReportMethod.class, false));
  }

  @Override
  public WebdavNsIntf getNsIntf(final HttpServletRequest req) {
    final CarddavBWIntf wi = new CarddavBWIntf();

    wi.init(this, req, methods, dumpContent);
    return wi;
  }

  /**
   * @return conf bean for service
   */
  public CardDav getConf() {
    return conf.cd;
  }

  /* ---------------------------------------------------------------
   *                         JMX support
   */

  static class Configurator extends ConfBase {
    CardDav cd;

    Configurator() {
      super(CardDav.serviceName,
            (String)null,
            null);
    }

    @Override
    public String loadConfig() {
      return null;
    }

    @Override
    public void start() {
      try {
        getManagementContext().start();

        cd = new CardDav();
        register("cardDav", "cardDav", cd);
        cd.loadConfig();
      } catch (final Throwable t){
        t.printStackTrace();
      }
    }

    @Override
    public void stop() {
      try {
        if (cd != null) {
          getManagementContext().stop();
        }
      } catch (final Throwable t){
        t.printStackTrace();
      }
    }
  }

  private static final Configurator conf = new Configurator();

  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    final String s = sce.getServletContext().getInitParameter("register-jmx");

    if (!Boolean.parseBoolean(s)) {
      return;
    }

    conf.start();
  }

  @Override
  public void contextDestroyed(final ServletContextEvent sce) {
    conf.stop();
  }
}
