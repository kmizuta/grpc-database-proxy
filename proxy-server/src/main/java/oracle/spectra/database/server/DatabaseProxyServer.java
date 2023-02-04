package oracle.spectra.database.server;


import io.grpc.stub.StreamObserver;
import io.helidon.microprofile.grpc.core.Bidirectional;
import io.helidon.microprofile.grpc.core.Grpc;
import jakarta.enterprise.context.ApplicationScoped;
import oracle.spectra.database.DatabaseProxy;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import java.sql.SQLException;

@ApplicationScoped
@Grpc(name = "DatabaseProxy")
public class DatabaseProxyServer implements DatabaseProxy {

    private final String TNS = "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.us-ashburn-1.oraclecloud.com))(connect_data=(service_name=myeuacmnbcwpupn_kmizutatest_low.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)(WALLET_LOCATION=/Users/kmizuta/work/grpc/wallet))))";
//    private final String TNS = "(description= (retry_count=20)(retry_delay=3)(address=(protocol=tcps)(port=1522)(host=adb.us-ashburn-1.oraclecloud.com))(connect_data=(service_name=myeuacmnbcwpupn_kmizutatest_low.adb.oraclecloud.com))(security=(ssl_server_dn_match=yes)(MY_WALLET_DIRECTORY=/Users/kmizuta/work/grpc/wallet))))";
    private final String JDBC_URL = String.format("jdbc:oracle:thin:@%s", TNS);
    private final String DBUSER = "scott";
    private final String DBPWD  = "TigerManager123!";
    private final PoolDataSource dataSource;

    public DatabaseProxyServer() {
        var pds = PoolDataSourceFactory.getPoolDataSource();
        try {
            System.out.println(JDBC_URL);
            pds.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
            pds.setUser(DBUSER);
            pds.setPassword(DBPWD);
            pds.setURL(JDBC_URL);
        } catch(SQLException sqle) {
            throw new RuntimeException(sqle);
        }
        this.dataSource = pds;
    }

    @Override
    @Bidirectional(name = "ExecuteCommand")
    public StreamObserver<DatabaseCommand> executeCommand(StreamObserver<DatabaseResult> result) {
        return new DatabaseProcessor(dataSource, result);
    }


}
