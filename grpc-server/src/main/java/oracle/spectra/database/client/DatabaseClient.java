package oracle.spectra.database.client;

import io.grpc.Channel;
import oracle.spectra.database.model.CommandModel.*;

import java.sql.SQLException;

public class DatabaseClient implements AutoCloseable {

    private final DatabaseRequestResponseManager manager;
    public DatabaseClient(Channel channel) {
        this.manager = new DatabaseRequestResponseManager(channel);
    }

    public DatabaseQueryStatement prepareQuery(String query) throws SQLException {
        return new DatabaseQueryStatement(manager, query);
    }

    @Override
    public void close() {
        manager.close();
    }

    static void checkError(DatabaseResult result) throws SQLException {
        if (Command.COMMAND_ERROR.equals(result.getType())) {
            var error = result.getError();
            throw new SQLException(error.getErrorMessage());
        }
    }
}
