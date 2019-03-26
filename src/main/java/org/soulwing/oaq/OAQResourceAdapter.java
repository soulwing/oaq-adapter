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

import static org.soulwing.oaq.OAQLogger.LOGGER;

import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

/**
 * A {@link ResourceAdapter} for Oracle AQ.
 *
 * @author Carl Harris
 */
public class OAQResourceAdapter implements MessageResourceAdapter {

  private final OAQConnectionRequestInfo connectionInfo =
      new OAQConnectionRequestInfo();

  private final MessageEndpointManager endpointManager;
  private final MessageConnectionFactoryProvider connectionFactoryProvider;
  
  private BootstrapContext bootstrapContext;
  private volatile XAConnectionFactory connectionFactory;

  /**
   * Constructs a new instance.
   */
  public OAQResourceAdapter() {
    this.endpointManager = new MapMessageEndpointManager(this);
    this.connectionFactoryProvider = new OAQConnectionFactoryProvider();
  }

  /**
   * Constructs a new instance.
   * @param endpointManager endpoint manager
   * @param connectionFactoryProvider connection factory provider
   */
  protected OAQResourceAdapter(MessageEndpointManager endpointManager,
      MessageConnectionFactoryProvider connectionFactoryProvider) {
    this.endpointManager = endpointManager;
    this.connectionFactoryProvider = connectionFactoryProvider;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void start(BootstrapContext ctx)
      throws ResourceAdapterInternalException {
    this.bootstrapContext = ctx;
    LOGGER.info("resource adapter started; " + connectionInfo);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() {
    endpointManager.dispose();
    bootstrapContext = null;
    connectionFactory = null;
    LOGGER.info("resource adapter stopped"); 
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endpointActivation(MessageEndpointFactory endpointFactory,
      ActivationSpec activationSpec) throws ResourceException {

    LOGGER.fine("activating endpoint with spec " + activationSpec);
    // JCA 1.7 section 5.3.3
    if (!this.equals(activationSpec.getResourceAdapter())) {
      throw new ResourceException(
          "Activation spec refers to different resource adapter");
    }
    
    if (!(activationSpec instanceof MessageActivationSpec)) {
      throw new ResourceException("Unrecognized activation spec type: "
          + activationSpec.getClass().getName());
    }
    
    try {
      MessageEndpointRunner endpoint = endpointManager.create(
          endpointFactory, (MessageActivationSpec) activationSpec);
      endpoint.start();
    }
    catch (RuntimeException ex) {
      new ResourceException("failed to start endpoint", ex);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void endpointDeactivation(MessageEndpointFactory endpointFactory,
      ActivationSpec activationSpec) {
    LOGGER.fine("deactivating endpoint with spec " + activationSpec);
    if (activationSpec instanceof MessageActivationSpec) {
      try {
        MessageEndpointRunner endpoint = endpointManager.remove(
            endpointFactory, (MessageActivationSpec) activationSpec);
        endpoint.stop();
      }
      catch (RuntimeException ex) {
        LOGGER.warning("failed to stop endpoint: " + ex);
      }
    }
    else {
      LOGGER.warning("unrecognized activation spec type: " 
          + activationSpec.getClass().getName());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public XAResource[] getXAResources(ActivationSpec[] specs)
      throws ResourceException {
    // There's only one resource manager (Oracle AQ) used by this adapter,
    // so all of the provided specs can utilize the same XA resource
    XAConnection connection = null;
    try {
      connection = createConnection(connectionInfo);
      XASession session = connection.createXASession();
      XAResource resource = session.getXAResource();
      return new XAResource[] { resource };
    }
    catch (JMSException ex) {
      throw new ResourceException(ex);
    }
    finally {
      try {
        connection.close();
      }
      catch (JMSException ex) {
        assert true;
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public BootstrapContext getBootstrapContext() {
    return bootstrapContext;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public XAConnection createConnection(MessageActivationSpec spec)
      throws JMSException {
    OAQConnectionRequestInfo info = connectionInfo.clone();
    if (spec.getUsername() != null) {
      info.setUsername(spec.getUsername());
      info.setPassword(spec.getPassword());
    }
    return createConnection(info);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public XAConnection createConnection(OAQConnectionRequestInfo info) 
      throws JMSException {
    return getConnectionFactory().createXAConnection(
        info.getUsername(), info.getPassword());
  }

  /**
   * Gets the adapter's (singleton) connection factory instance.
   * @return connection factory.
   * @throws JMSException
   */
  private XAConnectionFactory getConnectionFactory() throws JMSException {
    if (connectionFactory == null) {
      synchronized (this) {
         connectionFactory = 
             connectionFactoryProvider.createConnectionFactory(connectionInfo);
      }
    }
    return connectionFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAQConnectionRequestInfo getConnectionRequestInfo() {
    return connectionInfo;
  }

  /**
   * Gets the JDBC URL that will be used to connect to the database.
   * @return database URL or {@code null} if no URL has been configured
   */
  public String getDatabaseUrl() {
    return connectionInfo.getDatabaseUrl();
  }

  /**
   * Sets the JDBC URL that will be used to connect to the database.
   * @param databaseUrl the database URL to set
   */
  public void setDatabaseUrl(String databaseUrl) {
    connectionInfo.setDatabaseUrl(databaseUrl);
  }

  /**
   * Gets the username that will be used to connect to the database.
   * @return username or {@code null} if none has been set
   */
  public String getUsername() {
    return connectionInfo.getUsername();
  }

  /**
   * Sets the username that will be used to connect to the database.
   * @param username the username to set
   */
  public void setUsername(String username) {
    connectionInfo.setUsername(username);
  }

  /**
   * Gets the password that will be used to connect to the database.
   * @return password or {@code null} if none has been set
   */
  public String getPassword() {
    return connectionInfo.getPassword();
  }

  /**
   * Sets the password that will be used to connect to the database.
   * @param password the password to set
   */
  public void setPassword(String password) {
    connectionInfo.setPassword(password);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

}
