package ru.fizteh.fivt.students.Bulat_Galiev.junit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;

public final class TabledbProvider implements TableProvider {
    private Map<String, Table> tableMap;
    private Path tablesDirPath;
    private Table currentTable;

    public TabledbProvider(final String dir) {
        try {
            tablesDirPath = Paths.get(dir);
            if (!Files.exists(tablesDirPath)) {
                tablesDirPath.toFile().mkdir();
            }
            if (!tablesDirPath.toFile().isDirectory()) {
                throw new IllegalArgumentException("Incorrect path.");
            }
            currentTable = null;
            tableMap = new HashMap<>();
            String[] tablesDirlist = tablesDirPath.toFile().list();
            for (String curTableDir : tablesDirlist) {
                Path curTableDirPath = tablesDirPath.resolve(curTableDir);
                if (curTableDirPath.toFile().isDirectory()) {
                    Table curTable = new Tabledb(curTableDirPath, curTableDir);
                    tableMap.put(curTableDir, curTable);
                } else {
                    throw new IllegalArgumentException(
                            "Directory contains non-directory files.");
                }
            }
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("directory name " + dir
                    + " is incorrect. " + e.getMessage());
        }
    }

    public Set<String> getKeySet() {
        return tableMap.keySet();
    }

    public void changeCurTable(final String name) {
        try {
            if (name != null && !name.equals("")) {
                tablesDirPath.resolve(name);
                Table newTable = tableMap.get(name);
                if (newTable != null) {
                    currentTable = newTable;
                } else {
                    throw new IllegalStateException(name + " does not exist");
                }
            } else {
                throw new IllegalArgumentException("Null name.");
            }
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("table name " + name
                    + " is incorrect. " + e.getMessage());
        }
    }

    @Override
    public Table createTable(final String name) {
        try {
            if (name != null && !name.equals("")) {
                Path newTablePath = tablesDirPath.resolve(name);
                if (tableMap.get(name) != null) {
                    return null;
                }
                if (!newTablePath.toFile().mkdir()) {
                    throw new IllegalArgumentException(
                            "Error creating directory " + name);
                }
                Table newTable = new Tabledb(newTablePath, name);
                tableMap.put(name, newTable);
                return newTable;
            } else {
                throw new IllegalArgumentException("Null name.");
            }
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("table name " + name
                    + " is incorrect. " + e.getMessage());
        }
    }

    @Override
    public void removeTable(final String name) {
        try {
            if (name != null && !name.equals("")) {
                tablesDirPath.resolve(name);
                Table removedTable = tableMap.remove(name);
                if (removedTable == null) {
                    throw new IllegalStateException(name + " does not exist");
                } else {
                    if (currentTable == removedTable) {
                        currentTable = null;
                    }
                    ((Tabledb) removedTable).deleteTable();
                    System.out.println("dropped");
                }
            } else {
                throw new IllegalArgumentException("Null name.");
            }
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("table name " + name
                    + " is incorrect. " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Table getDataBase() {
        return currentTable;
    }

    public Path getTablesDirPath() {
        return tablesDirPath;
    }

    @Override
    public Table getTable(final String name) {
        try {
            if (name != null && !name.equals("")) {
                tablesDirPath.resolve(name);
                return tableMap.get(name);
            } else {
                throw new IllegalArgumentException("Null name.");
            }
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("table name " + name
                    + " is incorrect. " + e.getMessage());
        }
    }

}
