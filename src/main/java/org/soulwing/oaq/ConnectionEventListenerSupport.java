/*
 * File created on Nov 26, 2013 
 *
 * Copyright (c) 2013 Carl Harris, Jr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.soulwing.oaq;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ManagedConnection;


/**
 * Provides support for notifying a collection of 
 * {@link ConnectionEventListeners}.
 *
 * @author Carl Harris
 */
public class ConnectionEventListenerSupport {

  
  private final Set<ConnectionEventListener> listeners =
      new LinkedHashSet<ConnectionEventListener>();

  private final ManagedConnection source;

  /**
   * Constructs a new instance.
   * @param source
   */
  public ConnectionEventListenerSupport(ManagedConnection source) {
    this.source = source;
  }

  /**
   * Adds a listener to the receiver.
   * <p>
   * Adding an existing listener has no effect.
   * 
   * @param listener the listener to add
   */
  public void addConnectionEventListener(ConnectionEventListener listener) {
    this.listeners.add(listener);
  }
  
  /**
   * Removes a listener from the receiver.
   * <p>
   * Attempting to remove a non-existent listener has no effect.
   * 
   * @param listener the listener to remove
   */
  public void removeConnectionEventListener(ConnectionEventListener listener) {
    this.listeners.remove(listener);
  }
  
  /**
   * Fires a connection error event.
   * @param error the error that occurred
   */
  public void fireConnectionError(Exception error) {
    notifyListeners(new ConnectionEvent(source,
        ConnectionEvent.CONNECTION_ERROR_OCCURRED, error));
  }
  
  /**
   * Fires a connection closed event.
   * @param connection handle for the connection proxy
   */
  public void fireConnectionClosed(Object connection) {
    ConnectionEvent event = new ConnectionEvent(source, 
        ConnectionEvent.CONNECTION_CLOSED);
    event.setConnectionHandle(connection);
    notifyListeners(event);    
  }

  /**
   * Fires a local transaction started event.
   */
  public void fireLocalTransactionStarted() {
    notifyListeners(new ConnectionEvent(source,
        ConnectionEvent.LOCAL_TRANSACTION_STARTED));
  }

  /**
   * Fires a local transaction started event.
   */
  public void fireLocalTransactionCommitted() {
    notifyListeners(new ConnectionEvent(source,
        ConnectionEvent.LOCAL_TRANSACTION_COMMITTED));
  }
  
  /**
   * Fires a local transaction rollback event.
   */
  public void fireLocalTransactionRolledBack() {
    notifyListeners(new ConnectionEvent(source,
        ConnectionEvent.LOCAL_TRANSACTION_ROLLEDBACK));
  }
  
  private void notifyListeners(ConnectionEvent event) {
    for (ConnectionEventListener listener : listeners) {
      listener.connectionClosed(event);
    }
  }

}
