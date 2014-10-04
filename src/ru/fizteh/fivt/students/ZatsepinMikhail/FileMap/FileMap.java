package ru.fizteh.fivt.students.ZatsepinMikhail.FileMap;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.HashMap;
import java.util.Set;
import java.util.Scanner;

public class FileMap implements FileMapState {
    private HashMap<String, String> dataBase;
    private String diskFile;

    public FileMap(String newDiskFile) {
        diskFile = newDiskFile;
    }

    public FileMap getFileMap() {
        return this;
    }

    public String get(String key) {
        return dataBase.get(key);
    }

    public String remove(String key) {
        return dataBase.remove(key);
    }

    public String put(String key, String value) {
        return dataBase.put(key, value);
    }

    public boolean init() {
        try (FileInputStream inStream = new FileInputStream(diskFile)) {
            FileChannel inputChannel;
            inputChannel = inStream.getChannel();
            ByteBuffer bufferFromDisk;
            try {
                bufferFromDisk =
                        inputChannel.map(MapMode.READ_ONLY, 0, inputChannel.size());
            } catch (IOException e) {
                System.out.println("io exception");
                return false;
            }
            try {
                while (bufferFromDisk.remaining() > 0) {
                    byte[] key;
                    int keySize = bufferFromDisk.getInt();
                    key = new byte[keySize];
                    bufferFromDisk.get(key, 0, key.length);

                    byte[] value;
                    int valueSize = bufferFromDisk.getInt();
                    value = new byte[valueSize];
                    bufferFromDisk.get(value, 0, value.length);
                    try {
                        dataBase.put(new String(key, "UTF-8"), new String(value, "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        System.out.println("unsupported encoding");
                        return false;
                    }
                }
            } catch (NullPointerException e) {
                System.out.println("null pointer exception");
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            return false;
        } catch (IOException e) {
            System.out.println("io exception");
            return false;
        }
        return true;
    }

    public boolean load() {
        try (FileOutputStream outputStream = new FileOutputStream(diskFile)) {
            Set<String> keySet = dataBase.keySet();
            ByteBuffer bufferForSize = ByteBuffer.allocate(4);
            for (String key : keySet) {
                try {
                    byte[] keyByte = key.getBytes("UTF-8");
                    byte[] valueByte = dataBase.get(key).getBytes("UTF-8");
                    outputStream.write(bufferForSize.putInt(0, keyByte.length).array());
                    outputStream.write(keyByte);
                    outputStream.write(bufferForSize.putInt(0, valueByte.length).array());
                    outputStream.write(valueByte);
                } catch (UnsupportedEncodingException e) {
                    System.out.println("unsupported encoding");
                    return false;
                } catch (IOException e) {
                    System.out.println("io exception");
                    return false;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            return false;
        } catch (IOException e) {
            System.out.println("io exception");
            return false;
        }
        return true;
    }

}