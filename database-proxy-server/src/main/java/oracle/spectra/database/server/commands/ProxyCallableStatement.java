package oracle.spectra.database.server.commands;

import oracle.jdbc.OracleCallableStatement;

import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyCallableStatement {

    private final OracleCallableStatement stmt;

    public ProxyCallableStatement(OracleCallableStatement stmt) throws SQLException {
        this.stmt = stmt;
    }

    public OracleCallableStatement getStatement() {
        return stmt;
    }

    public void close() throws SQLException {
        stmt.close();
    }

    static Map<Integer, ProxyCallableStatement> callableStatement = new ConcurrentHashMap<>();
    static AtomicInteger callableStatementIdx = new AtomicInteger(0);

    public static int add(ProxyCallableStatement queryStmt) {
        var idx = callableStatementIdx.getAndAdd(1);
        callableStatement.put(idx, queryStmt);
        return idx;
    }

    public static ProxyCallableStatement get(int idx) {
        return callableStatement.get(idx);
    }


}
