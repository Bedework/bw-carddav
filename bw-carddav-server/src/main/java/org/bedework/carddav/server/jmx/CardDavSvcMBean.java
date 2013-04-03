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



/** Boot up the carddav service and maintian the jmx management context. This is
 * a standard jboss mbean and does very little other than register the resto of
 * the service.
 *
 * @author douglm
 */
public interface CardDavSvcMBean {
  /* ========================================================================
   * Attributes
   * ======================================================================== */

  /** Path to the old (3.8 and earlier) options
   *
   * @param val
   */
  void setOptionsPath(String val);

  /** Path to the old (3.8 and earlier) options
   *
   * @return path
   */
  String getOptionsPath();

  /**
   * @param val
   */
  void setConfigDir(final String val);

  /**
   * @return String path to configs
   */
  @MBeanInfo("Directory for configuration files")
  String getConfigDir();

  /* ========================================================================
   * Operations
   * ======================================================================== */

  /* No special operations */

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
