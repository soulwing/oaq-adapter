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

import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;

/**
 * A {@link ResourceAdapter} for a JMS resource.
 *
 * @author Carl Harris
 */
public interface MessageResourceAdapter extends ResourceAdapter {
  
  /**
   * Gets the receiver's bootstrap context.
   * @return bootstrap context
   */
  BootstrapContext getBootstrapContext();
  
  /**
   * Gets the connection request info associated with the resource 
   * adapter's configuration.
   * @return connection request info
   */
  OAQConnectionRequestInfo getConnectionRequestInfo();

  /**
   * Creates an AQ connection.
   * @param spec activation spec
   * @return an AQ connection via the receiver's JDBC data source
   * @throws JMSException
   */
  XAConnection createConnection(MessageActivationSpec spec) throws JMSException;

  /**
   * Creates an AQ connection.
   * @param info connection request info
   * @return an AQ connection via the receiver's JDBC data source
   * @throws JMSException
   */
  XAConnection createConnection(OAQConnectionRequestInfo info) 
      throws JMSException;
  
}
