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

import javax.jms.JMSException;
import javax.jms.ServerSession;
import javax.jms.Session;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;

/**
 * A {@link ServerSession} that dispatches message consumption using
 * a {@link WorkManager}.
 *
 * @author Carl Harris
 */
class ServerSessionWork implements ServerSession, Work {

  private final Logger logger = Logger.getLogger(getClass().getName());
  private final AtomicBoolean started = new AtomicBoolean();
  
  private final Session delegate;
  private final WorkManager workManager;
  private final MutableServerSessionPool sessionPool;
  private final SessionContext sessionContext;
  
  /**
   * Constructs a new instance.
   * @param delegate session delegate
   * @param workManager work manager that will run the session
   * @param sessionPool server session pool
   */
  public ServerSessionWork(Session delegate, 
      WorkManager workManager, MutableServerSessionPool sessionPool) {
    this(delegate, workManager, sessionPool, 
        ThreadLocalSessionContext.getInstance());
  }

  /**
   * Constructs a new instance.
   * @param delegate session delegate
   * @param workManager work manager that will run the session
   * @param sessionPool server session pool
   * @param sessionContext session context 
   */
  protected ServerSessionWork(Session delegate, 
      WorkManager workManager, MutableServerSessionPool sessionPool,
      SessionContext sessionContext) {
    this.delegate = delegate;
    this.workManager = workManager;
    this.sessionPool = sessionPool;
    this.sessionContext = sessionContext;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public Session getSession() throws JMSException {
    return delegate;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void start() throws JMSException {
    if (!started.compareAndSet(false, true)) {
      throw new JMSException("already started");
    }
    try {
      workManager.doWork(this, WorkManager.INDEFINITE, null, null);
    }
    catch (WorkException ex) {
      throw (JMSException) new JMSException(
          "cannot start session: " + ex.getMessage()).initCause(ex);
    }
  }

  /**
   * Closes the underlying session.
   * @throws JMSException
   */
  public void close() throws JMSException {
    delegate.close();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void run() {
    sessionContext.set(delegate);
    try {        
      delegate.run();
      sessionPool.releaseSession(this);
    }
    catch (Throwable ex) {
      logger.warning("session stopped on error: " + ex);
      sessionPool.invalidateSession(this);
    }
    finally {
      sessionContext.clear();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void release() {
  }
  
}
