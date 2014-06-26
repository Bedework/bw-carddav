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

import org.bedework.carddav.server.CarddavCardNode;
import org.bedework.carddav.server.CarddavCollection;
import org.bedework.carddav.server.dirHandlers.ldap.LdapMapping.AttrPropertyMapping;
import org.bedework.carddav.server.dirHandlers.ldap.LdapMapping.AttrValue;
import org.bedework.carddav.util.CardDAVContextConfig;
import org.bedework.carddav.util.CardDAVDuplicateUid;
import org.bedework.carddav.util.DirHandlerConfig;
import org.bedework.carddav.vcard.Card;
import org.bedework.webdav.servlet.shared.UrlHandler;
import org.bedework.webdav.servlet.shared.WdCollection;
import org.bedework.webdav.servlet.shared.WebdavBadRequest;
import org.bedework.webdav.servlet.shared.WebdavException;

import net.fortuna.ical4j.vcard.Group;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.Property;

import java.util.Collection;
import java.util.List;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * @author douglm
 *
 */
public class LdapAddrBookDirHandler extends LdapDirHandler {
  String searchBase; // searchBase which resulted in sresult;

  SearchControls constraints;
  NamingEnumeration<SearchResult> sresult;

  @Override
  public void init(final CardDAVContextConfig cdConfig,
                   final DirHandlerConfig dhConfig,
                   final UrlHandler urlHandler) throws WebdavException {
    super.init(cdConfig, dhConfig, urlHandler);
  }

  /* ====================================================================
   *                   Principals
   * ==================================================================== */

  public Card getPrincipalCard(final String href) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  @Override
  public Collection<String>getGroups(final String rootUrl,
                                     final String principalUrl) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* ====================================================================
   *                   Cards
   * ==================================================================== */

  @Override
  public void addCard(final String path,
                      final Card card) throws WebdavException {
    if (card.getUid() == null) {
      throw new WebdavBadRequest();
    }

    if (getCardByUid(path, card.getUid()) != null) {
      throw new CardDAVDuplicateUid();
    }

    /** Build a directory record and add the attributes
     */
    final DirRecord dirRec = new BasicDirRecord();

    final String colDn = makeAddrbookDn(path, true);
    final String cn = card.getName();
    if (cn == null) {
      throw new WebdavBadRequest();
    }

    dirRec.setDn("cn=" + dnEscape(cn) + ", " + colDn);

    for (LdapMapping lm: LdapMapping.attrToVcardProperty.values()) {
      if (lm instanceof AttrValue) {
        AttrValue av = (AttrValue)lm;

        setAttr(dirRec, av.getAttrId(), av.getValue());
        continue;
      }

      if (lm instanceof AttrPropertyMapping) {
        AttrPropertyMapping apm = (AttrPropertyMapping)lm;

        if (apm.getParameterName() == null) {
          if (!setAttr(dirRec, card, apm.getAttrId(), apm.getPropertyName())) {
            if (apm.getRequired()) {
              throw new WebdavBadRequest();
            }
          }
        }
        continue;
      }

    }

    Collection<Property> props = card.findProperties("TEL");
    for (Property prop: props) {
      Group work = prop.getGroup();
      List<Parameter> params = prop.getParameters();

      Parameter par = null;

      if (params != null) {
        // XXX Fix this
        for (Parameter p: params) {
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

  private boolean create(final DirRecord rec) throws WebdavException {
    try {
      ctx.createSubcontext(rec.getDn(), rec.getAttributes());
      return true;
    } catch (NameAlreadyBoundException nabe) {
      return false;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private boolean setAttr(final DirRecord dirRec, final Card card,
                       final String name, final String vpropName) throws WebdavException {
    Property vprop = card.findProperty(vpropName);
    if (vprop == null) {
      return false;
    }

    try {
      dirRec.setAttr(name, vprop.getValue());
      return true;
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  private String findProp(final Card card,
                          final String vpropName) throws WebdavException {
    Property vprop = card.findProperty(vpropName);
    if (vprop == null) {
      return null;
    }

    return vprop.getValue();
  }

  private void setAttr(final DirRecord dirRec,
                       final String name, final String val) throws WebdavException {
    if (val == null) {
      return;
    }

    try {
      dirRec.setAttr(name, val);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#updateCard(java.lang.String, org.bedework.carddav.server.Vcard)
   */
  public void updateCard(final String path,
                         final Card card) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#deleteCard(org.bedework.carddav.server.CarddavCardNode)
   */
  public void deleteCard(final CarddavCardNode val) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* ====================================================================
   *                   Collections
   * ==================================================================== */

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#makeCollection(org.bedework.carddav.server.CarddavCollection, java.lang.String)
   */
  public int makeCollection(final CarddavCollection col,
                            final String parentPath) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#deleteCollection(org.bedework.webdav.WdCollection)
   */
  public void deleteCollection(final WdCollection col) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#rename(org.bedework.webdav.WdCollection, java.lang.String)
   */
  public int rename(final WdCollection col,
                    final String newName) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#copyMove(org.bedework.carddav.server.Vcard, java.lang.String, java.lang.String, boolean, boolean)
   */
  public int copyMove(final Card from,
                      final String toPath,
                      final String name,
                      final boolean copy,
                      final boolean overwrite) throws WebdavException {
    throw new WebdavException("unimplemented");
  }

  /* (non-Javadoc)
   * @see org.bedework.carddav.bwserver.DirHandler#updateCollection(org.bedework.webdav.WdCollection)
   */
  public void updateCollection(final WdCollection val) throws WebdavException {
    throw new WebdavException("unimplemented");
  }
}
