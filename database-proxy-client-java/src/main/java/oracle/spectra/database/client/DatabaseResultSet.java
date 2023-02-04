package oracle.spectra.database.client;

import oracle.spectra.database.model.CommandModel;
import oracle.spectra.database.model.CommandModel.Command;
import oracle.spectra.database.model.CommandModel.DatabaseCommand;
import oracle.spectra.database.model.CommandModel.NextRowsRequest;
import oracle.spectra.database.model.CommandModel.Row;
import oracle.spectra.database.model.CommandModel.ValueType;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class DatabaseResultSet {
    private final DatabaseRequestResponseManager manager;
    private final int statementId;
    private final int resultSetId;
    private List<Row> rows;
    private boolean hasMore;
    private int rowCount;
    private int currentRowIdx;
    private Row currentRow;

    public DatabaseResultSet(DatabaseRequestResponseManager manager, int statementId, int resultSetId) throws SQLException {
        this.manager = manager;
        this.statementId = statementId;
        this.resultSetId = resultSetId;
        this.currentRowIdx = 0;
        this.currentRow = null;
        nextRows();
    }

    public boolean next() throws SQLException {
        if (currentRowIdx < rowCount) {
            currentRow = rows.get(currentRowIdx++);
            return true;
        } else {
            currentRowIdx = 0;
            currentRow = null;
            if (hasMore) {
                nextRows();
                if (rowCount == 0)
                    return false;
            } else {
                return false;
            }
            currentRow = rows.get(currentRowIdx++);
            return true;
        }
    }

    public Object getObject(int index) {
        if (currentRow == null) throw new IllegalStateException("There is no current row set");

        var field = currentRow.getField(index-1);
        var value = field.getValue();
        if (value.getIsNull()) return null;

        switch (value.getType()) {
            case VALUE_DATE:
                // TODO: Convert String to "Date" object
                return value.getDate();
            case VALUE_DECIMAL:
                return new BigDecimal(value.getDecimal());
            case VALUE_INTEGRAL:
                return value.getInt();
            case VALUE_TEXT:
                return value.getText();
            case VALUE_UNKNOWN:
            default:
                throw new IllegalStateException("Unknown data type");
        }
    }
    public String getString(int index) {
        Object value = getObject(index);
        return value == null ? null : value.toString();
    }


    public BigDecimal getDecimal(int index) {
        if (currentRow == null) throw new IllegalStateException("There is no current row set");

        var field = currentRow.getField(index-1);
        var value = field.getValue();
        if (value.getIsNull()) return null;

        if (ValueType.VALUE_DECIMAL.equals(value.getType()))
            return new BigDecimal(value.getDecimal());

        throw new IllegalStateException("Invalid type");
    }

    private void nextRows() throws SQLException {
        var nextRowsRequest = NextRowsRequest.newBuilder()
                .setStatementId(statementId)
                .setResultSetId(resultSetId)
                .build();
        var command = DatabaseCommand.newBuilder()
                .setType(Command.COMMAND_NEXTROWS)
                .setNextRows(nextRowsRequest)
                .build();
        var result = manager.execute(command);
        DatabaseClient.checkError(result);
        var nextRowsResponse = result.getNextRows();
        this.rows = nextRowsResponse.getRowList();
        this.hasMore = nextRowsResponse.getHasMore();
        this.rowCount = nextRowsResponse.getRowCount();
    }

    public void close() throws SQLException {
        var closeRequest = CommandModel.CloseResultSetRequest.newBuilder()
                .setResultSetId(resultSetId)
                .build();
        var command = DatabaseCommand.newBuilder()
                .setType(Command.COMMAND_CLOSERESULTSET)
                .setCloseResultSet(closeRequest)
                .build();
        var result= manager.execute(command);
        DatabaseClient.checkError(result);
    }
}
