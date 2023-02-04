package oracle.spectra.database.server.commands;

import oracle.jdbc.OraclePreparedStatement;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;
import oracle.spectra.database.model.CommandModel.PrepareStatementResponse;

import java.sql.Connection;

public class PrepareStatementProcessor extends CommandProcessor {

    @Override
    DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable {
        var prepareStatementRequest = command.getPrepareStatement();
        var sql = prepareStatementRequest.getSql();
        var stmt = (OraclePreparedStatement) conn.prepareStatement(sql);
        var queryStmt = new ProxyPreparedStatement(stmt);
        var idx = ProxyPreparedStatement.add(queryStmt);
        var prepareResponseBuilder = PrepareStatementResponse.newBuilder()
                .setStatementId(idx);
        var columnCount = queryStmt.getColumnCount();
        prepareResponseBuilder.setColumnCount(columnCount);
        for (int i=1; i<=columnCount; i++) {
            prepareResponseBuilder.addColumnNames(queryStmt.getColumnName(i));
        }
        return DatabaseResult.newBuilder()
                .setType(command.getType())
                .setPrepareStatement(prepareResponseBuilder.build())
                .build();
    }
}
