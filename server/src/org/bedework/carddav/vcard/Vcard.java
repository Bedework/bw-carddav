/* **********************************************************************
    Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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

package org.bedework.carddav.vcard;

import org.bedework.carddav.server.CarddavCollection;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collection;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.sss.util.Uid;

/** Temp def of vcard to get us going
 *
 * @author douglm
 *
 */
public class Vcard {

  private AccessPrincipal owner;

  private String name;

  private CarddavCollection parent;

  private String created;

  private Collection<Property> props;

  private String strForm;

  private String jsonStrForm;

  private String prevLastmod;

  /**
   * @param val
   */
  public void setOwner(AccessPrincipal val) {
    owner = val;
  }

  /**
   * @return AccessPrincipal
   */
  public AccessPrincipal getOwner() {
    return owner;
  }

  /** Set the name
  *
  * @param val    String name
  */
 public void setName(String val) {
   name = val;
 }

 /** Get the name
  *
  * @return String   name
  */
 public String getName() {
   return name;
 }

  /**
   * @param val
   */
  public void setCreated(String val) {
    created = val;
  }

  /**
   * @return String created
   */
  public String getCreated() {
    return created;
  }

  /**
   * @param val
   */
  public void setLastmod(String val) {
    Property rev = findProperty("REV");

    if (rev == null) {
      rev = new Property("REV", null);
      addProperty(rev);
    }

    rev.value = val;
  }

  /**
   * @return String
   */
  public String getLastmod() {
    Property rev = findProperty("REV");

    if (rev == null) {
      return null;
    }

    return rev.value;
  }

  /** Lastmod before any changes were made
   *
   * @return String
   */
  public String getPrevLastmod() {
    return prevLastmod;
  }

  /**
   * @param val
   */
  public void setUid(String val) {
    Property uid = findProperty("UID");

    if (uid == null) {
      uid = new Property("UID", null);
      addProperty(uid);
    }

    uid.value = val;
  }

  /**
   * @return String
   */
  public String getUid() {
    Property uid = findProperty("UID");

    if (uid == null) {
      setUid(Uid.getUid());
      uid = findProperty("UID");
    }

    return uid.value;
  }

  /**
   * @param val
   */
  public void setParent(CarddavCollection val) {
    parent = val;
  }

  /**
   * @return parent.
   */
  public CarddavCollection getParent() {
    return parent;
  }

  /**
   * @param val
   */
  public void addProperty(Property val) {
    if (props == null) {
      props = new ArrayList<Property>();
    }

    props.add(val);
  }

  /**
   * @param name
   * @return property or null
   */
  public Property findProperty(String name) {
    if (props == null) {
      return null;
    }

    for (Property p: props) {
      if (name.equals(p.name)) {
        return p;
      }
    }

    return null;
  }

  /**
   * @param name
   * @return property or null
   */
  public Collection<Property> findProperties(String name) {
    Collection<Property> props = new ArrayList<Property>();

    if (props == null) {
      return props;
    }

    for (Property p: props) {
      if (name.equals(p.name)) {
        props.add(p);
      }
    }

    return props;
  }

  private static final int WORD_CHAR_START = 32;

  private static final int WORD_CHAR_END = 255;

  private static final int WHITESPACE_CHAR_START = 0;

  private static final int WHITESPACE_CHAR_END = 20;

  /**
   * @param rdr
   * @return Vcard
   * @throws WebdavException
   */
  public Vcard parse(Reader rdr) throws WebdavException {
    Tokenizer tokeniser = new Tokenizer(rdr);

    try {
      tokeniser.resetSyntax();
      tokeniser.wordChars(WORD_CHAR_START, WORD_CHAR_END);
      tokeniser.whitespaceChars(WHITESPACE_CHAR_START,
                                WHITESPACE_CHAR_END);
      tokeniser.ordinaryChar(':');
      tokeniser.ordinaryChar(';');
      tokeniser.ordinaryChar('=');
      tokeniser.ordinaryChar('\t');
      tokeniser.eolIsSignificant(true);
      tokeniser.whitespaceChars(0, 0);
      tokeniser.quoteChar('"');

      // BEGIN:VCALENDAR
      tokeniser.assertToken("BEGIN");

      tokeniser.assertToken(':');

      tokeniser.assertToken("VCARD", true);

      tokeniser.assertToken(StreamTokenizer.TT_EOL);

      while (!tokeniser.testToken("END")) {
        addProperty(new Property().parse(tokeniser));
        tokeniser.skipWhitespace();
      }

      tokeniser.assertToken(':');

      tokeniser.assertToken("VCARD", true);
      tokeniser.skipWhitespace();

      prevLastmod = getLastmod();

      return this;
    } catch (WebdavException wde) {
      throw wde;
    } catch (Exception e) {
      throw new WebdavException(e);
    }
  }

  /**
   * @return String
   */
  public String output() {
    if (strForm != null) {
      return strForm;
    }

    StringBuilder sb = new StringBuilder();

    sb.append("BEGIN:VCARD\n");
    sb.append("VERSION:4.0\n");

    if (props != null) {
      for (Property p: props) {
        if ("VERSION".equals(p.name)) {
          continue;
        }

        p.output(sb);
      }
    }

    sb.append("END:VCARD\n\n");

    strForm = sb.toString();

    return strForm;
  }

  /**
   * @param indent
   * @return String
   */
  public String outputJson(String indent) {
    if (jsonStrForm != null) {
      return jsonStrForm;
    }

    StringBuilder sb = new StringBuilder();

    sb.append(indent);
    sb.append("{\n");

    indent += "";

    Property version = new Property(VcardDefs.getPropertyDef("VERSION"), "4.0");

    version.outputJson(indent, sb);

    for (String pname: VcardDefs.getPropertyNames()) {
      if ("VERSION".equals(pname)) {
        continue;
      }

      Collection<Property> props = findProperties(pname);

      if (!props.isEmpty()) {
        int ct = 0;
        for (Property pr: props) {
          if (ct == 0) {
            pr.outputJsonStart(indent, sb);
          }

          pr.outputJsonValue(indent, sb);

          ct++;
          if (ct == props.size()) {
            pr.outputJsonEnd(indent, sb);
          }
        }
      }
    }
    if (props != null) {
      for (Property p: props) {
        p.outputJson(indent, sb);
      }
    }

    sb.append(indent);
    sb.append("}");

    jsonStrForm = sb.toString();

    return jsonStrForm;
  }

  public String toString() {
    return output();
  }
}
