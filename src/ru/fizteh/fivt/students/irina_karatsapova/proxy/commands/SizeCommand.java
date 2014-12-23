package ru.fizteh.fivt.students.irina_karatsapova.proxy.commands;

import ru.fizteh.fivt.students.irina_karatsapova.proxy.InterpreterState;
import ru.fizteh.fivt.students.irina_karatsapova.proxy.utils.Utils;

public class SizeCommand implements Command {
    public void execute(InterpreterState state, String[] args) throws Exception {
        if (!Utils.checkTableChosen(state)) {
            return;
        }
        int size = state.table.size();
        state.out.println(size);
    }

    public String name() {
        return "size";
    }

    public int minArgs() {
        return 1;
    }

    public int maxArgs() {
        return 1;
    }
}
