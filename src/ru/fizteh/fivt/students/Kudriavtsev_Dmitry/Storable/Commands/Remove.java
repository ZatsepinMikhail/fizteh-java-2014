package ru.fizteh.fivt.students.Kudriavtsev_Dmitry.Storable.Commands;

import ru.fizteh.fivt.students.Kudriavtsev_Dmitry.Storable.Connector;

/**
 * Created by Дмитрий on 04.10.14.
 */
public class Remove extends StoreableCommand {
    public Remove() {
        super("remove", 1);
    }

    @Override
    public boolean exec(Connector dbConnector, String[] args) {
        if (!checkArguments(args.length)) {
            return !batchModeInInteractive;
        }
        if (dbConnector.getActiveTable() == null) {
            if (batchModeInInteractive) {
                System.err.println("No table");
                return false;
            }
            noTable();
            return true;
        }
        if (dbConnector.getActiveTable().remove(args[0]) != null) {
            System.out.println("removed");
            dbConnector.getActiveTable().getChangedFiles().put(
                    dbConnector.getActiveTable().whereToSave("", args[0]).getKey(), 0);
        } else {
            System.err.println("not found");
            if (batchModeInInteractive) {
                return false;
            }
        }
        return true;
    }

}
