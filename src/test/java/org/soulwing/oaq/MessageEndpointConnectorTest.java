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

import static org.jmock.lib.script.ScriptedAction.perform;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.resource.spi.work.WorkManager;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.lib.action.DoAllAction;
import org.jmock.lib.action.VoidAction;
import org.junit.Test;
import org.soulwing.oaq.MessageEndpointConnector;
import org.soulwing.oaq.MessageEndpointDetails;
import org.soulwing.oaq.ReconnectDelayManager;

/**
 * Unit tests for {@link MessageEndpointConnector}.
 *
 * @author Carl Harris
 */
public class MessageEndpointConnectorTest {

  private Mockery mockery = new Mockery();
  
  private XAConnection connection = mockery.mock(XAConnection.class);
  
  private WorkManager workManager = mockery.mock(WorkManager.class);
  
  private MessageEndpointDetails endpoint = 
      mockery.mock(MessageEndpointDetails.class);
  
  private MessageEndpointConnector.Callback callback = 
      mockery.mock(MessageEndpointConnector.Callback.class);
  
  private ReconnectDelayManager delayManager = 
      mockery.mock(ReconnectDelayManager.class);
  
  private MessageEndpointConnector connector = 
      new MessageEndpointConnector(endpoint, callback, delayManager);
  
  @Test
  public void testNormalStart() throws Exception {
    mockery.checking(new Expectations() { {
      // the connector is scheduled with the work manager
      oneOf(endpoint).getWorkManager();
      will(returnValue(workManager));
      oneOf(workManager).doWork(with(same(connector)));
      will(perform("$0.run()"));
      
      // the run loop creates, configures, and starts the connection
      oneOf(endpoint).createXAConnection();
      will(returnValue(connection));
      oneOf(connection).setExceptionListener(with(any(ExceptionListener.class)));
      oneOf(connection).start();
      
      // the callback is notified that the connection is ready
      oneOf(callback).connectionReady(with(same(connection)));
      oneOf(delayManager).reset();
    } });
    
    connector.start();
    mockery.assertIsSatisfied();
  }

  @Test
  public void testFailsWhileConnecting() throws Exception {
    
    // Set up to experience failCount exceptions on connection.start() 
    // followed by a success.
    final int failCount = 3;  // any non-zero value
    final Action[] startActions = new Action[failCount + 1];
    for (int i = 0; i < failCount; i++) {
      startActions[i] = Expectations.throwException(new JMSException("mock"));
    }
    startActions[failCount] = VoidAction.INSTANCE;

    mockery.checking(new Expectations() { {
      // the connector is scheduled exactly once
      oneOf(endpoint).getWorkManager();
      will(returnValue(workManager));
      oneOf(workManager).doWork(with(same(connector)));
      will(perform("$0.run()"));
      
      // the run loop creates, configures, and starts a connection 
      // failCount + 1 times
      exactly(failCount + 1).of(endpoint).createXAConnection();
      will(returnValue(connection));
      exactly(failCount + 1).of(connection).setExceptionListener(
          with(any(ExceptionListener.class)));
      exactly(failCount + 1).of(connection).start();
      will(onConsecutiveCalls(startActions));
      
      // the callback is notified failCount times that the connection failed
      exactly(failCount).of(callback).connectionFailed(with(same(connection)));
      exactly(failCount).of(connection).close();
      exactly(failCount).of(delayManager).pause();
      
      // finally, the callback is notified that the connection is ready
      oneOf(callback).connectionReady(with(same(connection)));
      oneOf(delayManager).reset();
      
    } });
    
    connector.start();
    mockery.assertIsSatisfied();
  }
  
  @Test
  public void testFailsAfterConnecting() throws Exception {
    final ExceptionListener[] listener = new ExceptionListener[1];
   
    mockery.checking(new Expectations() { {
      // the connector is scheduled exactly twice; 
      // once at start, and again after the initial failure occurs
      exactly(2).of(endpoint).getWorkManager();
      will(returnValue(workManager));
      exactly(2).of(workManager).doWork(with(same(connector)));
      will(perform("$0.run()"));
      
      // exactly twice, the run loop creates, configures, and 
      // starts a connection; once for the initial success, and again
      // on the reconnect after the connection fails
      exactly(2).of(endpoint).createXAConnection();
      will(returnValue(connection));
      exactly(2).of(connection).setExceptionListener(
          with(any(ExceptionListener.class)));
      will(new DoAllAction(
          perform("listener[0] = $0").where("listener", listener),
          returnValue(null)));
      exactly(2).of(connection).start();
      
      // the callback is twice notified that the connection is ready;
      // once on the initial success, and again after successful reconnect
      exactly(2).of(callback).connectionReady(with(same(connection)));
      exactly(2).of(delayManager).reset();
      
      // the callback is notified just once that the connection 
      // has failed (after the initial success)
      oneOf(callback).connectionFailed(with(same(connection)));
      oneOf(connection).close();
      oneOf(delayManager).pause();
      
    } });
    
    connector.start();
    listener[0].onException(new JMSException("mock"));
    mockery.assertIsSatisfied();
  }

  @Test
  public void testFailsDuringReconnect() throws Exception {
    final ExceptionListener[] listener = new ExceptionListener[1];
   
    // Set up to experience a success, failCount exceptions during reconnect
    // followed by a final success.
    final int failCount = 3;  // any non-zero value
    final Action[] startActions = new Action[failCount + 2];
    startActions[0] = VoidAction.INSTANCE;
    for (int i = 0; i < failCount; i++) {
      startActions[i + 1] = Expectations.throwException(new JMSException("mock"));
    }
    startActions[failCount + 1] = VoidAction.INSTANCE;

    mockery.checking(new Expectations() { {
      // the connector is scheduled exactly twice; 
      // once at start, and again after the initial failure occurs
      exactly(2).of(endpoint).getWorkManager();
      will(returnValue(workManager));
      exactly(2).of(workManager).doWork(with(same(connector)));
      will(perform("$0.run()"));
      
      // failCount + 2 times, the run loop creates, configures, and 
      // starts a connection; once for the initial success, failCount
      // times for failed reconnect attempts, and once more for the successful
      // reconnect
      exactly(failCount + 2).of(endpoint).createXAConnection();
      will(returnValue(connection));
      exactly(failCount + 2).of(connection).setExceptionListener(
          with(any(ExceptionListener.class)));
      will(new DoAllAction(
          perform("listener[0] = $0").where("listener", listener),
          returnValue(null)));
      exactly(failCount + 2).of(connection).start();
      will(onConsecutiveCalls(startActions));
      
      // the callback is twice notified that the connection is ready;
      // once on the initial success, and again after successful reconnect
      exactly(2).of(callback).connectionReady(with(same(connection)));
      exactly(2).of(delayManager).reset();
      
      // the callback is notified failCount + 1 times that the connection 
      // has failed; once on the first failure (after the initial connection
      // succeeds) and failCount more times for each failed reconnect attempt
      // the occurs after initial success
      exactly(failCount + 1).of(callback).connectionFailed(
          with(same(connection)));
      exactly(failCount + 1).of(connection).close();
      exactly(failCount + 1).of(delayManager).pause();
    } });
    
    connector.start();
    listener[0].onException(new JMSException("mock"));
    mockery.assertIsSatisfied();
  }

}
