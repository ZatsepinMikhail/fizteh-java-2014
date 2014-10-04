package ru.fizteh.fivt.students.ZatsepinMikhail.FileMap;

import java.util.HashMap;

/**
 * Created by mikhail on 26.09.14.
 */
public class Put extends CommandFileMap {
    public Put() {
        name = "put";
        numberOfArguments = 3;
    }
    @Override
    public boolean run(FileMapState newFileMap, String[] args) {
        if (args.length != numberOfArguments) {
            System.out.println(name + ": wrong number of arguments");
            return false;
        }
        FileMap myFileMap = newFileMap.getFileMap();
        String oldValue = myFileMap.put(args[1], args[2]);
        if (oldValue != null) {
            System.out.println("overwrite\n" + oldValue);
        } else {
            System.out.println("new");
        }
        myFileMap.load();
        return true;
    }
}
