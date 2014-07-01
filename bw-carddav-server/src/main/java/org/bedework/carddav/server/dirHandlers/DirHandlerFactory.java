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
package org.bedework.carddav.server.dirHandlers;

import org.bedework.carddav.bwserver.DirHandler;
import org.bedework.carddav.server.config.CardDAVConfig;
import org.bedework.carddav.server.config.DirHandlerConfig;
import org.bedework.webdav.servlet.shared.WebdavException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Create a directory handler for CardDAV
 *
 * @author douglm
 */
public class DirHandlerFactory {
  private final CardDAVConfig conf;

  private static class HandlerKey {
    String prefix;
    String account;

    HandlerKey(final String prefix, final String account) {
      this.prefix = prefix;
      this.account = account;
    }

    @Override
    public int hashCode() {
      int hc = prefix.hashCode();
      if (account != null) {
        hc *= account.hashCode();
      }

      return hc;
    }

    @Override
    public boolean equals(final Object o) {
      if (!(o instanceof HandlerKey)) {
        return false;
      }

      if (this == o) {
        return true;
      }

      final HandlerKey that = (HandlerKey)o;

      if (!prefix.equals(that.prefix)) {
        return false;
      }

      if ((account != null) && (that.account != null)) {
        return account.equals(that.account);
      }

      return (account == null) && (that.account == null);
    }
  }

  /* Indexed by the prefix from the config and the account. */
  private Map<HandlerKey, DirHandler> handlers;

  private final ArrayList<DirHandler> openHandlers = new ArrayList<>();

  /**
   * @param conf - configuration
   */
  public DirHandlerFactory(final CardDAVConfig conf) {
    this.conf = conf;
  }

  /**
   * @param account of caller
   * @param urlHandler to handle urls.
   * @return set of handlers.
   * @throws WebdavException
   */
  public List<DirHandler> getHandlers(final String account,
                                     final UrlHandler urlHandler) throws WebdavException {
    final Set<DirHandlerConfig> configs = conf.getDirHandlerConfigs();

    if (configs == null) {
      return null;
    }

    final List<DirHandler> dhs = new ArrayList<>();

    for (final DirHandlerConfig dhc: configs) {
      dhs.add(getHandler(dhc, account, urlHandler));
    }

    return dhs;
  }

  /**
   * @return DirHandler
   * @throws WebdavException
   */
  public DirHandler getHandler(final String path,
                               final String account,
                               final UrlHandler urlHandler) throws WebdavException {
    /* First determine which configuration handles this path */
    final DirHandlerConfig dhc = conf.findDirhandler(path);

    if (dhc == null) {
      throw new WebdavBadRequest("Bad path " + path);
    }

    return getHandler(dhc, account, urlHandler);
  }

  /**
   * @return DirHandler
   * @throws WebdavException
   */
  public DirHandler getPrincipalHandler(final String principalHref,
                                        final String account,
                                        final UrlHandler urlHandler) throws WebdavException {
    /* First determine which configuration handles this path */
    final DirHandlerConfig dhc = conf.findPrincipalDirhandler(principalHref);

    if (dhc == null) {
      throw new WebdavBadRequest("No handler for " + principalHref);
    }

    return getHandler(dhc, account, urlHandler);
  }

  public void close() throws WebdavException {
    for (final DirHandler handler: openHandlers) {
      if (handler.isOpen()) {
        handler.close();
      }
    }

    openHandlers.clear();
  }

  private DirHandler getHandler(final DirHandlerConfig dhc,
                                final String account,
                                final UrlHandler urlHandler) throws WebdavException {
    try {
      DirHandler dh = null;

      /* See if we have a handler for this path and this account */

      final HandlerKey hk = new HandlerKey(dhc.getPathPrefix(), account);

      if (handlers != null) {
        dh = handlers.get(hk);
      }

      if (dh == null) {
        // Get one from the factory and open it.
        dh = getHandlerObject(dhc.getClassName());
        dh.init(conf, dhc, urlHandler);

        if (handlers == null) {
          handlers = new HashMap<>();
        }

        handlers.put(hk, dh);
      }

      dh.open(account);
      openHandlers.add(dh);

      return dh;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  /**
   * @param name class name of handler
   * @return DirHandler
   * @throws WebdavException
   */
  private DirHandler getHandlerObject(final String name) throws WebdavException  {
    try {
      final Object o = Class.forName(name).newInstance();

      if (o == null) {
        throw new WebdavException("Class " + name + " not found");
      }

      if (!(o instanceof DirHandler)) {
        throw new WebdavException("Class " + name +
                                   " is not a subclass of " +
                                   DirHandler.class.getName());
       }

      return (DirHandler)o;
    } catch (final WebdavException wde) {
      throw wde;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

}
