package ru.fizteh.fivt.students.VasilevKirill.telnet.Commands;

import ru.fizteh.fivt.students.VasilevKirill.telnet.Commands.shelldata.Command;
import ru.fizteh.fivt.students.VasilevKirill.telnet.Commands.shelldata.Status;
import ru.fizteh.fivt.students.VasilevKirill.telnet.structures.MyTableProvider;

import java.io.IOException;

/**
 * Created by Kirill on 10.11.2014.
 */
public class RollbackCommand implements Command {
    @Override
    public int execute(String[] args, Status status) throws IOException {
        if (!checkArgs(args)) {
            throw new IOException("Wrong arguments");
        }
        MyTableProvider tableProvider = status.getMultiMap();
        if (tableProvider == null) {
            throw new IOException("Status has incorrect object");
        }
        tableProvider.handleTable(args);
        return 0;
    }

    @Override
    public boolean checkArgs(String[] args) {
        if (args == null) {
            return false;
        }
        return args.length == 1;
    }

    @Override
    public String toString() {
        return "rollback";
    }
}
