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
package org.bedework.carddav.tools;

import org.bedework.util.args.Args;
import org.bedework.util.http.PooledHttpClient;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.URI;

/** Import a file into CardDAV.
 *
 *   @author Mike Douglass douglm   rpi.edu
 *  @version 1.0
 */
public class Importer implements Logged {
  private static String url = "http://localhost:8080/ucarddav/";

  private String infileName;

  private String user;

  private String pw;

  private LineNumberReader lnr;

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
          putCard(name, sb.toString());
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

      if (args.ifMatch("-f")) {
        infileName = args.next();
      } else if (args.ifMatch("-url")) {
        url = args.next();
      } else if (args.ifMatch("-user")) {
        user = args.next();
      } else if (args.ifMatch("-pw")) {
        pw = args.next();
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

  private boolean putCard(final String name,
                          final String content) {
    PooledHttpClient client = null;

    try {
      int respCode;
      final CredentialsProvider credsProvider;

      if ((user == null) || (pw == null)) {
        credsProvider = null;
      } else {

        credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(user,
                                                pw));
      }

      client = new PooledHttpClient(new URI(url),
                                    null,
                                    credsProvider);

      respCode = client.put(Util.buildPath(false, "/", name, ".vcf"),
                                    content,
                                    "text/vcard");

      info("Response code: " + respCode + " at line " + lnr.getLineNumber());

      return true;
    } catch (Throwable t) {
      t.printStackTrace();
      return false;
    } finally {
      if (client != null) {
        client.release();
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

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}
