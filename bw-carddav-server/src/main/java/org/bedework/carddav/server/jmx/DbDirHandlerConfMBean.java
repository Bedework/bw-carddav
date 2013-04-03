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

import org.apache.activemq.broker.jmx.MBeanInfo;

import java.util.List;

/** Configure a carddav service dir handler
 *
 * @author douglm
 */
public interface DbDirHandlerConfMBean extends DirHandlerConfMBean {
  /* ========================================================================
   * Attributes
   * ======================================================================== */

  /**
   *
   * @param val
   */
  public void setRootAccess(final String val);

  /**
   * @return String
   */
  @MBeanInfo("This is the access set at the root. Needs to be an XML value")
  public String getRootAccess();

  /**
   *
   * @param val
   */
  public void setRootOwner(final String val);

  /**
   * @return String
   */
  @MBeanInfo("This is the owner of the user root and user home")
  public String getRootOwner();

  /** Set the query limit - 0 for no limit
   *
   * @param val
   */
  public void setQueryLimit(final int val);

  /**
   *
   * @return int val
   */
  @MBeanInfo("Max number of entries returned")
  public int getQueryLimit();

  /** Do we drop tables?
   *
   * @param val
   */
  public void setDrop(boolean val);

  /**
   * @return true for drop tables
   */
  @MBeanInfo("Drop tables on export (write) schema to database?")
  public boolean getDrop();

  /** Export schema to database?
   *
   * @param val
   */
  public void setExport(boolean val);

  /**
   * @return true for export schema
   */
  @MBeanInfo("Export (write) schema to database?")
  public boolean getExport();

  /** Output file name - full path
   *
   * @param val
   */
  public void setSchemaOutFile(String val);

  /**
   * @return Output file name - full path
   */
  @MBeanInfo("Full path of schema output file")
  public String getSchemaOutFile();

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /** Create or dump new schema. If export and drop set will try to drop tables.
   * Export and create will create a schema in the db and export, drop, create
   * will drop tables, and try to create a new schema.
   *
   * The export and drop flags will all be reset to false after this,
   * whatever the result. This avoids accidental damage to the db.
   *
   * @return Completion message
   */
  @MBeanInfo("Start build of the database schema. Set export flag to write to db.")
  public String schema();

  /** Returns status of the schema build.
   *
   * @return Completion messages
   */
  @MBeanInfo("Status of the database schema build.")
  public List<String> schemaStatus();

  /** List the hibernate properties
   *
   * @return properties
   */
  @MBeanInfo("List the hibernate properties")
  String listHibernateProperties();

  /** Display the named property
   *
   * @param name
   * @return value
   */
  @MBeanInfo("Display the named hibernate property")
  String displayHibernateProperty(@MBeanInfo("name") final String name);

  /** Remove the named property
   *
   * @param name
   */
  @MBeanInfo("Remove the named hibernate property")
  void removeHibernateProperty(@MBeanInfo("name") final String name);

  /**
   * @param name
   * @param value
   */
  @MBeanInfo("Add a hibernate property")
  void addHibernateProperty(@MBeanInfo("name") final String name,
                              @MBeanInfo("value") final String value);

  /**
   * @param name
   * @param value
   */
  @MBeanInfo("Set a hibernate property")
  void setHibernateProperty(@MBeanInfo("name") final String name,
                            @MBeanInfo("value") final String value);

  /* * Get info for a user
   *
   * @param cua
   * @return List of info lines
   * /
  @MBeanInfo("Return information for the given principal or cua")
  List<String> getUserInfo(@MBeanInfo("cua") String cua);
  */
}
