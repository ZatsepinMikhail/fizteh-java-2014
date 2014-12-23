package ru.fizteh.fivt.students.titov.JUnit.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.fizteh.fivt.storage.strings.TableProviderFactory;
import ru.fizteh.fivt.students.titov.JUnit.multi_file_hash_map.MFileHashMapFactory;
import ru.fizteh.fivt.students.titov.JUnit.shell.FileUtils;

import java.nio.file.Paths;

import static org.junit.Assert.*;

public class TestTableProviderFactory {
    String dir;
    TableProviderFactory testFactory;

    @Before
    public void setUp() {
        dir = Paths.get("").resolve("factory").toString();
        testFactory = new MFileHashMapFactory();
    }

    @After
    public void tearDown() {
        FileUtils.rmdir(Paths.get(dir));
        FileUtils.mkdir(Paths.get(dir));
    }

    @Test
    public void testCreateDirExists() throws Exception {
        FileUtils.mkdir(Paths.get(dir));
        assertNotNull(testFactory.create(dir));
    }

    @Test
    public void testCreateDirNotExists() throws Exception {
        assertNotNull(testFactory.create(dir));
    }

    @Test (expected = IllegalArgumentException.class)
    public void testCreateNull() throws Exception {
        testFactory.create(null);
    }
}
