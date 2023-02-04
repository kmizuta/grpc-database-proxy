# gRPC Oracle Database Proxy

There is a wide array of support for connecting to an Oracle database from various 
programming languages, but the drivers available have different levels of capability.
In addition, not all drivers are provided or maintained by Oracle and so there
are questions around whether those drivers will have support for some of the more advanced 
features that the Oracle database offers. On the other hand, Oracle provides the 
Java Oracle JDBC drivers and those have support for the latest features provided by the
database. This project provides a database proxy that can be deployed as a sidecar
such that you can connect to an Oracle database using the standard Oracle JDBC drivers
and be able to connect to it using any language.

## Overall Design

Database proxy is implemented as a gRPC service. The database proxy provides a single bidirectional 
method that takes a stream of database commands and receives a stream of responses. The method 
receives a polymorphic request (the database command) and provides a polymorphic response.
The number of round trips is intended to match the round trips that the JDBC driver makes to the
database to limit the network round trips. Each command request is matched with a single response. 
A database connection is checked out at the beginning of the gRPC call and the same connection
is kept checked out and is used for the duration of the call. In other words, a single gRPC
call is a database session.

## database-proxy-proto

Contains the proto files for the gRPC services. There is a single ExecuteCommand method that
takes a stream of DatabaseCommand and returns a stream of DatabaseResult. DatabaseCommand and
DatabaseResult are both polymorphic structures. A single DatabaseCommand is matched with a 
single DatabaseResult. 

| Command Type | Request                 | Response | Description                                          |
|--------------|-------------------------|----------|------------------------------------------------------|
| COMMAND_PREPARESTATEMENT | PrepareStatementRequest | PrepareStatementResponse | Equivalent to java.sql.Connection.prepareStatement method |
| COMMAND_PREPARECALL | PrepareCallRequest      | PrepareCallResponse | Equivalent to java.sql.Connection.prepareCall method |
| COMMAND_BIND | BindRequest             | BindResponse | A single call to bind all variables -- by name       |
| COMMAND_EXECUTE | ExecuteRequest | ExecuteResponse | Equivalent to java.sql.Statement.execute method |
| COMMAND_EXECUTEQUERY | ExecuteQueryRequest | ExecuteQueryResponse | Equivaqlent to java.sql.Statement.executeQuery method |
| COMMAND_NEXTROWS | NextRowsRequest | NextRowsResponse | Fetches the next batch of rows |
| COMMAND_CLOSERESULTSET | CloseResultSetRequest | CloseResultSetResonse | Equivalent to java.sql.ResultSet close method |
| COMMAND_CLOSEPREPAREDSTATEMENT | ClosePreparedStatementRequest | ClosePreparedStatementResponse | Equivalent to java.sql.PreparedStatement.close method |
| COMMAND_CLOSECALLABLESTATEMENT | CloseCallableStatementRequest | CloseCallableStatementResponse | Equivalent to java.sql.CallableStatement.close method |

## database-proxy-client-java

Provides the Java client implementation of the Database proxy.

### oracle.spectra.database.DatabaseProxy

This interface can be used directly in a Helidon MP application to inject the DatabaseProxy client as follows....

```java
@javax.inject.Inject  
@io.helidon.microprofile.grpc.client.GrpcProxy  
@io.helidon.microprofile.grpc.client.GrpcChannel(name = "channel name")  
private oracle.spectra.database.DatabaseProxy databaseProxy;
```

### oracle.spectra.database.client.DatabaseClient

This is a thick Database Proxy client. It provides methods that more closely matches how JDBC works and is 
therefore, easier to use for Java developers. 

In the future, we would want to provide an actual JDBC driver that wraps around this. 

## database-proxy-server

The database proxy server can be executed by running the JAR file directly - e.g., 
`java -jar proxy-server.jar`

To configure the database proxy server, the following is an example config file.

```yaml
grpc:
  name: techarch.server
  port: 8080

database:
  proxy:
    connection: oracle.ucp.jdbc.PoolDataSource.ApplicationDS

oracle:
  ucp:
    jdbc:
      PoolDataSource:
        ApplicationDS:
          connectionFactoryClassName: oracle.jdbc.pool.OracleDataSource
          URL: jdbc:oracle:thin:@(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.us-ashburn-1.oraclecloud.com))(connect_data=(service_name=myeuacmnbcwpupn_kmizutatest_low.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)(WALLET_LOCATION=/Users/kmizuta/work/grpc/wallet))))
          user: scott
          password: TigerManager123!
          minPoolSize: 1
          maxPoolSize: 2
          connectionHarvestTriggerCount: 6
          connectionHarvestMaxCount: 2

```