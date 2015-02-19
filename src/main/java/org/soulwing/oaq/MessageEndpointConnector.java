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

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;

/**
 * A {@link Work} object that connects an endpoint to Oracle AQ.  
 *
 * @author Carl Harris
 */
class MessageEndpointConnector implements Work {

  public interface Callback {
    void connectionReady(XAConnection connection) throws JMSException;
    void connectionFailed(XAConnection connection) throws JMSException;
  }
  

  private final Logger logger = Logger.getLogger(getClass().getName());
  
  private final AtomicBoolean running = new AtomicBoolean();
  private final AtomicBoolean connecting = new AtomicBoolean();
  
  private final MessageEndpointDetails endpoint;
  private final Callback callback;
  private final ReconnectDelayManager delayManager;
  
  private XAConnection connection;
    
  /**
   * Constructs a new instance.
   * @param endpoint endpoint details
   * @param callback callback for connection notifications
   */
  public MessageEndpointConnector(MessageEndpointDetails endpoint,
      Callback callback) {
    this(endpoint, callback, new ExponentialDelayManager());
  }

  /**
   * Constructs a new instance.
   * @param endpoint endpoint details
   * @param callback callback for connection notifications
   * @param delayManager reconnect delay manager
   */
  protected MessageEndpointConnector(MessageEndpointDetails endpoint,
      Callback callback, ReconnectDelayManager delayManager) {
    this.endpoint = endpoint;
    this.callback = callback;
    this.delayManager = delayManager;
  }


  /**
   * Initiates a connection request.
   */
  public void start() {
    if (!running.compareAndSet(false, true)) {
      logger.warning("connector is already running");
      return;
    }    
    connecting.set(true);
    connect();
  }

  /**
   * Terminates any existing connection attempt in progress
   */
  public void stop() {
    if (!running.compareAndSet(true, false)) {
      logger.warning("connector is not running");
      return;
    }
    delayManager.reset();
    if (connection != null) {
      try {
        connection.close();
        connection = null;
      }
      catch (JMSException ex) {
        logger.warning("failed to close AQ connection: " + ex.getMessage());
      }
    }
  }

  private void connect() {
    try {
      endpoint.getWorkManager().doWork(this);
    }
    catch (WorkException ex) {
      logger.severe("work manager won't accept work: " + ex.toString());
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void run() {
    try {
      while (running.get() && connecting.get()) {
        XAConnection connection = null;
        try {
          connection = endpoint.createXAConnection();
          connection.setExceptionListener(new ExceptionHandler(connection));
          connection.start();
          connecting.set(false);
          delayManager.reset();
          callback.connectionReady(connection);
          this.connection = connection;
        }
        catch (JMSException ex) {
          cleanup(connection);
          delayManager.pause();
        }
      }
    }
    catch (InterruptedException ex) {
      assert true;  // simply bail out on interrupt
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void release() {
  }

  /**
   * An {@link ExceptionListener} that reconnects after an exception is 
   * thrown.
   */
  private class ExceptionHandler implements ExceptionListener {

    private final XAConnection connection;
    
    /**
     * Constructs a new instance.
     * @param connection
     */
    public ExceptionHandler(XAConnection connection) {
      this.connection = connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onException(JMSException ex) {
      if (!running.get()) return;
      if (!connecting.compareAndSet(false, true)) return;
      cleanup(connection);
      try {
        delayManager.pause();
        connect();
      }
      catch (InterruptedException iex) {
        assert true;  // don't attempt to reconnect if interrupted
      }
    }    
   
  }
  
  private void cleanup(XAConnection connection) {
    if (connection == null) return;
    try {
      callback.connectionFailed(connection);
    }
    catch (JMSException ex) {
      logger.warning("error while cleaning up: " + ex.getMessage());
    }
    try {
      connection.close();
    }
    catch (JMSException ex) {
      assert true;  // safe to ignore here
    }
  }

}
