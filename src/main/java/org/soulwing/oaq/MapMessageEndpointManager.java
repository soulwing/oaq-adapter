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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.endpoint.MessageEndpointFactory;

import org.apache.commons.lang.Validate;

/**
 * A {@link MessageEndpointManager} implementation based on a map.
 * 
 * @author Carl Harris
 */
class MapMessageEndpointManager implements MessageEndpointManager {

  private final Lock lock = new ReentrantLock();

  private final Map<Key, MessageEndpointRunner> endpoints =
      new HashMap<Key, MessageEndpointRunner>();

  private final MessageResourceAdapter resourceAdapter;

  /**
   * Constructs a new instance.
   * @param resourceAdapter
   */
  public MapMessageEndpointManager(MessageResourceAdapter resourceAdapter) {
    this.resourceAdapter = resourceAdapter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageEndpointRunner create(MessageEndpointFactory endpointFactory,
      MessageActivationSpec activationSpec) {
    lock.lock();
    try {
      Key key = new Key(activationSpec, endpointFactory);
      Validate.isTrue(!endpoints.containsKey(key));
      MessageEndpointRunner runner = new MessageEndpointRunner(
          resourceAdapter, activationSpec, endpointFactory);
      endpoints.put(key, runner);
      return runner;
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public MessageEndpointRunner remove(MessageEndpointFactory endpointFactory,
      MessageActivationSpec activationSpec) {
    lock.lock();
    try {
      Key key = new Key(activationSpec, endpointFactory);
      Validate.isTrue(endpoints.containsKey(key));
      return endpoints.remove(key);
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void dispose() {
    for (MessageEndpointRunner endpoint : endpoints.values()) {
      endpoint.dispose();
    }
  }

  static class Key {
    private final ActivationSpec activationSpec;
    private final MessageEndpointFactory endpointFactory;

    /**
     * Constructs a new instance.
     * @param activationSpec
     * @param endpointFactory
     */
    public Key(ActivationSpec activationSpec,
        MessageEndpointFactory endpointFactory) {
      this.activationSpec = activationSpec;
      this.endpointFactory = endpointFactory;
    }

    /**
     * Gets the {@code activationSpec} property.
     * @return
     */
    public ActivationSpec getActivationSpec() {
      return activationSpec;
    }

    /**
     * Gets the {@code endpointFactory} property.
     * @return
     */
    public MessageEndpointFactory getEndpointFactory() {
      return endpointFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
      int hashCode = 0;
      if (activationSpec != null) {
        hashCode = 17 * hashCode + activationSpec.hashCode();
      }
      if (endpointFactory != null) {
        hashCode = 17 * hashCode + endpointFactory.hashCode();
      }
      return hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
      if (obj == this)
        return true;
      if (!(obj instanceof Key))
        return false;
      
      Key that = (Key) obj;
      if (this.activationSpec == null ^ that.activationSpec == null)
        return false;
      if (this.endpointFactory == null ^ that.endpointFactory == null)
        return false;
      if (this.activationSpec != null
          && !activationSpec.equals(that.activationSpec))
        return false;
      if (this.endpointFactory != null
          && !endpointFactory.equals(that.endpointFactory))
        return false;
      return true;
    }
  }
}
