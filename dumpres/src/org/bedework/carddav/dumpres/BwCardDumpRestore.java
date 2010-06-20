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
package org.bedework.carddav.dumpres;

import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import java.util.ArrayList;
import java.util.List;

/**
 * @author douglm
 *
 */
public class BwCardDumpRestore implements BwCardDumpRestoreMBean {
  private transient Logger log;

  private boolean started;

  private String account;

  private String appname;

  private boolean create;

  private String delimiter;

  private boolean drop;

  /* Be safe - default to false */
  private boolean export;

  private boolean format;

  private boolean haltOnError;

  private String schemaOutFile;

  private String sqlIn;

  private String dataIn;

  private String dataOut;

  private String dataOutPrefix;

  private Configuration cfg;

  /* ========================================================================
   * Attributes
   * ======================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.dumprestore.BwDumpRestoreMBean#getName()
   */
  public String getName() {
    /* This apparently must be the same as the name attribute in the
     * jboss service definition
     */
    return "org.bedework:service=CardDumpRestore";
  }

  /* (non-Javadoc)
   * @see org.bedework.dumprestore.BwDumpRestoreMBean#setAccount(java.lang.String)
   */
  public void setAccount(final String val) {
    account = val;
  }

  /* (non-Javadoc)
   * @see org.bedework.dumprestore.BwDumpRestoreMBean#getAccount()
   */
  public String getAccount() {
    return account;
  }

  public void setAppname(final String val) {
    appname = val;
  }

  public String getAppname() {
    return appname;
  }

  public void setCreate(final boolean val) {
    create = val;
  }

  public boolean getCreate() {
    return create;
  }

  public void setDelimiter(final String val) {
    delimiter = val;
  }

  public String getDelimiter() {
    return delimiter;
  }

  public void setDrop(final boolean val) {
    drop = val;
  }

  public boolean getDrop() {
    return drop;
  }

  public void setExport(final boolean val) {
    export = val;
  }

  public boolean getExport() {
    return export;
  }

  /** Format the output?
   *
   * @param val
   */
  public void setFormat(final boolean val) {
    format = val;
  }

  public boolean getFormat() {
    return format;
  }

  public void setHaltOnError(final boolean val) {
    haltOnError = val;
  }

  public boolean getHaltOnError() {
    return haltOnError;
  }

  public void setSchemaOutFile(final String val) {
    schemaOutFile = val;
  }

  public String getSchemaOutFile() {
    return schemaOutFile;
  }

  public void setSqlIn(final String val) {
    sqlIn = val;
  }

  public String getSqlIn() {
    return sqlIn;
  }

  public void setDataIn(final String val) {
    dataIn = val;
  }

  public String getDataIn() {
    return dataIn;
  }

  public void setDataOut(final String val) {
    dataOut = val;
  }

  public String getDataOut() {
    return dataOut;
  }

  public void setDataOutPrefix(final String val) {
    dataOutPrefix = val;
  }

  public String getDataOutPrefix() {
    return dataOutPrefix;
  }

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /* an example say's we need this  - we should probably implement some system
   * independent jmx support which will build this using introspection and/or lists
  public MBeanInfo getMBeanInfo() throws Exception {
    InitialContext ic = new InitialContext();
    RMIAdaptor server = (RMIAdaptor) ic.lookup("jmx/rmi/RMIAdaptor");

    ObjectName name = new ObjectName(MBEAN_OBJ_NAME);

    // Get the MBeanInfo for this MBean
    MBeanInfo info = server.getMBeanInfo(name);
    return info;
  }
  */

  public boolean testSchemaValid() {
    return true;
  }

  /* (non-Javadoc)
   * @see org.bedework.dumprestore.BwDumpRestoreMBean#schema()
   */
  public String schema() {
    String result = "Export complete: check logs";

    try {
      SchemaExport se = new SchemaExport(getConfiguration());

      if (getDelimiter() != null) {
        se.setDelimiter(getDelimiter());
      }

      se.setFormat(getFormat());
      se.setHaltOnError(getHaltOnError());
      se.setOutputFile(getSchemaOutFile());
      se.setImportFile(getSqlIn());

      se.execute(false, // script - causes write to System.out if true
                 getExport(),
                 getDrop(),
                 getCreate());
    } catch (Throwable t) {
      error(t);
      result = "Exception: " + t.getLocalizedMessage();
    } finally {
      create = false;
      drop = false;
      export = false;
    }

    return result;
  }

  public synchronized List<String> restoreData() {
    List<String> infoLines = new ArrayList<String>();

    try {
      /*
      if (!disableIndexer()) {
        infoLines.add("***********************************\n");
        infoLines.add("********* Unable to disable indexer\n");
        infoLines.add("***********************************\n");
      }

      long startTime = System.currentTimeMillis();

      Restore r = new Restore(getConfiguration());

      String[] args = new String[] {"-appname",
                                    appname
      };

      r.getConfigProperties(new Args(args));

      r.setFilename(getDataIn());

      r.setNoIndexes(true);

      r.open();

      r.doRestore();

      r.close();

      r.stats(infoLines);

      long millis = System.currentTimeMillis() - startTime;
      long seconds = millis / 1000;
      long minutes = seconds / 60;
      seconds -= (minutes * 60);

      infoLines.add("Elapsed time: " + minutes + ":" +
                    Restore.twoDigits(seconds) + "\n");

      infoLines.add("Restore complete" + "\n");
      */
      infoLines.add("************************Restore unimplemented *************************" + "\n");
    } catch (Throwable t) {
      error(t);
      infoLines.add("Exception - check logs: " + t.getMessage() + "\n");
    } finally {
      /*
      try {
        if (!reindex()) {
          infoLines.add("***********************************");
          infoLines.add("********* Unable to disable indexer");
          infoLines.add("***********************************");
        }
      } catch (Throwable t) {
        error(t);
        infoLines.add("Exception - check logs: " + t.getMessage() + "\n");
      }
        */
    }

    return infoLines;
  }

  public List<String> dumpData() {
    List<String> infoLines = new ArrayList<String>();

    try {
      /*
      long startTime = System.currentTimeMillis();

      Dump d = new Dump(getConfiguration());

      String[] args = new String[] {"-appname",
                                    appname
      };

      d.getConfigProperties(args);

      StringBuilder fname = new StringBuilder(getDataOut());
      if (!getDataOut().endsWith("/")) {
        fname.append("/");
      }

      fname.append(getDataOutPrefix());

      /* append "yyyyMMddTHHmmss" * /
      fname.append(DateTimeUtil.isoDateTime());
      fname.append(".xml");

      d.setFilename(fname.toString());

      d.open();

      d.doDump();

      d.close();

      d.stats(infoLines);

      long millis = System.currentTimeMillis() - startTime;
      long seconds = millis / 1000;
      long minutes = seconds / 60;
      seconds -= (minutes * 60);

      infoLines.add("Elapsed time: " + minutes + ":" +
                    Restore.twoDigits(seconds) + "\n");

      infoLines.add("Dump complete" + "\n");
      */
      infoLines.add("************************Dump unimplemented *************************" + "\n");
    } catch (Throwable t) {
      error(t);
      infoLines.add("Exception - check logs: " + t.getMessage() + "\n");
    }

    return infoLines;
  }

  public String dropTables() {
    return "Not implemented";
  }

  /* ========================================================================
   * Lifecycle
   * ======================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.dumprestore.BwDumpRestoreMBean#create()
   */
  public void create() {
    // An opportunity to initialise
  }

  /* (non-Javadoc)
   * @see org.bedework.indexer.BwIndexerMBean#start()
   */
  public void start() {
    started = true;
  }

  /* (non-Javadoc)
   * @see org.bedework.indexer.BwIndexerMBean#stop()
   */
  public void stop() {
    started = false;
  }

  /* (non-Javadoc)
   * @see org.bedework.indexer.BwIndexerMBean#isStarted()
   */
  public boolean isStarted() {
    return started;
  }

  /* (non-Javadoc)
   * @see org.bedework.dumprestore.BwDumpRestoreMBean#destroy()
   */
  public void destroy() {
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  private synchronized Configuration getConfiguration() {
    if (cfg == null) {
      cfg = new Configuration().configure();
    }

    return cfg;
  }

  /*
  private BwIndexerMBean indexer;

  private boolean disableIndexer() throws CalFacadeException {
    try {
      if (indexer == null) {
        indexer = (BwIndexerMBean)MBeanUtil.getMBean(BwIndexerMBean.class,
                                                     "org.bedework:service=Indexer");
      }

      indexer.setDiscardMessages(true);
      return true;
    } catch (Throwable t) {
      error(t);
      return false;
    }
  }

  private boolean reindex() throws CalFacadeException {
    try {
      if (indexer == null) {
        return false;
      }
      indexer.rebuildIndex();
      indexer.setDiscardMessages(false);
      return true;
    } catch (Throwable t) {
      error(t);
      return false;
    }
  }
  */

  /* ====================================================================
   *                   Protected methods
   * ==================================================================== */

  protected void info(final String msg) {
    getLogger().info(msg);
  }

  protected void trace(final String msg) {
    getLogger().debug(msg);
  }

  protected void error(final Throwable t) {
    getLogger().error(t);
  }

  protected void error(final String msg) {
    getLogger().error(msg);
  }

  /* Get a logger for messages
   */
  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }
}
