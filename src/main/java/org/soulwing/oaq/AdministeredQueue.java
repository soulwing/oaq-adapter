/*
 * File created on Feb 23, 2015 
 *
 * Copyright (c) 2015 Carl Harris, Jr
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

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.Referenceable;
import javax.resource.ResourceException;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;


/**
 * An administered OAQ queue.
 *
 * @author Carl Harris
 */
public class AdministeredQueue implements Queue, Referenceable, Serializable,
    ResourceAdapterAssociation {

  private static final long serialVersionUID = -196954607849333122L;

  private String physicalName;
  
  private String owner;
  
  private Reference reference;
  
  private ResourceAdapter ra;
  
  public AdministeredQueue() {
    OAQLogger.LOGGER.fine("created administered queue");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getQueueName() throws JMSException {
    return physicalName;
  }

  /**
   * Gets the {@code physicalName} property.
   * @return property value
   */
  public String getPhysicalName() {
    return physicalName;
  }

  /**
   * Sets the {@code physicalName} property.
   * @param physicalName the value to set
   */
  public void setPhysicalName(String physicalName) {
    this.physicalName = physicalName;
  }

  /**
   * Gets the {@code owner} property.
   * @return property value
   */
  public String getOwner() {
    return owner;
  }

  /**
   * Sets the {@code owner} property.
   * @param owner the value to set
   */
  public void setOwner(String owner) {
    this.owner = owner;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Reference getReference() throws NamingException {
    return reference;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setReference(Reference reference) {
    this.reference = reference;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ResourceAdapter getResourceAdapter() {
    return ra;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
    this.ra = ra;
  }

}
