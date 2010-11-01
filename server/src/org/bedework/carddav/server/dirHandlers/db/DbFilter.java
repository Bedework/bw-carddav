/* **********************************************************************
    Copyright 2010 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
package org.bedework.carddav.server.dirHandlers.db;

import org.bedework.carddav.server.filter.Filter;
import org.bedework.carddav.server.filter.PropFilter;
import org.bedework.carddav.server.filter.TextMatch;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.sss.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Add filtering expression to hsql.
 *
 * @author douglm
 *
 */
public class DbFilter {
  private static final String parPrefix = "FPAR";

  /* Entry for property we store as a column */
  static class PField {
    String pname;
    String col;

    PField(final String pname, final String col) {
      this.pname = pname;
      this.col = col;
    }
  }

  private static Map<String, PField> pfields = new HashMap<String, PField>();

  static {
    pfields.put("fn", new PField("fn", "fn"));
    pfields.put("kind", new PField("kind", "kind"));
  }

  int findex;
  List<String> params = new ArrayList<String>();
  StringBuilder sb;

  boolean first = true;

  DbFilter(final StringBuilder sb) {
    this.sb = sb;
  }

  void addPar(final StringBuilder sb,
              final String val) {
    sb.append(":");
    sb.append(parPrefix);
    sb.append(findex);
    findex++;

    params.add(val);
  }

  void parReplace(final HibSession sess) throws WebdavException {
    for (int i = 0; i < params.size(); i++) {
      sess.setString(parPrefix + i, params.get(i));
    }
  }

  void makeFilter(final Filter filter) {
    if (filter == null) {
      return;
    }

    Collection<PropFilter> pfilters = filter.getPropFilters();

    if ((pfilters == null) || pfilters.isEmpty()) {
      return;
    }

    sb.append(" and (");

    for (PropFilter pfltr: pfilters) {
      makePropFilterExpr(pfltr);
    }

    sb.append(")");
  }

  private void makePropFilterExpr(final PropFilter filter) {
    TextMatch tm = filter.getMatch();

    if (tm == null) {
      return;
    }

    int testAllAnyProps = filter.getTestAllAny();

    String name = filter.getName();

    int cpos = name.indexOf(',');

    if ((cpos < 0) && (Util.isEmpty(filter.getParamFilters()))) {
      // No group - no params - single attribute

      if (!first) {
        if (testAllAnyProps == Filter.testAllOf) {
          sb.append(" and (");
        } else {
          sb.append(" or (");
        }
      } else {
        sb.append("(");
        first = false;
      }

      makePropFilterExpr(name, tm);

      sb.append(")");
    }

    /* TODO Do this later
    if (cpos > 0) {
      if (name.endsWith(",")) {
        // Don't do that
        return;
      }

      name = name.substring(cpos + 1);
    }

    Collection<String> anames = LdapMapping.toLdapAttrNoGroup.get(name);
    if (Util.isEmpty(anames)) {
      return;
    }

    StringBuilder sb = new StringBuilder();

    boolean first = true;

    for (String attrId: anames) {
      String ptest = makePropFilterExpr(attrId, tm);

      if (ptest == null) {
        continue;
      }

      sb.append(ptest);

      if (first) {
        first = false;
        continue;
      }

      sb.append(")");

      if (testAllAnyProps == Filter.testAllOf) {
        sb.append(" and ");
      } else {
        sb.append(" or ");
      }
    }

    return sb.toString();
     */
  }

  private void makePropFilterExpr(final String name, final TextMatch tm) {
    PField pf = pfields.get(name.toLowerCase());

    boolean caseless = tm.getCaseless();

    if (pf != null) {
      // Use the column value

      if (caseless) {
        sb.append("upper(");
      }
      sb.append("card.");
      sb.append(pf.col);
      if (caseless) {
        sb.append(")");
      }
    } else {
      sb.append("props.name=");
      addPar(sb, name);

      if (caseless) {
        sb.append(" and upper(props.value)");
      } else {
        sb.append(" and props.value");
      }
    }

    int mt = tm.getMatchType();

    if (mt == TextMatch.matchTypeEquals) {
      sb.append("=");
      addPar(sb, tm.getVal());
    } else {
      sb.append(" like ");

      String val;

      if ((mt == TextMatch.matchTypeContains) ||
          (mt == TextMatch.matchTypeEndsWith)) {
        val = "%" + tm.getVal();
      } else {
        val = tm.getVal();
      }

      if ((mt == TextMatch.matchTypeContains) ||
          (mt == TextMatch.matchTypeStartsWith)) {
        val += "%";
      }

      addPar(sb, val);
    }
  }
}
