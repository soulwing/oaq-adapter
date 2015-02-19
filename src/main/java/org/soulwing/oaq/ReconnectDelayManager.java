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

/**
 * An object that manages reconnect delays.
 *
 * @author Carl Harris
 */
interface ReconnectDelayManager {

  /**
   * Pauses the calling thread until an appropriate delay interval has
   * elapsed or until {@link #reset()} is invoked by another thread. 
   * @throws InterruptedException if the calling thread is interrupted while
   *    waiting
   */
  void pause() throws InterruptedException;
  
  /**
   * Resets the state of the delay manager and causes any thread waiting
   * in {@link #pause()} to return immediately.
   * <p>
   * Typically this method would be called after a successful connection,
   * so that subsequent reconnect attempts would start with the appropriate
   * initial delay.
   * <p>
   * This method should also be called if a pause in progress should return
   * immediately; e.g. when a component is shutting down.
   */
  void reset();
  
}
