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

package org.bedework.carddav.server;

import org.bedework.carddav.util.Tokenizer;
import org.bedework.carddav.util.Uid;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Collection;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cct.webdav.servlet.shared.WebdavUserNode;

/** Temp def of vcard to get us going
 *
 * @author douglm
 *
 */
public class Vcard {
  public static class Param {
    String name;
    String value;

    public Param() {
    }

    public Param(String name, String value) {
      this.name = name;
      this.value = value;
    }

    /* Crude vcard parameter output method
     *
     * @param sb
     */
    public void output(StringBuilder sb) {
      if (value == null) {
        return;
      }

      sb.append(name);
      sb.append("=");
      sb.append(value);
    }

    public Param parse(Tokenizer tokeniser) throws WebdavException {
      try {
        tokeniser.assertWord();

        name = tokeniser.sval;

        if (tokeniser.nextToken() == '=') {
          tokeniser.assertWord();

          value = tokeniser.sval;
        } else {
          tokeniser.pushBack();
        }
      } catch (IOException e) {
        throw new WebdavException(e);
      }

      return this;
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();

      output(sb);

      return sb.toString();
    }
  }

  public static class Property {
    String group;
    String name;

    Collection<Param> params;

    String value;

    public Property() {
    }

    public Property(String name, String value) {
      this(null, name, (Collection<Param>)null, value);
    }

    public Property(String group, String name, String value) {
      this(group, name, (Collection<Param>)null, value);
    }

    public Property(String name,
                    Collection<Param> params,
                    String value) {
      this(null, name, params, value);
    }

    public Property(String group,
                    String name,
                    Collection<Param> params,
                    String value) {
      this.group = group;
      this.name = name;
      this.params = params;
      this.value = value;
    }

    /**
     * @return String name
     */
    public String getName() {
      return name;
    }

    /**
     * @return String value
     */
    public String getValue() {
      return value;
    }

    public void addParam(Param val) {
      if (params == null) {
        params = new ArrayList<Param>();
      }

      params.add(val);
    }

    public Property parse(Tokenizer tokeniser) throws WebdavException {
      try {
        tokeniser.assertWord();

        String temp = tokeniser.sval;

        if (tokeniser.nextToken() == ',') {
          group = temp;
          tokeniser.assertWord();

          temp = tokeniser.sval;
        } else {
          tokeniser.pushBack();
        }

        name = temp;

        while (tokeniser.nextToken() == ';') {
          addParam(new Param().parse(tokeniser));
        }

        tokeniser.assertToken(':');
      } catch (IOException e) {
        throw new WebdavException(e);
      }

      return this;
    }

    /* Crude vcard property output method
     *
     * @param sb
     */
    public void output(StringBuilder sb) {
      if (value == null) {
        return;
      }

      sb.append(name);

      if (params != null) {
        for (Param par: params) {
          sb.append(";");
          par.output(sb);
        }
      }

      sb.append(":");
      sb.append(value);
      sb.append("\n");
    }

    public String toString() {
      StringBuilder sb = new StringBuilder();

      output(sb);

      return sb.toString();
    }
  }

  private WebdavUserNode owner;

  private String name;

  private CarddavCollection parent;

  private String created;

  private Collection<Property> props;

  private String strForm;

  private String prevLastmod;

  /**
   * @param val
   */
  public void setOwner(WebdavUserNode val) {
    owner = val;
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
   * @return WebdavUserNode
   */
  public WebdavUserNode getOwner() {
    return owner;
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

  public void setUid(String val) {
    Property uid = findProperty("UID");

    if (uid == null) {
      uid = new Property("UID", null);
      addProperty(uid);
    }

    uid.value = val;
  }

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

  public void addProperty(Property val) {
    if (props == null) {
      props = new ArrayList<Property>();
    }

    props.add(val);
  }

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

  private static final int WORD_CHAR_START = 32;

  private static final int WORD_CHAR_END = 255;

  private static final int WHITESPACE_CHAR_START = 0;

  private static final int WHITESPACE_CHAR_END = 20;

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

  public String toString() {
    return output();
  }
}
