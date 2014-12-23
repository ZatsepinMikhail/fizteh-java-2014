package ru.fizteh.fivt.students.EgorLunichkin.JUnit;

import java.util.Scanner;

public class Executor {
    public Executor(String[] args) throws Exception {
        String dbPath = System.getProperty("fizteh.db.dir");
        myTableProvider = new MyTableProvider(dbPath);
        if (args.length == 0) {
            interactiveMode();
        } else {
            packageMode(args);
        }
    }

    private MyTableProvider myTableProvider;

    void interactiveMode() throws Exception {
        Scanner in = new Scanner(System.in);
        System.out.print("$ ");
        while (in.hasNextLine()) {
            String[] commands = in.nextLine().trim().split(";");
            for (String command : commands) {
                executeCommand(command);
            }
            System.out.print("$ ");
        }
        in.close();
        System.out.close();
    }

    void packageMode(String[] args) throws Exception {
        StringBuilder line = new StringBuilder();
        for (String arg : args) {
            line.append(arg + ' ');
        }
        String[] commands = line.toString().trim().split(";");
        for (String command : commands) {
            executeCommand(command);
        }
    }

    void executeCommand(String cmd) throws Exception {
        String[] command = cmd.trim().split("\\s+");
        JUnitCommand exec;
        switch(command[0]) {
            case "put":
                if (command.length > 3) {
                    throw new JUnitException("put: Too many arguments");
                }
                if (command.length < 3) {
                    throw new JUnitException("put: Too few arguments");
                }
                exec = new JUnitPutCommand(myTableProvider, command[1], command[2]);
                break;
            case "get":
                if (command.length > 2) {
                    throw new JUnitException("get: Too many arguments");
                }
                if (command.length < 2) {
                    throw new JUnitException("get: Too few arguments");
                }
                exec = new JUnitGetCommand(myTableProvider, command[1]);
                break;
            case "remove":
                if (command.length > 2) {
                    throw new JUnitException("remove: Too many arguments");
                }
                if (command.length < 2) {
                    throw new JUnitException("removeL Too few arguments");
                }
                exec = new JUnitRemoveCommand(myTableProvider, command[1]);
                break;
            case "list":
                if (command.length > 1) {
                    throw new JUnitException("list: Too many arguments");
                }
                exec = new JUnitListCommand(myTableProvider);
                break;
            case "create":
                if (command.length > 2) {
                    throw new JUnitException("create: Too many arguments");
                }
                if (command.length < 2) {
                    throw new JUnitException("create: Too few arguments");
                }
                exec = new JUnitCreateCommand(myTableProvider, command[1]);
                break;
            case "drop":
                if (command.length > 2) {
                    throw new JUnitException("drop: Too many arguments");
                }
                if (command.length < 2) {
                    throw new JUnitException("drop: Too few arguments");
                }
                exec = new JUnitDropCommand(myTableProvider, command[1]);
                break;
            case "use":
                if (command.length > 2) {
                    throw new JUnitException("use: Too many arguments");
                }
                if (command.length < 2) {
                    throw new JUnitException("use: Too few arguments");
                }
                exec = new JUnitUseCommand(myTableProvider, command[1]);
                break;
            case "show":
                if (command.length < 2 || !command[1].equals("tables")) {
                    throw new JUnitException("Unknown command");
                }
                if (command.length > 2) {
                    throw new JUnitException("show tables: Too many agruments");
                }
                exec = new JUnitShowTablesCommand(myTableProvider);
                break;
            case "size":
                if (command.length > 1) {
                    throw new JUnitException("size: Too many arguments");
                }
                exec = new SizeCommand(myTableProvider);
                break;
            case "commit":
                if (command.length > 1) {
                    throw new JUnitException("commit: Too many arguments");
                }
                exec = new CommitCommand(myTableProvider);
                break;
            case "rollback":
                if (command.length > 1) {
                    throw new JUnitException("rollback: Too many arguments");
                }
                exec = new RollbackCommand(myTableProvider);
                break;
            case "exit":
                if (command.length > 1) {
                    throw new JUnitException("exit: Too many arguments");
                }
                exec = new ExitCommand();
                break;
            default:
                throw new JUnitException("Unknown command");
        }
        exec.run();
    }
}
