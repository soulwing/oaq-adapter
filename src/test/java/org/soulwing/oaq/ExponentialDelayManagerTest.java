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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;

import org.junit.Test;
import org.soulwing.oaq.ExponentialDelayManager;

/**
 * Unit tests for {@link ExponentialDelayManager}.
 *
 * @author Carl Harris
 */
public class ExponentialDelayManagerTest {

  private static final long PERIOD = 50;
  private static final long FUDGE = 10;
  
  private ExponentialDelayManager delayManager =
      new ExponentialDelayManager();
  
  @Test(timeout = PERIOD + FUDGE)
  public void testInitialPause() throws InterruptedException {
    long start = System.currentTimeMillis();    
    delayManager.setPeriod(PERIOD);
    delayManager.pause();
    long elapsed = System.currentTimeMillis() - start;
    assertThat(elapsed, greaterThanOrEqualTo(PERIOD));
  }
  
  @Test(timeout = PERIOD + 2*PERIOD + FUDGE)
  public void testNextPause() throws InterruptedException {
    long start = System.currentTimeMillis();    
    delayManager.setPeriod(PERIOD);
    delayManager.pause();
    delayManager.pause();
    long elapsed = System.currentTimeMillis() - start;
    assertThat(elapsed, greaterThanOrEqualTo(PERIOD + 2*PERIOD));
  }

  @Test(timeout = PERIOD + PERIOD + FUDGE)
  public void testPauseAfterReset() throws InterruptedException {
    long start = System.currentTimeMillis();    
    delayManager.setPeriod(PERIOD);
    delayManager.pause();
    delayManager.reset();
    delayManager.pause();
    long elapsed = System.currentTimeMillis() - start;
    assertThat(elapsed, greaterThanOrEqualTo(PERIOD + PERIOD));
  }
  
  @Test(timeout = PERIOD + FUDGE)
  public void testResetWhilePaused() throws InterruptedException {
    Thread thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Thread.sleep(PERIOD / 2);
          delayManager.reset();
        }
        catch (InterruptedException ex) {
          assert true;
        }
      } 
    });
    
    long start = System.currentTimeMillis();    
    delayManager.setPeriod(PERIOD);
    thread.start();
    delayManager.pause();
    long elapsed = System.currentTimeMillis() - start;
    assertThat(elapsed, lessThan(PERIOD));
  }

}
