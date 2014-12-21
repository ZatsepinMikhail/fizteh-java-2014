package ru.fizteh.fivt.students.Kudriavtsev_Dmitry.Parallel.Commands;

import ru.fizteh.fivt.students.Kudriavtsev_Dmitry.Parallel.Welcome;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Дмитрий on 07.10.14.
 */
public class Create extends StoreableCommand {
    private static final HashMap<String, Class<?>> TYPES;

    static {
        TYPES = new HashMap<>();
        TYPES.put("int", Integer.class);
        TYPES.put("long", Long.class);
        TYPES.put("byte", Byte.class);
        TYPES.put("float", Float.class);
        TYPES.put("double", Double.class);
        TYPES.put("boolean", Boolean.class);
        TYPES.put("String", String.class);
    }

    public Create() {
        super("create", 1);
    }

    @Override
    public boolean exec(Welcome dbConnector, String[] args, PrintStream out, PrintStream err) {
        if (!checkMoreArguments(args.length, err)) {
            return !batchModeInInteractive;
        }
        if (!args[1].startsWith("(") || !args[args.length - 1].endsWith(")")) {
            err.println("wrong arguments: ( ) not found");
            return !batchModeInInteractive;
        }
        java.util.List<Class<?>> types = new ArrayList<>();
        args[1] = args[1].substring(1);
        args[args.length - 1] = args[args.length - 1].substring(0, (args[args.length - 1]).length() - 1);
        try {
            for (int i = 1; i < args.length; ++i) {
                String tempName = args[i];
                types.add(classByName(tempName));
            }
            for (Class<?> myType: types) {
                if (!TYPES.containsValue(myType)) {
                    out.println("wrong type ( don't have type: " + myType + " )");
                    return !batchModeInInteractive;
                }
            }
            if (dbConnector.getActiveTableProvider().createTable(args[0], types) != null) {
                out.println("created");
            }
        } catch (IOException e) {
            err.println("Some IOException happen (in createTable)");
            return !batchModeInInteractive;
        }
        return true;
    }

    private static Class<?> classByName(String name) throws IOException {
        if (!TYPES.containsKey(name)) {
            throw new IOException("Unknown type name: " + name);
        }
        return TYPES.get(name);
    }
}
