package ru.fizteh.fivt.students.Bulat_Galiev.proxy;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.storage.structured.TableProvider;

public final class Tabledb implements Table, AutoCloseable {
    private static final int INT_NUMBER = 4;
    static final String SIGNATURE_NAME = "signature.tsv";
    /*
     * Directory name depends on NUMBER_OF_DIRS constant from
     * DatabaseSerializer.java.
     */
    static final String PROPER_DIRECTORY_NAME = "([0-9]|1[0-5])\\.dir";
    /*
     * File name depends on NUMBER_OF_FILES constant from
     * DatabaseSerializer.java.
     */
    static final String PROPER_FILE_NAME = "([0-9]|1[0-5])\\.dat";
    private String tableName;
    private Path tablePath;
    private Map<CellForKey, DatabaseSerializer> databaseFiles;
    private TableProvider localProvider;
    private List<Class<?>> columnTypeList = new ArrayList<>();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
    private boolean closed = false;

    public Tabledb(final Path path, final String name,
            final TableProvider provider, final List<Class<?>> columnListType) {
        databaseFiles = new HashMap<CellForKey, DatabaseSerializer>();
        tablePath = path;
        tableName = name;
        localProvider = provider;
        if (columnListType != null) {
            columnTypeList = columnListType;
        }
        try {
            readSignature();
            readTableDir();
        } catch (IOException e) {
            throw new RuntimeException("Error reading table " + tableName
                    + ": " + e.getMessage());
        }
    }

    public void isClosed() {
        if (closed) {
            throw new IllegalStateException("table " + tableName + " is closed");
        }
    }

    @Override
    public String getName() {
        isClosed();
        return tableName;
    }

    public TableProvider getLocalProvider() {
        isClosed();
        return localProvider;
    }

    @Override
    public int commit() {
        isClosed();
        lock.writeLock().lock();
        try {
            try {
                int diffNumberRecords = writeTableToDir();
                return diffNumberRecords;
            } catch (IOException e) {
                throw new RuntimeException("Error writing table " + tableName,
                        e);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int rollback() {
        isClosed();
        lock.readLock().lock();
        try {
            Iterator<Entry<CellForKey, DatabaseSerializer>> it = databaseFiles
                    .entrySet().iterator();
            int diffNumberRecords = 0;
            while (it.hasNext()) {
                Entry<CellForKey, DatabaseSerializer> databaseFile = it.next();
                diffNumberRecords += databaseFile.getValue().rollback();
                if (databaseFile.getValue().getRecordsNumber() == 0) {
                    it.remove();
                }
            }
            return diffNumberRecords;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Storeable get(final String oldkey) {
        isClosed();
        lock.readLock().lock();
        try {
            if (oldkey != null) {
                String key = oldkey.trim();
                if (!key.isEmpty()) {
                    DatabaseSerializer databaseFile;
                    try {
                        databaseFile = databaseFiles.get(new CellForKey(key));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e.getMessage(), e);
                    }
                    if (databaseFile == null) {
                        return null;
                    } else {
                        return databaseFile.get(key);
                    }
                } else {
                    throw new IllegalArgumentException("Empty key");
                }
            } else {
                throw new IllegalArgumentException("Null key");
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Storeable put(final String oldkey, final Storeable value) {
        isClosed();
        if ((oldkey != null) && (value != null)) {
            try {
                String key = oldkey.trim();
                if (!key.isEmpty()) {
                    DatabaseSerializer databaseFile = databaseFiles
                            .get(new CellForKey(key));
                    if (databaseFile == null) {
                        databaseFile = DatabaseSerializer.create(key,
                                tablePath, this);
                        databaseFiles.put(new CellForKey(key), databaseFile);
                    }
                    if (!checkValue(this, value)) {
                        throw new ColumnFormatException("invalid value");
                    }
                    return databaseFile.put(key, value);
                } else {
                    throw new IllegalArgumentException("Empty key or name");
                }
            } catch (IOException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        } else {
            throw new IllegalArgumentException("Null key or name");
        }

    }

    private void readSignature() throws IOException {
        columnTypeList.clear();
        Path tableSignaturePath = tablePath.resolve(SIGNATURE_NAME);
        try (RandomAccessFile readSig = new RandomAccessFile(
                tableSignaturePath.toString(), "r")) {
            if (readSig.length() > 0) {
                while (readSig.getFilePointer() < readSig.length()) {
                    String types = readSig.readLine();
                    int i = 0;
                    String typesString = "";
                    String prefix = "class java.lang.";
                    if (types.startsWith(prefix)) {
                        typesString = types.substring(prefix.length());
                    } else {
                        throw new IllegalArgumentException("table named "
                                + tableName + " is incorrect: "
                                + SIGNATURE_NAME + " format is wrong");
                    }
                    String[] typesNames = typesString.split(" class java.lang.");
                    for (String type : typesNames) {
                        if (!type.equals("String")) {
                            type = Character.toLowerCase(type.charAt(0))
                                    + type.substring(1);
                        }
                        if (type.equals("integer")) {
                            type = "int";
                        }
                        if (Types.stringToClass(type) == null) {
                            throw new IllegalArgumentException("Class " + type
                                    + " is not supported");
                        }

                        if (Types.stringToClass(type) != null) {
                            columnTypeList.add(i++, Types.stringToClass(type));
                        } else {
                            throw new IllegalArgumentException("table named "
                                    + tableName + " is incorrect: "
                                    + SIGNATURE_NAME
                                    + " contains incorrect type " + type + "");

                        }
                    }
                }
            } else {
                throw new IllegalArgumentException("table named " + tableName
                        + " is incorrect: " + SIGNATURE_NAME + "is empty");
            }
        }

    }

    @Override
    public Storeable remove(final String oldkey) {
        isClosed();
        if (oldkey != null) {
            String key = oldkey.trim();
            if (!key.isEmpty()) {
                DatabaseSerializer databaseFile;
                try {
                    databaseFile = databaseFiles.get(new CellForKey(key));
                    if (databaseFile == null) {
                        return null;
                    } else {
                        return databaseFile.remove(key);
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            } else {
                throw new IllegalArgumentException("Empty key");
            }
        } else {
            throw new IllegalArgumentException("Null key");
        }
    }

    @Override
    public int size() {
        isClosed();
        int numrecords = 0;
        for (Entry<CellForKey, DatabaseSerializer> databaseFile : databaseFiles
                .entrySet()) {
            numrecords += databaseFile.getValue().getRecordsNumber();
        }

        return numrecords;

    }

    @Override
    public List<String> list() {
        isClosed();
        lock.readLock().lock();
        try {
            List<String> list = new LinkedList<String>();
            for (Entry<CellForKey, DatabaseSerializer> pair : databaseFiles
                    .entrySet()) {
                list.addAll(pair.getValue().list());
            }
            return list;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void deleteTable() throws IOException {
        isClosed();
        String[] dirList = tablePath.toFile().list();
        for (String curDir : dirList) {
            String[] fileList = tablePath.resolve(curDir).toFile().list();
            if (fileList != null) {
                for (String file : fileList) {
                    Paths.get(tablePath.toString(), curDir, file).toFile()
                            .delete();
                }
            }
            tablePath.resolve(curDir).toFile().delete();
        }
        tablePath.toFile().delete();
        databaseFiles.clear();
        close();
    }

    private void readTableDir() throws IOException {
        String[] dirList = tablePath.toFile().list();
        for (String dir : dirList) {
            if (!dir.equals(SIGNATURE_NAME)) {
                Path curDir = tablePath.resolve(dir);
                if (!curDir.toFile().isDirectory()
                        || !dir.matches(PROPER_DIRECTORY_NAME)) {
                    throw new IOException(
                            "it contains file or inappropriate directory "
                                    + dir.toString());
                }
                String[] fileList = curDir.toFile().list();
                if (fileList.length == 0) {
                    throw new IOException("it contains empty directory "
                            + dir.toString());
                }
                for (String file : fileList) {
                    Path filePath = curDir.resolve(file);
                    if (!filePath.toFile().isFile()
                            || !file.matches(PROPER_FILE_NAME)) {
                        throw new IOException(dir.toString()
                                + "contains directory or inappropriate file"
                                + file.toString());
                    }
                    int ndirectory = Integer.parseInt(dir.substring(0,
                            dir.length() - INT_NUMBER));
                    int nfile = Integer.parseInt(file.substring(0,
                            file.length() - INT_NUMBER));
                    DatabaseSerializer databaseFile;
                    try {
                        databaseFile = new DatabaseSerializer(tablePath,
                                ndirectory, nfile, this);
                        databaseFiles.put(new CellForKey(ndirectory, nfile),
                                databaseFile);
                    } catch (Exception e) {
                        throw new RuntimeException("Error reading table "
                                + tableName + ": " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    private int writeTableToDir() throws IOException {
        Iterator<Entry<CellForKey, DatabaseSerializer>> it = databaseFiles
                .entrySet().iterator();
        int diffNumberRecords = 0;
        while (it.hasNext()) {
            Entry<CellForKey, DatabaseSerializer> databaseFile = it.next();
            diffNumberRecords += databaseFile.getValue().commit();
            if (databaseFile.getValue().getRecordsNumber() == 0) {
                it.remove();
            }
        }
        return diffNumberRecords;
    }

    @Override
    public int getColumnsCount() {
        isClosed();
        return columnTypeList.size();
    }

    @Override
    public Class<?> getColumnType(final int columnIndex) {
        isClosed();
        return columnTypeList.get(columnIndex);
    }

    public boolean checkValue(final Table table, final Storeable value) {
        isClosed();
        for (int i = 0; i < table.getColumnsCount(); ++i) {
            try {
                value.getColumnAt(i);
            } catch (IndexOutOfBoundsException e) {
                return false;
            }
        }

        for (int i = 0; i < table.getColumnsCount(); ++i) {
            if (!((value.getColumnAt(i) == null) || ((Types.classToString(value
                    .getColumnAt(i).getClass()) != null) && (table
                    .getColumnType(i) == value.getColumnAt(i).getClass())))) {
                return false;
            }
        }

        try {
            value.getColumnAt(table.getColumnsCount());
        } catch (IndexOutOfBoundsException e) {
            return true;
        }

        return false;
    }

    @Override
    public int getNumberOfUncommittedChanges() {
        isClosed();
        int diffNumRecords = 0;
        for (Entry<CellForKey, DatabaseSerializer> databaseFile : databaseFiles
                .entrySet()) {
            diffNumRecords += databaseFile.getValue().getChangedRecordsNumber();
        }
        return diffNumRecords;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append('[');
        sb.append(Paths.get(tablePath.toString(), tableName).toString());
        sb.append(']');
        return sb.toString();
    }

    @Override
    public void close() {
        rollback();
        closed = true;
    }
}
