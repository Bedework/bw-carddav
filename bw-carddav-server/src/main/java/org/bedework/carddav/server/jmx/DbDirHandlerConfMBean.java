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

import org.bedework.util.jmx.MBeanInfo;

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
   * @param val access set at the root. Needs to be an XML value
   */
  public void setRootAccess(final String val);

  /**
   * @return String
   */
  @MBeanInfo("This is the access set at the root. Needs to be an XML value")
  public String getRootAccess();

  /**
   *
   * @param val owner of the user root and user home
   */
  public void setRootOwner(final String val);

  /**
   * @return  owner of the user root and user home
   */
  @MBeanInfo("This is the owner of the user root and user home")
  public String getRootOwner();

  /** Set the query limit - 0 for no limit
   *
   * @param val Max number of entries returned
   */
  public void setQueryLimit(final int val);

  /**
   *
   * @return int Max number of entries returned
   */
  @MBeanInfo("Max number of entries returned")
  public int getQueryLimit();

  /** Export schema to database?
   *
   * @param val true to export
   */
  public void setExport(boolean val);

  /**
   * @return true for export schema
   */
  @MBeanInfo("Export (write) schema to database?")
  public boolean getExport();

  /** Output file name - full path
   *
   * @param val Output file name - full path
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

  /**
   * @param value the hibernate dialect
   */
  @MBeanInfo("Set the hibernate dialect")
  void setHibernateDialect(@MBeanInfo("value: a valid hibernate dialect class") final String value);

  /**
   * @return Completion messages
   */
  @MBeanInfo("Get the hibernate dialect")
  String getHibernateDialect();

  /** List the hibernate properties
   *
   * @return properties
   */
  @MBeanInfo("List the hibernate properties")
  String listHibernateProperties();

  /** Display the named property
   *
   * @param name of property
   * @return value
   */
  @MBeanInfo("Display the named hibernate property")
  String displayHibernateProperty(@MBeanInfo("name") final String name);

  /** Remove the named property
   *
   * @param name of property
   */
  @MBeanInfo("Remove the named hibernate property")
  void removeHibernateProperty(@MBeanInfo("name") final String name);

  /**
   * @param name of property
   * @param value of property
   */
  @MBeanInfo("Add a hibernate property")
  void addHibernateProperty(@MBeanInfo("name") final String name,
                            @MBeanInfo("value") final String value);

  /**
   * @param name of property
   * @param value of property
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
