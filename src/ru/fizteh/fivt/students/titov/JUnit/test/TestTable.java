package ru.fizteh.fivt.students.titov.JUnit.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.storage.strings.TableProviderFactory;
import ru.fizteh.fivt.students.titov.JUnit.multi_file_hash_map.MFileHashMapFactory;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;

public class TestTable {
    String key;
    String value;
    String newValue;
    String providerDirectory;
    String tableName;

    Table testTable;
    TableProvider provider;
    TableProviderFactory factory;

    @Before
    public void setUp() {
        key = "key";
        value = "value";
        newValue = "newvalue";
        providerDirectory = Paths.get("").resolve("provider").toString();
        tableName = "testTable";
        factory = new MFileHashMapFactory();
        provider = factory.create(providerDirectory);
        testTable = provider.createTable(tableName);
    }

    @After
    public void tearDown() {
        provider.removeTable(tableName);
    }

    @Test
    public void testGetName() throws Exception {
        assertEquals(testTable.getName(), tableName);
    }

    @Test
    public void testGet() throws Exception {
        assertNull(testTable.get(key));
        testTable.put(key, value);
        assertEquals(testTable.get(key), value);
        testTable.put(key, newValue);
        assertEquals(testTable.get(key), newValue);
        testTable.remove(key);
        assertNull(testTable.get(key));
        testTable.rollback();

        testTable.put(key, value);
        testTable.commit();
        testTable.put(key, newValue);
        assertEquals(testTable.get(key), newValue);
        testTable.remove(key);
        assertNull(testTable.get(key));
    }

    @Test
    public void testRemove() throws Exception {
        assertNull(testTable.remove(key));
        testTable.put(key, value);
        assertEquals(testTable.remove(key), value);

        testTable.rollback();
        testTable.put(key, value);
        assertEquals(testTable.remove(key), value);
        testTable.put(key, value);
        testTable.commit();
        assertEquals(testTable.remove(key), value);
        assertNull(testTable.remove(key));
    }

    @Test
    public void testPut() throws Exception {
        assertNull(testTable.put(key, value));
        assertEquals(testTable.put(key, value), value);
        assertEquals(testTable.get(key), value);
        assertEquals(testTable.put(key, newValue), value);
        assertEquals(testTable.get(key), newValue);
        testTable.remove(key);
        assertNull(testTable.put(key, value));

        String keyForCommit = "keyCM";
        String valueForCommit = "vakueCM";
        int size = 5;
        for (int i = 0; i < size; ++i) {
            assertNull(testTable.put(keyForCommit + i, valueForCommit + i));
        }
        testTable.commit();
        for (int i = 0; i < size; ++i) {
            assertEquals(testTable.get(keyForCommit + i), valueForCommit + i);
        }

        String freshValue = "addValue";
        testTable.rollback();
        testTable.remove(keyForCommit + 1);
        assertNull(testTable.put(keyForCommit + 1, valueForCommit + 1));
        assertEquals(testTable.put(keyForCommit + 2, freshValue), valueForCommit + 2);
        assertEquals(testTable.put(keyForCommit + 2, freshValue + "*"), freshValue);
    }

    @Test
    public void testSize() throws Exception {
        int size = 5;
        for (int i = 0; i < size; ++i) {
            testTable.put(key + i, value + i);
        }
        assertEquals(testTable.size(), size);
        testTable.remove(key + 0);
        assertEquals(testTable.size(), size - 1);
    }

    @Test
    public void testRollback() throws Exception {
        int size = 5;
        for (int i = 0; i < size; ++i) {
            testTable.put(key + i, value + i);
        }
        testTable.remove(key + 0);
        testTable.remove(key + 2);
        testTable.put(key + 1, newValue);
        assertEquals(testTable.rollback(), size - 2);
        testTable.put(key, value);
        assertEquals(testTable.rollback(), 1);
        testTable.put(key, value);
        testTable.commit();
        assertEquals(testTable.rollback(), 0);
    }

    @Test
    public void testCommit() throws Exception {
        int size = 5;
        for (int i = 0; i < size; ++i) {
            testTable.put(key + i, value + i);
        }
        testTable.remove(key + 0);
        testTable.remove(key + 2);
        testTable.put(key + 1, newValue);
        assertEquals(testTable.commit(), size - 2);
        assertEquals(testTable.rollback(), 0);
        testTable.put(key, newValue);
        testTable.remove(key);
        assertEquals(testTable.commit(), 0);
        testTable.put(key + 1, newValue + 2);
        assertEquals(testTable.commit(), 1);
    }

    @Test
    public void testList() throws Exception {
        int size = 5;
        for (int i = 0; i < size; ++i) {
            testTable.put(key + i, value + i);
        }
        boolean theSame = true;
        List<String> testList = testTable.list();
        for (int i = 0; i < size; ++i) {
            if (!testList.contains(key + i)) {
                theSame = false;
            }
        }
        testTable.remove(key + 4);
        testTable.remove(key + 3);
        testList = testTable.list();
        for (int i = 0; i < size - 2; ++i) {
            if (!testList.contains(key + i)) {
                theSame = false;
            }
        }
        assertTrue(theSame);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetThrowsExceptionWhenNullIsPassed() {
        testTable.get(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullRemove() {
        testTable.remove(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPutValue() {
        testTable.put(null, value);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPutKey() {
        testTable.put(key, null);
    }
}
