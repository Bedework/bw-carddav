/* **********************************************************************
    Copyright 2006 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
package org.bedework.carddav.server.access;

import org.bedework.carddav.util.User;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.Ace;
import edu.rpi.cmt.access.AceWho;
import edu.rpi.cmt.access.PrivilegeDefs;
import edu.rpi.cmt.access.Access.AccessCb;
import edu.rpi.cmt.access.Acl.CurrentAccess;

import java.io.Serializable;
import java.util.Collection;

/** An access helper interface. This interface makes some assumptions about the
 * classes it deals with but there is no explicit hibernate, or other
 * persistence engine, dependencies.
 *
 * <p>It assumes that it has access to the parent object when needed,
 * continuing on up to the root. For systems which do not allow for a
 * retrieval of the parent on calls to the getCalendar method, the getParent
 * method for this class will need to be overridden. This would presumably
 * take place within the core implementation.
 *
 *
 * @author Mike Douglass   douglm  rpi.edu
 */
public interface AccessUtilI extends PrivilegeDefs, Serializable {
  /** Methods called to obtain system information.
   *
   */
  public static abstract class CallBack implements AccessCb, Serializable {
    /**
     * @param href
     * @return AccessPrincipal or null for not valid
     * @throws WebdavException
     */
    public abstract AccessPrincipal getPrincipal(String href) throws WebdavException;

    /** Returns something like "/user/". This provides what is considered to be
     * the root of the file system for which we are evaluating access.
     *
     * @return String root - must be prefixed and suffixed with "/"
     * @throws WebdavException
     */
    public abstract String getUserHomeRoot() throws WebdavException;

    /** Get a collection given the path. No access checks are performed.
     *
     * @param  path          String path of collection
     * @return SharedEntity null for unknown collection
     * @throws WebdavException
     */
    public abstract SharedEntity getCollection(String path) throws WebdavException;
  }

  /**
   *
   * @param cb
   * @throws WebdavException
   */
  public void init(CallBack cb) throws WebdavException;

  /** Indicate if we are in superuser mode.
   * @param val
   */
  public void setSuperUser(boolean val);

  /**
   * @return boolean
   */
  public boolean getSuperUser();

  /** Set the current authenticated user.
   *
   * @param val
   */
  public void setAuthUser(User val);

  /** Called at request start
   *
   */
  public void open();

  /** Called at request end
   *
   */
  public void close();

  /** Called to get the parent object for a shared entity.
   *
   * @param val
   * @return parent calendar or null.
   * @throws WebdavException
   */
  public SharedEntity getParent(SharedEntity val) throws WebdavException;

  /* ====================================================================
   *                   Access control
   * ==================================================================== */

  /** Get the default public access
   *
   * @return String value for default access
   */
  public String getDefaultPublicAccess();

  /**
   *
   * @return String default user access
   */
  public String getDefaultPersonalAccess();

  /** Change the access to the given calendar entity using the supplied aces.
   * We are changing access so we remove all access for each who in the list and
   * then add the new aces.
   *
   * @param ent        DbEntity
   * @param aces       Collection of ace objects
   * @param replaceAll true to replace the entire access list.
   * @throws WebdavException
   */
  public void changeAccess(SharedEntity ent,
                           Collection<Ace> aces,
                           boolean replaceAll) throws WebdavException;

  /** Remove any explicit access for the given who to the given calendar entity.
  *
  * @param ent      DbEntity
  * @param who      AceWho
  * @throws WebdavException
  */
 public void defaultAccess(SharedEntity ent,
                           AceWho who) throws WebdavException;

  /** Return a Collection of the objects after checking access
   *
   * @param ents          Collection of DbEntity
   * @param desiredAccess access we want
   * @param alwaysReturn boolean flag behaviour on no access
   * @return Collection   of checked objects
   * @throws WebdavException for no access or other failure
   */
  public Collection<? extends SharedEntity>
                 checkAccess(Collection<? extends SharedEntity> ents,
                                int desiredAccess,
                                boolean alwaysReturn)
          throws WebdavException;

  /** Check access for the given entity. Returns the current access
   *
   * <p>We special case the access to the user root e.g /user and the home
   * directory, e.g. /user/douglm
   *
   * We deny access to /user to anybody without superuser access. This
   * prevents user browsing. This could be made a system property if the
   * organization wants user browsing.
   *
   * Default access to the home directory is read, write-content to the owner
   * only and unlimited to superuser.
   *
   * Specific access should be no more than read, write-content to the home
   * directory.
   *
   * @param ent
   * @param desiredAccess
   * @param alwaysReturnResult
   * @return  CurrentAccess
   * @throws WebdavException
   */
  public CurrentAccess checkAccess(SharedEntity ent, int desiredAccess,
                        boolean alwaysReturnResult) throws WebdavException;
}
