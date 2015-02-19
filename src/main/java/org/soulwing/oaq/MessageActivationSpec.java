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

import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

import org.apache.commons.lang.StringUtils;

/**
 * An {@link ActivationSpec} for Oracle AQ.
 *
 * @author Carl Harris
 */
public class MessageActivationSpec implements ActivationSpec {

  private static final Integer DEFAULT_MAX_MESSAGES = 10;
  
  private static final String AUTO_ACKNOWLEDGE_MODE = "Auto-acknowledge";
  private static final String DUPS_OK_ACKNOWLEDGE_MODE = "Dups-ok-acknowledge";
  private static final String DURABLE_SUBSCRIPTION = "Durable";
  private static final String NON_DURABLE_SUBSCRIPTION = "NonDurable";

  private ResourceAdapter resourceAdapter;
  private String acknowledgeMode;
  private String clientId;
  private String connectionFactoryLookup;
  private String destination;
  private String destinationType;
  private String destinationLookup;
  private Integer maxMessages;
  private String messageSelector;
  private String password;
  private String subscriptionDurability;
  private String subscriptionName;
  private String username;
  
  /**
   * Locates (using JNDI) the destination specified by the receiver.
   * @return destination object
   * @throws NamingException
   */
  public Destination lookupDestination() throws NamingException {
    if (javax.jms.Queue.class.getName().equals(getDestinationType())) {
      return lookup(javax.jms.Queue.class, getDestination());
    }
    return lookup(javax.jms.Topic.class, getDestination());
  }
  
  @SuppressWarnings("unchecked")
  private <T> T lookup(Class<T> objClass, String objName) 
      throws NamingException {
    Context ctx = new InitialContext();
    Object obj = ctx.lookup(objName);
    if (!objClass.isAssignableFrom(obj.getClass())) {
      throw new IllegalArgumentException(
          "object '" + objName + "' is of type " + obj.getClass().getName()
          + " not " + objClass.getName());
    }
    return (T) ctx.lookup(objName);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public ResourceAdapter getResourceAdapter() {
    return resourceAdapter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setResourceAdapter(ResourceAdapter resourceAdapter)
      throws ResourceException {
    // JCA 1.7 Section 5.3
    if (resourceAdapter != null) {
      throw new ResourceException("resource adapter already set");
    }
    // It must be an instance of our resource adapter
    if (!(resourceAdapter instanceof MessageResourceAdapter)) {
      throw new ResourceException("Unrecognized resource adapter type");
    }
    this.resourceAdapter = resourceAdapter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void validate() throws InvalidPropertyException {
    Errors errors = new Errors();
    validateAcknowledgeMode(errors);
    validateClientId(errors);
    validateConnectionFactoryLookup(errors);
    validateDestination(errors);
    validateDestinationType(errors);
    validateDestinationLookup(errors);
    validateMaxMessages(errors);
    validateMessageSelector(errors);
    validateSubscriptionDurability(errors);
    validateSubscriptionName(errors);
    validateUsername(errors);
    if (errors.hasErrors()) {
      throw errors.newException();
    }
  }

  private void validateAcknowledgeMode(Errors errors) {
    String mode = getAcknowledgeMode();
    if (mode == null) return;
    if (mode.equals(AUTO_ACKNOWLEDGE_MODE)) return;
    if (mode.equals(DUPS_OK_ACKNOWLEDGE_MODE)) return;
    errors.addError("acknowledgeMode", 
        "mode must be either " + AUTO_ACKNOWLEDGE_MODE 
        + " or " + DUPS_OK_ACKNOWLEDGE_MODE);
  }
  
  private void validateClientId(Errors errors) {
    if (getSubscriptionDurability() == null) return;
    if (NON_DURABLE_SUBSCRIPTION.equals(getSubscriptionDurability())) return;
    if (getClientId() != null) return;
    errors.addError("clientId", 
        "ID must is required for durable subscription");
  }
  
  private void validateConnectionFactoryLookup(Errors errors) {
  }
  
  private void validateDestination(Errors errors) {
    if (getDestination() != null) return;
    errors.addError("destination", "physical destination is required");
  }
  
  private void validateDestinationType(Errors errors) {
    if (javax.jms.Queue.class.getName().equals(getDestinationType())) return;
    if (javax.jms.Topic.class.getName().equals(getDestinationType())) return;
    errors.addError("destinationType",
        "type must be either " + javax.jms.Queue.class.getName() 
        + " or " + javax.jms.Topic.class.getName());    
  }
  
  private void validateDestinationLookup(Errors errors) {    
  }
  
  private void validateMaxMessages(Errors errors) {
    if (getMaxMessages() > 0) return;
    errors.addError("maxMessages", "a positive integer value is required");
  }
  
  private void validateMessageSelector(Errors errors) {    
  }

  private void validateSubscriptionDurability(Errors errors) {
    if (DURABLE_SUBSCRIPTION.equals(getSubscriptionDurability())) {
      if (javax.jms.Topic.class.equals(getDestinationType())) return;
      errors.addError("subscriptionDurability", 
          "cannot specify a durable subscription for a queue");
      return;
    }
    if (NON_DURABLE_SUBSCRIPTION.equals(getSubscriptionDurability())) return;
    errors.addError("subscriptionDurability",
        "must be either " + DURABLE_SUBSCRIPTION
        + " or " + NON_DURABLE_SUBSCRIPTION);
  }
  
  private void validateSubscriptionName(Errors errors) {
    if (getSubscriptionDurability() == null) return;
    if (NON_DURABLE_SUBSCRIPTION.equals(getSubscriptionDurability())) return;
    if (getSubscriptionName() != null) return;
    errors.addError("subscriptionName",
        "name is required for durable subscription");
  }
  
  private void validateUsername(Errors errors) {
    if (getUsername() == null) return;
    if (getPassword() != null) return;
    errors.addError("username", 
        "password is required when specifying a username");
  }
  
  /**
   * Gets the {@code acknowledgeMode} property.
   * @return
   */
  public String getAcknowledgeMode() {
    if (StringUtils.isBlank(acknowledgeMode)) return null;
    return acknowledgeMode;
  }

  /**
   * Sets the {@code acknowledgeMode} property.
   * @param acknowledgeMode
   */
  public void setAcknowledgeMode(String acknowledgeMode) {
    this.acknowledgeMode = acknowledgeMode;
  }

  /**
   * Gets the {@code clientId} property.
   * @return
   */
  public String getClientId() {
    if (StringUtils.isBlank(clientId)) return null;
    return clientId;
  }

  /**
   * Sets the {@code clientId} property.
   * @param clientId
   */
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * Gets the {@code connectionFactoryLookup} property.
   * @return
   */
  public String getConnectionFactoryLookup() {
    if (StringUtils.isBlank(connectionFactoryLookup)) return null;
    return connectionFactoryLookup;
  }

  /**
   * Sets the {@code connectionFactoryLookup} property.
   * @param connectionFactoryLookup
   */
  public void setConnectionFactoryLookup(String connectionFactoryLookup) {
    this.connectionFactoryLookup = connectionFactoryLookup;
  }

  /**
   * Gets the {@code destination} property.
   * @return
   */
  public String getDestination() {
    return destination;
  }

  /**
   * Sets the {@code destination} property.
   * @param destination
   */
  public void setDestination(String destination) {
    this.destination = destination;
  }

  /**
   * Gets the {@code destinationType} property.
   * @return
   */
  public String getDestinationType() {
    if (StringUtils.isBlank(destinationType)) return null;
    return destinationType;
  }

  /**
   * Sets the {@code destinationType} property.
   * @param destinationType
   */
  public void setDestinationType(String destinationType) {
    this.destinationType = destinationType;
  }

  /**
   * Gets the {@code destinationLookup} property.
   * @return
   */
  public String getDestinationLookup() {
    if (StringUtils.isBlank(destinationLookup)) return null;
    return destinationLookup;
  }

  /**
   * Sets the {@code destinationLookup} property.
   * @param destinationLookup
   */
  public void setDestinationLookup(String destinationLookup) {
    this.destinationLookup = destinationLookup;
  }

  /**
   * Gets the {@code maxMessages} property.
   * @return
   */
  public Integer getMaxMessages() {
    if (maxMessages == null) return DEFAULT_MAX_MESSAGES;
    return maxMessages;
  }

  /**
   * Sets the {@code maxMessages} property.
   * @param maxMessages
   */
  public void setMaxMessages(Integer maxMessages) {
    this.maxMessages = maxMessages;
  }

  /**
   * Gets the {@code messageSelector} property.
   * @return
   */
  public String getMessageSelector() {
    if (StringUtils.isBlank(messageSelector)) return null;
    return messageSelector;
  }

  /**
   * Sets the {@code messageSelector} property.
   * @param messageSelector
   */
  public void setMessageSelector(String messageSelector) {
    this.messageSelector = messageSelector;
  }

  /**
   * Gets the {@code password} property.
   * @return
   */
  public String getPassword() {
    if (StringUtils.isBlank(password)) return null;
    return password;
  }

  /**
   * Sets the {@code password} property.
   * @param password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Gets the {@code subscriptionDurability} property.
   * @return
   */
  public String getSubscriptionDurability() {
    if (StringUtils.isBlank(subscriptionDurability)) return null;
    return subscriptionDurability;
  }

  /**
   * Sets the {@code subscriptionDurability} property.
   * @param subscriptionDurability
   */
  public void setSubscriptionDurability(String subscriptionDurability) {
    this.subscriptionDurability = subscriptionDurability;
  }

  /**
   * Tests whether this spec indicates a durable subscription.
   * @return {@code true} if a durable subscription is desired
   */
  public boolean isDurableSubscription() {
    return DURABLE_SUBSCRIPTION.equals(getSubscriptionDurability());
  }
  
  /**
   * Gets the {@code subscriptionName} property.
   * @return
   */
  public String getSubscriptionName() {
    if (StringUtils.isBlank(subscriptionName)) return null;
    return subscriptionName;
  }

  /**
   * Sets the {@code subscriptionName} property.
   * @param subscriptionName
   */
  public void setSubscriptionName(String subscriptionName) {
    this.subscriptionName = subscriptionName;
  }

  /**
   * Gets the {@code username} property.
   * @return
   */
  public String getUsername() {
    if (StringUtils.isBlank(username)) return null;
    return username;
  }

  /**
   * Sets the {@code username} property.
   * @param username
   */
  public void setUsername(String username) {
    this.username = username;
  }

}
