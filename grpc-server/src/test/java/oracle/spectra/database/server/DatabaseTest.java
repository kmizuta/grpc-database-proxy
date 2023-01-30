package oracle.spectra.database.server;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import oracle.spectra.database.DatabaseProxy;
import oracle.spectra.database.DatabaseProxyGrpc;
import oracle.spectra.database.client.DatabaseClient;
import oracle.spectra.database.client.DatabaseRequestResponseManager;
import oracle.spectra.database.model.CommandModel.*;
import io.helidon.microprofile.grpc.client.GrpcChannel;
import io.helidon.microprofile.grpc.client.GrpcProxy;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

@HelidonTest
public class DatabaseTest {
    static final Logger logger = LoggerFactory.getLogger(DatabaseTest.class);

    @Inject
    @GrpcProxy
    @GrpcChannel(name = "test-server")
    private DatabaseProxy client;

    @Inject
    @GrpcChannel(name = "test-server")
    private Channel channel;

    @Test
    void testJdbcProxy() throws SQLException {

        try (var dbclient = new DatabaseClient(channel)) {

//        var stmt = dbclient.prepareQuery("select * from emp where ename like :name");
//        stmt.bind("name", "SM%");
            var stmt = dbclient.prepareQuery("select * from emp");

            var colCount = stmt.getColumnCount();
            var rset = stmt.executeQuery();
            int rowIdx = 0;
            while (rset.next()) {
                logger.info(String.format("Row #%d ------------------------", ++rowIdx));
                for (int i = 1; i <= colCount; i++) {
                    var name = stmt.getColumnName(i);
                    var value = rset.getString(i);
                    if (value == null)
                        logger.info(String.format("%s is null", name));
                    else
                        logger.info(String.format("%s (%d/%d) = %s", name, i, colCount, value));
                }
            }
        }
    }

    public void testStubClient() {
        var channel = ManagedChannelBuilder.forAddress("localhost", 8080).build();
        var client = DatabaseProxyGrpc.newStub(channel);
    }
}
