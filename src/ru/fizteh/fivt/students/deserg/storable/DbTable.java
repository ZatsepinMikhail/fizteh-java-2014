package ru.fizteh.fivt.students.deserg.storable;

import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
/**
 * Created by deserg on 04.10.14.
 */
public class DbTable implements Table {

    private List<Class<?>> signature = new ArrayList<>();
    private Map<String, Storeable> committedData = new HashMap<>();
    private Map<String, Storeable> addedData = new HashMap<>();
    private Map<String, Storeable> removedData = new HashMap<>();
    private Map<String, Storeable> changedData = new HashMap<>();
    private String tableName;
    private Path tablePath;


    public DbTable(Path path, List<Class<?>> signature) {
        tablePath = path;
        tableName = path.getFileName().toString();
        this.signature = signature;
        try {
            read();
        } catch (MyIOException ex) {
            System.out.println("Table \"" + tableName + "\": error while reading");
            System.exit(1);
        }
    }

    /**
     * Возвращает название базы данных.
     */
    @Override
    public String getName() {
        return tableName;
    }

    /**
     * Получает значение по указанному ключу.
     *
     * @param key Ключ.
     * @return Значение. Если не найдено, возвращает null.
     *
     * @throws IllegalArgumentException Если значение параметра key является null.
     */
    @Override
    public Storeable get(String key) throws IllegalArgumentException {

        if (key == null) {
            throw new IllegalArgumentException("Table \"" + tableName + "\": get: null key");
        }

        if (key.isEmpty()) {
            throw new IllegalArgumentException("Table \"" + tableName + "\": get: empty key");
        }


        Storeable value = addedData.get(key);
        if (value != null) {
            return value;
        }

        value = changedData.get(key);
        if (value != null) {
            return value;
        }

        value = committedData.get(key);
        if (value != null) {
            return value;
        }

        return null;
    }

    /**
     * Устанавливает значение по указанному ключу.
     *
     * @param key Ключ.
     * @param value Значение.
     * @return Значение, которое было записано по этому ключу ранее. Если ранее значения не было записано,
     * возвращает null.
     *
     * @throws IllegalArgumentException Если значение параметров key или value является null.
     */
    @Override
    public Storeable put(String key, Storeable value) {

        if (key == null) {
            throw new IllegalArgumentException("Table \"" + tableName + "\": put: null key");
        }

        if (value == null) {
            throw new IllegalArgumentException("Table \"" + tableName + "\": put: null value");
        }

        if (key.isEmpty()) {
            throw new IllegalArgumentException("Table \"" + tableName + "\": put: empty key");
        }

        if (committedData.containsKey(key)) {

            if (removedData.containsKey(key)) {
                removedData.remove(key);
                if (!committedData.get(key).equals(value)) {
                    changedData.put(key, value);
                }
                return null;
            } else if (changedData.containsKey(key)) {

                if (committedData.get(key).equals(value)) {
                    return changedData.remove(key);
                } else {
                    return changedData.put(key, value);
                }
            } else {

                changedData.put(key, value);
                return committedData.get(key);

            }

        } else {
            return addedData.put(key, value);
        }

    }

    /**
     * Удаляет значение по указанному ключу.
     *
     * @param key Ключ.
     * @return Значение. Если не найдено, возвращает null.
     *
     * @throws IllegalArgumentException Если значение параметра key является null.
     */
    @Override
    public Storeable remove(String key) {

        if (key == null) {
            throw new IllegalArgumentException("Table \"" + tableName + "\": remove: null key");
        }

        if (key.isEmpty()) {
            throw new IllegalArgumentException("Table \"" + tableName + "\": remove: empty key");
        }

        if (addedData.containsKey(key)) {
            return addedData.remove(key);
        }

        if (changedData.containsKey(key)) {
            Storeable value = changedData.get(key);
            changedData.remove(key);
            removedData.put(key, value);
            return value;
        }

        if (removedData.containsKey(key)) {
            return null;
        }

        if (committedData.containsKey(key)) {
            Storeable value = committedData.get(key);
            removedData.put(key, value);
            return value;
        }

        return null;
    }

    /**
     * Возвращает количество ключей в таблице.
     *
     * @return Количество ключей в таблице.
     */
    @Override
    public int size() {
        return committedData.size() - removedData.size() + addedData.size();
    }

    /**
     * Выполняет фиксацию изменений.
     *
     * @return Количество сохранённых ключей.
     */
    @Override
    public int commit() {

        committedData.keySet().removeAll(removedData.keySet());
        committedData.putAll(addedData);
        committedData.putAll(changedData);

        int changedKeys = getNumberOfUncommittedChanges();
        addedData.clear();
        removedData.clear();
        changedData.clear();

        try {
            write();
        } catch (MyIOException ex) {
            throw new MyException("Table \"" + tableName + "\": errors while saving commit");
        }

        return changedKeys;
    }

    /**
     * Выполняет откат изменений с момента последней фиксации.
     *
     * @return Количество отменённых ключей.
     */
    @Override
    public int rollback() {

        int changedKeys = getNumberOfUncommittedChanges();
        addedData.clear();
        removedData.clear();
        changedData.clear();

        return changedKeys;
    }

    /**
     * Выводит список ключей таблицы
     *
     * @return Список ключей.
     */
    @Override
    public List<String> list() {

        List<String> keyList = new LinkedList<>();
        keyList.addAll(committedData.keySet());
        keyList.addAll(addedData.keySet());

        return keyList;
    }


    /**
     * Возвращает количество изменений, ожидающих фиксации.
     *
     * @return Количество изменений, ожидающих фиксации.
     */
    @Override
    public int getNumberOfUncommittedChanges() {
        return addedData.size() + removedData.size() + changedData.size();
    }


    /**
     * Возвращает количество колонок в таблице.
     *
     * @return Количество колонок в таблице.
     */
    @Override
    public int getColumnsCount() {
        return signature.size();
    }


    /**
     * Возвращает тип значений в колонке.
     *
     * @param columnIndex Индекс колонки. Начинается с нуля.
     * @return Класс, представляющий тип значения.
     *
     * @throws IndexOutOfBoundsException - неверный индекс колонки
     */
    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {

        if (columnIndex < 0 || columnIndex >= signature.size()) {
            throw new IndexOutOfBoundsException("DbTable: getColumnType: index \""
                    + columnIndex + "\" is out of bounds");
        }

        return signature.get(columnIndex);

    }



    /**
     * Not-interface methods begin here
     */

    public List<Class<?>> getSignature() {
        return signature;
    }

    private void readKeyValue(Path filePath, int dir, int file) throws MyIOException {

        if (Files.exists(filePath)) {
            try (DataInputStream is = new DataInputStream(Files.newInputStream(filePath))) {

                if (is.available() == 0) {
                    throw new MyIOException("File is empty: " + filePath);
                }

                while (is.available() > 0) {

                    int keyLen = is.readInt();
                    if (is.available() < keyLen) {
                        throw new MyIOException("Wrong key size");
                    }

                    byte[] key = new byte[keyLen];
                    is.read(key, 0, keyLen);


                    int valLen = is.readInt();
                    if (is.available() < valLen) {
                        throw new MyIOException("Wrong value size");
                    }
                    byte[] value = new byte[valLen];
                    is.read(value, 0, valLen);

                    String keyStr = new String(key, "UTF-8");
                    Storeable valueStr = new TableRow(signature);
                    try {
                         valueStr = Serializer.deserialize(this, new String(value, "UTF-8"));
                    } catch (ParseException ex) {
                        System.out.println("Parse exception while reading");
                        System.exit(1);
                    }

                    int hashValue = keyStr.hashCode();
                    if (hashValue % 16 != dir || hashValue / 16 % 16 != file) {
                        throw new MyIOException("Wrong key file");
                    }

                    committedData.put(keyStr, valueStr);
                }

            } catch (IOException e) {
                throw new MyIOException("Reading from disk failed");
            }


        }
    }

    public void read() throws MyIOException {

        committedData.clear();
        addedData.clear();
        changedData.clear();
        removedData.clear();

        for (int dir = 0; dir < 16; ++dir) {
            for (int file = 0; file < 16; ++file) {
                Path filePath = tablePath.resolve(dir + ".dir").resolve(file + ".dat");
                try {
                    readKeyValue(filePath, dir, file);
                } catch (MyException ex) {
                    System.out.println(ex.getMessage());
                    System.exit(1);
                }
            }
        }

    }

    private void writeKeyValue(Path filePath, String keyStr, String valueStr) throws MyIOException {

        try (DataOutputStream os = new DataOutputStream(Files.newOutputStream(filePath))) {
            byte[] key = keyStr.getBytes("UTF-8");
            byte[] value = valueStr.getBytes("UTF-8");
            os.writeInt(key.length);
            os.write(key);
            os.writeInt(value.length);
            os.write(value);

        } catch (IOException ex) {
            throw new MyIOException("Writing to dist failed");
        }


    }

    public void write() throws MyIOException {

        if (Files.exists(tablePath)) {
            Shell.deleteContent(tablePath);
        } else {
            try {
                Files.createDirectory(tablePath);
            } catch (IOException ex) {
                throw new MyIOException("Error has occurred while creating table directory");
            }
        }

        Shell.writeSignature(signature, tablePath);

        for (HashMap.Entry<String, Storeable> entry : committedData.entrySet()) {

            String key = entry.getKey();
            String value = Serializer.serialize(this, entry.getValue());
            int hashCode = key.hashCode();
            int dir = hashCode % 16;
            int file = hashCode / 16 % 16;

            Path dirPath = tablePath.resolve(dir + ".dir");
            Path filePath = dirPath.resolve(file + ".dat");

            if (!Files.exists(dirPath)) {
                try {
                    Files.createDirectory(dirPath);
                } catch (IOException ex) {
                    throw new MyIOException(dirPath + ": unable to create");
                }
            }
            if (!Files.exists(filePath)) {
                try {
                    Files.createFile(filePath);
                } catch (IOException ex) {
                    throw new MyIOException(filePath + ": unable to create");
                }
            }

            writeKeyValue(filePath, key, value);

        }

    }


}
