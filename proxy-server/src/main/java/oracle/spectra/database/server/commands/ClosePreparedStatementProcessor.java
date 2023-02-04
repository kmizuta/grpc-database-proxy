package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel.Command;
import oracle.spectra.database.model.CommandModel.ClosePreparedStatementResponse;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;

import java.sql.Connection;

public class ClosePreparedStatementProcessor extends CommandProcessor {

    @Override
    DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable {
        var request = command.getClosePreparedStatement();
        var stmtId = request.getStatementId();
        var stmt = ProxyPreparedStatement.get(stmtId);
        stmt.close();
        return DatabaseResult.newBuilder()
                .setType(Command.COMMAND_CLOSEPREPAREDSTATEMENT)
                .setClosePreparedStatement(ClosePreparedStatementResponse.newBuilder().build())
                .build();
    }
}
