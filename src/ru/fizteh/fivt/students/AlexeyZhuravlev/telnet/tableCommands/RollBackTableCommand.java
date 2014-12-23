package ru.fizteh.fivt.students.AlexeyZhuravlev.telnet.tableCommands;

import ru.fizteh.fivt.students.AlexeyZhuravlev.telnet.ShellTableProvider;

import java.io.PrintStream;

/**
 * @author AlexeyZhuravlev
 */
public class RollBackTableCommand extends TableCommand {

    @Override
    public void execute(ShellTableProvider base, PrintStream out) throws Exception {
        if (base.getUsing() == null) {
            out.println("no table");
        } else {
            out.println(base.getUsing().rollback());
        }
    }

    @Override
    protected int numberOfArguments() {
        return 0;
    }
}
