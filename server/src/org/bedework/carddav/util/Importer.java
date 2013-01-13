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
package org.bedework.carddav.util;

import edu.rpi.sss.util.Args;
import edu.rpi.sss.util.http.BasicHttpClient;

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

  private static String scheme = "http";

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
    BasicHttpClient client = null;

    try {
      int respCode;

      client = new BasicHttpClient(host, port, scheme, 0);

      client.setCredentials(user, pw);

      respCode = client.sendRequest("PUT", urlPrefix + "/" + name + ".vcf",
                                    null, null,
                                    "text/vcard", content.length,
                                    content);

      InputStream in = null;

      try {
        in = client.getResponseBodyAsStream();

        info("Response code: " + respCode + " at line " + lnr.getLineNumber());

        if (in != null) {
          readContent(in, client.getResponseContentLength(),
                      client.getResponseCharSet());
        }
      } finally {
        in.close();
      }

      return true;
    } catch (Throwable t) {
      t.printStackTrace();
      return false;
    } finally {
      if (client != null) {
        try {
          client.release();
        } catch (Throwable t) {}
        client.close();
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
