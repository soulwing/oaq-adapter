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

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.ServerSessionPool;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicSession;

/**
 * A proxy for a JMS connection associated with a managed connection.
 *
 * @author Carl Harris
 */
public class ManagedConnectionProxy
    implements Connection, QueueConnection, TopicConnection,
    ExceptionListener, Disposable {
  
  private final MessageManagedConnection parent;

  /**
   * Constructs a new instance.
   * @param parent
   */
  public ManagedConnectionProxy(MessageManagedConnection parent) {
    this.parent = parent;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void close() throws JMSException {
    if (parent == null) return;
    parent.closeConnection(this);  
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    // TODO clean up associated session proxy manager
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Session createSession() throws JMSException {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Session createSession(int sessionMode) throws JMSException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Session createSession(boolean transacted, int acknowledgeMode) 
      throws JMSException {
    if (!transacted && acknowledgeMode == Session.SESSION_TRANSACTED) {
      acknowledgeMode = Session.AUTO_ACKNOWLEDGE;
    }
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public QueueSession createQueueSession(boolean transaction, 
      int acknowledgeMode) throws JMSException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public TopicSession createTopicSession(boolean transaction, 
      int acknowledgeMode) throws JMSException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionConsumer createConnectionConsumer(Topic topic, 
      String messageSelector, ServerSessionPool sessionPool, int maxMessages) 
      throws JMSException {
    throw (JMSException) new JMSException("unsupported operation")
        .initCause(new UnsupportedOperationException());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionConsumer createConnectionConsumer(Queue queue, 
      String messageSelector, ServerSessionPool sessionPool, int maxMessages) 
      throws JMSException {
    throw (JMSException) new JMSException("unsupported operation")
        .initCause(new UnsupportedOperationException());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionConsumer createConnectionConsumer(Destination destination,
      String messageSelector, ServerSessionPool sessionPool, int maxMessages) 
          throws JMSException {
    throw (JMSException) new JMSException("unsupported operation")
        .initCause(new UnsupportedOperationException());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionConsumer createDurableConnectionConsumer(Topic topic,
      String subscriptionName, String messageSelector, 
      ServerSessionPool sessionPool, int maxMessages) throws JMSException {
    throw (JMSException) new JMSException("unsupported operation")
        .initCause(new UnsupportedOperationException());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionConsumer createSharedConnectionConsumer(Topic topic,
      String subscriptionName, String messageSelector, 
      ServerSessionPool sessionPool, int maxMessages) throws JMSException {
    throw (JMSException) new JMSException("unsupported operation")
        .initCause(new UnsupportedOperationException());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic,
      String subscriptionName, String messageSelector, 
      ServerSessionPool sessionPool, int maxMessages) throws JMSException {
    throw (JMSException) new JMSException("unsupported operation")
        .initCause(new UnsupportedOperationException());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ConnectionMetaData getMetaData() throws JMSException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getClientID() throws JMSException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setClientID(String clientId) throws JMSException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExceptionListener getExceptionListener() throws JMSException {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setExceptionListener(ExceptionListener exceptionListener)
      throws JMSException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void onException(JMSException ex) {
    // TODO Auto-generated method stub
  
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() throws JMSException {
    // TODO Auto-generated method stub

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void stop() throws JMSException {
    // TODO Auto-generated method stub

  }

}
