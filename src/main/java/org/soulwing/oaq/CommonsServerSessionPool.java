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

import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.resource.spi.UnavailableException;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * A {@link MutableServerSessionPool} implemented using Commons Pool.
 *
 * @author Carl Harris
 */
public class CommonsServerSessionPool implements MutableServerSessionPool {

  private final GenericObjectPool<ServerSession> pool;
  
  /**
   * Constructs a new instance.
   * @param endpoint
   */
  public CommonsServerSessionPool(MessageEndpointDetails endpoint,
      PoolableObjectFactory<ServerSession> objectFactory) {
    this.pool = new GenericObjectPool<ServerSession>(objectFactory);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ServerSession getServerSession() throws JMSException {
    try {
      return pool.borrowObject();
    }
    catch (RuntimeException ex) {
      throw ex;
    }
    catch (UnavailableException ex) {
      throw (JMSException) new JMSException("cannot create endpoint")
          .initCause(ex);
    }
    catch (JMSException ex) {
      throw ex;
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Releases a session (obtained via {@link #getServerSession()}) back
   * to the pool.
   * @param session
   */
  @Override
  public void releaseSession(ServerSession session) {
    try {
      pool.returnObject(session);
    }
    catch (RuntimeException ex) {
      throw ex;
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * Permanently removes a session from the pool.
   * @param session the session to remove
   */
  @Override
  public void invalidateSession(ServerSession session) {
    try {
      pool.invalidateObject(session);
    }
    catch (RuntimeException ex) {
      throw ex;
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
 
  /**
   * {@inheritDoc}
   */
  @Override
  public void close() {
    // TODO
    throw new UnsupportedOperationException();
  }

  /**
   * Gets the maximum number of sessions allowed in the pool.
   * @return number of sessions
   * @see org.apache.commons.pool.impl.GenericObjectPool#getMaxActive()
   */
  public int getMaxActive() {
    return pool.getMaxActive();
  }

  /**
   * Sets the maximum number of sessions to allow in the pool.
   * @param maxActive number of sessions
   * @see org.apache.commons.pool.impl.GenericObjectPool#setMaxActive(int)
   */
  public void setMaxActive(int maxActive) {
    pool.setMaxActive(maxActive);
  }

  /**
   * Gets the maximum number of idle sessions to allow in the pool.
   * @return number of sessions
   * @see org.apache.commons.pool.impl.GenericObjectPool#getMaxIdle()
   */
  public int getMaxIdle() {
    return pool.getMaxIdle();
  }

  /**
   * Sets the maximum number of idle sessions to allow in the pool.
   * @param maxIdle number of sessions
   * @see org.apache.commons.pool.impl.GenericObjectPool#setMaxIdle(int)
   */
  public void setMaxIdle(int maxIdle) {
    pool.setMaxIdle(maxIdle);
  }

}
