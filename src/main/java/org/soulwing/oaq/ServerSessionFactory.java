/*
 * File created on Nov 28, 2013 
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

import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.XAConnection;
import javax.jms.XASession;
import javax.resource.spi.UnavailableException;
import javax.transaction.xa.XAResource;

import org.apache.commons.lang.Validate;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.PoolableObjectFactory;

/**
 * A {@link PoolableObjectFactory} that produces {@link ServerSession}
 * objects for an details.
 *
 * @author Carl Harris
 */
public class ServerSessionFactory
    extends BasePoolableObjectFactory<ServerSession> {

  private final MessageEndpointDetails details;
  private MutableServerSessionPool sessionPool;
  private XAConnection connection; 
  
  /**
   * Constructs a new instance.
   * @param details details to associate with the session.
   */
  public ServerSessionFactory(MessageEndpointDetails endpoint) {
    this.details = endpoint;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerSession makeObject() throws JMSException, 
      UnavailableException {
    Validate.notNull(getSessionPool());
    Validate.notNull(getConnection());
    XASession session = getConnection().createXASession();
    XAResource resource = session.getXAResource();
    MessageEndpointProxy endpoint = new MessageEndpointProxy(
        details.getEndpointFactory().createEndpoint(resource));
    session.setMessageListener(endpoint);
    return new ServerSessionWork(session, details.getWorkManager(), 
        getSessionPool());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroyObject(ServerSession session) throws Exception {
    ((ServerSessionWork) session).close();
  }

  /**
   * Gets the {@code sessionPool} property.
   * @return
   */
  public MutableServerSessionPool getSessionPool() {
    return sessionPool;
  }

  /**
   * Sets the {@code sessionPool} property.
   * @param sessionPool
   */
  public void setSessionPool(MutableServerSessionPool sessionPool) {
    this.sessionPool = sessionPool;
  }

  /**
   * Gets the {@code connection} property.
   * @return
   */
  public XAConnection getConnection() {
    return connection;
  }

  /**
   * Sets the {@code connection} property.
   * @param connection
   */
  public void setConnection(XAConnection connection) {
    this.connection = connection;
  }

}
