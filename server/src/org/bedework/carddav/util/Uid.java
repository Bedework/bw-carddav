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
package org.bedework.carddav.util;

import java.net.InetAddress;

/** Temp to avoid some imports for the time being.
 *
 * @author douglm
 *
 */
public class Uid {
  /*  ---------------- UID gen fields -------------------- */

  /* This should be the MAC address -
   * Java 6 java.net.NetworkInterface class now has method getHardwareAddress()
   */
  private static final int IP;
  static {
    int ipadd;
    try {
      ipadd = toInt(InetAddress.getLocalHost().getAddress());
    } catch (Exception e) {
      ipadd = 0;
    }
    IP = ipadd;
  }

  private static short counter = (short) 0;
  private static final int JVM = (int) ( System.currentTimeMillis() >>> 8 );

  private static String sep = "-";

  /*  ---------------- UID gen fields -------------------- */

  /** Code copied and modified from hibernate UUIDHexGenerator. Generates a
   * unique 36 character key of hex + separators.
   *
   * @return String uid.
   */
  public static String getUid() {
    /* Unique down to millisecond */
    short hiTime = (short)(System.currentTimeMillis() >>> 32);

    int loTime = (int)System.currentTimeMillis();

    int ct;

    synchronized(Uid.class) {
      if (counter < 0) {
        counter = 0;
      }

      ct = counter++;
    }

    return new StringBuilder(36).
            append(format(IP)).append(sep).
            append(format(JVM)).append(sep).
            append(format(hiTime)).append(sep).
            append(format(loTime)).append(sep).
            append(format(ct)).
            toString();
  }

  private static String format(int intval) {
    String formatted = Integer.toHexString(intval);
    StringBuilder buf = new StringBuilder("00000000");
    buf.replace(8 - formatted.length(), 8, formatted);

    return buf.toString();
  }

  private static String format(short shortval) {
    String formatted = Integer.toHexString(shortval);
    StringBuilder buf = new StringBuilder("0000");
    buf.replace(4 - formatted.length(), 4, formatted);

    return buf.toString();
  }

  /** From hibernate.util
   *
   * @param bytes
   * @return int
   */
  public static int toInt(byte[] bytes ) {
    int result = 0;
    for (int i = 0; i < 4; i++) {
      result = (result << 8) - Byte.MIN_VALUE + (int)bytes[i];
    }

    return result;
  }
}
