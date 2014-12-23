package ru.fizteh.fivt.students.irina_karatsapova.proxy.server.database.commands.database_commands;

import ru.fizteh.fivt.students.irina_karatsapova.proxy.server.database.InterpreterStateDatabase;
import ru.fizteh.fivt.students.irina_karatsapova.proxy.server.database.commands.DatabaseCommand;
import ru.fizteh.fivt.students.irina_karatsapova.proxy.server.database.utils.Utils;

public class RollbackCommand implements DatabaseCommand {
    public void execute(InterpreterStateDatabase state, String[] args) throws Exception {
        if (!Utils.checkTableChosen(state)) {
            return;
        }
        int changesNumber = state.getTable().rollback();
        state.out.println(changesNumber);
    }

    public String name() {
        return "rollback";
    }

    public int minArgs() {
        return 1;
    }

    public int maxArgs() {
        return 1;
    }
}
