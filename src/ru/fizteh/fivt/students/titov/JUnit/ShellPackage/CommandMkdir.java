package ru.fizteh.fivt.students.titov.JUnit.ShellPackage;

import java.nio.file.Files;

public class CommandMkdir extends Command {
    public CommandMkdir() {
        name = "mkdir";
        numberOfArguments = 2;
    }

    @Override
    public boolean run(final String[] arguments) {
        if (arguments.length != numberOfArguments) {
            System.err.println("wrong number of arguments");
            return false;
        }
        try {
            Files.createDirectory(PathsFunction.toAbsolutePathString(arguments[1]));
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
