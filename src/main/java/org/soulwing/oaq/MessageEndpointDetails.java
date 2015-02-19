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

import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkManager;

/**
 * A provider of the details about a message endpoint.
 *
 * @author Carl Harris
 */
public interface MessageEndpointDetails {

  /**
   * Gets the work manager that is to be used to perform work for the 
   * endpoint.
   * @return work manager
   */
  WorkManager getWorkManager();
  
  /**
   * Gets the container's endpoint factory.
   * @return endpoint factory
   */
  MessageEndpointFactory getEndpointFactory();
  
  /**
   * Gets the adapter-specific activation spec for the endpoint.
   * @return activation spec
   */
  MessageActivationSpec getActivationSpec();

  /**
   * Creates an XA JMS connection to Oracle AQ for the endpoint.
   * <p>
   * The returned object is an instance of {@link oracle.jms.AQjmsXAConnection}
   * but is returned using the interface type to faciliate testing with
   * mocks.
   * 
   * @return connection object
   * @throws JMSException
   */
  XAConnection createXAConnection() throws JMSException;
    
}
