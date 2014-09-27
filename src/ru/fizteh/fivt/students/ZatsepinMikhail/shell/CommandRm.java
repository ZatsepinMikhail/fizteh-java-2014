package ru.fizteh.fivt.students.ZatsepinMikhail.shell;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CommandRm extends Command {
    public CommandRm() {
        name = "rm";
        numberOfArguments = 3;
    }

    @Override
    public boolean run(final String[] arguments) {
        if (arguments.length != numberOfArguments
                & arguments.length != numberOfArguments - 1) {
            return false;
        }
        boolean recursive =
                (arguments.length == numberOfArguments & arguments[1].equals("-r"));
        boolean result;
        if (!recursive) {
            result = generalDelete(arguments);
        } else {
            result = recursiveDelete(arguments);
        }
        return result;
    }

    private boolean generalDelete(final String[] arguments) {
        Path filePath = PathsFunction.toAbsolutePathString(arguments[1]);
        try {
            if (!Files.deleteIfExists(filePath)) {
                System.out.println(name + ": cannot remove \'" + arguments[1] + "\'"
                                   + ": No such file or directory");
            }
        } catch (Exception e) {
            if (Files.isDirectory(filePath)) {
                System.out.println(name + ": " + arguments[1] + ": is a directory");
            }
            return false;
        }
        return true;
    }

    private boolean recursiveDelete(final String[] arguments) {
        Path dirPath = PathsFunction.toAbsolutePathString(arguments[2]);
        if (!Files.isDirectory(dirPath)) {
            return false;
        }

        FileVisitorDelete myFileVisitorDelete = new FileVisitorDelete();

        try {
            Files.walkFileTree(dirPath, myFileVisitorDelete);
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
