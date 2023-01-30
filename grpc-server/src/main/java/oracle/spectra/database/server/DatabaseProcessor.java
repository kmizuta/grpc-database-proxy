package oracle.spectra.database.server;

import io.grpc.stub.StreamObserver;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;
import oracle.spectra.database.server.commands.CommandProcessor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

class DatabaseProcessor implements StreamObserver<DatabaseCommand> {

    private final DatabaseProxyServer databaseProxyServer;
    private final StreamObserver<DatabaseResult> resultObserver;
    private Connection conn = null;
    private int queryStatementIdx = 0;
    private final Map<Integer, QueryStatement> queryStatements = new HashMap<>();
    private int resultSetIdx = 0;
    private final Map<Integer, ResultSet> resultSets = new HashMap<>();

    DatabaseProcessor(DatabaseProxyServer databaseProxyServer, DataSource dataSource, StreamObserver<DatabaseResult> resultObserver) {
        this.databaseProxyServer = databaseProxyServer;
        this.resultObserver = resultObserver;
        try {
            this.conn = dataSource.getConnection();
        } catch (SQLException e) {
            closeConnection();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onNext(DatabaseCommand jdbcCommand) {
        DatabaseResult result = CommandProcessor.doCommand(conn, jdbcCommand);
        if (result == null) {
            throw new UnsupportedOperationException(
                    String.format("Operation not yet implemented. Operation Id = %s",
                            jdbcCommand.getType().toString()));
        } else {
            resultObserver.onNext(result);
        }
    }

    @Override
    public void onError(Throwable throwable) {
        // TODO: When some error occurs, server doesn't finish. Something fishy here.
        closeConnection();
        resultObserver.onError(throwable);
    }

    @Override
    public void onCompleted() {
        closeConnection();
        resultObserver.onCompleted();
    }

    private void closeConnection() {
        if (this.conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
            }
        }
    }

}
