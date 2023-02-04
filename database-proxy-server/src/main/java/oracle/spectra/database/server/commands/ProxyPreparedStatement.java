package oracle.spectra.database.server.commands;

import oracle.jdbc.OraclePreparedStatement;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyPreparedStatement {

    private final OraclePreparedStatement stmt;
    private final int fetchSize;
    private final ResultSetMetaData resultSetMetaData;

    public ProxyPreparedStatement(OraclePreparedStatement stmt) throws SQLException {
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

    public void close() throws SQLException {
        List<SQLException> exceptions = null;
        for (ProxyResultSet resultSet : resultSets) {
            try {
                resultSet.close();
            } catch(SQLException e) {
                if (exceptions == null)
                    exceptions = new ArrayList<>();
            }
        }
        try {
            stmt.close();
        } catch(SQLException e) {
            if (exceptions == null)
                exceptions = new ArrayList<>();
        }

        if (exceptions == null)
            return;
        else if (exceptions.size() == 1)
            throw exceptions.get(0);
        else
            throw new SQLException("Multiple exceptions on close");
    }

    static Map<Integer, ProxyPreparedStatement> queryStatements = new ConcurrentHashMap<>();
    static AtomicInteger queryStatementIdx = new AtomicInteger(0);

    public static int add(ProxyPreparedStatement queryStmt) {
        var idx = queryStatementIdx.getAndAdd(1);
        queryStatements.put(idx, queryStmt);
        return idx;
    }

    public static ProxyPreparedStatement get(int idx) {
        return queryStatements.get(idx);
    }
    List<ProxyResultSet> resultSets = new ArrayList<>();

    void addResultSet(ProxyResultSet resultSet) {
        resultSets.add(resultSet);
    }

    void removeResultSet(ProxyResultSet resultSet) {
        resultSets.remove(resultSet);
    }
}
