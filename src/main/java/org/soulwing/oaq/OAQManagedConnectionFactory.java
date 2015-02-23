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
import java.util.Iterator;
import java.util.Set;

import javax.jms.JMSException;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

import org.apache.commons.lang.Validate;

/**
 * A {@link ManagedConnectionFactory} that provides access to Oracle AQ.
 *
 * @author Carl Harris
 */
public class OAQManagedConnectionFactory
    implements ManagedConnectionFactory, ResourceAdapterAssociation {

  private static final long serialVersionUID = -3798948417121742603L;

  private PrintWriter logWriter;
  private MessageResourceAdapter resourceAdapter;
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Object createConnectionFactory() throws ResourceException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object createConnectionFactory(ConnectionManager connectionManager)
      throws ResourceException {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ManagedConnection createManagedConnection(Subject subject,
      ConnectionRequestInfo connectionRequestInfo) throws ResourceException {
    OAQConnectionRequestInfo info = 
        resourceAdapter.getConnectionRequestInfo();
    if (connectionRequestInfo instanceof OAQConnectionRequestInfo) {
      info = (OAQConnectionRequestInfo) connectionRequestInfo;
    }
    try {
      OAQManagedConnection connection = 
          new OAQManagedConnection(subject, info, 
              resourceAdapter.createConnection(info));
      
      connection.setLogWriter(getLogWriter());
      return connection;
    }
    catch (JMSException ex) {
      throw new ResourceException("failed to create connection", ex);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("rawtypes")
  public ManagedConnection matchManagedConnections(Set connectionSet, 
      Subject subject, ConnectionRequestInfo info) throws ResourceException {
    Iterator i = connectionSet.iterator();
    while (i.hasNext()) {
      OAQManagedConnection connection = (OAQManagedConnection) i.next();
      if (connection.matches(subject, info)) {
        connection.setLogWriter(getLogWriter());
        return connection;
      }
    }
    return null;
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
  public ResourceAdapter getResourceAdapter() {
    return resourceAdapter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setResourceAdapter(ResourceAdapter resourceAdapter)
      throws ResourceException {
    Validate.isTrue(resourceAdapter instanceof MessageResourceAdapter,
        "unrecognized resource adapter type");
    this.resourceAdapter = (MessageResourceAdapter) resourceAdapter;
  }

}
