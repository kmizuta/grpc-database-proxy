package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel.Command;
import oracle.spectra.database.model.CommandModel.CloseCallableStatementResponse;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;

import java.sql.Connection;

public class CloseCallableStatementProcessor extends CommandProcessor {

    @Override
    DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable {
        var request = command.getCloseCallableStatement();
        var stmtId = request.getStatementId();
        var stmt = ProxyCallableStatement.get(stmtId);
        stmt.close();
        return DatabaseResult.newBuilder()
                .setType(Command.COMMAND_CLOSEPREPAREDSTATEMENT)
                .setCloseCallableStatement(CloseCallableStatementResponse.newBuilder().build())
                .build();
    }
}
