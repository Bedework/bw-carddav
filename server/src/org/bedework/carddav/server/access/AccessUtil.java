/* **********************************************************************
    Copyright 2007 Rensselaer Polytechnic Institute. All worldwide rights reserved.

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
import edu.rpi.cct.webdav.servlet.shared.WebdavForbidden;
import edu.rpi.cmt.access.Access;
import edu.rpi.cmt.access.AccessPrincipal;
import edu.rpi.cmt.access.Ace;
import edu.rpi.cmt.access.AceWho;
import edu.rpi.cmt.access.Acl;
import edu.rpi.cmt.access.PrivilegeSet;
import edu.rpi.cmt.access.Acl.CurrentAccess;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.TreeSet;

/** An access helper class. This class makes some assumptions about the
 * classes it deals with but there are no explicit hibernate, or other
 * persistence engine, dependencies.
 *
 * <p>It assumes access to the parent object when needed,
 * continuing on up to the root. For systems which do not allow for a
 * retrieval of the parent on calls to the getCalendar method, the getParent
 * method for this class will need to be overridden. This would presumably
 * take place within the core implementation.
 *
 * @author Mike Douglass
 */
public class AccessUtil implements AccessUtilI {
  private boolean debug;

  /** For evaluating access control
   */
  private Access access;

  private boolean superUser;

  private User authUser;

  private CallBack cb;

  /* Null allows all accesses according to user - otherwise restricted to this. */
  private PrivilegeSet maxAllowedPrivs;

  private transient Logger log;

  /**
   * @param cb
   * @throws WebdavException
   */
  public void init(final CallBack cb) throws WebdavException {
    this.cb = cb;
    debug = getLog().isDebugEnabled();
    try {
      access = new Access();
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /** Indicate if we are in superuser mode.
   * @param val
   */
  public void setSuperUser(final boolean val) {
    superUser = val;
  }

  /**
   * @return boolean
   */
  public boolean getSuperUser() {
    return superUser;
  }

  /**
   * @param val
   */
  public void setMaximumAllowedPrivs(final PrivilegeSet val) {
    maxAllowedPrivs = val;
  }

  /** Set the current authenticated user.
   *
   * @param val
   */
  public void setAuthUser(final User val) {
    authUser = val;
  }

  /** Called at request start
   *
   */
  public void open() {
  }

  /** Called at request end
   *
   */
  public void close() {
    //pathInfoMap.flush();
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.server.access.AccessUtilI#getParent(org.bedework.carddav.server.access.SharedEntity)
   */
  public SharedEntity getParent(final SharedEntity val) throws WebdavException {
    if (val.getParentPath() == null) {
      return null;
    }

    return cb.getCollection(val.getParentPath());
  }

  /* ====================================================================
   *                   Access control
   * ==================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.calcorei.AccessUtilI#getDefaultPublicAccess()
   */
  public String getDefaultPublicAccess() {
    return Access.getDefaultPublicAccess();
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.AccessUtilI#getDefaultPersonalAccess()
   */
  public String getDefaultPersonalAccess() {
    return Access.getDefaultPersonalAccess();
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.AccessUtilI#changeAccess(org.bedework.calfacade.base.SharedEntity, java.util.Collection, boolean)
   */
  public void changeAccess(final SharedEntity ent,
                           final Collection<Ace> aces,
                           final boolean replaceAll) throws WebdavException {
    try {
      Acl acl = checkAccess(ent, privWriteAcl, false).getAcl();

      Collection<Ace> allAces;
      if (replaceAll) {
        allAces = aces;
      } else {
        allAces = acl.getAces();
        allAces.addAll(aces);
      }


      ent.setAccess(new Acl(allAces).encodeStr());

//      pathInfoMap.flush();
    } catch (WebdavException cfe) {
      throw cfe;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.AccessUtilI#defaultAccess(org.bedework.calfacade.base.SharedEntity, edu.rpi.cmt.access.AceWho)
   */
  public void defaultAccess(final SharedEntity ent,
                            final AceWho who) throws WebdavException {
    try {
      Acl acl = checkAccess(ent, privWriteAcl, false).getAcl();

      /* Now remove any access */

      if (acl.removeWho(who) != null) {
        ent.setAccess(acl.encodeStr());

//        pathInfoMap.flush();
      }
    } catch (WebdavException cfe) {
      throw cfe;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.AccessUtilI#checkAccess(java.util.Collection, int, boolean)
   */
  public Collection<? extends SharedEntity>
                checkAccess(final Collection<? extends SharedEntity> ents,
                                final int desiredAccess,
                                final boolean alwaysReturn)
          throws WebdavException {
    TreeSet<SharedEntity> out = new TreeSet<SharedEntity>();

    for (SharedEntity sdbe: ents) {
      if (checkAccess(sdbe, desiredAccess, alwaysReturn).getAccessAllowed()) {
        out.add(sdbe);
      }
    }

    return out;
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.AccessUtilI#checkAccess(org.bedework.calfacade.base.SharedEntity, int, boolean)
   */
  public CurrentAccess checkAccess(final SharedEntity ent,
                                   final int desiredAccess,
                        final boolean alwaysReturnResult) throws WebdavException {
    if (ent == null) {
      return null;
    }

    AccessState as = ent.getAccessState();

    if (as != null) {
      CurrentAccess ca = as.getCurrentAccess(desiredAccess);

      if (ca != null) {
        // Checked already

        if (!ca.getAccessAllowed() && !alwaysReturnResult) {
          throw new WebdavForbidden();
        }

        return ca;
      }
    }

    /*
    if (debug) {
      String cname = ent.getClass().getName();
      String ident;
      if (ent instanceof DbCollection) {
        ident = ((DbCollection)ent).getPath();
      } else {
        ident = String.valueOf(ent.getId());
      }
      getLog().debug("Check access for object " +
                     cname.substring(cname.lastIndexOf(".") + 1) +
                     " ident=" + ident +
                     " desiredAccess = " + desiredAccess);
    }
    */

    try {
      CurrentAccess ca = null;

      AccessPrincipal owner = cb.getPrincipal(ent.getOwnerHref());
      PrivilegeSet maxPrivs = null;

      char[] aclChars = null;

      if (ent.isCollection()) {
        String path = ent.getPath();

        /* Special case the access to the user root e.g /user and
         * the 'home' directory, e.g. /user/douglm
         */

        if (!getSuperUser()) {
          if (cb.getUserHomeRoot().equals(path)) {
            ca = new CurrentAccess();

            ca = Acl.defaultNonOwnerAccess;
          } else if (path.equals(cb.getUserHomeRoot() + owner.getAccount() + "/")){
            // Accessing user home directory
            // Set the maximumn access

            maxPrivs = PrivilegeSet.userHomeMaxPrivileges;
          }
        }
      }

      if (maxPrivs == null) {
        maxPrivs = maxAllowedPrivs;
      } else if (maxAllowedPrivs != null) {
        maxPrivs = PrivilegeSet.filterPrivileges(maxPrivs, maxAllowedPrivs);
      }

      if (ca == null) {
        /* Not special. getAclChars provides merged access for the current
         * entity.
         */
        aclChars = getAclChars(ent);

        if (debug) {
          getLog().debug("aclChars = " + new String(aclChars));
        }

        if (desiredAccess == privAny) {
          ca = access.checkAny(cb, authUser, owner, aclChars, maxPrivs);
        } else if (desiredAccess == privRead) {
          ca = access.checkRead(cb, authUser, owner, aclChars, maxPrivs);
        } else if (desiredAccess == privWrite) {
          ca = access.checkReadWrite(cb, authUser, owner, aclChars, maxPrivs);
        } else {
          ca = access.evaluateAccess(cb, authUser, owner, desiredAccess, aclChars,
                                     maxPrivs);
        }
      }

      if ((authUser != null) && superUser) {
        // Nobody can stop us - BWAAA HAA HAA

        /* Override rather than just create a readable access as code further
         * up expects a valid filled in object.
         */
        if (debug && !ca.getAccessAllowed()) {
          getLog().debug("Override for superuser");
        }
        ca = Acl.forceAccessAllowed(ca);
      }

      if (ent.isCollection()) {
        if (as == null) {
          as = new AccessState(ent);
          ent.setAccessState(as);
        }

        as.setCurrentAccess(ca, desiredAccess);
      }

      if (!ca.getAccessAllowed() && !alwaysReturnResult) {
        throw new WebdavForbidden();
      }

      return ca;
    } catch (WebdavException cfe) {
      throw cfe;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* ====================================================================
   *                   Private methods
   * ==================================================================== */

  /* If the entity is not a collection we merge the access in with the container
   * access then return the merged aces. We do this because we call getParentPathInfo
   * with a collection entity. That method will recurse up to the root.
   *
   * For a collection we just use the access for the collection itself.
   *
   * The collection access might be cached in the pathInfoTable.
   */
  private char[] getAclChars(final SharedEntity ent) throws WebdavException {
    SharedEntity container;

    if (ent.isCollection()) {
      container = ent;
    } else {
      container = getParent(ent);
    }

    String path = container.getPath();

    String aclStr;
    char[] aclChars = null;

    /* Get access for the parent first if we have one */
    SharedEntity parent = getParent(container);

    if (parent != null) {
      aclStr = new String(merged(getAclChars(parent),
                                 parent.getPath(),
                                 container.getAccess()));
    } else if (container.getAccess() != null) {
      aclStr = new String(container.getAccess());
    } else {
      // At root
      throw new WebdavException("Collections must have default access set at root");
    }

    if (aclStr != null) {
      aclChars = aclStr.toCharArray();
    }

    if (ent.isCollection()) {
      return aclChars;
    }

    /* Create a merged access string from the entity access and the
     * container access
     */

    return merged(aclChars, path, ent.getAccess());
  }

  private char[] merged(final char[] parentAccess,
                        final String path,
                        final String access) throws WebdavException {
    try {
      Acl acl = null;

      if (access != null) {
        acl = Acl.decode(access.toCharArray());
      }

      if (acl == null) {
        acl = Acl.decode(parentAccess, path);
      } else {
        acl = acl.merge(parentAccess, path);
      }

      return acl.encodeAll();
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private Logger getLog() {
    if (log == null) {
      log = Logger.getLogger(getClass());
    }

    return log;
  }

//  private void warn(String msg) {
//    getLog().warn(msg);
//  }
}

