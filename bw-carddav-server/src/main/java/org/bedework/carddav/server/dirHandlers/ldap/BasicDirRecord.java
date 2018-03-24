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
package org.bedework.carddav.server.dirHandlers.ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

/** This class represents a directory record which may be built from a
    number of attributes represented as a BasicAttributes object.
 */
public class BasicDirRecord extends DirRecord {
  private Attributes attrs;

  /** Create a record which can have values added.
   */
  public BasicDirRecord() {
  }

  /** Create a record with the given attributes.
   * @param attrs
   */
  public BasicDirRecord(Attributes attrs) {
    this.attrs = attrs;
  }

  public Attributes getAttributes() throws NamingException {
    if (attrs == null) attrs = new BasicAttributes(true);

    return attrs;
  }

  public void clear() {
    super.clear();
    attrs = null;
  }
}
