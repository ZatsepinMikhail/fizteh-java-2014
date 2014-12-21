package ru.fizteh.fivt.students.alexpodkin.FileMap;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class Writer implements AutoCloseable {

    private String fileMapPath;
    private DataOutputStream dataOutputStream;

    public Writer(String path) {
        fileMapPath = path;
    }

    private void writeWord(DataOutputStream dataOutputStream, String word) throws IOException {
        byte[] byteWord = word.getBytes("UTF-8");
        dataOutputStream.writeInt(byteWord.length);
        dataOutputStream.write(byteWord);
    }

    public void writeDataToFile(HashMap<String, String> fileMap) throws IOException {
        dataOutputStream = new DataOutputStream(new FileOutputStream(fileMapPath));
        for (HashMap.Entry<String, String> entry : fileMap.entrySet()) {
            writeWord(dataOutputStream, entry.getKey());
            writeWord(dataOutputStream, entry.getValue());
        }
    }

    @Override
    public void close() throws Exception {
        dataOutputStream.close();
    }
}
