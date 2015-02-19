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
 * An object that manages a context allowing a session to be shared by
 * consumers and producers that are executing on the same thread. 
 *
 * @author Carl Harris
 */
public interface SessionContext {

  /**
   * Registers the given session as the calling thread's session.
   * @param session the session to register
   */
  void set(Session session);
  
  /**
   * Clears any session associated with the calling thread.
   */
  void clear();
  
}
