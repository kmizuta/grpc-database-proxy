package oracle.spectra.database.server.commands;

import oracle.jdbc.OraclePreparedStatement;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ProxyResultSet {

    private final ProxyPreparedStatement stmt;
    private final ResultSet rset;

    public ProxyResultSet(ProxyPreparedStatement stmt, ResultSet rset) {
        this.stmt = stmt;
        this.rset = rset;
        stmt.addResultSet(this);
    }

    ProxyPreparedStatement getPreparedStatement() {
        return stmt;
    }

    public ResultSet getResultSet() { return rset; }

    public void close() throws SQLException {
        stmt.removeResultSet(this);
        rset.close();
    }

    static Map<Integer, ProxyResultSet> resultSets = new ConcurrentHashMap<>();
    static AtomicInteger resultSetIdx = new AtomicInteger(0);


    public static int add(ProxyResultSet resultSet) {
        var idx = resultSetIdx.getAndAdd(1);
        resultSets.put(idx, resultSet);
        return idx;
    }

    public static ProxyResultSet get(int idx) {
        return resultSets.get(idx);
    }

}
