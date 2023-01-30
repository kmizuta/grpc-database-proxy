package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class CommandProcessor {

    public static DatabaseResult doCommand(Connection conn, DatabaseCommand command) {
        CommandProcessor processor = CommandProcssorFactory.getInstance(command.getType());
        if (processor != null) {
            try {
                return processor.doCommandImpl(conn, command);
            } catch (Throwable t) {
                // TODO: implement error handling
                return DatabaseResult.newBuilder()
                        .setType(Command.COMMAND_ERROR)
                        .setError(ErrorResponse.newBuilder()
                                .setErrorMessage(t.getMessage())
                                .build())
                        .build();
            }
        } else {
            return DatabaseResult.newBuilder()
                    .setType(Command.COMMAND_ERROR)
                    .setError(ErrorResponse.newBuilder()
                            .setErrorMessage(String.format("No process for this command - %s", command.getType())).build())
                    .build();
        }
    }

    abstract DatabaseResult doCommandImpl(Connection conn, DatabaseCommand command) throws Throwable;

}
