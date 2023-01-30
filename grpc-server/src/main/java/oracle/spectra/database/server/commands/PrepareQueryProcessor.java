package oracle.spectra.database.server.commands;

import oracle.jdbc.OraclePreparedStatement;
import oracle.spectra.database.model.CommandModel;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;
import oracle.spectra.database.server.QueryStatement;

import java.sql.Connection;

public class PrepareQueryProcessor extends CommandProcessor {

    @Override
    DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable {
        var prepareQueryRequest = command.getPrepareQuery();
        var sql = prepareQueryRequest.getSql();
        var stmt = (OraclePreparedStatement) conn.prepareStatement(sql);
        var queryStmt = new QueryStatement(stmt);
        var idx = QueryStatement.add(queryStmt);
        var prepareResponseBuilder = CommandModel.PrepareResponse.newBuilder()
                .setStatementId(idx);
        var columnCount = queryStmt.getColumnCount();
        prepareResponseBuilder.setColumnCount(columnCount);
        for (int i=1; i<=columnCount; i++) {
            prepareResponseBuilder.addColumnNames(queryStmt.getColumnName(i));
        }
        var result = CommandModel.DatabaseResult.newBuilder()
                .setType(command.getType())
                .setPrepareQuery(prepareResponseBuilder.build())
                .build();
        return result;
    }
}
