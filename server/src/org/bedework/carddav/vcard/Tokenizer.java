package org.bedework.carddav.vcard;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;

import org.apache.log4j.Logger;
import org.bedework.carddav.server.Debug;

import edu.rpi.cct.webdav.servlet.shared.WebdavException;

/**
 * @author douglm
 *
 */
public class Tokenizer extends StreamTokenizer {
  private transient Logger log;

  /**
   * @param rdr
   */
  public Tokenizer(Reader rdr) {
    super(new UnfoldingReader(rdr));
  }

  /**
   * Asserts that the next token in the stream matches the specified token.
   *
   * @param token expected token
   * @throws WebdavException
   */
  public void assertToken(int token) throws WebdavException {
    try {
      if (nextToken() != token) {
        throw new WebdavException("Expected [" + token + "], read [" +
                                  ttype + "] at " + lineno());
      }

      if (Debug.enabled) {
        debugMsg("[" + token + "]");
      }
    } catch (IOException e) {
      throw new WebdavException(e);
    }
  }

  /**
   * @throws WebdavException
   */
  public void assertWord() throws WebdavException {
    assertToken(StreamTokenizer.TT_WORD);
  }

  /**
   * Asserts that the next token in the stream matches the specified token.
   * This method is case-sensitive.
   *
   * @param token
   * @throws WebdavException
   */
  public void assertToken(String token) throws WebdavException {
    assertToken(token, false);
  }

  /**
   * Asserts that the next token in the stream matches the specified token.
   *
   * @param token expected token
   * @param ignoreCase
   * @throws WebdavException
   */
  public void assertToken(String token, boolean ignoreCase) throws WebdavException {
    // ensure next token is a word token..
    assertWord();

    if (ignoreCase) {
      if (!token.equalsIgnoreCase(sval)) {
        throw new WebdavException("Expected [" + token + "], read [" +
                                  sval + "] at " + lineno());
      }
    }
    else if (!token.equals(sval)) {
      throw new WebdavException( "Expected [" + token + "], read [" +
                                sval + "] at " + lineno());
    }

    if (Debug.enabled) {
      debugMsg("[" + token + "]");
    }
  }

  /**
   * Tests that the next token in the stream matches the specified token.
   * This method is case-sensitive.
   *
   * @param token
   * @return boolean
   * @throws WebdavException
   */
  public boolean testToken(int token) throws WebdavException {
    try {
      boolean res = nextToken() == token;

      if (!res) {
        pushBack();
        return false;
      }

      return true;
    } catch (IOException e) {
      throw new WebdavException(e);
    }
  }

  /**
   * Tests if the next token in the stream matches the specified token.
   *
   * @param token expected token
   * @return int
   * @throws WebdavException
   */
  public boolean testToken(String token) throws WebdavException {
    return testToken(token, false);
  }

  /**
   * Tests if the next token in the stream matches the specified token.
   *
   * @param token expected token
   * @param ignoreCase
   * @return boolean
   * @throws WebdavException
   */
  public boolean testToken(String token, boolean ignoreCase) throws WebdavException {
    // ensure next token is a word token..
    if (!testToken(StreamTokenizer.TT_WORD)) {
      return false;
    }

    if (ignoreCase) {
      if (!token.equalsIgnoreCase(sval)) {
        pushBack();
        return false;
      }
    } else if (!token.equals(sval)) {
      pushBack();
      return false;
    }

    return true;
  }

  /**
   * Absorbs extraneous newlines.
   * @param tokeniser
   * @throws WebdavException
   */
  public void skipWhitespace() throws WebdavException {
    while (true) {
      assertToken(StreamTokenizer.TT_EOL);
    }
  }

  protected Logger getLogger() {
    if (log == null) {
      log = Logger.getLogger(this.getClass());
    }

    return log;
  }

  protected void error(Throwable t) {
    getLogger().error(this, t);
  }

  protected void warn(String msg) {
    getLogger().warn(msg);
  }

  protected void debugMsg(String msg) {
    getLogger().debug(msg);
  }

  protected void logIt(String msg) {
    getLogger().info(msg);
  }
}
