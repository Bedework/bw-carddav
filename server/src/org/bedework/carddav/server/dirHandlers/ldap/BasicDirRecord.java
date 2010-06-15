package org.bedework.carddav.server.dirHandlers.ldap;


import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.NamingException;

/** This class represents a directory record which may be built from a
    number of attributes represented as a BasicAttributes object.
 */
public class BasicDirRecord extends DirRecord {
  private Attributes attrs;

  /** Create a record which can have values added.
   */
  public BasicDirRecord() {
  }

  /** Create a record with the given attributes.
   * @param attrs
   */
  public BasicDirRecord(Attributes attrs) {
    this.attrs = attrs;
  }

  public Attributes getAttributes() throws NamingException {
    if (attrs == null) attrs = new BasicAttributes(true);

    return attrs;
  }

  public void clear() {
    super.clear();
    attrs = null;
  }
}
