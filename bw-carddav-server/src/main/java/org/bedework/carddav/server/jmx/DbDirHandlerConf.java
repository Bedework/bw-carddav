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
import org.bedework.util.hibernate.HibConfig;
import org.bedework.util.hibernate.SchemaThread;

import java.util.List;
import java.util.Properties;

/**
 * @author douglm
 *
 */
public class DbDirHandlerConf extends DirHandlerConf implements DbDirHandlerConfMBean {
  /* Be safe - default to false */
  private boolean export;

  private String schemaOutFile;

  private class SchemaBuilder extends SchemaThread {

    SchemaBuilder(final String outFile,
                  final boolean export,
                  final Properties hibConfig) {
      super(outFile, export, hibConfig);
    }

    @Override
    public void completed(final String status) {
      setExport(false);
      info("Schema build completed with status " + status);
    }
  }

  private SchemaBuilder buildSchema;

  /* ========================================================================
   * Schema attributes
   * ======================================================================== */

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
      final HibConfig hc = new HibConfig(getConfig());

      buildSchema = new SchemaBuilder(getSchemaOutFile(),
                                      getExport(),
                                      hc.getHibConfiguration().getProperties());

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

}
