package ru.fizteh.fivt.students.ZatsepinMikhail.FileMap;

public class Get extends CommandFileMap {
    public Get() {
        name = "get";
        numberOfArguments = 2;
    }
    @Override
    public boolean run(FileMapState newFileMap, String[] args) {
        if (args.length != numberOfArguments) {
            System.out.println(name + ": wrong number of arguments");
            return false;
        }
        FileMap myFileMap = newFileMap.getFileMap();
        String value = myFileMap.get(args[1]);
        if (value != null) {
            System.out.println("found\n" + value);
        } else {
            System.out.println("not found");
        }
        myFileMap.load();
        return true;
    }
}
