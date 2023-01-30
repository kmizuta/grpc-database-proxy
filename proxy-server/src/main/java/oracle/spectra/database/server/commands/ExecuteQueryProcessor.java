package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;
import oracle.spectra.database.server.QueryStatement;

import java.sql.Connection;

public class ExecuteQueryProcessor extends CommandProcessor {

    @Override
    DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable {
        var executeQuery = command.getExecuteQuery();
        var idx = executeQuery.getStatementId();
        var queryStmt = QueryStatement.get(idx);
        var stmt = queryStmt.getStatement();
        var rset = stmt.executeQuery();
        var rsetIdx = queryStmt.addResultSet(rset);
        var executeQueryResponse = CommandModel.ExecuteQueryResponse.newBuilder()
                .setResultSetId(rsetIdx)
                .build();
        return DatabaseResult.newBuilder()
                .setType(command.getType())
                .setExecuteQuery(executeQueryResponse)
                .build();
    }
}
