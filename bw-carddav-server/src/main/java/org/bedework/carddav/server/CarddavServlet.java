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

import edu.rpi.cct.webdav.servlet.common.MethodBase.MethodInfo;
import edu.rpi.cct.webdav.servlet.common.WebdavServlet;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;
import org.bedework.util.jmx.ConfBase;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

/** This class extends the webdav servlet class, implementing the abstract
 * methods and overriding others to extend/modify the behaviour.
 *
 * @author Mike Douglass   douglm  rpi . edu
 */
public class CarddavServlet extends WebdavServlet
    implements ServletContextListener {
  /* ====================================================================
   *                     Abstract servlet methods
   * ==================================================================== */

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.common.WebdavServlet#addMethods()
   */
  @Override
  protected void addMethods() {
    super.addMethods();

    // Replace methods
    methods.put("REPORT", new MethodInfo(CarddavReportMethod.class, false));
  }

  @Override
  public WebdavNsIntf getNsIntf(final HttpServletRequest req)
      throws WebdavException {
    CarddavBWIntf wi = new CarddavBWIntf();

    wi.init(this, req, methods, dumpContent);
    return wi;
  }

  /* -----------------------------------------------------------------------
   *                         JMX support
   */

  class Configurator extends ConfBase {
    CardDav cd;

    Configurator() {
      super("org.bedework.synch:service=Synch");
    }

    void start() {
      try {
        getManagementContext().start();

        cd = new CardDav();
        register("cardDav", "cardDav", cd);
        cd.loadConfigs();
      } catch (Throwable t){
        t.printStackTrace();
      }
    }

    void stop() {
      try {
        if (cd != null) {
          getManagementContext().stop();
        }
      } catch (Throwable t){
        t.printStackTrace();
      }
    }
  }

  private Configurator conf = new Configurator();

  @Override
  public void contextInitialized(final ServletContextEvent sce) {
    String s = sce.getServletContext().getInitParameter("register-jmx");

    if (!Boolean.valueOf(s)) {
      return;
    }

    conf.start();
  }

  @Override
  public void contextDestroyed(final ServletContextEvent sce) {
    conf.stop();
  }

}
