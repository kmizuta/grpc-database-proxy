package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel.Command;

import java.util.HashMap;
import java.util.Map;

public class CommandProcssorFactory {
    static Map<Command, CommandProcessor> processors = new HashMap<>();
    static {
        processors.put(Command.COMMAND_PREPARESTATEMENT, new PrepareStatementProcessor());
        processors.put(Command.COMMAND_PREPARECALL, new PrepareCallProcessor());
        processors.put(Command.COMMAND_BIND, new BindProcessor());
        processors.put(Command.COMMAND_EXECUTE, new ExecuteProcessor());
        processors.put(Command.COMMAND_EXECUTEQUERY, new ExecuteQueryProcessor());
        processors.put(Command.COMMAND_NEXTROWS, new NextRowsProcessor());
        processors.put(Command.COMMAND_CLOSERESULTSET, new CloseResultSetProcessor());
        processors.put(Command.COMMAND_CLOSEPREPAREDSTATEMENT, new ClosePreparedStatementProcessor());
        processors.put(Command.COMMAND_CLOSECALLABLESTATEMENT, new CloseCallableStatementProcessor());
    }

    public static CommandProcessor getInstance(Command command) {
        return processors.get(command);
    }
}
