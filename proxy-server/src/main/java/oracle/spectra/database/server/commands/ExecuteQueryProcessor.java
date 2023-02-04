package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;

import java.sql.Connection;

public class ExecuteQueryProcessor extends CommandProcessor {

    @Override
    DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable {
        var executeQuery = command.getExecuteQuery();
        var idx = executeQuery.getStatementId();

        var queryStmt = ProxyPreparedStatement.get(idx);
        var stmt = queryStmt.getStatement();
        var rset = stmt.executeQuery();
        var resultSet = new ProxyResultSet(queryStmt, rset);
        var resultSetIdx = ProxyResultSet.add(resultSet);

        var executeQueryResponse = CommandModel.ExecuteQueryResponse.newBuilder()
                .setResultSetId(resultSetIdx)
                .build();
        return DatabaseResult.newBuilder()
                .setType(command.getType())
                .setExecuteQuery(executeQueryResponse)
                .build();
    }
}
