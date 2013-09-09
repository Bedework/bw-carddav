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
package org.bedework.carddav.vcard;

import org.bedework.webdav.servlet.shared.WebdavException;

import net.fortuna.ical4j.vcard.Group;
import net.fortuna.ical4j.vcard.GroupRegistry;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.ParameterFactory;
import net.fortuna.ical4j.vcard.ParameterFactoryRegistry;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.PropertyFactory;
import net.fortuna.ical4j.vcard.PropertyFactoryRegistry;

import java.util.ArrayList;
import java.util.List;

/** Vcard property builder
 *
 * @author douglm
 *
 */
public class PropertyBuilder {
  private static final GroupRegistry groupRegistry = new GroupRegistry();

  private static final PropertyFactoryRegistry propertyFactoryRegistry =
    new PropertyFactoryRegistry();

  private static final ParameterFactoryRegistry parameterFactoryRegistry =
    new ParameterFactoryRegistry();

  private PropertyBuilder() {
  }

  /**
   * @param name
   * @param value
   * @return Property or null
   * @throws WebdavException
   */
  public static Property getProperty(final String name,
                                     final String value) throws WebdavException {
    PropertyFactory<?> factory = propertyFactoryRegistry.getFactory(name);

    if (factory == null) {
        return null;
    }

    try {
      return factory.createProperty(new ArrayList<Parameter>(), value);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /**
   * @param groupVal
   * @param name
   * @param value
   * @return Property or null
   * @throws WebdavException
   */
  public static Property getProperty(final String groupVal,
                                     final String name,
                                     final String value) throws WebdavException {
    Group group = groupRegistry.getGroup(groupVal);
    PropertyFactory<?> factory = propertyFactoryRegistry.getFactory(name);

    if (factory == null) {
        return null;
    }

    try {
      return factory.createProperty(group, new ArrayList<Parameter>(), value);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }

  /**
   * @param groupVal
   * @param paramName
   * @param paramValue
   * @param name
   * @param value
   * @return Property or null
   * @throws WebdavException
   */
  public static Property getProperty(final String groupVal,
                                     final String paramName,
                                     final String paramValue,
                                     final String name,
                                     final String value) throws WebdavException {
    Group group = groupRegistry.getGroup(groupVal);

    List<Parameter> parameters = new ArrayList<Parameter>();

    ParameterFactory<? extends Parameter> paramFactory =
      parameterFactoryRegistry.getFactory(paramName.toUpperCase());

    if (paramFactory == null) {
      return null;
    }

    parameters.add(paramFactory.createParameter(paramValue));

    PropertyFactory<?> factory = propertyFactoryRegistry.getFactory(name);

    if (factory == null) {
      return null;
    }

    try {
      return factory.createProperty(group, parameters, value);
    } catch (Throwable t) {
      throw new WebdavException(t);
    }
  }
}
