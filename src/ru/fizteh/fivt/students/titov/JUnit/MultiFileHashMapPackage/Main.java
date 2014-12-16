package ru.fizteh.fivt.students.titov.JUnit.MultiFileHashMapPackage;

import ru.fizteh.fivt.students.titov.JUnit.FileMapPackage.Shell;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    private static final String PROJECTPROPERTY = "fizteh.db.dir";

    public static void main(String[] args) {

        if (System.getProperty(PROJECTPROPERTY) == null) {
            System.err.println("we need working directory");
            System.exit(6);
        }
        Path dataBaseDirectory
                = Paths.get(System.getProperty("user.dir")).resolve(System.getProperty(PROJECTPROPERTY));

        boolean allRight = true;
        if (Files.exists(dataBaseDirectory)) {
            if (!Files.isDirectory(dataBaseDirectory)) {
                System.err.println("working directory is a file");
                System.exit(4);
            }
        } else {
            try {
                Files.createDirectory(dataBaseDirectory);
            } catch (IOException e) {
                System.err.println("error while creating directory");
                allRight = false;
            } finally {
                if (!allRight) {
                    System.exit(5);
                }
            }
        }
        MFileHashMap myMFileHashMap = new MFileHashMap(dataBaseDirectory.toString());
        if (!myMFileHashMap.init()) {
            System.exit(3);
        }

        Shell<MFileHashMap> myShell = new Shell<>(myMFileHashMap);
        setUpShell(myShell);

        if (args.length > 0) {
            allRight = myShell.batchMode(args);
        } else {
            allRight = myShell.interactiveMode();
        }
        if (allRight) {
            System.exit(0);
        } else {
            System.exit(1);
        }
    }


    public static void setUpShell(Shell<MFileHashMap> myShell) {

        myShell.addCommand(new CommandCreate());
        myShell.addCommand(new CommandDrop());
        myShell.addCommand(new CommandUse());
        myShell.addCommand(new CommandGetDistribute());
        myShell.addCommand(new CommandPutDistribute());
        myShell.addCommand(new CommandListDistribute());
        myShell.addCommand(new CommandRemoveDistribute());
        myShell.addCommand(new CommandShowTables());
        myShell.addCommand(new CommandRollback());
        myShell.addCommand(new CommandCommit());
        myShell.addCommand(new CommandSize());
        myShell.addCommand(new CommandExit());
    }
}

