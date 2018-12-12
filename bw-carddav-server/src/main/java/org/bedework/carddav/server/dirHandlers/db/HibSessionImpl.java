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
package org.bedework.carddav.server.dirHandlers.db;

import org.bedework.util.logging.Logged;
import org.bedework.webdav.servlet.shared.WebdavException;

import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StaleStateException;
import org.hibernate.Transaction;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/** Convenience class to do the actual hibernate interaction. Intended for
 * one use only.
 *
 * @author Mike Douglass douglm     rpi.edu
 */
public class HibSessionImpl implements HibSession, Logged {
  Session sess;
  transient Transaction tx;
  boolean rolledBack;

  transient Query q;
  transient Criteria crit;

  /** Exception from this session. */
  Throwable exc;

  private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

  /** Set up for a hibernate interaction. Throw the object away on exception.
   *
   * @param sessFactory
   * @throws WebdavException
   */
  public void init(final SessionFactory sessFactory) throws WebdavException {
    try {
      sess = sessFactory.openSession();
      rolledBack = false;
      //sess.setFlushMode(FlushMode.COMMIT);
//      tx = sess.beginTransaction();
    } catch (Throwable t) {
      exc = t;
      tx = null;  // not even started. Should be null anyway
      close();
    }
  }

  public Session getSession() throws WebdavException {
    return sess;
  }

  /**
   * @return boolean true if open
   * @throws WebdavException
   */
  public boolean isOpen() throws WebdavException {
    try {
      if (sess == null) {
        return false;
      }
      return sess.isOpen();
    } catch (Throwable t) {
      handleException(t);
      return false;
    }
  }

  /** Clear a session
   *
   * @throws WebdavException
   */
  public void clear() throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      sess.clear();
      tx =  null;
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Disconnect a session
   *
   * @throws WebdavException
   */
  public void disconnect() throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      if (exc instanceof WebdavException) {
        throw (WebdavException)exc;
      }
      throw new WebdavException(exc);
    }

    try {
      sess.disconnect();
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** set the flushmode
   *
   * @param val
   * @throws WebdavException
   */
  public void setFlushMode(final FlushMode val) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      if (tx != null) {
        throw new WebdavException("Transaction already started");
      }

      sess.setFlushMode(val);
    } catch (Throwable t) {
      exc = t;
      throw new WebdavException(t);
    }
  }

  /** Begin a transaction
   *
   * @throws WebdavException
   */
  public void beginTransaction() throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      if (tx != null) {
        throw new WebdavException("Transaction already started");
      }

      tx = sess.beginTransaction();
      rolledBack = false;
      if (tx == null) {
        throw new WebdavException("Transaction not started");
      }
    } catch (WebdavException cfe) {
      exc = cfe;
      throw cfe;
    } catch (Throwable t) {
      exc = t;
      throw new WebdavException(t);
    }
  }

  /** Return true if we have a transaction started
   *
   * @return boolean
   */
  public boolean transactionStarted() {
    return tx != null;
  }

  /** Commit a transaction
   *
   * @throws WebdavException
   */
  public void commit() throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
//      if (tx != null &&
//          !tx.wasCommitted() &&
//          !tx.wasRolledBack()) {
        //if (getLogger().isDebugEnabled()) {
        //  getLogger().debug("About to comnmit");
        //}
      if (tx != null) {
        tx.commit();
      }

      tx = null;
    } catch (Throwable t) {
      exc = t;

      if (t instanceof StaleStateException) {
        throw new DbStaleStateException(t.getMessage());
      }
      throw new WebdavException(t);
    }
  }

  /** Rollback a transaction
   *
   * @throws WebdavException
   */
  public void rollback() throws WebdavException {
/*    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }
*/
    if (debug()) {
      debug("Enter rollback");
    }
    try {
      if ((tx != null) &&
          !rolledBack) {
        if (debug()) {
          debug("About to rollback");
        }
        tx.rollback();
        //tx = null;
        clear();
      }
    } catch (Throwable t) {
      exc = t;
      throw new WebdavException(t);
    } finally {
      rolledBack = true;
    }
  }

  public boolean rolledback() throws WebdavException {
    return rolledBack;
  }

  /** Create a Criteria ready for the additon of Criterion.
   *
   * @param cl           Class for criteria
   * @return Criteria    created Criteria
   * @throws WebdavException
   */
  public Criteria createCriteria(final Class cl) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      crit = sess.createCriteria(cl);
      q = null;

      return crit;
    } catch (Throwable t) {
      handleException(t);
      return null;  // Don't get here
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.HibSession#evict(java.lang.Object)
   */
  public void evict(final Object val) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      sess.evict(val);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.HibSession#createQuery(java.lang.String)
   */
  public void createQuery(final String s) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q = sess.createQuery(s);
      crit = null;
    } catch (Throwable t) {
      handleException(t);
    }
  }

  public void createNoFlushQuery(final String s) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q = sess.createQuery(s);
      crit = null;
      q.setFlushMode(FlushMode.COMMIT);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.HibSession#getQueryString()
   */
  public String getQueryString() throws WebdavException {
    if (q == null) {
      return "*** no query ***";
    }

    try {
      return q.getQueryString();
    } catch (Throwable t) {
      handleException(t);
      return null;
    }
  }

  /** Create a sql query ready for parameter replacement or execution.
   *
   * @param s             String hibernate query
   * @param returnAlias
   * @param returnClass
   * @throws WebdavException
   */
  public void createSQLQuery(final String s, final String returnAlias, final Class returnClass)
        throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      SQLQuery sq = sess.createSQLQuery(s);
      sq.addEntity(returnAlias, returnClass);

      q = sq;
      crit = null;
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Create a named query ready for parameter replacement or execution.
   *
   * @param name         String named query name
   * @throws WebdavException
   */
  public void namedQuery(final String name) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q = sess.getNamedQuery(name);
      crit = null;
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Mark the query as cacheable
   *
   * @throws WebdavException
   */
  public void cacheableQuery() throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q.setCacheable(true);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      String parameter value
   * @throws WebdavException
   */
  public void setString(final String parName, final String parVal) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q.setString(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      Date parameter value
   * @throws WebdavException
   */
  public void setDate(final String parName, final Date parVal) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      // Remove any time component
      synchronized (dateFormatter) {
        q.setDate(parName, java.sql.Date.valueOf(dateFormatter.format(parVal)));
      }
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      boolean parameter value
   * @throws WebdavException
   */
  public void setBool(final String parName, final boolean parVal) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q.setBoolean(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      int parameter value
   * @throws WebdavException
   */
  public void setInt(final String parName, final int parVal) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q.setInteger(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      long parameter value
   * @throws WebdavException
   */
  public void setLong(final String parName, final long parVal) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q.setLong(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      Object parameter value
   * @throws WebdavException
   */
  public void setEntity(final String parName, final Object parVal) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q.setEntity(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.HibSession#setParameter(java.lang.String, java.lang.Object)
   */
  public void setParameter(final String parName, final Object parVal) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q.setParameter(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.HibSession#setParameterList(java.lang.String, java.util.Collection)
   */
  public void setParameterList(final String parName, final Collection parVal) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q.setParameterList(parName, parVal);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.HibSession#setFirstResult(int)
   */
  public void setFirstResult(final int val) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q.setFirstResult(val);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.HibSession#setMaxResults(int)
   */
  public void setMaxResults(final int val) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      q.setMaxResults(val);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.HibSession#getUnique()
   */
  public Object getUnique() throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      if (q != null) {
        return q.uniqueResult();
      }

      return crit.uniqueResult();
    } catch (Throwable t) {
      handleException(t);
      return null;  // Don't get here
    }
  }

  /** Return a list resulting from the query.
   *
   * @return List          list from query
   * @throws WebdavException
   */
  public List getList() throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      List l;
      if (q != null) {
        l = q.list();
      } else {
        l = crit.list();
      }

      if (l == null) {
        return new ArrayList();
      }

      return l;
    } catch (Throwable t) {
      handleException(t);
      return null;  // Don't get here
    }
  }

  /**
   * @return int number updated
   * @throws WebdavException
   */
  public int executeUpdate() throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      if (q == null) {
        throw new WebdavException("No query for execute update");
      }

      return q.executeUpdate();
    } catch (Throwable t) {
      handleException(t);
      return 0;  // Don't get here
    }
  }

  /** Update an object which may have been loaded in a previous hibernate
   * session
   *
   * @param obj
   * @throws WebdavException
   */
  public void update(final Object obj) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      beforeSave(obj);
      sess.update(obj);
      deleteSubs(obj);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Merge and update an object which may have been loaded in a previous hibernate
   * session
   *
   * @param obj
   * @return Object   the persistent object
   * @throws WebdavException
   */
  public Object merge(Object obj) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      beforeSave(obj);

      obj = sess.merge(obj);
      deleteSubs(obj);

      return obj;
    } catch (Throwable t) {
      handleException(t, obj);
      return null;
    }
  }

  /** Save a new object or update an object which may have been loaded in a
   * previous hibernate session
   *
   * @param obj
   * @throws WebdavException
   */
  public void saveOrUpdate(final Object obj) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      beforeSave(obj);

      sess.saveOrUpdate(obj);
      deleteSubs(obj);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Copy the state of the given object onto the persistent object with the
   * same identifier. If there is no persistent instance currently associated
   * with the session, it will be loaded. Return the persistent instance.
   * If the given instance is unsaved or does not exist in the database,
   * save it and return it as a newly persistent instance. Otherwise, the
   * given instance does not become associated with the session.
   *
   * @param obj
   * @return Object
   * @throws WebdavException
   */
  public Object saveOrUpdateCopy(final Object obj) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      return sess.merge(obj);
    } catch (Throwable t) {
      handleException(t);
      return null;  // Don't get here
    }
  }

  /** Return an object of the given class with the given id if it is
   * already associated with this session. This must be called for specific
   * key queries or we can get a NonUniqueObjectException later.
   *
   * @param  cl    Class of the instance
   * @param  id    A serializable key
   * @return Object
   * @throws WebdavException
   */
  public Object get(final Class cl, final Serializable id) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      return sess.get(cl, id);
    } catch (Throwable t) {
      handleException(t);
      return null;  // Don't get here
    }
  }

  /** Return an object of the given class with the given id if it is
   * already associated with this session. This must be called for specific
   * key queries or we can get a NonUniqueObjectException later.
   *
   * @param  cl    Class of the instance
   * @param  id    int key
   * @return Object
   * @throws WebdavException
   */
  public Object get(final Class cl, final int id) throws WebdavException {
    return get(cl, new Integer(id));
  }

  /** Save a new object.
   *
   * @param obj
   * @throws WebdavException
   */
  public void save(final Object obj) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      beforeSave(obj);
      sess.save(obj);
      deleteSubs(obj);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /* * Save a new object with the given id. This should only be used for
   * restoring the db from a save or for assigned keys.
   *
   * @param obj
   * @param id
   * @throws WebdavException
   * /
  public void save(Object obj, Serializable id) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      sess.save(obj, id);
    } catch (Throwable t) {
      handleException(t);
    }
  }*/

  /** Delete an object
   *
   * @param obj
   * @throws WebdavException
   */
  public void delete(final Object obj) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      beforeDelete(obj);

      sess.delete(obj);
      deleteSubs(obj);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /** Save a new object with the given id. This should only be used for
   * restoring the db from a save.
   *
   * @param obj
   * @throws WebdavException
   */
  public void restore(final Object obj) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      sess.replicate(obj, ReplicationMode.IGNORE);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.calcorei.HibSession#reAttach(org.bedework.calfacade.base.BwUnversionedDbentity)
   */
  public void reAttach(final UnversionedDbentity<?> val) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      if (!val.unsaved()) {
        sess.lock(val, LockMode.NONE);
      }
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /**
   * @param o
   * @throws WebdavException
   */
  public void lockRead(final Object o) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      sess.lock(o, LockMode.READ);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /**
   * @param o
   * @throws WebdavException
   */
  public void lockUpdate(final Object o) throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    try {
      sess.lock(o, LockMode.UPGRADE);
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /**
   * @throws WebdavException
   */
  public void flush() throws WebdavException {
    if (exc != null) {
      // Didn't hear me last time?
      throw new WebdavException(exc);
    }

    if (debug()) {
      debug("About to flush");
    }
    try {
      sess.flush();
    } catch (Throwable t) {
      handleException(t);
    }
  }

  /**
   * @throws WebdavException
   */
  public void close() throws WebdavException {
    if (sess == null) {
      return;
    }

//    throw new WebdavException("XXXXXXXXXXXXXXXXXXXXXXXXXXXXX");/*
    try {
      if (sess.isDirty()) {
        sess.flush();
      }
      if ((tx != null) && !rolledback()) {
        tx.commit();
      }
    } catch (Throwable t) {
      if (exc == null) {
        exc = t;
      }
    } finally {
      tx = null;
      if (sess != null) {
        try {
          sess.close();
        } catch (Throwable t) {}
      }
    }

    sess = null;
    if (exc != null) {
      throw new WebdavException(exc);
    }
//    */
  }

  private void handleException(final Throwable t) throws WebdavException {
    handleException(t, null);
  }

  private void handleException(final Throwable t,
                               final Object o) throws WebdavException {
    try {
      if (debug()) {
        debug("handleException called");
        if (o != null) {
          debug(o.toString());
        }
        error(t);
      }
    } catch (Throwable dummy) {}

    try {
      if (tx != null) {
        try {
          tx.rollback();
        } catch (Throwable t1) {
          rollbackException(t1);
        }
        tx = null;
      }
    } finally {
      try {
        sess.close();
      } catch (Throwable t2) {}
      sess = null;
    }

    exc = t;

    if (t instanceof StaleStateException) {
      throw new DbStaleStateException(t.getMessage());
    }

    throw new WebdavException(t);
  }

  private void beforeSave(final Object o) throws WebdavException {
    if (!(o instanceof DbEntity)) {
      return;
    }

    DbEntity ent = (DbEntity)o;

    ent.beforeSave();
  }

  private void beforeDelete(final Object o) throws WebdavException {
    if (!(o instanceof DbEntity)) {
      return;
    }

    DbEntity ent = (DbEntity)o;

    ent.beforeDeletion();
  }

  private void deleteSubs(final Object o) throws WebdavException {
    if (!(o instanceof DbEntity)) {
      return;
    }

    DbEntity ent = (DbEntity)o;

    Collection<DbEntity> subs = ent.getDeletedEntities();
    if (subs == null) {
      return;
    }

    for (DbEntity sub: subs) {
      delete(sub);
    }
  }

  /** This is just in case we want to report rollback exceptions. Seems we're
   * likely to get one.
   *
   * @param t   Throwable from the rollback
   */
  private void rollbackException(final Throwable t) {
    error(t);
  }
}
