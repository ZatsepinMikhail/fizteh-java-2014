package ru.fizteh.fivt.students.dnovikov.storeable;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.students.dnovikov.storeable.Exceptions.LoadOrSaveException;
import ru.fizteh.fivt.students.dnovikov.storeable.Exceptions.TableNotFoundException;
import ru.fizteh.fivt.students.dnovikov.storeable.Interpreter.Interpreter;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class MultiFileHashMapMain {

    private DataBaseProvider provider;

    public static void main(String[] args) {
        MultiFileHashMapMain fileMap = new MultiFileHashMapMain();
        String directoryPath = System.getProperty("fizteh.db.dir");
        if (directoryPath == null) {
            System.err.println("database directory not set");
            System.exit(1);
        }
        try {
            fileMap.run(args, directoryPath);
        } catch (LoadOrSaveException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private void run(String[] args, String directoryPath) throws LoadOrSaveException {
        provider = (DataBaseProvider) new DataBaseProviderFactory().create(directoryPath);
        DataBaseState dbState = new DataBaseState(provider);
        DataBaseCommand[] commands = createCommands();
        Interpreter interpreter = new Interpreter(dbState, System.in, System.out, System.err, commands);
        interpreter.run(args);
    }

    DataBaseCommand[] createCommands() {
        DataBaseCommand[] commands = new DataBaseCommand[]{
                new DataBaseCommand("get", 1, (state, args) -> {
                    DataBaseTable currentTable = ((DataBaseState) state).getCurrentTable();
                    if (currentTable == null) {
                        System.out.println("no table");
                    } else {
                        StoreableType result = (StoreableType) currentTable.get(args[0]);
                        if (result == null) {
                            System.out.println("not found");
                        } else {
                            System.out.println("found");
                            for (int i = 0; i < result.getNumberOfColumns(); ++i) {
                                System.out.println(result.getColumnAt(i));
                            }
                        }
                    }
                }),
                new DataBaseCommand("put", -1, (state, args) -> {
                    DataBaseTable currentTable = ((DataBaseState) state).getCurrentTable();
                    DataBaseProvider connector = ((DataBaseState) state).getTableProvider();
                    if (currentTable == null) {
                        System.out.println("no table");
                    } else {
                        Storeable result;
                        try {
                            String value = Utils.getJSONStringForPut(args);
                            result = currentTable.put(args[0], connector.deserialize(currentTable, value));
                        } catch (ParseException | ColumnFormatException e) {
                            System.err.println("wrong type (" + e.getMessage() + ")");
                            return;
                        }
                        if (result == null) {
                            System.out.println("new");
                        } else {
                            System.out.println("overwrite");
                            System.out.println(connector.serialize(currentTable, result));
                        }
                    }
                }),
                new DataBaseCommand("list", 0, (state, args) -> {
                    DataBaseTable currentTable = ((DataBaseState) state).getCurrentTable();
                    if (currentTable == null) {
                        System.out.println("no table");
                    } else {
                        System.out.println(String.join(", ", currentTable.list()));
                    }
                }),
                new DataBaseCommand("remove", 1, (state, args) -> {
                    DataBaseTable currentTable = ((DataBaseState) state).getCurrentTable();
                    if (currentTable == null) {
                        System.out.println("no table");
                    } else {
                        Storeable result = currentTable.remove(args[0]);
                        if (result == null) {
                            System.out.println("not found");
                        } else {
                            System.out.println("removed");
                        }
                    }
                }),
                new DataBaseCommand("rollback", 0, (state, args) -> {
                    DataBaseTable currentTable = ((DataBaseState) state).getCurrentTable();
                    if (currentTable == null) {
                        System.out.println("no table");
                    } else {
                        System.out.println(currentTable.rollback());
                    }
                }),
                new DataBaseCommand("commit", 0, (state, args) -> {
                    try {
                        DataBaseTable currentTable = ((DataBaseState) state).getCurrentTable();
                        if (currentTable == null) {
                            System.out.println("no table");
                        } else {
                            System.out.println(currentTable.commit());
                        }
                    } catch (LoadOrSaveException e) {
                        System.err.println(e.getMessage());
                    }
                }),
                new DataBaseCommand("size", 0, (state, args) -> {
                    DataBaseTable currentTable = ((DataBaseState) state).getCurrentTable();
                    if (currentTable == null) {
                        System.out.println("no table");
                    } else {
                        System.out.println(currentTable.size());
                    }
                }),
                new DataBaseCommand("create", -1, (state, args) -> {
                    try {
                        List<Class<?>> typesList = Utils.getTypesForCreate(args);
                        if (((DataBaseState) state).getTableProvider().createTable(args[0], typesList) == null) {
                            System.out.println(args[0] + " exists");
                        } else {
                            System.out.println("created");
                        }
                    } catch (ParseException | IOException e) {
                        System.err.println("wrong type (" + e.getMessage() + ")");
                    }
                }),
                new DataBaseCommand("show", 1, (state, args) -> {
                    if (!args[0].equals("tables")) {
                        System.err.println("show" + ' ' + args[0] + ": no such command");
                    } else {
                        List<TableInfo> result = ((DataBaseState) state).getTableProvider().showTable();
                        for (TableInfo table : result) {
                            System.out.println(table.name + " " + table.size);
                        }
                    }
                }),
                new DataBaseCommand("use", 1, (state, args) -> {
                    String name = new String(args[0]);
                    DataBaseTable currentTable = ((DataBaseState) state).getCurrentTable();
                    DataBaseProvider dbConnector = ((DataBaseState) state).getTableProvider();
                    if (currentTable == null) {
                        if (dbConnector.getTable(name) != null) {
                            ((DataBaseState) state).setCurrentTable((DataBaseTable) dbConnector.getTable(name));
                            System.out.println("using " + name);
                        } else {
                            System.out.println(name + " not exists");
                        }
                    } else {
                        Utils.tryToChangeUsedCurrentTable((DataBaseState) state, name);
                    }
                }),
                new DataBaseCommand("drop", 1, (state, args) -> {
                    try {
                        DataBaseTable currentTable = ((DataBaseState) state).getCurrentTable();
                        DataBaseProvider connector = ((DataBaseState) state).getTableProvider();
                        DataBaseTable table = (DataBaseTable) connector.getTable(args[0]);
                        if (table != null) {
                            if (table.equals(currentTable)) {
                                ((DataBaseState) state).setCurrentTable(null);
                            }
                        }
                        ((DataBaseState) state).getTableProvider().removeTable(args[0]);
                        System.out.println("dropped");
                    } catch (LoadOrSaveException e) {
                        System.err.println(e.getMessage());
                    } catch (TableNotFoundException e) {
                        System.out.println(args[0] + " not exists");
                    }
                }),
                new DataBaseCommand("exit", 0, (state, args) -> {
                    try {
                        DataBaseTable currentTable = ((DataBaseState) state).getCurrentTable();
                        if (currentTable != null) {
                            int unsavedChanges = currentTable.getNumberOfUncommittedChanges();
                            if (unsavedChanges != 0) {
                                System.out.println("cannot exit: " + unsavedChanges + " unsaved changes");
                                return;
                            }
                        }
                        ((DataBaseState) state).saveCurrentTable();
                        System.exit(0);
                    } catch (LoadOrSaveException e) {
                        System.err.println(e.getMessage());
                        System.exit(1);
                    }
                })};
        return commands;
    }
}

