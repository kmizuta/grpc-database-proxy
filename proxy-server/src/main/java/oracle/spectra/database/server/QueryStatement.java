package oracle.spectra.database.server;

import oracle.jdbc.OraclePreparedStatement;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class QueryStatement {

    private final OraclePreparedStatement stmt;
    private final int fetchSize;
    private final ResultSetMetaData resultSetMetaData;

    public QueryStatement(OraclePreparedStatement stmt) throws SQLException {
        this.stmt = stmt;
        this.fetchSize = stmt.getFetchSize();
        this.resultSetMetaData = stmt.getMetaData();
    }

    public OraclePreparedStatement getStatement() {
        return stmt;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public int getColumnCount() throws SQLException {
        return resultSetMetaData.getColumnCount();
    }

    public String getColumnName(int idx) throws SQLException {
        return resultSetMetaData.getColumnName(idx);
    }

    static Map<Integer, QueryStatement> queryStatements = new ConcurrentHashMap<>();
    static AtomicInteger queryStatementIdx = new AtomicInteger(0);

    public static int add(QueryStatement queryStmt) {
        var idx = queryStatementIdx.getAndAdd(1);
        queryStatements.put(idx, queryStmt);
        return idx;
    }

    public static QueryStatement get(int idx) {
        return queryStatements.get(idx);
    }

    Map<Integer, ResultSet> resultSets = new ConcurrentHashMap<>();
    AtomicInteger resultSetIdx = new AtomicInteger(0);

    public int addResultSet(ResultSet resultSet) {
        var idx = resultSetIdx.getAndAdd(1);
        resultSets.put(idx, resultSet);
        return idx;
    }

    public ResultSet getResultSet(int idx) {
        return resultSets.get(idx);
    }
}
