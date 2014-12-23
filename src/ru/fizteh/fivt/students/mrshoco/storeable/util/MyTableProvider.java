package storeable.util;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import storeable.structured.ColumnFormatException;
import storeable.structured.Storeable;
import storeable.structured.Table;
import storeable.structured.TableProvider;

public class MyTableProvider implements TableProvider{
    File root;
    MyTable currentTable;

    public MyTableProvider(File file) {
        root = file;
        if (!file.isDirectory()) {
            throw new IllegalArgumentException("Given property isn't a directory");
        }
    }

    @Override
    public Table getTable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Bad name");
        }

        File tableFile = new File(root, name);
        if (!tableFile.isDirectory()) {
            return null;
        } else {
            if (currentTable != null && currentTable.diff() != 0) {
                throw new IllegalArgumentException(currentTable.diff() + " unsaved changes");
            }
        }
        currentTable = new MyTable(tableFile, this);
        return currentTable;
    }

    @Override
    public Table createTable(String name, List<Class<?>> columnTypes) {
        if (name == null) {
            throw new IllegalArgumentException("Bad name");
        }

        File tableFile = new File(root, name);
        if (tableFile.exists()) {
            return null;
        } else {
            tableFile.mkdir();
            FolderData.saveSignature(tableFile, columnTypes);
        }
        currentTable = new MyTable(tableFile, this);
        return currentTable;
    }
    
    @Override
    public void removeTable(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Bad name");
        }

        File tableFile = new File(root, name);
        if (!tableFile.isDirectory()) {
            throw new IllegalStateException("Table doesn't exist");
        } else {
            try {
                File signature = new File(tableFile, "signature.tsv");
                signature.delete();
                for (int i = 0; i < 16; i++) {
                    File folder = new File(tableFile, i + ".dir");
                    if (folder.exists()) {
                        for (int j = 0; j < 16; j++) {
                            File file = new File(folder, j + ".dat");
                            if (file.exists()) {
                                file.delete();
                            }
                        }
                        folder.delete();
                    }
                }
                tableFile.delete();
            } catch (Exception e) {
                throw new IllegalArgumentException("Some of directories is not empty");
            }
        }
    }
    
    @Override
    public Storeable deserialize(Table table, String value) throws ParseException {
        int size = table.getColumnsCount();
        List<Class<?>> types = new ArrayList<Class<?>>();
        for (int i = 0; i < size; i++) {
            types.add(table.getColumnType(i));
        }
        try {
            List<Object> values = XmlSerializer.deserializeString(value, types);
            return createFor(table, values);
        } catch (ParserConfigurationException e) {
            throw new ParseException("Error in parser configuration", 0);
        }
    }

    @Override
    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        int sz = table.getColumnsCount();
        if (sz != ((MyStoreable) value).getSize()) {
            throw new ColumnFormatException("Column format is bad");
        }

        for (int i = 0; i < table.getColumnsCount(); i++) {
            Class<?> own = table.getColumnType(i);
            Class<?> passed = ((MyStoreable) value).getTypeAt(i);

            if (!own.equals(passed)) {
                throw new ColumnFormatException("Column format is bad");
            }
        }

        List<Object> values = new ArrayList<Object>();
        for (int i = 0; i < sz; i++) {
            values.add(value.getColumnAt(i));
        }
        return XmlSerializer.serializeObjectList(values);
    }

    @Override
    public Storeable createFor(Table table) {
        int sz = table.getColumnsCount();
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for (int i = 0; i < sz; i++) {
            classes.add(table.getColumnType(i));
        }

        return new MyStoreable(classes);
    }

    @Override
    public Storeable createFor(Table table, List<?> values) 
                                throws ColumnFormatException, IndexOutOfBoundsException {
        Storeable storeable = createFor(table);

        for (int i = 0; i < values.size(); i++) {
            storeable.setColumnAt(i, values.get(i));
        }

        return storeable;
    }

    @Override
    public List<String> getTableNames() {
        List<String> tables = new ArrayList<String>();

        for (File table : root.listFiles()) {
            if (table.getName().equals(root.getName())) {
                tables.add(table.getName());
            } else if (table.isDirectory()) {
                try {
                    tables.add(table.getName());
                } catch (Exception e) {
                    tables.add("Problem with one of Data Bases");
                }
            }
        }
        return tables;
    }

}
