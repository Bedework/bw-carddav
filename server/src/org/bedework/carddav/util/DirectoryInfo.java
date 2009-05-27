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
package org.bedework.carddav.util;

import java.io.Serializable;

/** This class defines the various directory properties we need to make visible
 * to applications.
 *
 * @author Mike Douglass
 */
public class DirectoryInfo implements Serializable {
  /* Principals */
  private String principalRoot;
  private String userPrincipalRoot;
  private String groupPrincipalRoot;
  private String resourcePrincipalRoot;
  private String venuePrincipalRoot;
  private String ticketPrincipalRoot;
  private String hostPrincipalRoot;

  /** Set the principal root e.g. "/principals"
   *
   * @param val    String
   */
  public void setPrincipalRoot(String val) {
    principalRoot = val;
  }

  /** get the principal root e.g. "/principals"
   *
   * @return String
   */
  public String getPrincipalRoot() {
    return principalRoot;
  }

  /** Set the user principal root e.g. "/principals/users"
   *
   * @param val    String
   */
  public void setUserPrincipalRoot(String val) {
    userPrincipalRoot = val;
  }

  /** get the principal root e.g. "/principals/users"
   *
   * @return String
   */
  public String getUserPrincipalRoot() {
    return userPrincipalRoot;
  }

  /** Set the group principal root e.g. "/principals/groups"
   *
   * @param val    String
   */
  public void setGroupPrincipalRoot(String val) {
    groupPrincipalRoot = val;
  }

  /** get the group principal root e.g. "/principals/groups"
   *
   * @return String
   */
  public String getGroupPrincipalRoot() {
    return groupPrincipalRoot;
  }

  /** Set the resource principal root e.g. "/principals/resources"
   *
   * @param val    String
   */
  public void setResourcePrincipalRoot(String val) {
    resourcePrincipalRoot = val;
  }

  /** get the resource principal root e.g. "/principals/resources"
   *
   * @return String
   */
  public String getResourcePrincipalRoot() {
    return resourcePrincipalRoot;
  }

  /** Set the venue principal root e.g. "/principals/locations"
   *
   * @param val    String
   */
  public void setVenuePrincipalRoot(String val) {
    venuePrincipalRoot = val;
  }

  /** get the venue principal root e.g. "/principals/locations"
   *
   * @return String
   */
  public String getVenuePrincipalRoot() {
    return venuePrincipalRoot;
  }

  /** Set the ticket principal root e.g. "/principals/tickets"
   *
   * @param val    String
   */
  public void setTicketPrincipalRoot(String val) {
    ticketPrincipalRoot = val;
  }

  /** get the ticket principal root e.g. "/principals/tickets"
   *
   * @return String
   */
  public String getTicketPrincipalRoot() {
    return ticketPrincipalRoot;
  }

  /** Set the host principal root e.g. "/principals/hosts"
   *
   * @param val    String
   */
  public void setHostPrincipalRoot(String val) {
    hostPrincipalRoot = val;
  }

  /** get the host principal root e.g. "/principals/hosts"
   *
   * @return String
   */
  public String getHostPrincipalRoot() {
    return hostPrincipalRoot;
  }
}
