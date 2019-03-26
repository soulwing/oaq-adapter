oaq-adapter
===========

A JCA resource adapter for Oracle AQ.

This module builds a resource adapter for Oracle AQ that can be deployed
in a Wildfly Java EE container.  It allows EJB components in the container to 
use the messaging facilities of Oracle AQ via JMS, just like any other JMS
provider.

The project for which this was a dependency made a decision to abandon Oracle
AQ and this resource adapter has never been used in a production capacity. It
remains here as a reference to others who might be interested in building 
such a thing. It will likely require some effort to restore it to a working
condition.

In order to build this module, you will need to install the Oracle JDBC driver
and AQ API dependencies listed in `pom.xml` in your local repository or Nexus
instance. These dependencies are available from Oracle.
