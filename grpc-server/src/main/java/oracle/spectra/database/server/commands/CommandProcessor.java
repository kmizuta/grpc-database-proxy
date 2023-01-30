package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel.*;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public abstract class CommandProcessor {

    static Map<Command, CommandProcessor> processors = new HashMap<>();
    static {
        processors.put(Command.COMMAND_PREPAREQUERY, new PrepareQueryProcessor());
        processors.put(Command.COMMAND_BIND, new BindProcessor());
        processors.put(Command.COMMAND_EXECUTEQUERY, new ExecuteQueryProcessor());
        processors.put(Command.COMMAND_NEXTROWS, new NextRowsProcessor());
    }

    public static final DatabaseResult doCommand(Connection conn, DatabaseCommand command) {
        CommandProcessor processor = processors.get(command.getType());
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
