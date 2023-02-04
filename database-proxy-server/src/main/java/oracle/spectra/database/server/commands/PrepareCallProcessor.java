package oracle.spectra.database.server.commands;

import oracle.jdbc.OracleCallableStatement;
import oracle.spectra.database.model.CommandModel.*;

import java.sql.Connection;

public class PrepareCallProcessor extends CommandProcessor {

    @Override
    DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable {
        var request = command.getPrepareCall();
        var sql = request.getSql();
        var stmt = conn.prepareCall(sql);
        var callableStatement = new ProxyCallableStatement((OracleCallableStatement) stmt);
        var stmtId = ProxyCallableStatement.add(callableStatement);
        var response = PrepareCallResponse.newBuilder()
                .setStatementId(stmtId)
                .build();
        return DatabaseResult.newBuilder()
                .setType(Command.COMMAND_PREPARECALL)
                .setPrepareCall(response)
                .build();
    }
}
