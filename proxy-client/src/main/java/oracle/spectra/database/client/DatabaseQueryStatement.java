package oracle.spectra.database.client;

import oracle.spectra.database.model.CommandModel.BindRequest;
import oracle.spectra.database.model.CommandModel.Command;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.ExecuteQueryRequest;
import oracle.spectra.database.model.CommandModel.PrepareRequest;
import oracle.spectra.database.model.CommandModel.PrepareResponse;
import oracle.spectra.database.model.CommandModel.Value;
import oracle.spectra.database.model.CommandModel.ValueType;

import java.sql.SQLException;

public class DatabaseQueryStatement {

    private final DatabaseRequestResponseManager manager;
    private BindRequest.Builder bindRequestBuilder;
    private final PrepareResponse response;

    DatabaseQueryStatement(DatabaseRequestResponseManager manager, String sql) throws SQLException {
        this.manager = manager;
        this.bindRequestBuilder = null;

        var result = manager.execute(DatabaseCommand.newBuilder()
                .setType(Command.COMMAND_PREPAREQUERY)
                .setPrepareQuery(PrepareRequest.newBuilder()
                        .setSql(sql).build())
                .build());
        DatabaseClient.checkError(result);
        response = result.getPrepareQuery();
    }

    public void bind(String name, String textValue) {
        Value value = Value.newBuilder()
                .setType(ValueType.VALUE_TEXT)
                .setText(textValue)
                .build();
        getBindRequestBuilder().putValues(name, value);
    }

    public void bind(String name, int intValue) {
        Value value = Value.newBuilder()
                .setType(ValueType.VALUE_INTEGRAL)
                .setInt(intValue)
                .build();
        getBindRequestBuilder().putValues(name, value);
    }

    // TODO: Implement other bind types

    public DatabaseResultSet executeQuery() throws SQLException {
        if (bindRequestBuilder != null) {
            var bindResult = manager.execute(DatabaseCommand.newBuilder()
                    .setType(Command.COMMAND_BIND)
                    .setBind(bindRequestBuilder.build())
                    .build());
            DatabaseClient.checkError(bindResult);
        }

        var queryRequest = ExecuteQueryRequest.newBuilder()
                .setStatementId(response.getStatementId())
                .build();
        var executeResult = manager.execute(DatabaseCommand.newBuilder()
                .setType(Command.COMMAND_EXECUTEQUERY)
                .setExecuteQuery(queryRequest)
                .build());
        DatabaseClient.checkError(executeResult);
        var resultSetId = executeResult.getExecuteQuery().getResultSetId();
        return new DatabaseResultSet(manager, response.getStatementId(), resultSetId);
    }


    private synchronized BindRequest.Builder getBindRequestBuilder() {
        if (bindRequestBuilder == null)
            bindRequestBuilder = BindRequest.newBuilder();
        return bindRequestBuilder;
    }

    public int getColumnCount() {
        return response.getColumnCount();
    }

    public String getColumnName(int idx) {
        return response.getColumnNames(idx-1);
    }

}
