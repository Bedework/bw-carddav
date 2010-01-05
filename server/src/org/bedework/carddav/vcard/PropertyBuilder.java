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
package org.bedework.carddav.vcard;

import net.fortuna.ical4j.vcard.Group;
import net.fortuna.ical4j.vcard.GroupRegistry;
import net.fortuna.ical4j.vcard.Parameter;
import net.fortuna.ical4j.vcard.ParameterFactory;
import net.fortuna.ical4j.vcard.ParameterFactoryRegistry;
import net.fortuna.ical4j.vcard.Property;
import net.fortuna.ical4j.vcard.PropertyFactory;
import net.fortuna.ical4j.vcard.PropertyFactoryRegistry;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;

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

  @SuppressWarnings("unused")
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
      return factory.createProperty(null, value);
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
      return factory.createProperty(group, null, value);
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