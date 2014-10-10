package ru.fizteh.fivt.students.ZatsepinMikhail.FileMap;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * FileMap
 * Version from 28.09.2014
 * Created by Mikhail Zatsepin, 395
 */

public class DbMain {
    public static void main(String[] args) {
        String dataBasePath = System.getProperty("db.file");
        if (dataBasePath == null || !Files.exists(Paths.get(dataBasePath))) {
            System.out.println("cannot access the database");
            System.exit(1);
        }
        FileMap myFileMap = new FileMap(dataBasePath);
        boolean errorOcuried = false;
        if (!myFileMap.init()) {
            errorOcuried = true;
        }

        Shell<FileMap> myShell = new Shell<>(myFileMap);
        myShell.addCommand(new Get());
        myShell.addCommand(new List());
        myShell.addCommand(new Put());
        myShell.addCommand(new Remove());

        if (args.length > 0) {
            if (!myShell.packetMode(args)) {
                errorOcuried = true;
            }
        } else {
            if (!myShell.interactiveMode()) {
                errorOcuried = true;
            }
        }
        if (errorOcuried) {
            System.exit(2);
        } else {
            System.exit(0);
        }
    }
}
