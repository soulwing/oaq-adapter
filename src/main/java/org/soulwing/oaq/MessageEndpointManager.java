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

import javax.resource.spi.endpoint.MessageEndpointFactory;

/**
 * A manager of {@link MessageEndpointRunner} objects.
 *
 * @author Carl Harris
 */
interface MessageEndpointManager extends Disposable {

  /**
   * Creates and registers a new endpoint for the given spec.
   * @param endpointFactory endpoint factory
   * @param activationSpec activation spec
   * @return registered endpoint object
   */
  MessageEndpointRunner create(MessageEndpointFactory endpointFactory,
      MessageActivationSpec activationSpec);

  /**
   * Removes an endpoint associated with the given spec.
   * @param endpointFactory endpoint factory
   * @param activationSpec activation spec
   * @return endpoint object that was removed or {@code null} if no such
   *         endpoint exists
   */
  MessageEndpointRunner remove(MessageEndpointFactory endpointFactory,
      MessageActivationSpec activationSpec);

}
