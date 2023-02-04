package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel.*;

import java.sql.Connection;

public class ExecuteProcessor extends CommandProcessor {

    @Override
    DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable {
        var request = command.getExecute();
        var stmtId = request.getStatementId();
        var stmt = ProxyCallableStatement.get(stmtId);
        stmt.close();
        return DatabaseResult.newBuilder()
                .setType(Command.COMMAND_EXECUTE)
                .setExecute(ExecuteResponse.newBuilder().build())
                .build();
    }
}
