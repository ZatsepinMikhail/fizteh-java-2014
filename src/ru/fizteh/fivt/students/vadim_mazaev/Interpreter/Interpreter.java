package ru.fizteh.fivt.students.vadim_mazaev.Interpreter;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Interpreter {
    public static final String PROMPT = "$ ";
    public static final String NO_SUCH_COMMAND_MSG = "No such command declared: ";
    private static final String IGNORE_IN_DOUBLE_QUOTES_REGEX = "(?=([^\"]*\"[^\"]*\")*[^\"]*$)";
    private static final String SPLIT_BY_SPACES_NOT_IN_BRACKETS_REGEX
        = "\\s*(\".*\"|\\(.*\\)|\\[.*\\]|[^\\s]+)\\s*";
    public static final String COMMAND_SEPARATOR = ";";
    private InputStream in;
    private PrintStream out;
    private Object connector;
    private Map<String, Command> commands;
    private Callable<Boolean> exitHandler;
    
    public Interpreter(Object connector, Command[] commands, InputStream in, PrintStream out) {
        if (in == null || out == null) {
            throw new IllegalArgumentException("Input or Output stream is null");
        }
        this.commands = new HashMap<>();
        this.in = in;
        this.out = out;
        this.connector = connector;
        for (Command cmd : commands) {
            this.commands.put(cmd.getName(), cmd);
        }
    }
    
    public Interpreter(Object connector, Command[] commands) {
        this(connector, commands, System.in, System.out);
    }
    
    public int run(String[] args) throws Exception {
        int exitStatus;
        try {
            if (args.length == 0) {
                exitStatus = interactiveMode();
            } else {
                exitStatus = batchMode(args);
            }
        } catch (StopInterpreterException e) {
            exitStatus = e.getExitStatus();
        }
        return exitStatus;
    }
    
    public void setExitHandler(Callable<Boolean> callable) {
        exitHandler = callable;
    }
    
    private int batchMode(String[] args) throws Exception {
        int exitStatus = executeLine(String.join(" ", args));
        if (exitHandler != null) {
            exitHandler.call();
        }
        return exitStatus;
    }

    private int interactiveMode() throws Exception {
        int exitStatus = 0;
        try (Scanner in = new Scanner(this.in)) {
            while (true) {
                out.print(PROMPT);
                try {
                    exitStatus = executeLine(in.nextLine().trim());
                } catch (NoSuchElementException e) {
                    break;
                } catch (StopInterpreterException e) {
                    if (e.getExitStatus() == 0) {
                        break;
                    }
                }
            }
        }
        return exitStatus;
    }
    
    private int executeLine(String line) throws Exception {
        String[] cmds = line.split(COMMAND_SEPARATOR + IGNORE_IN_DOUBLE_QUOTES_REGEX);
        Pattern p = Pattern.compile(SPLIT_BY_SPACES_NOT_IN_BRACKETS_REGEX);
        List<String> tokens = new LinkedList<>();
        try {
            for (String current : cmds) {
                tokens.clear();
                Matcher m = p.matcher(current.trim());
                while (m.find()) {
                    tokens.add(m.group().trim());
                }
                parse(tokens.toArray(new String[tokens.size()]));
            }
            return 0;
        } catch (StopLineInterpretationException e) {
            out.println(e.getMessage());
            return 1;
        }
    }
    
    private void parse(String[] commandWithArgs) throws Exception {
        if (commandWithArgs.length > 0 && !commandWithArgs[0].isEmpty()) {
            String commandName = commandWithArgs[0];
            if (commandName.equals("exit")) {
                if (exitHandler == null) {
                    throw new StopInterpreterException(0);
                }
                if (exitHandler.call()) {
                    throw new StopInterpreterException(0);
                } else {
                    throw new StopInterpreterException(1);
                }
            }
            Command command = commands.get(commandName);
            if (command == null) {
                throw new StopLineInterpretationException(NO_SUCH_COMMAND_MSG + commandName);
            } else {
                String[] args = new String[commandWithArgs.length - 1];
                //Exclude quotes along the edges of the string, if they presents.
                for (int i = 1; i < commandWithArgs.length; i++) {
                    if (commandWithArgs[i].charAt(0) == '"'
                            && commandWithArgs[i].charAt(commandWithArgs[i].length() - 1) == '"') {
                        args[i - 1] = commandWithArgs[i].substring(1, commandWithArgs[i].length() - 1);
                    } else {
                        args[i - 1] = commandWithArgs[i];
                    }
                }
                try {
                    command.execute(connector, args);
                } catch (RuntimeException e) {
                    throw new StopLineInterpretationException(e.getMessage());
                }
            }
        }
    }
}
