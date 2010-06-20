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

import java.util.List;

/** Run the dump/restore and/or the schema initialization as an mbean
 *
 * @author douglm
 */
public interface BwCardDumpRestoreMBean {
  /* ========================================================================
   * Attributes
   * ======================================================================== */

  /** Name apparently must be the same as the name attribute in the
   * jboss service definition
   *
   * @return Name
   */
  public String getName();

  /** Account we run under
   *
   * @param val
   */
  public void setAccount(String val);

  /**
   * @return String account we use
   */
  public String getAccount();

  /** Application name - for config info
   *
   * @param val
   */
  public void setAppname(String val);

  /**
   * @return String application namee
   */
  public String getAppname();

  /** Do we create tables?
   *
   * @param val
   */
  public void setCreate(boolean val);

  /**
   * @return true for create tables
   */
  public boolean getCreate();

  /** Statement delimiter
   *
   * @param val
   */
  public void setDelimiter(String val);

  /**
   * @return Statement delimiter
   */
  public String getDelimiter();

  /** Do we drop tables?
   *
   * @param val
   */
  public void setDrop(boolean val);

  /**
   * @return true for drop tables
   */
  public boolean getDrop();

  /** Export to database?
   *
   * @param val
   */
  public void setExport(boolean val);

  /**
   * @return true for export
   */
  public boolean getExport();

  /** Format the output?
   *
   * @param val
   */
  public void setFormat(boolean val);

  /**
   * @return true for Format the output
   */
  public boolean getFormat();

  /** Do we halt on error?
   *
   * @param val
   */
  public void setHaltOnError(boolean val);

  /**
   * @return true for halt on error
   */
  public boolean getHaltOnError();

  /** Output file name - full path
   *
   * @param val
   */
  public void setSchemaOutFile(String val);

  /**
   * @return Output file name - full path
   */
  public String getSchemaOutFile();

  /** SQL input file name - full path. Used instead of the configuration?
   *
   * @param val
   */
  public void setSqlIn(String val);

  /**
   * @return SQL input file name - full path
   */
  public String getSqlIn();

  /** XML data input file name - full path. Used for data restore
   *
   * @param val
   */
  public void setDataIn(String val);

  /**
   * @return XML data input file name - full path
   */
  public String getDataIn();

  /** XML data output directory name - full path. Used for data restore
   *
   * @param val
   */
  public void setDataOut(String val);

  /**
   * @return XML data output directory name - full path
   */
  public String getDataOut();

  /** XML data output file prefix - for data dump
   *
   * @param val
   */
  public void setDataOutPrefix(String val);

  /**
   * @return XML data output file prefix - for data dump
   */
  public String getDataOutPrefix();

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /** Return true if the schema appears to be valid
   *
   * @return true if it looks ok to us
   */
  public boolean testSchemaValid();

  /** Create or dump new schema. If export and drop set will try to drop tables.
   * Export and create will create a schema in the db and export, drop, create
   * will drop tables, and try to create  anew schema.
   *
   * The export, create and drop flags will all be reset to false after this,
   * whatever the result. This avoids accidental damage to the db.
   *
   * @return Completion message
   */
  public String schema();

  /** Restores the data from the DataIn path. Will not restore if there appears
   * to be any data already in the db.
   *
   * @return Completion messages and stats
   */
  public List<String> restoreData();

  /** Dumps the data to a file in the DataOut directory.
   *
   * @return Completion messages and stats
   */
  public List<String> dumpData();

  /** Try to drop all the tables. May get errors for a partial db or for an updated
   * db.
   *
   * @return Completion message
   */
  public String dropTables();

  /* ========================================================================
   * Lifecycle
   * ======================================================================== */

  /** Lifecycle
   *
   */
  public void create();

  /** Lifecycle
   *
   */
  public void start();

  /** Lifecycle
   *
   */
  public void stop();

  /** Lifecycle
   *
   * @return true if started
   */
  public boolean isStarted();

  /** Lifecycle
   *
   */
  public void destroy();
}
