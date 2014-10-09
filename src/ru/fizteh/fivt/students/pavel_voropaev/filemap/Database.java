package ru.fizteh.fivt.students.pavel_voropaev.filemap;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Database {

    private String dbFilePath;
    private HashMap<String, String> database;

    public Database(String dbPath) throws Exception {
        dbFilePath = dbPath;
        database = new HashMap<String, String>();
        if (Paths.get(dbFilePath).normalize().toFile().exists()) {
            load();
        } else {
            File file = new File(dbFilePath);
            file.createNewFile();
        }
    }

    private void load() throws Exception {
        DataInputStream stream = new DataInputStream(new FileInputStream(dbFilePath));

        boolean finished = false;
            while (!finished) {
                try {
                    int length = stream.readInt();
                    byte[] word = new byte[length];
                    stream.readFully(word);
                    String key = new String(word, "UTF-8");
                    
                    length = stream.readInt();
                    word = new byte[length];
                    stream.readFully(word);
                    String value = new String(word, "UTF-8");
                    database.put(key, value);
                } catch (IOException e) {
                    finished = true;
                }
            }
            
            stream.close();
        }
    

    public void write() throws Exception {
        FileOutputStream output = new FileOutputStream(dbFilePath);
        Set<String> keyList = database.keySet();
        ByteBuffer buffer = ByteBuffer.allocate(4);
        Iterator<String> it = keyList.iterator();
        try {
            while (it.hasNext()) {
                String key = it.next();
                byte[] keyByte = key.getBytes("UTF-8");
                byte[] valueByte = database.get(key).getBytes("UTF-8");

                output.write(buffer.putInt(0, keyByte.length).array());
                output.write(keyByte);

                output.write(buffer.putInt(0, valueByte.length).array());
                output.write(valueByte);
            }
        } catch (Exception e) {
            output.close();
            ThrowExc.cannotWrite(dbFilePath, e.getMessage());
        }
        output.close();
    }

    public String put(String key, String value) {
        return database.put(key, value);
    }

    public String get(String key) {
        return database.get(key);
    }

    public String remove(String key) {
        return database.remove(key);
    }

    public String[] list() {
        Set<String> keysList = database.keySet();
        Iterator<String> it = keysList.iterator();
        String[] retVal = new String[database.size()];

        for (int i = 0; it.hasNext(); ++i) {
            retVal[i] = it.next();
        }

        return retVal;
    }

}
