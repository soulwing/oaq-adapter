/*
 * File created on Nov 26, 2013 
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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A manager of {@link ManagedConnectionProxy} objects used by a
 * {@link MessageManagedConnection}.
 *
 * @author Carl Harris
 */
public class MessageConnectionManager {

  private final Lock lock = new ReentrantLock();
  
  private final Set<ManagedConnectionProxy> connections = 
      new LinkedHashSet<ManagedConnectionProxy>();
  
  /**
   * Creates a new managed proxy.
   * @param parent parent managed connection
   * @return proxy object
   */
  public ManagedConnectionProxy create(MessageManagedConnection parent) {    
    ManagedConnectionProxy proxy = new ManagedConnectionProxy(parent);
    add(proxy);
    return proxy;
  }
  
  /**
   * Adds an existing proxy to this manager.
   * @param proxy the proxy to add
   */
  public void add(ManagedConnectionProxy proxy) {
    lock.lock();
    try {
      connections.add(proxy);
    }
    finally {
      lock.unlock();
    }
  }
  
  /**
   * Removes a proxy from this manager.
   * <p>
   * The {@link Disposable#dispose()} method is invoked on the removed
   * proxy.
   * @param proxy the proxy to remove.
   * @return {@code true} if a proxy was removed
   */
  public boolean remove(ManagedConnectionProxy proxy) {
    lock.lock();
    try {
      boolean removed = connections.remove(proxy);
      proxy.dispose();
      return removed;
    }
    finally {
      lock.unlock();
    }
  }
  
  /**
   * Removes all connections from the receiver.
   * <p>
   * The {@link Disposable#dispose()} method is invoked on each removed
   * proxy.
   */
  public void removeAll() {
    lock.lock();
    try {
      Iterator<ManagedConnectionProxy> i = connections.iterator();
      while (i.hasNext()) {
        ManagedConnectionProxy proxy = i.next();
        i.remove();
        proxy.dispose();
      }
    }
    finally {
      lock.unlock();
    }
  }
  
}

