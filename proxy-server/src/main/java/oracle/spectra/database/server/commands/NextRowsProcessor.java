package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.DatabaseResult;

import java.sql.Connection;

public class NextRowsProcessor extends CommandProcessor {

    @Override
    DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable {
        var nextRows = command.getNextRows();
        var rsetIdx = nextRows.getResultSetId();
        var resultSet = ProxyResultSet.get(rsetIdx);
        var queryStmt = resultSet.getPreparedStatement();
        var fetchSize = queryStmt.getFetchSize();
        var columnCount = queryStmt.getColumnCount();
        var rset = resultSet.getResultSet();
        var responseBuilder = CommandModel.NextRowsResponse.newBuilder().setHasMore(true);
        for (int i = 0; i < fetchSize; i++) {
            if (rset.next()) {
                var row = CommandModel.Row.newBuilder();
                for (int colIdx = 1; colIdx <= columnCount; colIdx++) {
                    var name = queryStmt.getColumnName(colIdx);
                    var columnValue = rset.getString(colIdx);
                    var valueBuilder = CommandModel.Value.newBuilder();
                    if (columnValue == null) {
                        valueBuilder.setIsNull(true);
                    } else {
                        // TODO: Handle all data types
                        valueBuilder
                                .setIsNull(false)
                                .setType(CommandModel.ValueType.VALUE_TEXT)
                                .setText(columnValue);
                    }
                    var field = CommandModel.Field.newBuilder()
                            .setName(name)
                            .setValue(valueBuilder.build())
                            .build();
                    row.addField(field);
                }
                responseBuilder.addRow(row);
            } else {
                responseBuilder.setHasMore(false);
                break;
            }
        }
        return CommandModel.DatabaseResult.newBuilder()
                .setType(command.getType())
                .setNextRows(responseBuilder.build())
                .build();   }
}
