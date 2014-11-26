package ru.fizteh.fivt.students.torunova.storeable.actions;

import ru.fizteh.fivt.students.torunova.storeable.DatabaseWrapper;
import ru.fizteh.fivt.students.torunova.storeable.exceptions.IncorrectFileException;

import java.io.IOException;

/**
 * Created by nastya on 21.10.14.
 */
public class DropTable extends Action {
    @Override
    public boolean run(String[] args, DatabaseWrapper db) throws IOException, IncorrectFileException {
        if (!checkNumberOfArguments(1, args.length)) {
            return false;
        }
        try {
            db.removeTable(args[0]);
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            return false;
        }
        System.out.println("dropped");
        return true;
    }

    @Override
    public String getName() {
        return "drop";
    }
}
