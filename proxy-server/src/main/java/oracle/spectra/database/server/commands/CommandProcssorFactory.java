package oracle.spectra.database.server.commands;

import oracle.spectra.database.model.CommandModel.Command;

import java.util.HashMap;
import java.util.Map;

public class CommandProcssorFactory {
    static Map<Command, CommandProcessor> processors = new HashMap<>();
    static {
        processors.put(Command.COMMAND_PREPAREQUERY, new PrepareQueryProcessor());
        processors.put(Command.COMMAND_BIND, new BindProcessor());
        processors.put(Command.COMMAND_EXECUTEQUERY, new ExecuteQueryProcessor());
        processors.put(Command.COMMAND_NEXTROWS, new NextRowsProcessor());
    }

    public static CommandProcessor getInstance(Command command) {
        return processors.get(command);
    }
}
