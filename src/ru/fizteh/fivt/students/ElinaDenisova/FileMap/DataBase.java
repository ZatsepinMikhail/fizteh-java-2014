package ru.fizteh.fivt.students.ElinaDenisova.FileMap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DataBase {
    public DataBase(String dbName) throws DataBaseException {
        try {
            dBasePath = Paths.get(dbName);
            dBase = new HashMap<String, String>();
            File ctFile = new File(dbName);
            if (ctFile.isDirectory()) {
                throw new DataBaseException("It is a directory");
            }
            if (ctFile.exists()) {
                throw new DataBaseException("File already exist");
            } else {
                try {
                    Files.createFile(ctFile.toPath());
                } catch (IOException e) {
                    throw new DataBaseException("Error IOstream", e);
                }
            }
        } catch (DataBaseException ex) {
            if (ex.toString().equals(
                    "File already exist")) {
                try {
                    RandomAccessFile dbFile = new RandomAccessFile(dBasePath.toString(), "r");
                    if (dbFile.length() > 0) {
                        while (dbFile.getFilePointer() < dbFile.length()) {
                            String key = readFromDataBase(dbFile);
                            String value = readFromDataBase(dbFile);
                            dBase.put(key, value);
                        }
                    }
                    dbFile.close();
                } catch (FileNotFoundException e) {
                    throw new DataBaseException(
                            "Not found file", e);
                } catch (IOException e) {
                    throw new DataBaseException(
                            "Error IOstream", e);
                }
            } 
            if (ex.toString().equals("It is a directory")) {
                System.err.println("It is a directory");
                System.exit(1);
            }
        }
    }

    private  String readFromDataBase(RandomAccessFile dbFile)
                    throws DataBaseException {

        try {
            int wordLength = dbFile.readInt();
            byte[] word = new byte[wordLength];
            dbFile.read(word, 0, wordLength);
            return new String(word, "UTF-8");
        } catch (IOException e) {
            throw new DataBaseException("Can't read from database", e);
        }
    }


    private void writeToDataBase(RandomAccessFile dbFile,
            String word) throws DataBaseException {
        try {
            dbFile.writeInt(word.getBytes("UTF-8").length);
            dbFile.write(word.getBytes("UTF-8"));
        } catch (IOException ex) {
            throw new DataBaseException("Error IOstream", ex);
        }

    }

    public void listCommand() {
        System.out.print(String.join(", ", dBase.keySet()));
        System.out.println("");

    }
    public void writeInFile() throws DataBaseException {
        try {
            Set<Entry<String, String>> baseSet = dBase.entrySet();
            File ctFile = new File(dBasePath.toString());
            try {
                Files.deleteIfExists(ctFile.toPath());
                Files.createFile(ctFile.toPath());
            } catch (IOException e) {
                throw new DataBaseException(e.getMessage(), e);
            }
            RandomAccessFile dbFile = new
                    RandomAccessFile(dBasePath.toString(), "rw");
            for (Entry<String, String> current : baseSet) {
                writeToDataBase(dbFile, current.getKey());
                writeToDataBase(dbFile, current.getValue());
            }
        } catch (FileNotFoundException ex) {
            throw new DataBaseException("Not found file", ex);
        }
    }
    public void put(String key,  String value) {
        if (dBase.containsKey(key)) {
            System.out.println("overwrite");
            System.out.println(dBase.get(key));
            dBase.remove(key);
            dBase.put(key, value);
        } else {
            System.out.println("new");
            dBase.put(key, value);
        }
    }

    public void get(String key) {
        if (dBase.containsKey(key)) {
            System.out.println("found");
            System.out.println(dBase.get(key));
        } else {
            System.out.println("not found");
        }
    }
    public void remove(String key) {
        if (dBase.containsKey(key)) {
            System.out.println("removed");
            dBase.remove(key);
        } else {
            System.out.println("not found");
        }
    }
    private Map<String, String> dBase;
    private Path dBasePath;
}

