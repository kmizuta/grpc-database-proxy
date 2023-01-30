package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;
import oracle.spectra.database.server.QueryStatement;

import java.sql.Connection;

public class BindProcessor extends CommandProcessor {

    @Override
    DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable {
        var bind = command.getBind();
        var idx = bind.getStatementId();
        var queryStmt = QueryStatement.get(idx);
        var stmt = queryStmt.getStatement();
        var valuesMap = bind.getValuesMap();
        for (String k : valuesMap.keySet()) {
            var v = valuesMap.get(k);
            // TODO: Change to handle Value type
            stmt.setStringAtName(k, v.getText());
        }

        var bindResponse = CommandModel.BindResponse.newBuilder().build();
        return DatabaseResult.newBuilder()
                .setType(command.getType())
                .setBind(bindResponse)
                .build();
    }
}
