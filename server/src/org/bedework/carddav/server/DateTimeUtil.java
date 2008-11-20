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
package org.bedework.carddav.server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;

/** Date and time utilities
 *
 * @author Mike Douglass     douglm - rpi.edu
 *  @version 1.0
 */
public class DateTimeUtil {
  private static final DateFormat isoDateFormat =
      new SimpleDateFormat("yyyyMMdd");

  private static final DateFormat rfcDateFormat =
    new SimpleDateFormat("yyyy'-'MM'-'dd");

  private static final DateFormat isoDateTimeFormat =
      new SimpleDateFormat("yyyyMMdd'T'HHmmss");

  private static final DateFormat rfcDateTimeFormat =
    new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");

  private static final DateFormat isoDateTimeTZFormat =
      new SimpleDateFormat("yyyyMMdd'T'HHmmss");

  private static final DateFormat rfcDateTimeTZFormat =
      new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss");

  private static final DateFormat isoDateTimeUTCTZFormat =
      new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

//  private static final DateFormat rfcDateTimeUTCTZFormat =
  //  new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");

  private static final DateFormat isoDateTimeUTCFormat =
      new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

  private static final DateFormat rfcDateTimeUTCFormat =
    new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");

  private static final DateFormat rfc822GMTFormat =
    new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

  static {
    isoDateTimeUTCFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    isoDateTimeUTCFormat.setLenient(false);

    rfcDateTimeUTCFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
    rfcDateTimeUTCFormat.setLenient(false);

    rfc822GMTFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
  }

  /**
   * @author douglm
   *
   */
  public static class WebdavBadDateException extends WebdavException {
    WebdavBadDateException() {
      super("Bad date");
    }
  }

  private DateTimeUtil() {
  }

  /**
   * @return Date value for yesterday.
   */
  public static Date yesterday() {
    Calendar cal = Calendar.getInstance();
    cal.add(Calendar.DATE, -1);

    return cal.getTime();
  }

  /** Turn Date into "yyyyMMdd"
   *
   * @param val date
   * @return String "yyyyMMdd"
   */
  public static String isoDate(Date val) {
    synchronized (isoDateFormat) {
      return isoDateFormat.format(val);
    }
  }

  /** Turn Now into "yyyyMMdd"
   *
   * @return String "yyyyMMdd"
   */
  public static String isoDate() {
    return isoDate(new Date());
  }

  /** Turn Date into "yyyy-MM-dd"
   *
   * @param val date
   * @return String "yyyy-MM-dd"
   */
  public static String rfcDate(Date val) {
    synchronized (rfcDateFormat) {
      return rfcDateFormat.format(val);
    }
  }

  /** Turn Now into "yyyy-MM-dd"
   *
   * @return String "yyyy-MM-dd"
   */
  public static String rfcDate() {
    return rfcDate(new Date());
  }

  /** Turn Date into "yyyyMMddTHHmmss"
   *
   * @param val date
   * @return String "yyyyMMddTHHmmss"
   */
  public static String isoDateTime(Date val) {
    synchronized (isoDateTimeFormat) {
      return isoDateTimeFormat.format(val);
    }
  }

  /** Get Now as "yyyyMMddTHHmmss"
   *
   * @return String "yyyyMMddTHHmmss"
   */
  public static String isoDateTime() {
    return  isoDateTime(new Date());
  }

  /** Turn Date into "yyyyMMddTHHmmss" for a given timezone
   *
   * @param val date
   * @param tz TimeZone
   * @return String "yyyyMMddTHHmmss"
   */
  public static String isoDateTime(Date val, TimeZone tz) {
    synchronized (isoDateTimeTZFormat) {
      isoDateTimeTZFormat.setTimeZone(tz);
      return isoDateTimeTZFormat.format(val);
    }
  }

  /** Turn Date into "yyyy-MM-ddTHH:mm:ss"
   *
   * @param val date
   * @return String "yyyy-MM-ddTHH:mm:ss"
   */
  public static String rfcDateTime(Date val) {
    synchronized (rfcDateTimeFormat) {
      return rfcDateTimeFormat.format(val);
    }
  }

  /** Get Now as "yyyy-MM-ddTHH:mm:ss"
   *
   * @return String "yyyy-MM-ddTHH:mm:ss"
   */
  public static String rfcDateTime() {
    return  rfcDateTime(new Date());
  }

  /** Turn Date into "yyyy-MM-ddTHH:mm:ss" for a given timezone
   *
   * @param val date
   * @param tz TimeZone
   * @return String "yyyy-MM-ddTHH:mm:ss"
   */
  public static String rfcDateTime(Date val, TimeZone tz) {
    synchronized (rfcDateTimeTZFormat) {
      rfcDateTimeTZFormat.setTimeZone(tz);
      return rfcDateTimeTZFormat.format(val);
    }
  }

  /** Turn Date into "yyyyMMddTHHmmssZ"
   *
   * @param val date
   * @return String "yyyyMMddTHHmmssZ"
   */
  public static String isoDateTimeUTC(Date val) {
    synchronized (isoDateTimeUTCFormat) {
      return isoDateTimeUTCFormat.format(val);
    }
  }

  /** Turn Date into "yyyy-MM-ddTHH:mm:ssZ"
   *
   * @param val date
   * @return String "yyyy-MM-ddTHH:mm:ssZ"
   */
  public static String rfcDateTimeUTC(Date val) {
    synchronized (rfcDateTimeUTCFormat) {
      return rfcDateTimeUTCFormat.format(val);
    }
  }

  /** Turn Date into "???"
   *
   * @param val date
   * @return String "???"
   */
  public static String rfc822Date(Date val) {
    synchronized (rfc822GMTFormat) {
      return rfc822GMTFormat.format(val);
    }
  }

  /** Get Date from "yyyyMMdd"
   *
   * @param val String "yyyyMMdd"
   * @return Date
   * @throws WebdavException
   */
  public static Date fromISODate(String val) throws WebdavException {
    try {
      synchronized (isoDateFormat) {
        return isoDateFormat.parse(val);
      }
    } catch (Throwable t) {
      throw new WebdavBadDateException();
    }
  }

  /** Get Date from "yyyy-MM-dd"
   *
   * @param val String "yyyy-MM-dd"
   * @return Date
   * @throws WebdavException
   */
  public static Date fromRfcDate(String val) throws WebdavException {
    try {
      synchronized (rfcDateFormat) {
        return rfcDateFormat.parse(val);
      }
    } catch (Throwable t) {
      throw new WebdavBadDateException();
    }
  }

  /** Get Date from "yyyyMMddThhmmss"
   *
   * @param val String "yyyyMMddThhmmss"
   * @return Date
   * @throws WebdavException
   */
  public static Date fromISODateTime(String val) throws WebdavException {
    try {
      synchronized (isoDateTimeFormat) {
        return isoDateTimeFormat.parse(val);
      }
    } catch (Throwable t) {
      throw new WebdavBadDateException();
    }
  }

  /** Get Date from "yyyy-MM-ddThh:mm:ss"
   *
   * @param val String "yyyy-MM-ddThh:mm:ss"
   * @return Date
   * @throws WebdavException
   */
  public static Date fromRfcDateTime(String val) throws WebdavException {
    try {
      synchronized (rfcDateTimeFormat) {
        return rfcDateTimeFormat.parse(val);
      }
    } catch (Throwable t) {
      throw new WebdavBadDateException();
    }
  }

  /** Get Date from "yyyyMMddThhmmss" with timezone
   *
   * @param val String "yyyyMMddThhmmss"
   * @param tz TimeZone
   * @return Date
   * @throws WebdavException
   */
  public static Date fromISODateTime(String val,
                                     TimeZone tz) throws WebdavException {
    try {
      synchronized (isoDateTimeTZFormat) {
        isoDateTimeTZFormat.setTimeZone(tz);
        return isoDateTimeTZFormat.parse(val);
      }
    } catch (Throwable t) {
      throw new WebdavBadDateException();
    }
  }

  /** Get Date from "yyyy-MM-ddThh:mm:ss" with timezone
   *
   * @param val String "yyyy-ddThh:mm:ss"
   * @param tz TimeZone
   * @return Date
   * @throws WebdavException
   */
  public static Date fromRfcDateTime(String val,
                                     TimeZone tz) throws WebdavException {
    try {
      synchronized (rfcDateTimeTZFormat) {
        rfcDateTimeTZFormat.setTimeZone(tz);
        return rfcDateTimeTZFormat.parse(val);
      }
    } catch (Throwable t) {
      throw new WebdavBadDateException();
    }
  }

  /** Get Date from "yyyyMMddThhmmssZ" with timezone
   *
   * @param val String "yyyyMMddThhmmssZ"
   * @param tz TimeZone
   * @return Date
   * @throws WebdavException
   */
  public static Date fromISODateTimeUTC(String val,
                                        TimeZone tz) throws WebdavException {
    try {
      synchronized (isoDateTimeUTCTZFormat) {
        isoDateTimeUTCTZFormat.setTimeZone(tz);
        return isoDateTimeUTCTZFormat.parse(val);
      }
    } catch (Throwable t) {
      throw new WebdavBadDateException();
    }
  }

  /** Get Date from "yyyyMMddThhmmssZ"
   *
   * @param val String "yyyyMMddThhmmssZ"
   * @return Date
   * @throws WebdavException
   */
  public static Date fromISODateTimeUTC(String val) throws WebdavException {
    try {
      synchronized (isoDateTimeUTCFormat) {
        return isoDateTimeUTCFormat.parse(val);
      }
    } catch (Throwable t) {
      throw new WebdavBadDateException();
    }
  }

  /** Get Date from "yyyy-MM-ddThh:mm:ssZ"
   *
   * @param val String "yyyy-MM-ddThh:mm:ssZ"
   * @return Date
   * @throws WebdavException
   */
  public static Date fromRfcDateTimeUTC(String val) throws WebdavException {
    try {
      synchronized (rfcDateTimeUTCFormat) {
        return rfcDateTimeUTCFormat.parse(val);
      }
    } catch (Throwable t) {
      throw new WebdavBadDateException();
    }
  }

  /** Get RFC822 form from "yyyyMMddThhmmssZ"
   *
   * @param val String "yyyyMMddThhmmssZ"
   * @return Date
   * @throws WebdavException
   */
  public static String fromISODateTimeUTCtoRfc822(String val) throws WebdavException {
    try {
      synchronized (isoDateTimeUTCFormat) {
        return rfc822Date(isoDateTimeUTCFormat.parse(val));
      }
    } catch (Throwable t) {
      throw new WebdavBadDateException();
    }
  }

  /** Check Date is "yyyyMMdd"
   *
   * @param val String to check
   * @return boolean
   * @throws WebdavException
   */
  public static boolean isISODate(String val) throws WebdavException {
    try {
      if (val.length() != 8) {
        return false;
      }
      fromISODate(val);
      return true;
    } catch (Throwable t) {
      return false;
    }
  }

  /** Check Date is "yyyyMMddThhmmddZ"
   *
   * @param val String to check
   * @return boolean
   * @throws WebdavException
   */
  public static boolean isISODateTimeUTC(String val) throws WebdavException {
    try {
      if (val.length() != 16) {
        return false;
      }
      fromISODateTimeUTC(val);
      return true;
    } catch (Throwable t) {
      return false;
    }
  }

  /** Check Date is "yyyyMMddThhmmdd"
   *
   * @param val String to check
   * @return boolean
   * @throws WebdavException
   */
  public static boolean isISODateTime(String val) throws WebdavException {
    try {
      if (val.length() != 15) {
        return false;
      }
      fromISODateTime(val);
      return true;
    } catch (Throwable t) {
      return false;
    }
  }
}
