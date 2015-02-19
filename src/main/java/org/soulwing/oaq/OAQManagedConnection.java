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

import java.io.PrintWriter;

import javax.jms.XAConnection;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

import org.apache.commons.lang.Validate;

/**
 * A {@link MessageManagedConnection} to Oracle AQ.
 *
 * @author Carl Harris
 */
public class OAQManagedConnection 
    implements ManagedConnection, MessageManagedConnection {

  private final ConnectionEventListenerSupport listenerSupport =
      new ConnectionEventListenerSupport(this);
  
  private final MessageConnectionManager connectionManager = 
      new MessageConnectionManager();
  
  private Subject subject;
  private ConnectionRequestInfo info;
  private XAConnection delegate;
  private PrintWriter logWriter;
  
  /**
   * Constructs a new instance.
   * @param subject
   * @param info
   * @param delegate
   */
  public OAQManagedConnection(Subject subject, 
      ConnectionRequestInfo info,
      XAConnection physicalConnection) {
    this.subject = subject;
    this.info = info;
    this.delegate = physicalConnection;
  }

  public XAConnection getDelegate() {
    return delegate;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void cleanup() throws ResourceException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy() throws ResourceException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getConnection(Subject subject, ConnectionRequestInfo info)
      throws ResourceException {
    return connectionManager.create(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void associateConnection(Object connection) throws ResourceException {
    if (!(connection instanceof ManagedConnectionProxy)) {
      throw new ResourceException("cannot associate connection of type " 
          + connection.getClass().getName());
    }
    connectionManager.add((ManagedConnectionProxy) connection);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void closeConnection(Object connection) {
    Validate.isTrue(connection instanceof ManagedConnectionProxy);
    connectionManager.remove((ManagedConnectionProxy) connection);
    listenerSupport.fireConnectionClosed(connection);
  }
  

  /**
   * {@inheritDoc}
   */
  @Override
  public LocalTransaction getLocalTransaction() throws ResourceException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PrintWriter getLogWriter() throws ResourceException {
    return logWriter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setLogWriter(PrintWriter logWriter) throws ResourceException {
    this.logWriter = logWriter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ManagedConnectionMetaData getMetaData() throws ResourceException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public XAResource getXAResource() throws ResourceException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addConnectionEventListener(ConnectionEventListener listener) {
    listenerSupport.addConnectionEventListener(listener);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeConnectionEventListener(ConnectionEventListener listener) {
    listenerSupport.removeConnectionEventListener(listener);
  }

  /**
   * Determines whether the receiver matches the given subject and 
   * delegate info
   * @param subject subject to match
   * @param info info to match
   * @return {@code true} if subject and info both match the receiver
   */
  public boolean matches(Subject subject, ConnectionRequestInfo info) {
    if (this.subject == null ^ subject == null) return false;
    if (this.subject == null || !this.subject.equals(subject)) return false;
    if (this.info == null ^ info == null) return false;
    if (this.info == null || !this.info.equals(subject)) return false;
    return true;
  }

}
