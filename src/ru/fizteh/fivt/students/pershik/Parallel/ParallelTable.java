package ru.fizteh.fivt.students.pershik.Parallel;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Created by pershik on 11/9/14.
 */

public class ParallelTable implements Table {

    private final int mod = 16;

    private final String dbDirPath;
    private final List<Class<?>> signature;
    private final String name;
    private final ParallelTableProvider provider;
    private Map<String, Storeable> db;
    private final ThreadLocal<Map<String, Storeable>> added = new ThreadLocal<Map<String, Storeable>>() {
        @Override
        protected Map<String, Storeable> initialValue() {
            return new HashMap<String, Storeable>();
        }
    };
    private final ThreadLocal<Set<String>> removed = new ThreadLocal<Set<String>>() {
        @Override
        protected Set<String> initialValue() {
            return new HashSet<>();
        }
    };
    private final ThreadLocal<Integer> uncommitted = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };
    private final ThreadLocal<Integer> sz = new ThreadLocal<Integer>() {
        @Override
        protected Integer initialValue() {
            return 0;
        }
    };
    private final  ReentrantReadWriteLock lock;
    private final ReentrantReadWriteLock providerLock;

    public ParallelTable(ParallelTableProvider newProvider,  String dbName,
                          String parentDir, List<Class<?>> newSignature,
                          ReentrantReadWriteLock newProviderLock) {
        name = dbName;
        dbDirPath = parentDir + File.separator + dbName;
        db = new HashMap<>();
        signature = newSignature;
        provider = newProvider;
        providerLock = newProviderLock;
        lock = new ReentrantReadWriteLock();
        readDb();
    }

    @Override
    public Storeable put(String key, Storeable value)
            throws IllegalArgumentException {
        checkExistance();
        checkArg("key", key);
        checkArg("value", value);
        checkSignature(value);
        inc(uncommitted);
        Storeable res;
        if (removed.get().contains(key)) {
            res = null;
            inc(sz);
        } else if (added.get().containsKey(key)) {
            res = added.get().get(key);
        } else {
            lock.readLock().lock();
            try {
                if (db.containsKey(key)) {
                    res = db.get(key);
                } else {
                    res = null;
                    inc(sz);
                }
            } finally {
                lock.readLock().unlock();
            }
        }
        removed.get().remove(key);
        added.get().put(key, value);
        return res;
    }

    @Override
    public Storeable remove(String key) {
        checkExistance();
        checkArg("key", key);
        Storeable res;
        if (removed.get().contains(key)) {
            res = null;
        } else if (added.get().containsKey(key)) {
            res = added.get().get(key);
            inc(uncommitted);
            dec(sz);
        } else {
            lock.readLock().lock();
            try {
                if (db.containsKey(key)) {
                    res = db.get(key);
                    inc(uncommitted);
                    dec(sz);
                } else {
                    res = null;
                }
            } finally {
                lock.readLock().unlock();
            }
        }
        if (res != null) {
            removed.get().add(key);
            added.get().remove(key);
        }
        return res;
    }

    @Override
    public int size() {
        checkExistance();
        lock.readLock().lock();
        try {
            return db.size() + sz.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public List<String> list() {
        lock.readLock().lock();
        Set<String> res;
        try {
            res = new HashSet<>(db.keySet());
        } finally {
            lock.readLock().unlock();
        }
        for (String addedString : added.get().keySet()) {
            res.add(addedString);
        }
        for (String removedString : removed.get()) {
            res.remove(removedString);
        }
        return new ArrayList<>(res);
    }

    @Override
    public int commit() throws IOException {
        checkExistance();
        int res = uncommitted.get();
        uncommitted.set(0);
        lock.writeLock().lock();
        try {
            for (String addedString : added.get().keySet()) {
                db.put(addedString, added.get().get(addedString));
            }
            for (String removedString : removed.get()) {
                db.remove(removedString);
            }
            writeDb();
        } finally {
            lock.writeLock().unlock();
        }
        return res;
    }

    @Override
    public int rollback() {
        checkExistance();
        int res = uncommitted.get();
        uncommitted.set(0);
        sz.set(0);
        added.get().clear();
        removed.get().clear();
        return res;
    }

    @Override
    public int getColumnsCount() {
        checkExistance();
        return signature.size();
    }

    @Override
    public Class<?> getColumnType(int columnIndex)
            throws IndexOutOfBoundsException {
        checkExistance();
        if (columnIndex >= getColumnsCount()) {
            throw new IndexOutOfBoundsException("ColumnIndex is out of bounds");
        }
        return signature.get(columnIndex);
    }

    @Override
    public String getName() {
        checkExistance();
        return name;
    }

    @Override
    public Storeable get(String key)
            throws IllegalArgumentException {
        checkExistance();
        checkArg("key", key);
        Storeable res;
        if (removed.get().contains(key)) {
            res = null;
        } else if (added.get().containsKey(key)) {
            res = added.get().get(key);
        } else {
            lock.readLock().lock();
            try {
                if (db.containsKey(key)) {
                    res = db.get(key);
                } else {
                    res = null;
                }
            } finally {
                lock.readLock().unlock();
            }
        }
        return res;
    }

    private void writeDb() throws IOException {
        removeFromDisk(false);
        Map<String, Storeable>[][] dbParts = new HashMap[mod][mod];
        for (int i = 0; i < mod; i++) {
            for (int j = 0; j < mod; j++) {
                dbParts[i][j] = new HashMap<>();
            }
        }
        for (String key : db.keySet()) {
            int hashCode = key.hashCode();
            int dirNumber = hashCode % mod;
            if (dirNumber < 0) {
                dirNumber += 16;
            }
            int fileNumber = hashCode / mod % mod;
            if (fileNumber < 0) {
                fileNumber += 16;
            }
            dbParts[dirNumber][fileNumber].put(key, db.get(key));
        }
        for (int i = 0; i < mod; i++) {
            String dirPath = dbDirPath + File.separator
                    + Integer.toString(i) + ".dir";
            File dir = new File(dirPath);
            for (int j = 0; j < mod; j++) {
                String path = dirPath + File.separator
                        + Integer.toString(j) + ".dat";
                File file = new File(path);
                writeFile(dbParts[i][j], dir, file);
            }
        }
    }

    private void writeFile(
            Map<String, Storeable> dbPart, File dir, File file)
            throws IOException {
        if (dbPart.isEmpty()) {
            return;
        }
        if (!dir.mkdir()) {
            throw new IOException("Can't write db");
        }
        try (DataOutputStream stream = new DataOutputStream(
                new FileOutputStream(file))) {
            for (String key : dbPart.keySet()) {
                writeToken(stream, key);
                String strValue = provider.serialize(this, dbPart.get(key));
                writeToken(stream, strValue);
            }
        }
    }

    private void writeToken(DataOutputStream stream, String str)
            throws IOException {
        byte[] strBytes = str.getBytes("UTF-8");
        stream.writeInt(strBytes.length);
        stream.write(strBytes);
    }

    private void readDb() {
        try {
            db = new HashMap<>();
            File dbDir = new File(dbDirPath);
            String[] subdirectories = dbDir.list();
            for (String subdirectory : subdirectories) {
                String dirPath = dbDirPath + File.separator + subdirectory;
                File dir = new File(dirPath);
                if ("signature.tsv".equals(dir.getName())) {
                    continue;
                }
                if (!dir.isDirectory() || !isCorrectName(subdirectory, ".dir")
                        || !isCorrectSubdirectory(dir)) {
                    throw new RuntimeException("Incorrect database directory");
                }
                String[] files = dir.list();
                for (String file : files) {
                    String filePath = dirPath + File.separator + file;
                    readFile(new File(filePath));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Can't read db from file");
        }
    }

    private void readFile(File file) throws IOException {
        try {
            try (DataInputStream stream = new DataInputStream(
                    new FileInputStream(file))) {
                String key;
                String valueStr;
                while ((key = readToken(stream)) != null
                        && (valueStr = readToken(stream)) != null) {
                    Storeable value = provider.deserialize(this, valueStr);
                    checkSignature(value);
                    db.put(key, value);
                }
            }
        } catch (ParseException e) {
            throw new IOException("Invalid db directory");
        } catch (ColumnFormatException e) {
            throw new IOException("Invalid db format");
        }
    }

    private String readToken(DataInputStream stream) throws IOException {
        if (stream.available() == 0) {
            return null;
        }
        int size = stream.readInt();
        byte[] buf = new byte[size];
        stream.readFully(buf);
        return new String(buf, "UTF-8");
    }

    private boolean isCorrectSubdirectory(File dir) {
        String[] files = dir.list();
        for (String file : files) {
            File curFile = new File(file);
            if (curFile.isDirectory() || !isCorrectName(file, ".dat")) {
                return false;
            }
        }
        return true;
    }

    private boolean isCorrectName(String name, String suf) {
        try {
            if (name.length() < 4) {
                return false;
            }
            if (!name.endsWith(suf)) {
                return false;
            }
            name = name.replace(suf, "");
            int num = Integer.parseInt(name);
            return (0 <= num && num <= 15);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void removeFromDisk(boolean providerCall) throws IOException {
        if (providerCall) {
            lock.writeLock().lock();
        }
        try {
            String pathTable = dbDirPath;
            for (int i = 0; i < mod; i++) {
                String pathDir = pathTable + File.separator
                        + Integer.toString(i) + ".dir";
                for (int j = 0; j < mod; j++) {
                    String pathFile = pathDir + File.separator
                            + Integer.toString(j) + ".dat";
                    File curFile = new File(pathFile);
                    if (curFile.exists() && !curFile.delete()) {
                        throw new IOException("Can't remove from disk");
                    }
                }
                File curDir = new File(pathDir);
                if (curDir.exists() && !curDir.delete()) {
                    throw new IOException("Can't remove from disk");
                }
            }
        } finally {
            if (providerCall) {
                lock.writeLock().unlock();
            }
        }
    }

    private void checkSignature(Storeable storeable) {
        try {
            for (int i = 0; i < signature.size(); i++) {
                if (storeable.getColumnAt(i) == null) {
                    continue;
                }
                if (!storeable.getColumnAt(i).getClass().equals(signature.get(i))) {
                    throw new ColumnFormatException("Invalid column format");
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new ColumnFormatException("Invalid column number");
        }
    }

    private void checkExistance() throws IllegalStateException {
        providerLock.readLock().lock();
        try {
            if (!provider.contains(name)) {
                throw new IllegalStateException("Table doesn't exist anymore");
            }
        } finally {
            providerLock.readLock().unlock();
        }
    }

    private void checkArg(String name, Object value)
            throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(name + " shouldn't be null");
        }
    }

    @Override
    public int getNumberOfUncommittedChanges() {
        return uncommitted.get();
    }

    private void inc(ThreadLocal<Integer> number) {
        number.set(number.get() + 1);
    }

    private void dec(ThreadLocal<Integer> number) {
        number.set(number.get() - 1);
    }
}
