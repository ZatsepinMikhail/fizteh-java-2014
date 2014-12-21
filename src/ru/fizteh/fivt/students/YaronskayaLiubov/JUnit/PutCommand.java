package ru.fizteh.fivt.students.YaronskayaLiubov.JUnit;

/**
 * Created by luba_yaronskaya on 19.10.14.
 */
public class PutCommand extends Command {
    PutCommand() {
        name = "put";
        numberOfArguments = 3;
    }

    boolean execute(String[] args) {
        if (args.length != numberOfArguments) {
            System.err.println(name + ": wrong number of arguements");
            return false;
        }
        if (MultiFileHashMap.currTable == null) {
            System.err.println("no table");
            return false;
        }
        String old = MultiFileHashMap.currTable.put(args[1], args[2]);
        if (old != null) {
            System.out.println("overwrite");
            System.out.println(old);
        } else {
            System.out.println("new");
        }
        return true;
    }
}
