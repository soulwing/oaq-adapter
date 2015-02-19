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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.fail;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.resource.spi.work.ExecutionContext;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;
import org.soulwing.oaq.MutableServerSessionPool;
import org.soulwing.oaq.ServerSessionWork;
import org.soulwing.oaq.SessionContext;

/**
 * Unit tests for {@link ServerSessionWork}.
 *
 * @author Carl Harris
 */
public class ServerSessionWorkTest {

  private Mockery mockery = new Mockery();
  
  private Session session = mockery.mock(Session.class);

  private WorkManager workManager = mockery.mock(WorkManager.class);
  
  private MutableServerSessionPool sessionPool = 
      mockery.mock(MutableServerSessionPool.class);
  
  private SessionContext sessionContext = mockery.mock(SessionContext.class);
  
  private ServerSessionWork work = new ServerSessionWork(session, workManager, 
      sessionPool, sessionContext);
  
  
  @Test  
  public void testStart() throws Exception {
    mockery.checking(new Expectations() { { 
      oneOf(workManager).doWork(with(same(work)), 
          with(equalTo(WorkManager.INDEFINITE)),
          with(nullValue(ExecutionContext.class)), 
          with(nullValue(WorkListener.class)));
    } });
    
    work.start();
    mockery.assertIsSatisfied();
  }

  @Test  
  public void testStartWhenStarted() throws Exception {
    mockery.checking(new Expectations() { { 
      oneOf(workManager).doWork(with(same(work)), 
          with(equalTo(WorkManager.INDEFINITE)),
          with(nullValue(ExecutionContext.class)), 
          with(nullValue(WorkListener.class)));
    } });
    
    work.start();
    try {
      work.start();
      fail("expected JMSException");
    }
    catch (JMSException ex) {
      assertThat(ex.getMessage().contains("already"), equalTo(true));
    }
    mockery.assertIsSatisfied();
  }

  @Test
  public void testRunSessionOnRun() throws Exception {
    mockery.checking(new Expectations() { {
      oneOf(sessionContext).set(with(same(session)));
      oneOf(session).run();
      oneOf(sessionPool).releaseSession(with(same(work)));
      oneOf(sessionContext).clear();
    } });
    
    work.run();
  }

  @Test
  public void testRunInvalidatesSessionOnError() throws Exception {
    mockery.checking(new Expectations() { {
      oneOf(sessionContext).set(with(same(session)));
      oneOf(session).run();
      will(throwException(new RuntimeException("mock exception")));
      oneOf(sessionPool).invalidateSession(with(same(work)));
      oneOf(sessionContext).clear();
    } });
    
    work.run();
  }

}
