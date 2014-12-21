package ru.fizteh.fivt.students.torunova.parallel.database;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.TableProvider;
import ru.fizteh.fivt.students.torunova.parallel.database.exceptions.IncorrectFileException;
import ru.fizteh.fivt.students.torunova.parallel.database.exceptions.TableNotCreatedException;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by nastya on 19.11.14.
 */
public class TableWrapper implements ru.fizteh.fivt.storage.structured.Table {
    private static final String SIGNATURE_FILE = "signature.tsv";
    private TableImpl table;
    private StoreableType headOfTable;
    private TableProvider tableProvider;

    public TableWrapper(String tableName,
                        DatabaseWrapper newTableProvider, Class<?>...newTypes)
            throws IOException,
            TableNotCreatedException,
            IncorrectFileException {
        table = new TableImpl(tableName);
        headOfTable = new StoreableType(newTypes);
        tableProvider = newTableProvider;
    }
    public TableWrapper(TableImpl newTable, DatabaseWrapper newTableProvider, Class<?>... newTypes)
            throws TableNotCreatedException, IOException, IncorrectFileException {
        this(newTable.getName(), newTableProvider, newTypes);
    }
    public TableWrapper(TableImpl newTable, DatabaseWrapper newTableProvider) {
        table = newTable;
        tableProvider = newTableProvider;
        File tableDir = new File(newTableProvider.getDbName(), table.getName());
        File signature = new File(tableDir, SIGNATURE_FILE);
        Scanner scanner;
        try {
            scanner = new Scanner(new FileInputStream(signature));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        List<Class<?>> types = new ArrayList<>();
        while (scanner.hasNext()) {
            types.add(classForName(scanner.next()));
        }
        headOfTable = new StoreableType(types.toArray(new Class[0]));
    }

    @Override
    public int hashCode() {
        if (table != null && headOfTable != null) {
            return (table.hashCode() + headOfTable.hashCode());
        }
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TableWrapper)) {
            return false;
        }
        return table.equals(((TableWrapper) obj).table) && headOfTable.equals(((TableWrapper) obj).headOfTable);
    }

    @Override
    public Storeable put(String key, Storeable value) throws ColumnFormatException {
        if (table.isRemoved()) {
            throw new IllegalStateException("Table already removed.");
        }
        checkValueFormat((StoreableType) value);
        Storeable storeableValue;
        try {
            storeableValue =  tableProvider.deserialize(this,
                    table.put(key, tableProvider.serialize(this, value)));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return storeableValue;
    }

    @Override
    public Storeable remove(String key) {
        if (table.isRemoved()) {
            throw new IllegalStateException("Table already removed.");
        }
        Storeable storeableValue;
        try {
            storeableValue = tableProvider.deserialize(this, table.remove(key));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return  storeableValue;
    }

    @Override
    public int size() {
        if (table.isRemoved()) {
            throw new IllegalStateException("Table already removed.");
        }
        return table.size();
    }

    @Override
    public List<String> list() {
        if (table.isRemoved()) {
            throw new IllegalStateException("Table already removed.");
        }
        return table.list();
    }

    @Override
    public int commit() {
        if (table.isRemoved()) {
            throw new IllegalStateException("Table already removed.");
        }
        return table.commit();
    }

    @Override
    public int rollback() {
        if (table.isRemoved()) {
            throw new IllegalStateException("Table already removed.");
        }
        return table.rollback();
    }

    @Override
    public int getNumberOfUncommittedChanges() {
        if (table.isRemoved()) {
            throw new IllegalStateException("Table already removed.");
        }
        return table.countChangedEntries();
    }

    @Override
    public int getColumnsCount() {
        if (table.isRemoved()) {
            throw new IllegalStateException("Table already removed.");
        }
        return headOfTable.getNumberOfColumns();
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        if (table.isRemoved()) {
            throw new IllegalStateException("Table already removed.");
        }
        return headOfTable.getColumnType(columnIndex);
    }

    @Override
    public String getName() {
        if (table.isRemoved()) {
            throw new IllegalStateException("Table already removed.");
        }
        return table.getName();
    }

    @Override
    public Storeable get(String key) {
        if (table.isRemoved()) {
            throw new IllegalStateException("Table already removed.");
        }
        Storeable storeableValue;
        try {
            storeableValue = tableProvider.deserialize(this, table.get(key));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return storeableValue;
    }

    private void checkValueFormat(StoreableType value) throws ColumnFormatException {
        if (headOfTable.getNumberOfColumns() != value.getNumberOfColumns()) {
            throw new ColumnFormatException();
        }
        int n = headOfTable.getNumberOfColumns();
        for (int i = 0; i < n; i++) {
            if (value.getColumnType(i) != headOfTable.getColumnType(i)) {
                throw new ColumnFormatException("Wrong value format");
            }
        }
    }
    private String normalSimpleName(Class<?> type) {
        String simpleName = type.getSimpleName();
        switch (simpleName) {
            case "Integer" :
                return "int";
            case "Float" :
                return "float";
            case "Double" :
                return "double";
            case "Long" :
                return "long";
            case "Boolean" :
                return "boolean";
            case "Byte" :
                return "byte";
            case "String" :
                return simpleName;
            default: throw new RuntimeException("Unsupported type " + simpleName);
        }
    }
    public Class<?> classForName(String className) {
        switch(className) {
            case "int":
                return Integer.class;
            case "double":
                return Double.class;
            case "long":
                return Long.class;
            case "String":
                return String.class;
            case "byte":
                return Byte.class;
            case "float":
                return Float.class;
            case "boolean":
                return Boolean.class;
            default: throw new RuntimeException("Unsupported type " + className);
        }
    }
}
