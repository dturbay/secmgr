// Copyright 2018 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * The SessionManagerInterface provides a basic interface for interacting
 * with the GSA session management system.
 *
 * A session manager holds data for any number of simultaneous sessions,
 * identified by a sessionId string, which is returned when a store is created
 * for a new session with the createSession() method.  Each store can hold
 * any number of key/value pairs, using the setValue and getValue methods.
 *
 * The concept is that the sessionId can be stored in a user browser cookie
 * and becomes a handle to obtaining any amount of server side state values
 * between HTTP requests.  sessionId values are small enough to fit easily
 * in a cookie, but will be chosen (randomly) from a large enough space to
 * be effectively unpredictable/unguessable.
 *
 * Session stores can be dropped immediately by the deleteSession method, and/or
 * will be cleaned up automatically by a session garbage collector (the details
 * of which are up to the implementation), in-case the user does not activate
 * a log-out sequence.
 *
 * note- there is no assumption that differeing implementation of this
 * interface will be interoperable.  This interface provides for a common API
 * not a common datatype.
 *
 */

package com.google.enterprise.sessionmanager;

/**
 * This is the primary interface for interacting with the sessionmanager.
 * It has been separated out to facilitate possible future alternate
 * implementations.
 *
 */
public interface SessionManagerInterfaceBase {

  /**
   * checks if a given session exists
   *
   * @param sessionId   the sessionId (returned by createSession) for the
   *                    session store receiveing the new key/value pair
   *
   * @return            returns true if the sessionId exists and false if
   *                    it does not.
   */
  public boolean sessionExists(String sessionId);

  /**
   * checks if a key exists associated with a given session
   *
   * @param sessionId   the sessionId (returned by createSession) for the
   *                    session store receiveing the new key/value pair
   *
   * @return            returns true if the key exists and false if
   *                    it does not.
   */
  public boolean keyExists(String sessionId, String key);

  /**
   * gets the age of a session in seconds
   *
   * @param sessionId   the sessionId (returned by createSession) for the
   *                    session store receiveing the new key/value pair
   *
   * @return            the age in seconds.
   */
  public long sessionAge(String sessionId);

  /**
   * creates a new session store (a data alloaction for a new session)
   *
   * @return    returns the short sessionId value which can be passed to the
   *            other methods in this interface to get or set values within
   *            the store for this session.
   * @throws    RuntimeException if the underlying backend cannot create a
   *            session.
   */
  public String createSession();

  /**
   * sets a given key to a given data value, within a particular session store.
   * Any previous value for that key is replaced.  New keys are accepted
   * silently.  Callers need to ensure that key names do not conflict between
   * different sections of the overall system;  prefixing your key name with
   * your package name is advised.
   *
   * @param sessionId   the sessionId (returned by createSession) for the
   *                    session store receiveing the new key/value pair
   * @param key         arbitrary string the caller wishes to assign, which
   *                    is used for later retrieval of this data using the
   *                    getValue method.
   * @param newValue    the new value to assign to this key within this session.
   *                    if null, a blank newValue is recorded
   * @throws            IndexOutOfBoundsException if the sessionId given is not
   *                    an existing sessionId
   */
  public void setValue(String sessionId, String key, String newValue)
      throws IndexOutOfBoundsException;

  /**
   * retrieves the value set for a given key in a given session
   *
   * @param sessionId   the sessionId (returned by createSession) for the
   *                    session store with the key whose value is sought
   * @param key         the key value used in setValue() for the session data
   *                    being retrieved
   * @return            the value set by setValue() for this key in this session
   *                    or null if no such key has been set thus far.  Returns
   *                    null if key is passed null.
   * @throws            IndexOutOfBoundsException if the sessionId given is not
   *                    an existing sessionId
   */
  public String getValue(String sessionId, String key)
      throws IndexOutOfBoundsException;

  /**
   * sets a given key to a given binary value, within a particular session.
   * Any previous value for that key is replaced.  New keys are accepted
   * silently.  Callers need to ensure that key names do not conflict between
   * different sections of the overall system;  prefixing your key name with
   * your package name is advised.
   *
   * @param sessionId   the sessionId (returned by createSession) for the
   *                    session store receiveing the new key/value pair
   * @param key         arbitrary string the caller wishes to assign, which
   *                    is used for later retrieval of this data using the
   *                    getValue method.
   * @param newValue    the new value to assign to this key within this session.
   *                    if null, a blank newValue is recorded
   * @throws            IndexOutOfBoundsException if the sessionId given is not
   *                    an existing sessionId
   */
  public void setValueBin(String sessionId, String key, byte[] newValue)
      throws IndexOutOfBoundsException;

  /**
   * retrieves a binary value set for a given key in a given session
   *
   * @param sessionId   the sessionId (returned by createSession) for the
   *                    session store with the key whose value is sought
   * @param key         the key value used in setValue() for the session data
   *                    being retrieved
   * @return            the value set by setValue() for this key in this session
   *                    or null if no such key has been set thus far.  Returns
   *                    null if key is passed null.
   * @throws            IndexOutOfBoundsException if the sessionId given is not
   *                    an existing sessionId
   */
  public byte[] getValueBin(String sessionId, String key)
      throws IndexOutOfBoundsException;

  /**
   * deletes the indicated session store
   *
   * @param sessionId   the session store to be removed from the database
   * @throws            IndexOutOfBoundsException if the sessionId given is not
   *                    an existing sessionId
   * @throws            RuntimeException if the underlying backend cannot
   *                    delete a session or the keys associated with the
   *                    session.
   */
  public void deleteSession(String sessionId)
      throws IndexOutOfBoundsException;

  /**
   * Passes an SPNEGO/Kerberos token to the Session Manager so that it may extract
   * the delegated user identity for use in subsequent Head Requests
   * @param sessionId
   * @param spnegoBlob  SPNEGO/Kerberos token fetched from the client
   * @return            the Kerberos identity if the operation completed
   *                    successfully, null otherwise
   * @throws            IndexOutOfBoundsException if the sessionId given is not
   *                    an existing sessionId
   */
  public KerberosId storeKrb5Identity(String sessionId, String spnegoBlob)
    throws IndexOutOfBoundsException;

  /**
   * Requests a Kerberos KeyMaterial object based on the currently Kerberos
   * identity associated with the session
   * @param sessionId
   * @param server      Target server name
   * @return            Base64-encoded value of the SPNEGO/Kerberos token
   * @throws            IndexOutOfBoundsException if the sessionId given is not
   *                    an existing sessionId
   */
  public KeyMaterial getKrb5TokenForServer(String sessionId, String server)
    throws IndexOutOfBoundsException;

  /**
   * Returns the Kerberos identity if it has been initialized.
   * @param sessionId
   * @return            A non-null string with the Kerberos identity if the
   *                    credentials have been properly initialized for
   *                    delegation. Null otherwise.
   * @throws            IndexOutOfBoundsException if the sessionId given is not
   *                    an existing sessionId
   */
  public String getKrb5Identity(String sessionId)
      throws IndexOutOfBoundsException;

  /**
   * Returns the path to the Kerberos Credentials Cache where the user
   * credentials are stored.
   * @param sessionId
   * @return            Credentials Cache filename.
   * @throws            IndexOutOfBoundsException if the sessionId given is not
   *                    an existing sessionId
   */
  public String getKrb5CcacheFilename(String sessionId)
      throws IndexOutOfBoundsException;

  /**
   * Parses the given keytab filename.
   * @return           Principal name inside the first entry in the keytab
   *                   file on success, null otherwise.
   * @throws           RuntimeException if not implemented.
   */
  public String parseKrb5Keytab(String filepath);

  /**
   * Gets the server principal name.
   * @return           Server principal name if the Kerberos engine has been
   *                   initialized, null otherwise.
   * @throws           RuntimeException if not implemented.
   */
  public String getKrb5ServerNameIfEnabled();
}
