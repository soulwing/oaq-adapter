package org.soulwing.oaq;

import java.util.LinkedList;
import java.util.List;

import javax.resource.spi.InvalidPropertyException;

class Errors {

  private final List<Error> errors = new LinkedList<Error>();
  
  /**
   * Tests whether this errors collections contains any errors.
   * @return {@code true} if any errors have been added to the receiver
   */
  public boolean hasErrors() {
    return !errors.isEmpty();
  }
  
  /**
   * Adds an error to the receiver.
   * @param propertyName property name in error
   * @param message error message
   */
  public void addError(String propertyName, String message) {
    errors.add(new Error(propertyName, message));
  }
  
  /**
   * Creates a new exception describing the errors in the receiver.
   * @return exception object
   */
  public InvalidPropertyException newException() {
    StringBuilder sb = new StringBuilder();
    sb.append("configuration error: ");
    for (Error error : errors) {
      sb.append(error.toString());
      sb.append("; ");
    }
    return new InvalidPropertyException(sb.toString());
  }
  
  /**
   * An immutable value holder for error details.
   */
  private static class Error {

    private final String propertyName;
    private final String message;
    
    /**
     * Constructs a new instance.
     * @param propertyName
     * @param message
     */
    public Error(String propertyName, String message) {
      this.propertyName = propertyName;
      this.message = message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
      return String.format("property %s: %s", propertyName, message);
    }
    
  }

}
