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

import javax.jms.Session;

/**
 * A {@link SessionContext} that utilizes a {@link ThreadLocal}.
 *
 * @author Carl Harris
 */
public class ThreadLocalSessionContext implements SessionContext {

  private static final SessionContext instance = 
      new ThreadLocalSessionContext();
  
  private final ThreadLocal<Session> threadLocal =
      new ThreadLocal<Session>();
  
  /**
   * Gets the (singleton) instance.
   * @return session context instance
   */
  public static SessionContext getInstance() {
    return instance;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void set(Session session) {
    threadLocal.set(session);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void clear() {
    threadLocal.set(null);
  }

}
