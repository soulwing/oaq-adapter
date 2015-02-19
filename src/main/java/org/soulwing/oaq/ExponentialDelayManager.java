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

import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link ReconnectDelayManager} that implements a simple truncated
 * exponentially increasing delay.
 *
 * @author Carl Harris
 */
class ExponentialDelayManager implements ReconnectDelayManager {

  private static final long DEFAULT_PERIOD = 1000;
  private static final long DEFAULT_MAX_DELAY = 60000;
  
  private final Lock lock = new ReentrantLock();
  private final Condition resetCondition = lock.newCondition();
  
  private int count;
  private boolean waiting;
  private long period = DEFAULT_PERIOD;
  private long maxDelay = DEFAULT_MAX_DELAY;
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void pause() throws InterruptedException {
    lock.lock();
    try {
      waiting = true;
      long delay = Math.min(getMaxDelay(), 
          (long)(Math.pow(2, count++)*getPeriod()));
      
      Date deadline = new Date(System.currentTimeMillis() + delay);
      boolean stillWaiting = true;
      while (waiting && stillWaiting) {
        stillWaiting = resetCondition.awaitUntil(deadline);
      }
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void reset() {
    lock.lock();
    try {
      count = 0;
      waiting = false;
      resetCondition.signalAll();
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * Gets the {@code period} property.
   * @return
   */
  public long getPeriod() {
    return period;
  }

  /**
   * Sets the {@code period} property.
   * @param period
   */
  public void setPeriod(long period) {
    this.period = period;
  }

  /**
   * Gets the {@code maxDelay} property.
   * @return
   */
  public long getMaxDelay() {
    return maxDelay;
  }

  /**
   * Sets the {@code maxDelay} property.
   * @param maxDelay
   */
  public void setMaxDelay(long maxDelay) {
    this.maxDelay = maxDelay;
  }

}
