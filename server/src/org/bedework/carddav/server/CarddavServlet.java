/* **********************************************************************
    Copyright 2008 Rensselaer Polytechnic Institute. All worldwide rights reserved.

    Redistribution and use of this distribution in source and binary forms,
    with or without modification, are permitted provided that:
       The above copyright notice and this permission notice appear in all
        copies and supporting documentation;

        The name, identifiers, and trademarks of Rensselaer Polytechnic
        Institute are not used in advertising or publicity without the
        express prior written permission of Rensselaer Polytechnic Institute;

    DISCLAIMER: The software is distributed" AS IS" without any express or
    implied warranty, including but not limited to, any implied warranties
    of merchantability or fitness for a particular purpose or any warrant)'
    of non-infringement of any current or pending patent rights. The authors
    of the software make no representations about the suitability of this
    software for any particular purpose. The entire risk as to the quality
    and performance of the software is with the user. Should the software
    prove defective, the user assumes the cost of all necessary servicing,
    repair or correction. In particular, neither Rensselaer Polytechnic
    Institute, nor the authors of the software are liable for any indirect,
    special, consequential, or incidental damages related to the software,
    to the maximum extent the law permits.
*/
package org.bedework.carddav.server;

import edu.rpi.cct.webdav.servlet.common.WebdavServlet;
import edu.rpi.cct.webdav.servlet.common.MethodBase.MethodInfo;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavNsIntf;

import javax.servlet.http.HttpServletRequest;

/** This class extends the webdav servlet class, implementing the abstract
 * methods and overriding others to extend/modify the behaviour.
 *
 * @author Mike Douglass   douglm  rpi . edu
 */
public class CarddavServlet extends WebdavServlet {
  private String id = null;
  /* ====================================================================
   *                     Abstract servlet methods
   * ==================================================================== */

  public String getId() {
    if (id != null) {
      return id;
    }

    if (props == null) {
      return getClass().getName();
    }

    id = props.getProperty("edu.rpi.cct.uwcal.appname");
    if (id == null) {
      id = getClass().getName();
    }

    return id;
  }

  /* (non-Javadoc)
   * @see edu.rpi.cct.webdav.servlet.common.WebdavServlet#addMethods()
   */
  protected void addMethods() {
    super.addMethods();

    // Replace methods
    methods.put("REPORT", new MethodInfo(CarddavReportMethod.class, false));
  }

  public WebdavNsIntf getNsIntf(HttpServletRequest req)
      throws WebdavException {
    CarddavBWIntf wi = new CarddavBWIntf();

    wi.init(this, req, props, debug, methods, dumpContent);
    return wi;
  }
}