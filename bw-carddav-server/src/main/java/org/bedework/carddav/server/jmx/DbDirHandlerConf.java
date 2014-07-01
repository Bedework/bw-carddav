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
package org.bedework.carddav.server.jmx;

import org.bedework.carddav.server.config.DbDirHandlerConfig;

import org.bedework.util.jmx.InfoLines;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import java.io.StringReader;
import java.util.List;
import java.util.Properties;

/**
 * @author douglm
 *
 */
public class DbDirHandlerConf extends DirHandlerConf implements DbDirHandlerConfMBean {
  private boolean drop;

  /* Be safe - default to false */
  private boolean export;

  private String schemaOutFile;

  private Configuration cfg;

  private class SchemaThread extends Thread {
    InfoLines infoLines = new InfoLines();

    SchemaThread() {
      super("BuildSchema");
    }

    @Override
    public void run() {
      try {
        infoLines.addLn("Started export of schema");

        final long startTime = System.currentTimeMillis();

        final SchemaExport se = new SchemaExport(getConfiguration());

//      if (getDelimiter() != null) {
//        se.setDelimiter(getDelimiter());
//      }

        se.setFormat(true);       // getFormat());
        se.setHaltOnError(false); // getHaltOnError());
        se.setOutputFile(getSchemaOutFile());
        /* There appears to be a bug in the hibernate code. Everybody initialises
        this to /import.sql. Set to null causes an NPE
        Make sure it refers to a non-existant file */
        //se.setImportFile("not-a-file.sql");

        se.execute(false, // script - causes write to System.out if true
                   getExport(),
                   getDrop(),
                   true);   //   getCreate());

        final long millis = System.currentTimeMillis() - startTime;
        final long seconds = millis / 1000;
        final long minutes = seconds / 60;

        infoLines.addLn("Elapsed time: " + minutes + ":" +
                                twoDigits(seconds - (minutes * 60)));
      } catch (final Throwable t) {
        error(t);
        infoLines.exceptionMsg(t);
      } finally {
        infoLines.addLn("Schema build completed");
        drop = false;
        export = false;
      }
    }
  }

  private final SchemaThread buildSchema = new SchemaThread();


  /* ========================================================================
   * Schema attributes
   * ======================================================================== */

  @Override
  public void setDrop(final boolean val) {
    drop = val;
  }

  @Override
  public boolean getDrop() {
    return drop;
  }

  @Override
  public void setExport(final boolean val) {
    export = val;
  }

  @Override
  public boolean getExport() {
    return export;
  }

  @Override
  public void setSchemaOutFile(final String val) {
    schemaOutFile = val;
  }

  @Override
  public String getSchemaOutFile() {
    return schemaOutFile;
  }

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  @Override
  public void setRootAccess(final String val)  {
    getConf().setRootAccess(val);
  }

  @Override
  public String getRootAccess()  {
    return getConf().getRootAccess();
  }

  @Override
  public void setRootOwner(final String val)  {
    getConf().setRootOwner(val);
  }

  @Override
  public String getRootOwner()  {
    return getConf().getRootOwner();
  }

  @Override
  public void setQueryLimit(final int val)  {
    getConf().setQueryLimit(val);
  }

  @Override
  public int getQueryLimit()  {
    return getConf().getQueryLimit();
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

  @Override
  public String schema() {
    try {
//      buildSchema = new SchemaThread();

      buildSchema.start();

      return "OK";
    } catch (final Throwable t) {
      error(t);

      return "Exception: " + t.getLocalizedMessage();
    }
  }

  @Override
  public synchronized List<String> schemaStatus() {
    return buildSchema.infoLines;
  }

  @Override
  public void setHibernateDialect(final String value) {
    getConf().setHibernateDialect(value);
  }

  @Override
  public String getHibernateDialect() {
    return getConf().getHibernateDialect();
  }

  @Override
  public String listHibernateProperties() {
    final StringBuilder res = new StringBuilder();

    final List<String> ps = getConf().getHibernateProperties();

    for (final String p: ps) {
      res.append(p);
      res.append("\n");
    }

    return res.toString();
  }

  @Override
  public String displayHibernateProperty(final String name) {
    final String val = getConf().getHibernateProperty(name);

    if (val != null) {
      return val;
    }

    return "Not found";
  }

  @Override
  public void removeHibernateProperty(final String name) {
    getConf().removeHibernateProperty(name);
  }

  @Override
  public void addHibernateProperty(final String name,
                                   final String value) {
    getConf().addHibernateProperty(name, value);
  }

  @Override
  public void setHibernateProperty(final String name,
                                   final String value) {
    getConf().setHibernateProperty(name, value);
  }

  /*
  @Override
  public List<String> getUserInfo(final String cua) {
    return new ArrayList<String>();
  }
  */

  /* ====================================================================
   *                   Non-mbean methods
   * ==================================================================== */

  /**
   * @return current state of config
   */
  @Override
  public DbDirHandlerConfig getConf() {
    return (DbDirHandlerConfig)super.getConf();
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private synchronized Configuration getConfiguration() {
    if (cfg == null) {
      try {
        cfg = new Configuration();

        final StringBuilder sb = new StringBuilder();

        final List<String> ps = getConf().getHibernateProperties();

        for (final String p: ps) {
          sb.append(p);
          sb.append("\n");
        }

        final Properties hprops = new Properties();
        hprops.load(new StringReader(sb.toString()));

        cfg.addProperties(hprops).configure();
      } catch (final Throwable t) {
        // Always bad.
        error(t);
      }
    }

    return cfg;
  }

  /**
   * @param val numeric
   * @return 2 digit val
   */
  private static String twoDigits(final long val) {
    if (val < 10) {
      return "0" + val;
    }

    return String.valueOf(val);
  }

  /* ========================================================================
   * Lifecycle
   * ======================================================================== */

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */
}
