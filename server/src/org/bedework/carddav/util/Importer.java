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
package org.bedework.carddav.util;

import org.bedework.http.client.dav.DavClient;
import org.bedework.http.client.dav.DavResp;

import edu.rpi.sss.util.Args;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.LineNumberReader;

/** Import a file into CardDAV.
 *
 *   @author Mike Douglass douglm@rpi.edu
 *  @version 1.0
 */
public class Importer {
  private static String host = "localhost";

  private static int port = 8080;

  private static boolean secure = false;

  private String urlPrefix;

  private boolean debug;

  private String infileName;

  private String user;

  private String pw;

  private LineNumberReader lnr;

  private transient Logger log;

  boolean importData() throws Throwable {
    FileReader frdr = null;

    try {
      frdr = new FileReader(infileName);
      lnr = new LineNumberReader(frdr);

      StringBuilder sb = null;
      String name = null;

      do {
        String ln = lnr.readLine();


        if (ln == null) {
          break;
        }

        ln = ln.trim();

        if (ln.startsWith("BEGIN:VCARD")) {
          sb = new StringBuilder();
        } else if (ln.startsWith("UID:")) {
          name = ln.substring(4) + ".vcf";
          name = name.replace('/', '_');
          name = name.replace(' ', '_');
        }

        if (sb != null) {
          sb.append(ln);
          sb.append("\n");
        }

        if (ln.startsWith("END:VCARD")) {
          byte[] content = sb.toString().getBytes();

          putCard(name, content);
          sb = null;
        }


      } while (true);
    } finally {
      if (frdr != null) {
        try {
          frdr.close();
        } catch (Throwable t) {}
      }
    }


    return true;
  }

  boolean processArgs(final Args args) throws Throwable {
    if (args == null) {
      return true;
    }

    while (args.more()) {
      if (args.ifMatch("")) {
        continue;
      }

      if (args.ifMatch("-debug")) {
        debug = true;
      } else if (args.ifMatch("-ndebug")) {
        debug = false;
      } else if (args.ifMatch("-f")) {
        infileName = args.next();
      } else if (args.ifMatch("-host")) {
        host = args.next();
      } else if (args.ifMatch("-port")) {
        port = Integer.parseInt(args.next());
      } else if (args.ifMatch("-user")) {
        user = args.next();
      } else if (args.ifMatch("-pw")) {
        pw = args.next();
      } else if (args.ifMatch("-urlPrefix")) {
        urlPrefix = args.next();

        if (!urlPrefix.endsWith("/")) {
          urlPrefix += "/";
        }
      } else {
        error("Illegal argument: " + args.current());
        usage();
        return false;
      }
    }

    return true;
  }

  void usage() {
    System.out.println("Usage:");
    System.out.println("args   -debug");
    System.out.println("       -ndebug");
    System.out.println("       -f <filename>");
    System.out.println("            specify file containing cards");
    System.out.println("");
  }

  /** Main
   *
   * @param args
   */
  public static void main(final String[] args) {
    Importer imp = null;

    try {
      imp = new Importer();

      if (!imp.processArgs(new Args(args))) {
        return;
      }

      imp.importData();
    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  protected Logger getLog() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  protected void info(final String msg) {
    getLog().info(msg);
  }

  protected void error(final String msg) {
    getLog().error(msg);
  }

  protected void trace(final String msg) {
    getLog().debug(msg);
  }

  private boolean putCard(final String name, final byte[] content) {
    DavClient cio = null;

    try {
      int respCode;

      cio = new DavClient(host, port, 0, secure);

      cio.setCredentials(user, pw);

      respCode = cio.sendRequest("PUT", urlPrefix + "/" + name + ".vcf",
                                 null, null,
                                 "text/vcard", content.length,
                                 content);

      DavResp resp = cio.getResponse();

      InputStream in = null;

      try {
        in = resp.getContentStream();

        info("Response code: " + respCode + " at line " + lnr.getLineNumber());

        if (in != null) {
          readContent(in, resp.getContentLength(), resp.getCharset());
        }
      } finally {
        if (in != null) {
          in.close();
        }
      }

      return true;
    } catch (Throwable t) {
      t.printStackTrace();
      return false;
    } finally {
      if (cio != null) {
        try {
          cio.release();
        } catch (Throwable t) {}
        cio.close();
      }
    }
  }

  static void readContent(final InputStream in, final long expectedLen,
                          final String charset) throws Throwable {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int len = 0;

    boolean hadLf = false;
    boolean hadCr = false;

    while ((expectedLen < 0) || (len < expectedLen)) {
      int ich = in.read();
      if (ich < 0) {
        break;
      }

      len++;

      if (ich == '\n') {
        if (hadLf) {
          System.out.println("");
          hadLf = false;
          hadCr = false;
        } else {
          hadLf = true;
        }
      } else if (ich == '\r') {
        if (hadCr) {
          System.out.println("");
          hadLf = false;
          hadCr = false;
        } else {
          hadCr = true;
        }
      } else if (hadCr || hadLf) {
        hadLf = false;
        hadCr = false;

        if (baos.size() > 0) {
          String ln = new String(baos.toByteArray(), charset);
          System.out.println(ln);
        }

        baos.reset();
        baos.write(ich);
      } else {
        baos.write(ich);
      }
    }

    if (baos.size() > 0) {
      String ln = new String(baos.toByteArray(), charset);

      System.out.println(ln);
    }
  }
}
