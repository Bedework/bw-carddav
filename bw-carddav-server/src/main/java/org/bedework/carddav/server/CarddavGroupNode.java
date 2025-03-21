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
package org.bedework.carddav.server;

import org.bedework.access.AccessPrincipal;

/** Class to represent a group in caldav.
 *
 *
 *   @author Mike Douglass   douglm  rpi.edu
 */
public class CarddavGroupNode extends CarddavPrincipalNode {
  /**
   * @param cdURI referencing resource
   * @param sysi system interface
   * @param ap principal
   */
  public CarddavGroupNode(final CarddavURI cdURI, final SysIntf sysi,
                          final AccessPrincipal ap) {
    super(cdURI, sysi, ap);
  }
}
