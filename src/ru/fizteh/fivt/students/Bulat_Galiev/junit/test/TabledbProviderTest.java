package ru.fizteh.fivt.students.Bulat_Galiev.junit.test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ru.fizteh.fivt.storage.strings.Table;
import ru.fizteh.fivt.storage.strings.TableProvider;
import ru.fizteh.fivt.students.Bulat_Galiev.junit.TabledbProvider;

public class TabledbProviderTest {
    private TableProvider provider;
    private Path testDir;
    private Path testDirTwo;

    @Before
    public final void setUp() throws Exception {
        String tmpDirPrefix = "Swing_";
        String tmpDirPrefixTwo = "Swing_Two";
        testDir = Files.createTempDirectory(tmpDirPrefix);
        testDirTwo = Files.createTempDirectory(tmpDirPrefixTwo);
        provider = new TabledbProvider(testDir.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testPassDirectoryWithNotExistParentToCreate()
            throws Exception {
        new TabledbProvider(testDirTwo.toString() + File.separatorChar
                + "IDoNot_exist" + File.separatorChar + "IDoNot_existToo");
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testHomeDirectoryContainsFiles() throws Exception {
        testDirTwo.resolve("fileName").toFile().createNewFile();
        new TabledbProvider(testDirTwo.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testHomeDirectoryIsFile() throws Exception {
        testDirTwo = testDirTwo.resolve("fileName");
        testDirTwo.toFile().createNewFile();
        new TabledbProvider(testDirTwo.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testPassInvalidCharactersDir() throws Exception {
        provider.createTable("/*?*/");
    }

    @Test
    public final void testPassNormalDirectory() throws Exception {
        provider = new TabledbProvider(testDirTwo.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testCreateNullTable() throws Exception {
        provider.createTable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testCreateNamelessTable() throws Exception {
        provider.createTable("");
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testCreateSpacesNameTable() throws Exception {
        provider.createTable("          ");
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testCreateInvalidCharactersTable() throws Exception {
        provider.createTable("/*?*/");
    }

    @Test
    public final void testCreateNormalTable() throws Exception {
        Assert.assertNotNull(provider.createTable("table"));
    }

    @Test
    public final void testCreateExistingTable() throws Exception {
        provider.createTable("table1");
        Assert.assertNull(provider.createTable("table1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testGetNullTable() throws Exception {
        provider.getTable(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testGetNamelessTable() throws Exception {
        provider.getTable("          ");
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testGetInvalidCharactersTable() throws Exception {
        provider.getTable("/*?*/");
    }

    @Test
    public final void testGetNotExistingTable() throws Exception {
        Assert.assertNull(provider.getTable("notExistingTable"));
    }

    @Test
    public final void testGetExistingTable() throws Exception {
        Table table1 = provider.createTable("firstTable");
        Assert.assertEquals(table1, provider.getTable("firstTable"));
        Table table2 = provider.createTable("secondTable");
        Assert.assertEquals(table1, provider.getTable("firstTable"));
        Assert.assertEquals(table2, provider.getTable("secondTable"));
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testRemoveNamelessTable() throws Exception {
        provider.removeTable("          ");
    }

    @Test(expected = IllegalArgumentException.class)
    public final void testRemoveInvalidCharactersTable() throws Exception {
        provider.removeTable("/*?*/");
    }

    @Test(expected = IllegalStateException.class)
    public final void testRemoveNotExistingTable() throws Exception {
        provider.removeTable("notExistingTable");
    }

    @Test
    public final void testRemoveExistingTable() throws Exception {
        provider.createTable("table");
        provider.removeTable("table");
        Assert.assertNotNull(provider.createTable("table"));
    }

    @Test
    public final void testChangeCurTable() throws Exception {
        Table singletable = provider.createTable("table");
        ((TabledbProvider) provider).changeCurTable("table");
        Assert.assertEquals(((TabledbProvider) provider).getDataBase(),
                singletable);
    }

    @Test
    public final void testGetNullDataBase() throws Exception {
        Assert.assertNull(((TabledbProvider) provider).getDataBase());
    }

    @Test
    public final void testGetNormalDataBase() throws Exception {
        provider.createTable("table");
        ((TabledbProvider) provider).changeCurTable("table");
        Assert.assertNotNull(((TabledbProvider) provider).getDataBase());
    }

    @After
    public final void tearDown() throws Exception {
        Cleaner.clean(testDir.toFile());
    }

}
