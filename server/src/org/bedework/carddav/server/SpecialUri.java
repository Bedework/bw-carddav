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

import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.server.filter.PropFilter;
import org.bedework.carddav.server.filter.TextMatch;
import org.bedework.carddav.util.CardDAVConfig;
import org.bedework.carddav.vcard.Vcard;

import edu.rpi.cct.webdav.servlet.shared.WebdavBadRequest;
import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.sss.util.Util;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Implement the special uri(s).
 *
 */
public class SpecialUri {
  /** */
  public static String formatVcard = "vcard";

  /** */
  public static String formatJson = "json";

  /**
   * @param req
   * @param resp
   * @param resourceUri
   * @param sysi
   * @param config
   * @param debug
   * @return true for a special uri
   * @throws WebdavException
   */
  public static boolean process(HttpServletRequest req,
                                HttpServletResponse resp,
                                String resourceUri,
                                SysIntf sysi,
                                CardDAVConfig config,
                                boolean debug) throws WebdavException {
    if (config.getWebaddrServiceURI() == null) {
      return false;
    }

    if (!config.getWebaddrServiceURI().equals(resourceUri)) {
      return false;
    }

    String format = Util.checkNull(req.getParameter("format"));

    if (format == null) {
      format = formatVcard;
    }

    startResponse(resp, format);

    buildResponse: {
      String addrbook = req.getParameter("addrbook");

      if (addrbook == null) {
        addrbook = config.getWebaddrPublicAddrbook();
      }

      CarddavCollection col = sysi.getCollection(addrbook);

      if (col == null) {
        setStatus(resp, HttpServletResponse.SC_NOT_FOUND, format);
        break buildResponse;
      }

      Collection<Vcard> cards = doSearch(req, col, sysi, debug);

      if ((cards == null) || (cards.size() == 0)) {
        setStatus(resp, HttpServletResponse.SC_NOT_FOUND, format);
        break buildResponse;
      }

      doOutput(resp, cards, format);
    }

    endResponse(resp, format);

    return true;
  }

  private static void startResponse(HttpServletResponse resp,
                                    String format) throws WebdavException {
    if (format.equals(formatVcard)) {
      return;
    }

    // vcard
    try {
      resp.getWriter().write("{\n");
    } catch (IOException ie) {
      throw new WebdavException(ie);
    }
  }

  private static Collection<Vcard> doSearch(HttpServletRequest req,
                                            CarddavCollection col,
                                            SysIntf sysi,
                                            boolean debug) throws WebdavException {
    String user = req.getParameter("user");
    String mail = req.getParameter("mail");
    String org = req.getParameter("org");
    String text = req.getParameter("text");

    if ((user == null) && (mail == null) &&
        (org == null) && (text == null)) {
      throw new WebdavBadRequest();
    }

    boolean orThem = true;

    if (text != null) {
      if ((user != null) && (mail != null)) {
        // Conflict
        throw new WebdavBadRequest("Conflicting request parameters");
      }

      if (user == null) {
        user = text;
      }
      if (mail == null) {
        mail = text;
      }
    }

    Filter fltr = new Filter(debug);

    if (orThem) {
      fltr.setTestAllAny(Filter.testAnyOf);
    } else {
      fltr.setTestAllAny(Filter.testAllOf);
    }

    if (user != null) {
      fltr.addPropFilter(new PropFilter("FN", new TextMatch(user)));
    }

    if (mail != null) {
      fltr.addPropFilter(new PropFilter("EMAIL", new TextMatch(mail)));
    }

    if (org != null) {
      fltr.addPropFilter(new PropFilter("ORG", new TextMatch(org)));
    }

    return sysi.getCards(col, fltr);
  }

  private static void doOutput(HttpServletResponse resp,
                               Collection<Vcard> cards,
                               String format) throws WebdavException {
    try {
      Writer wtr = resp.getWriter();

      if (format.equals(formatVcard)) {
        for (Vcard card: cards) {
          wtr.write(card.output());
        }
        resp.setStatus(HttpServletResponse.SC_OK);

        return;
      }

      // vcard
      wtr.write("  \"microformats\": {");
      wtr.write("\n");

      wtr.write("    \"vcard\": [");
      wtr.write("\n");

      boolean first = false;

      for (Vcard card: cards) {
        if (first) {
          first = false;
        } else {
          wtr.write(",\n");
        }
        wtr.write(card.outputJson("      "));
      }

      wtr.write("\n    ]\n");

      resp.setStatus(HttpServletResponse.SC_OK);
    } catch (IOException ie) {
      throw new WebdavException(ie);
    }
  }

  private static void setStatus(HttpServletResponse resp,
                                int status,
                                String format) throws WebdavException {
    try {
      if (format.equals(formatVcard)) {
        resp.setStatus(status);
        return;
      }

      // vcard
      Writer wtr = resp.getWriter();

      wtr.write("    \"errors\" : [ \"");
      wtr.write(String.valueOf(status));
      wtr.write("\" ]\n");
    } catch (IOException ie) {
      throw new WebdavException(ie);
    }
  }

  private static void endResponse(HttpServletResponse resp,
                                    String format) throws WebdavException {
    if (format.equals(formatVcard)) {
      return;
    }

    // vcard
    try {
      resp.getWriter().write("}\n");
    } catch (IOException ie) {
      throw new WebdavException(ie);
    }
  }
}
