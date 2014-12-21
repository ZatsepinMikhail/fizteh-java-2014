package ru.fizteh.fivt.students.sautin1.storeable;

import ru.fizteh.fivt.storage.structured.*;
import ru.fizteh.fivt.students.sautin1.storeable.filemap.GeneralTableProvider;
import ru.fizteh.fivt.students.sautin1.storeable.filemap.TableIOTools;
import ru.fizteh.fivt.students.sautin1.storeable.shell.FileUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sautin1 on 12/10/14.
 */
public class StoreableTableProvider extends GeneralTableProvider<Storeable, StoreableTable> implements TableProvider {
    private final String signatureFileName = "signature.tsv";

    StoreableTableProvider(Path rootDir, boolean autoCommit, TableIOTools tableIOTools) throws IOException {
        super(rootDir, autoCommit, tableIOTools);
    }

    @Override
    public StoreableTable establishTable(String name, Object[] args) {
        return new StoreableTable(name, autoCommit, (List<Class<?>>) args[0], this);
    }

    void createSignatureFile(String tableName, List<Class<?>> columnTypes) {
        Path signatureFilePath = getRootDir().resolve(tableName).resolve(signatureFileName);
        try {
            Files.createFile(signatureFilePath);
            String signatureString = StoreableXMLUtils.buildSignatureString(columnTypes);
            FileUtils.printToFile(signatureString, signatureFilePath);
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public StoreableTable createTable(String name, Object[] args) {
        try {
            StoreableTable table = createTable(name, (List<Class<?>>) args[0]);
            return table;
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    @Override
    public StoreableTable createTable(String name, List<Class<?>> columnTypes) throws IOException {
        StoreableTable newTable = super.createTable(name, new Object[]{columnTypes});
        if (newTable == null) {
            return null;
        }
        if (columnTypes == null || columnTypes.isEmpty()) {
            throw new IllegalArgumentException("No column types provided");
        }

        createSignatureFile(name, columnTypes);
        return newTable;
    }

    @Override
    public Storeable deserialize(Table table, String serializedValue) throws ParseException {
        List<Object> rawObjectList = new ArrayList<>();
        try (StoreableTableXMLReader xmlReader = new StoreableTableXMLReader(serializedValue)) {
            for (int columnIndex = 0; columnIndex < table.getColumnsCount(); ++columnIndex) {
                rawObjectList.add(xmlReader.deserializeColumn(table.getColumnType(columnIndex)));
            }
        } catch (XMLStreamException e) {
            throw new RuntimeException(e.getMessage());
        }
        return createFor(table, rawObjectList);
    }

    @Override
    public String serialize(Table table, Storeable value) throws ColumnFormatException {
        StoreableValidityChecker.checkValueFormat(table, value);
        String serializedValue;
        try {
            StoreableTableXMLWriter xmlWriter = new StoreableTableXMLWriter();
            serializedValue = xmlWriter.serializeStoreable(value);
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException(e.getMessage());
        }
        return serializedValue;
    }

    @Override
    public Storeable deserialize(StoreableTable table, String serialized) throws ParseException {
        return deserialize((Table) table, serialized);
    }

    @Override
    public String serialize(StoreableTable table, Storeable value) throws ColumnFormatException {
        return serialize((Table) table, value);
    }

    @Override
    public Storeable createFor(Table table) {
        List<Class<?>> valueTypes = new ArrayList<>(table.getColumnsCount());
        for (int valueIndex = 0; valueIndex < table.getColumnsCount(); ++valueIndex) {
            valueTypes.add(valueIndex, table.getColumnType(valueIndex));
        }
        return new TableRow(valueTypes);
    }

    @Override
    public Storeable createFor(Table table, List<?> values) throws ColumnFormatException, IndexOutOfBoundsException {
        if (values.size() != table.getColumnsCount()) {
            throw new IndexOutOfBoundsException("Wrong number of columns provided");
        }
        Storeable tableRow = createFor(table);
        for (int valueIndex = 0; valueIndex < table.getColumnsCount(); ++valueIndex) {
            tableRow.setColumnAt(valueIndex, values.get(valueIndex));
        }
        return tableRow;
    }

    @Override
    public List<String> getTableNames() {
        return new ArrayList<String>(tableMap.keySet());
    }

    /**
     * Load table from the file.
     *
     * @param root      - path to the root directory.
     * @param tableName - name of the table to load.
     * @throws java.io.IOException if any IO error occured.
     */
    @Override
    public void loadTable(Path root, String tableName) throws IOException, ParseException {
        String signature;
        try {
            signature = FileUtils.readFromFile(root.resolve(tableName).resolve(signatureFileName));
        } catch (IOException e) {
            throw new IOException("Cannot access signature file for table " + tableName);
        }
        List<Class<?>> valueTypes = StoreableXMLUtils.parseSignatureString(signature);
        StoreableTable table = tableIOTools.readTable(this, root, tableName, new Object[]{valueTypes});
        table.commit();
        tableMap.put(table.getName(), table);
    }

    /**
     * Save table to file.
     * @param root - path to the root directory.
     * @param tableName - name of the table to save.
     */
    @Override
    public void saveTable(Path root, String tableName) throws IOException {
        StoreableTable table = tableMap.get(tableName);
        super.saveTable(root, tableName);
        Path tablePath = root.resolve(tableName);
        if (Files.exists(tablePath)) {
            // directory was not deleted because of save
            createSignatureFile(tableName, table.getColumnTypes());
        }
    }
}
