/* **********************************************************************
    Copyright 2008 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
package org.bedework.carddav.util;

/** Class defining the configuration for a directory handler.
 *
 * @author douglm
 */
public class DirHandlerConfig {
  private String pathPrefix;

  private boolean addressBook;

  private String className;

  private boolean debug;

  /** Set the pathPrefix which defines the paths for which we call this handler.
   *
   * <p>For example we might put the enterprise directory on a path like
   * <pre>
   *   /public/rpi.edu
   * <pre>
   * while we put users personal directories on a path like
   * <pre>
   *   /user/
   * <pre>
   *
   * <p>This allows us to register directories for multiple systems.
   *
   * @param val    String path
   */
  public void setPathPrefix(String val) {
    pathPrefix = val;
  }

  /** Get the pathPrefix
   *
   * @return String   path
   */
  public String getPathPrefix() {
    return pathPrefix;
  }

  /** True if this prefix represents an addressbook. Only required if we have no
   * way of adding objectClasses or attributes to the directory itself.
   *
   * @param val
   */
  public void setAddressBook(boolean val)  {
    addressBook = val;
  }

  /** Is debugging on?
   *
   * @return boolean val
   */
  public boolean getAddressBook()  {
    return addressBook;
  }

  /** Set the interface implementation
  *
  * @param val    String
  */
 public void setClassName(String val) {
   className = val;
 }

 /** get the interface implementation
  *
  * @return String
  */
 public String getClassName() {
   return className;
 }

 /**
  * @param val
  */
 public void setDebug(boolean val)  {
   debug = val;
 }

 /** Is debugging on?
  *
  * @return boolean val
  */
 public boolean getDebug()  {
   return debug;
 }
}
