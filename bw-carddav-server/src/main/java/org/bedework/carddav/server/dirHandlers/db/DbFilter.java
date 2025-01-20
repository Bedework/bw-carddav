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
package org.bedework.carddav.server.dirHandlers.db;

import org.bedework.base.exc.BedeworkException;
import org.bedework.carddav.common.filter.Filter;
import org.bedework.carddav.common.filter.PropFilter;
import org.bedework.carddav.common.filter.TextMatch;
import org.bedework.util.hibernate.HibSession;
import org.bedework.util.misc.Util;
import org.bedework.webdav.servlet.shared.WebdavException;

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

  private final static Map<String, PField> pfields = new HashMap<>();

  static {
    pfields.put("fn", new PField("fn", "fn"));
    pfields.put("kind", new PField("kind", "kind"));
  }

  int findex;
  List<String> params = new ArrayList<>();
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

  void parReplace(final HibSession sess) {
    for (int i = 0; i < params.size(); i++) {
      try {
        sess.setString(parPrefix + i, params.get(i));
      } catch (final BedeworkException e) {
        throw new WebdavException(e);
      }
    }
  }

  void makeFilter(final Filter filter) {
    if (filter == null) {
      return;
    }

    final Collection<PropFilter> pfilters = filter.getPropFilters();

    if ((pfilters == null) || pfilters.isEmpty()) {
      return;
    }

    sb.append(" and (");

    for (final PropFilter pfltr: pfilters) {
      makePropFilterExpr(pfltr);
    }

    sb.append(")");
  }

  private void makePropFilterExpr(final PropFilter filter) {
    final TextMatch tm = filter.getMatch();

    if (tm == null) {
      presenceCheck(filter);
      return;
    }

    final int testAllAnyProps = filter.getTestAllAny();

    final String name = filter.getName();

    final int cpos = name.indexOf(',');

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

  private void presenceCheck(final PropFilter filter) {
    final String name = filter.getName();

    final PField pf = pfields.get(name.toLowerCase());

    if (pf != null) {
      sb.append(pf.col);

      if (filter.getIsNotDefined()) {
        sb.append(" is null ");
      } else{
        sb.append(" is not null ");
      }
      return;
    }

    sb.append("props.name");
    if (filter.getIsNotDefined()) {
      sb.append(" not in ('");
    } else {
      sb.append(" in ('");
    }

    sb.append(name);
    sb.append("') ");
  }

  private void makePropFilterExpr(final String name, final TextMatch tm) {
    final PField pf = pfields.get(name.toLowerCase());

    final boolean caseless = tm.testCaseless();

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

    final int mt = tm.getMatchType();
    final String parVal;

    if (caseless) {
      parVal = tm.getVal().toUpperCase();
    } else {
      parVal = tm.getVal();
    }

    if (mt == TextMatch.matchTypeEquals) {
      if (tm.getNegated()) {
        sb.append("<>");
      } else {
        sb.append("=");
      }
      addPar(sb, parVal);
    } else {
      if (tm.getNegated()) {
        sb.append(" not");
      }
      sb.append(" like ");

      String val;

      if ((mt == TextMatch.matchTypeContains) ||
          (mt == TextMatch.matchTypeEndsWith)) {
        val = "%" + parVal;
      } else {
        val = parVal;
      }

      if ((mt == TextMatch.matchTypeContains) ||
          (mt == TextMatch.matchTypeStartsWith)) {
        val += "%";
      }

      addPar(sb, val);
    }
  }
}
