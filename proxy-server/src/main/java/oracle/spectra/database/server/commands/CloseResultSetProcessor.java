package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel.Command;
import oracle.spectra.database.model.CommandModel.CloseResultSetResponse;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;

import java.sql.Connection;

public class CloseResultSetProcessor extends CommandProcessor {

    @Override
    DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable {
        var request = command.getCloseResultSet();
        var rsetId = request.getResultSetId();
        var rset = ProxyResultSet.get(rsetId);
        rset.close();
        return DatabaseResult.newBuilder()
                .setType(Command.COMMAND_CLOSERESULTSET)
                .setCloseResultSet(CloseResultSetResponse.newBuilder().build())
                .build();
    }
}
