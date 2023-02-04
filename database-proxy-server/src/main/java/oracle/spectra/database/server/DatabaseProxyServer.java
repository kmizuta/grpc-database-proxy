package oracle.spectra.database.server;


import io.grpc.stub.StreamObserver;
import io.helidon.microprofile.grpc.core.Bidirectional;
import io.helidon.microprofile.grpc.core.Grpc;
import jakarta.enterprise.context.ApplicationScoped;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

@ApplicationScoped
@Grpc(name = "DatabaseProxy")
public class DatabaseProxyServer {
    static final Logger logger = LoggerFactory.getLogger(DatabaseProxyServer.class);

    private final PoolDataSource dataSource;

    public DatabaseProxyServer() {
        logger.debug("DatabaseProxyServer()+");

        var config = ConfigProvider.getConfig();
        var configPrefix = config.getConfigValue("database.proxy.connection").getValue();

        var pds = PoolDataSourceFactory.getPoolDataSource();
        final String[] configNames = new String[] {
          "connectionFactoryClassName", "URL", "user", "password", "minPoolSize", "maxPoolSize", "connectionHarvestTriggerCount", "connectionHarvestMaxCount"
        };
        try {
            for (String configName: configNames) {
                var fullConfigName = String.format("%s.%s", configPrefix, configName);
                var configValue = config.getConfigValue(fullConfigName);

                if (configValue != null) {
                    if (logger.isDebugEnabled()) {
                        if (! "password".equals(configName))
                            logger.debug(String.format("%s = %s", fullConfigName, configValue.getValue()));
                    }

                    switch (configName) {
                        case "connectionFactoryClassName":
                            pds.setConnectionFactoryClassName(configValue.getValue());
                            break;
                        case "URL":
                            pds.setURL(configValue.getValue());
                            break;
                        case "user":
                            pds.setUser(configValue.getValue());
                            break;
                        case "password":
                            pds.setPassword(configValue.getValue());
                            break;
                        case "minPoolSize":
                            pds.setMinPoolSize(Integer.valueOf(configValue.getValue()));
                            break;
                        case "maxPoolSize":
                            pds.setMaxPoolSize(Integer.valueOf(configValue.getValue()));
                            break;
                        case "connectionHarvestTriggerCount":
                            pds.setConnectionHarvestTriggerCount(Integer.valueOf(configValue.getValue()));
                            break;
                        case "connectionHarvestMaxCount":
                            pds.setConnectionHarvestMaxCount(Integer.valueOf(configValue.getValue()));
                    }
                }
            }
        } catch(SQLException sqle) {
            throw new RuntimeException(sqle);
        }
        this.dataSource = pds;

        logger.debug("DatabaseProxyServer()-");
    }

    @Bidirectional(name = "ExecuteCommand")
    public StreamObserver<DatabaseCommand> executeCommand(StreamObserver<DatabaseResult> result) {
        return new DatabaseProcessor(dataSource, result);
    }


}
