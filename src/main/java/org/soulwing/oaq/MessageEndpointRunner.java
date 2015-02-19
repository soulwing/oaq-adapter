/*
 * File created on Nov 27, 2013 
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

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.naming.NamingException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;

/**
 * An Oracle AQ message endpoint.
 *
 * @author Carl Harris
 */
public class MessageEndpointRunner implements MessageEndpointDetails, 
    MessageEndpointConnector.Callback, Disposable {

  private final Logger logger = Logger.getLogger(getClass().getName());
  
  private final AtomicBoolean started = new AtomicBoolean();

  private final ServerSessionFactory sessionFactory = 
      new ServerSessionFactory(this);
  
  private final CommonsServerSessionPool sessionPool =
      new CommonsServerSessionPool(this, sessionFactory); 
  
  private final MessageResourceAdapter resourceAdapter;
  private final MessageActivationSpec activationSpec;
  private final MessageEndpointFactory endpointFactory;
  private final MessageEndpointConnector connector;
  
  private ConnectionConsumer consumer;
  
  /**
   * Constructs a new instance.
   * @param resourceAdapter associated resource adapter
   * @param activationSpec endpoint's activation spec
   * @param endpointFactory endpoint factory
   */
  public MessageEndpointRunner(MessageResourceAdapter resourceAdapter,
      MessageActivationSpec activationSpec, 
      MessageEndpointFactory endpointFactory) {
    this.resourceAdapter = resourceAdapter;
    this.activationSpec = activationSpec;
    this.endpointFactory = endpointFactory;
    this.connector = new MessageEndpointConnector(this, this);
    sessionFactory.setSessionPool(sessionPool);
  }

  /**
   * Starts the endpoint 
   */
  public void start() {
    if (!started.compareAndSet(false, true)) {
      logger.severe("endpoint has already been started");
      return;
    }
    connector.start();
  }

  /** 
   * Stops the endpoint
   */
  public void stop() {
    if (!started.compareAndSet(true, false)) {
      logger.warning("endpoint has already been stopped");
      return;
    }
    connector.stop();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    stop();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void connectionReady(XAConnection connection) throws JMSException {
    sessionFactory.setConnection(connection);
    consumer = createConnectionConsumer(connection);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void connectionFailed(XAConnection connection) throws JMSException {
    sessionFactory.setConnection(null);
    if (consumer != null) {
      try {
        consumer.close();
      }
      catch (JMSException ex) {
        assert true;  // okay to ignore here
      }
    }
  }

  private ConnectionConsumer createConnectionConsumer(Connection connection) 
      throws JMSException {
    try {
      if (activationSpec.isDurableSubscription()) {
        return connection.createDurableConnectionConsumer(
            (javax.jms.Topic) activationSpec.lookupDestination(), 
            activationSpec.getSubscriptionName(),
            activationSpec.getMessageSelector(),
            sessionPool, activationSpec.getMaxMessages());
      }
      else {
        return connection.createConnectionConsumer(
            activationSpec.lookupDestination(), 
            activationSpec.getMessageSelector(), 
            sessionPool, activationSpec.getMaxMessages());
      }
    }
    catch (NamingException ex) {
      throw (JMSException) new JMSException("cannot locate destination named '" 
          + activationSpec.getDestination() + "'").initCause(ex);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public WorkManager getWorkManager() {
    return resourceAdapter.getBootstrapContext().getWorkManager();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageEndpointFactory getEndpointFactory() {
    return endpointFactory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageActivationSpec getActivationSpec() {
    return activationSpec;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public XAConnection createXAConnection() throws JMSException {
    return resourceAdapter.createConnection(getActivationSpec());
  }
  
}
