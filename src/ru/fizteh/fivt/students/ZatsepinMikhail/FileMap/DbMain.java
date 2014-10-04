package ru.fizteh.fivt.students.ZatsepinMikhail.FileMap;

/**
 * FileMap
 * Version from 28.09.2014
 * Created by Mikhail Zatsepin, 395
 */

public class DbMain {
    public static void main(String[] args) {
        FileMap myFileMap = new FileMap(System.getProperty("db.file"));
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
