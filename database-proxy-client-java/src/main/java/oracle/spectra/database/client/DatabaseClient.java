package oracle.spectra.database.client;

import io.grpc.Channel;
import oracle.spectra.database.model.CommandModel.*;

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseClient implements AutoCloseable {
    private final static Logger logger = LoggerFactory.getLogger(DatabaseClient.class);

    private final DatabaseRequestResponseManager manager;
    public DatabaseClient(Channel channel) {
        this.manager = new DatabaseRequestResponseManager(channel);
    }

    public DatabasePreparedStatement prepareStatement(String sql) throws SQLException {
        logger.debug(String.format("prepareStatement(%s)", sql));
        return new DatabasePreparedStatement(manager, sql);
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
