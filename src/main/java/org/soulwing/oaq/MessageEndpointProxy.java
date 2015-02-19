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

import java.lang.reflect.Method;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;


/**
 * An endpoint that listens for messages on behalf of a message-driven bean.
 *
 * @author Carl Harris
 */
public class MessageEndpointProxy implements MessageListener {

  private static final Method onMessageMethod;
  
  private final MessageEndpoint endpoint;
  
  static {
    try {
      onMessageMethod = MessageListener.class.getMethod("onMessage");
    }
    catch (NoSuchMethodException ex) {
      throw new ExceptionInInitializerError(ex);
    }
  }
  
  /**
   * Constructs a new instance.
   * @param endpoint
   */
  public MessageEndpointProxy(MessageEndpoint endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public void onMessage(Message message) {
    // TODO: make a stateful implementation that doesn't complain each
    // time something goes wrong here
    try {
      endpoint.beforeDelivery(onMessageMethod);
      ((MessageListener) endpoint).onMessage(message);
      endpoint.afterDelivery();
    }
    catch (NoSuchMethodException ex) {
      throw new RuntimeException(ex);
    }
    catch (ResourceException ex) {
      throw new RuntimeException(ex);
    }
  }

}
