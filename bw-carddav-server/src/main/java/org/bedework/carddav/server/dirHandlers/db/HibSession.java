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

import org.bedework.webdav.servlet.shared.WebdavException;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/** Interface to do hibernate interactions.
 *
 * @author Mike Douglass douglm at rpi.edu
 */
public interface HibSession extends Serializable {
  /** Set up for a hibernate interaction. Throw the object away on exception.
   *
   * @param sessFactory
   * @param log
   * @throws WebdavException
   */
  public void init(SessionFactory sessFactory,
                   Logger log) throws WebdavException;

  /**
   * @return Session
   * @throws WebdavException
   */
  public Session getSession() throws WebdavException;

  /**
   * @return boolean true if open
   * @throws WebdavException
   */
  public boolean isOpen() throws WebdavException;

  /** Clear a session
   *
   * @throws WebdavException
   */
  public void clear() throws WebdavException;

  /** Disconnect a session
   *
   * @throws WebdavException
   */
  public void disconnect() throws WebdavException;

  /** set the flushmode
   *
   * @param val
   * @throws WebdavException
   */
  public void setFlushMode(FlushMode val) throws WebdavException;

  /** Begin a transaction
   *
   * @throws WebdavException
   */
  public void beginTransaction() throws WebdavException;

  /** Return true if we have a transaction started
   *
   * @return boolean
   */
  public boolean transactionStarted();

  /** Commit a transaction
   *
   * @throws WebdavException
   */
  public void commit() throws WebdavException;

  /** Rollback a transaction
   *
   * @throws WebdavException
   */
  public void rollback() throws WebdavException;

  /** Did we rollback the transaction?
   *
   * @return boolean
   * @throws WebdavException
   */
  public boolean rolledback() throws WebdavException;

  /** Create a Criteria ready for the additon of Criterion.
   *
   * @param cl           Class for criteria
   * @return Criteria    created Criteria
   * @throws WebdavException
   */
  public Criteria createCriteria(Class<?> cl) throws WebdavException;

  /** Evict an object from the session.
   *
   * @param val          Object to evict
   * @throws WebdavException
   */
  public void evict(Object val) throws WebdavException;

  /** Create a query ready for parameter replacement or execution.
   *
   * @param s             String hibernate query
   * @throws WebdavException
   */
  public void createQuery(String s) throws WebdavException;

  /** Create a query ready for parameter replacement or execution and flag it
   * for no flush. This assumes that any queued changes will not affect the
   * result of the query.
   *
   * @param s             String hibernate query
   * @throws WebdavException
   */
  public void createNoFlushQuery(String s) throws WebdavException;

  /**
   * @return query string
   * @throws WebdavException
   */
  public String getQueryString() throws WebdavException;

  /** Create a sql query ready for parameter replacement or execution.
   *
   * @param s             String hibernate query
   * @param returnAlias
   * @param returnClass
   * @throws WebdavException
   */
  public void createSQLQuery(String s, String returnAlias, Class<?> returnClass)
        throws WebdavException;

  /** Create a named query ready for parameter replacement or execution.
   *
   * @param name         String named query name
   * @throws WebdavException
   */
  public void namedQuery(String name) throws WebdavException;

  /** Mark the query as cacheable
   *
   * @throws WebdavException
   */
  public void cacheableQuery() throws WebdavException;

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      String parameter value
   * @throws WebdavException
   */
  public void setString(String parName, String parVal) throws WebdavException;

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      Date parameter value
   * @throws WebdavException
   */
  public void setDate(String parName, Date parVal) throws WebdavException;

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      boolean parameter value
   * @throws WebdavException
   */
  public void setBool(String parName, boolean parVal) throws WebdavException;

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      int parameter value
   * @throws WebdavException
   */
  public void setInt(String parName, int parVal) throws WebdavException;

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      long parameter value
   * @throws WebdavException
   */
  public void setLong(String parName, long parVal) throws WebdavException;

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      Object parameter value
   * @throws WebdavException
   */
  public void setEntity(String parName, Object parVal) throws WebdavException;

  /** Set the named parameter with the given value
   *
   * @param parName     String parameter name
   * @param parVal      Object parameter value
   * @throws WebdavException
   */
  public void setParameter(String parName, Object parVal) throws WebdavException ;

  /** Set the named parameter with the given Collection
   *
   * @param parName     String parameter name
   * @param parVal      Collection parameter value
   * @throws WebdavException
   */
  public void setParameterList(String parName,
                               Collection<?> parVal) throws WebdavException ;

  /** Set the first result for a paged batch
   *
   * @param val      int first index
   * @throws WebdavException
   */
  public void setFirstResult(int val) throws WebdavException;

  /** Set the max number of results for a paged batch
   *
   * @param val      int max number
   * @throws WebdavException
   */
  public void setMaxResults(int val) throws WebdavException;

  /** Return the single object resulting from the query.
   *
   * @return Object          retrieved object or null
   * @throws WebdavException
   */
  public Object getUnique() throws WebdavException;

  /** Return a list resulting from the query.
   *
   * @return List          list from query
   * @throws WebdavException
   */
  public List getList() throws WebdavException;

  /**
   * @return int number updated
   * @throws WebdavException
   */
  public int executeUpdate() throws WebdavException;

  /** Update an object which may have been loaded in a previous hibernate
   * session
   *
   * @param obj
   * @throws WebdavException
   */
  public void update(Object obj) throws WebdavException;

  /** Merge and update an object which may have been loaded in a previous hibernate
   * session
   *
   * @param obj
   * @return Object   the persiatent object
   * @throws WebdavException
   */
  public Object merge(Object obj) throws WebdavException;

  /** Save a new object or update an object which may have been loaded in a
   * previous hibernate session
   *
   * @param obj
   * @throws WebdavException
   */
  public void saveOrUpdate(Object obj) throws WebdavException;

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
  public Object saveOrUpdateCopy(Object obj) throws WebdavException;

  /** Return an object of the given class with the given id if it is
   * already associated with this session. This must be called for specific
   * key queries or we can get a NonUniqueObjectException later.
   *
   * @param  cl    Class of the instance
   * @param  id    A serializable key
   * @return Object
   * @throws WebdavException
   */
  public Object get(Class cl, Serializable id) throws WebdavException;

  /** Return an object of the given class with the given id if it is
   * already associated with this session. This must be called for specific
   * key queries or we can get a NonUniqueObjectException later.
   *
   * @param  cl    Class of the instance
   * @param  id    int key
   * @return Object
   * @throws WebdavException
   */
  public Object get(Class cl, int id) throws WebdavException;

  /** Save a new object.
   *
   * @param obj
   * @throws WebdavException
   */
  public void save(Object obj) throws WebdavException;

  /** Delete an object
   *
   * @param obj
   * @throws WebdavException
   */
  public void delete(Object obj) throws WebdavException;

  /** Save a new object with the given id. This should only be used for
   * restoring the db from a save.
   *
   * @param obj
   * @throws WebdavException
   */
  public void restore(Object obj) throws WebdavException;

  /**
   * @param val
   * @throws WebdavException
   */
  public void reAttach(UnversionedDbentity<?> val) throws WebdavException;

  /**
   * @param o
   * @throws WebdavException
   */
  public void lockRead(Object o) throws WebdavException;

  /**
   * @param o
   * @throws WebdavException
   */
  public void lockUpdate(Object o) throws WebdavException;

  /**
   * @throws WebdavException
   */
  public void flush() throws WebdavException;

  /**
   * @throws WebdavException
   */
  public void close() throws WebdavException;
}
