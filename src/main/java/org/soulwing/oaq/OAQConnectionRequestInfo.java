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

import static org.soulwing.oaq.OAQLogger.LOGGER;

import java.io.Serializable;
import java.sql.SQLException;

import javax.resource.spi.ConnectionRequestInfo;

import oracle.jdbc.xa.client.OracleXADataSource;


/**
 * An immutable holder for the information needed to create an Oracle
 * JBDC data source.
 * 
 * @author Carl Harris
 */
public final class OAQConnectionRequestInfo 
    implements ConnectionRequestInfo, Serializable, Cloneable {

  private static final long serialVersionUID = -4979996165883547540L;

  private String databaseUrl;
  private String username;
  private String password;
    
  private volatile OracleXADataSource dataSource;
  
  /**
   * Gets the (singleton) Oracle data source associated with the receiver.
   * @return data source
   * @throws SQLException
   */
  public OracleXADataSource getDataSource() throws SQLException {
    if (dataSource == null) {
      synchronized (this) {
        if (dataSource == null) {
          dataSource = new OracleXADataSource();
          // TODO: prevent the data source from pooling connections
          dataSource.setURL(getDatabaseUrl());
          dataSource.setUser(getUsername());
          dataSource.setPassword(getPassword());
          LOGGER.fine("created XA data source");
        }
      }
    }
    return dataSource;
  }
  
  /**
   * Gets the {@code databaseUrl} property.
   * @return
   */
  public String getDatabaseUrl() {
    return databaseUrl;
  }

  /**
   * Sets the {@code databaseUrl} property.
   * @param databaseUrl
   */
  public void setDatabaseUrl(String databaseUrl) {
    LOGGER.fine("set databaseUrl to '" + databaseUrl + "'"); 
    this.databaseUrl = databaseUrl;
  }

  /**
   * Gets the {@code username} property.
   * @return
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the {@code username} property.
   * @param username
   */
  public void setUsername(String username) {
    LOGGER.fine("set username to '" + username + "'"); 
    this.username = username;
  }

  /**
   * Gets the {@code password} property.
   * @return
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the {@code password} property.
   * @param password
   */
  public void setPassword(String password) {
    LOGGER.fine("set password to '**********'"); 
    this.password = password;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int hashCode = super.hashCode();
    if (databaseUrl != null) {
      hashCode += 17*databaseUrl.hashCode();
    }
    if (username != null) {
      hashCode += 17*username.hashCode();
    }
    if (password != null) {
      hashCode += 17*password.hashCode();
    }
    return hashCode;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == this) return true;
    if (!(obj instanceof OAQConnectionRequestInfo)) return false;
    OAQConnectionRequestInfo that = (OAQConnectionRequestInfo) obj;
    if (notEqual(this.databaseUrl, that.databaseUrl)) return false;
    if (notEqual(this.username, that.username)) return false;
    if (notEqual(this.password, that.password)) return false;
    return true;
  }
  
  private static boolean notEqual(Object a, Object b) {
    if (a == null ^ b == null) return true;
    return a != null && !a.equals(b);
  }

  /**
   * Tests whether this connection info object is sufficiently configured.
   * @return
   */
  public boolean isComplete() {
    return databaseUrl != null && username != null && password != null; 
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public OAQConnectionRequestInfo clone() {
    try {
      OAQConnectionRequestInfo info = (OAQConnectionRequestInfo) 
          super.clone();
      info.dataSource = null;
      return info;
    }
    catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return String.format("{ databaseUrl='%s' username='%s' password='%s' }",
        databaseUrl, username, password != null ? "**********" : null);
  }
 
}
