package ru.fizteh.fivt.students.ZatsepinMikhail.FileMap;

import java.util.HashMap;

public class Remove extends CommandFileMap {
    public Remove() {
        name = "remove";
        numberOfArguments = 2;
    }
    @Override
    public boolean run(FileMapState newFileMap, String[] args) {
        if (args.length != numberOfArguments) {
            System.out.println(name + ": wrong number of arguments");
            return false;
        }
        FileMap myFileMap = newFileMap.getFileMap();
        String value = myFileMap.remove(args[1]);
        if (value != null) {
            System.out.println("removed");
        } else {
            System.out.println("not found");
        }
        myFileMap.load();
        return true;
    }
}
