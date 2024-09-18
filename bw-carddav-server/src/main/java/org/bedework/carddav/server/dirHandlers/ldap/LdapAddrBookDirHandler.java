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

import org.bedework.carddav.common.CarddavCollection;
import org.bedework.carddav.common.config.CardDAVConfigI;
import org.bedework.carddav.common.config.DirHandlerConfig;
import org.bedework.carddav.common.util.CardDAVDuplicateUid;
import org.bedework.carddav.common.vcard.Card;
import org.bedework.carddav.server.dirHandlers.ldap.LdapMapping.AttrPropertyMapping;
import org.bedework.carddav.server.dirHandlers.ldap.LdapMapping.AttrValue;
import org.bedework.webdav.servlet.shared.UrlHandler;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WebdavBadRequest;
import org.bedework.webdav.servlet.shared.WebdavException;

import net.fortuna.ical4j.vcard.Group;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.naming.NameAlreadyBoundException;

/**
 * @author douglm
 *
 */
public class LdapAddrBookDirHandler extends LdapDirHandler {
  /*
  String searchBase; // searchBase which resulted in sresult;

  SearchControls constraints;
  NamingEnumeration<SearchResult> sresult;
  */

  @Override
  public void init(final CardDAVConfigI cdConfig,
                   final DirHandlerConfig dhConfig,
                   final UrlHandler urlHandler) {
    super.init(cdConfig, dhConfig, urlHandler);
    addToPrincipal(dhConfig.getPathPrefix(), cdConfig.getUserPrincipalRoot());
  }

  /* ====================================================================
   *                   Principals
   * ==================================================================== */

  public Card getPrincipalCard(final String href) {
    throw new WebdavException("unimplemented");
  }

  @Override
  public Collection<String>getGroups(final String rootUrl,
                                     final String principalUrl) {
    throw new WebdavException("unimplemented");
  }

  /* ====================================================================
   *                   Cards
   * ==================================================================== */

  @Override
  public void addCard(final String path,
                      final Card card) {
    if (card.getUid() == null) {
      throw new WebdavBadRequest();
    }

    if (getCardByUid(path, card.getUid()) != null) {
      throw new CardDAVDuplicateUid();
    }

    /* Build a directory record and add the attributes
     */
    final DirRecord dirRec = new BasicDirRecord();

    final String colDn = makeAddrbookDn(path, true);
    final String cn = card.getName();
    if (cn == null) {
      throw new WebdavBadRequest();
    }

    dirRec.setDn("cn=" + dnEscape(cn) + ", " + colDn);

    for (final LdapMapping lm: LdapMapping.attrToVcardProperty.values()) {
      if (lm instanceof AttrValue) {
        final AttrValue av = (AttrValue)lm;

        setAttr(dirRec, av.getAttrId(), av.getValue());
        continue;
      }

      if (!(lm instanceof AttrPropertyMapping)) {
        continue;
      }

      final AttrPropertyMapping apm = (AttrPropertyMapping)lm;

      if (apm.getParameterName() == null) {
        if (!setAttr(dirRec, card, apm.getAttrId(), apm.getPropertyName())) {
          if (apm.getRequired()) {
            throw new WebdavBadRequest();
          }
        }
      }

    }

    final Collection<Property> props = card.findProperties("TEL");
    for (final Property prop: props) {
      final Group work = prop.getGroup();
      final List<Parameter> params = prop.getParameters();

      Parameter par = null;

      if (params != null) {
        // XXX Fix this
        for (final Parameter p: params) {
          if (p.getId().equals(Parameter.Id.TYPE)) {
            par = p;
            break;
          }
        }
      }

      if ((work == null) || (work.equals(Group.WORK))) {
        if ((par == null) ||
            (par.getValue().equalsIgnoreCase("voice"))) {
          setAttr(dirRec, "telephoneNumber", prop.getValue());
        } else if (par.getValue().equalsIgnoreCase("fax")) {
          setAttr(dirRec, "facsimileTelephoneNumber", prop.getValue());
        }
      } else {
        // HOME
        if ((par == null) ||
            (par.getValue().equalsIgnoreCase("voice"))) {
          setAttr(dirRec, "homePhone", prop.getValue());
        } else if (par.getValue().equalsIgnoreCase("cell")) {
          setAttr(dirRec, "mobile", prop.getValue());
        }
      }
    }

    openContext();
    create(dirRec);
  }

  private boolean create(final DirRecord rec) {
    try {
      ctx.createSubcontext(rec.getDn(), rec.getAttributes());
      return true;
    } catch (final NameAlreadyBoundException nabe) {
      return false;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  private boolean setAttr(final DirRecord dirRec,
                          final Card card,
                          final String name,
                          final String vpropName) {
    final Property vprop = card.findProperty(vpropName);
    if (vprop == null) {
      return false;
    }

    try {
      dirRec.setAttr(name, vprop.getValue());
      return true;
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  private String findProp(final Card card,
                          final String vpropName) {
    final Property vprop = card.findProperty(vpropName);
    if (vprop == null) {
      return null;
    }

    return vprop.getValue();
  }

  private void setAttr(final DirRecord dirRec,
                       final String name,
                       final String val) {
    if (val == null) {
      return;
    }

    try {
      dirRec.setAttr(name, val);
    } catch (final Throwable t) {
      throw new WebdavException(t);
    }
  }

  @Override
  public void updateCard(final String path,
                         final Card card) {
    throw new WebdavException("unimplemented");
  }

  @Override
  public void deleteCard(final String path) {
    throw new WebdavException("unimplemented");
  }

  @Override
  public Iterator<Card> getAll(final String path) {
    return null;
  }

  /* ====================================================================
   *                   Collections
   * ==================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#makeCollection(org.bedework.carddav.server.CarddavCollection, java.lang.String)
   */
  public int makeCollection(final CarddavCollection col,
                            final String parentPath) {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#deleteCollection(org.bedework.webdav.WdCollection)
   */
  public void deleteCollection(final WdCollection<?> col) {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#rename(org.bedework.webdav.WdCollection, java.lang.String)
   */
  public int rename(final WdCollection col,
                    final String newName) {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#copyMove(org.bedework.carddav.server.Vcard, java.lang.String, java.lang.String, boolean, boolean)
   */
  public int copyMove(final Card from,
                      final String toPath,
                      final String name,
                      final boolean copy,
                      final boolean overwrite) {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#updateCollection(org.bedework.webdav.WdCollection)
   */
  public void updateCollection(final WdCollection val) {
    throw new WebdavException("unimplemented");
  }
}
