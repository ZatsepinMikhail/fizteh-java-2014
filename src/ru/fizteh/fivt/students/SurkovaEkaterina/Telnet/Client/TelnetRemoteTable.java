package ru.fizteh.fivt.students.SurkovaEkaterina.Telnet.Client;

import ru.fizteh.fivt.storage.structured.ColumnFormatException;
import ru.fizteh.fivt.storage.structured.Storeable;
import ru.fizteh.fivt.storage.structured.Table;
import ru.fizteh.fivt.students.SurkovaEkaterina.Telnet.TypesParser;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class TelnetRemoteTable implements Table, AutoCloseable {
    String name;
    Scanner inputStream;
    PrintStream outputStream;
    TelnetRemoteTableProvider provider;
    Socket socket;

    TelnetRemoteTable(String name, String host, int port, TelnetRemoteTableProvider provider)
            throws IOException {
        this.name = name;
        this.socket = new Socket(host, port);
        this.inputStream =  new Scanner(socket.getInputStream());
        this.outputStream = new PrintStream(socket.getOutputStream());
        this.provider = provider;
        outputStream.println("use " + name);
        String result = inputStream.nextLine();
        System.out.println(result);
    }

    @Override
    public Storeable put(String key, Storeable value) throws ColumnFormatException {
        if (key == null || value == null) {
            throw new IllegalArgumentException();
        }
        String serialized = provider.serialize(this, value);
        outputStream.println("put " + key + " " + serialized);
        String result = inputStream.nextLine();
        System.out.println(result);
        if (result.equals("new")) {
            return null;
        }
        if (result.equals("overwrite")) {
            String old = inputStream.nextLine();
            try {
                return provider.deserialize(this, old);
            } catch (ParseException e) {
                throw new ColumnFormatException(e.getMessage());
            }
        } else {
            result = result.substring(11, result.length() - 1);
            throw new ColumnFormatException(result);
        }
}

    @Override
    public Storeable remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        outputStream.println("remove " + key);
        String result = inputStream.nextLine();
        System.out.println(result);
        switch (result) {
            case "not found":
                return null;
            case "removed":
                String old = inputStream.nextLine();
                try {
                    return provider.deserialize(this, old);
                } catch (ParseException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    @Override
    public int size() {
        outputStream.println("size");
        String result = inputStream.nextLine();
        System.out.println(result);
        return Integer.parseInt(result);
    }

    @Override
    public List<String> list() {
        outputStream.println("list");
        String list = inputStream.nextLine();
        System.out.println(list);
        List<String> result = new ArrayList<>();
        Collections.addAll(result, list.split(", "));
        return result;
    }

    @Override
    public int commit() throws IOException {
        outputStream.println("commit");
        String result = inputStream.nextLine();
        System.out.println(result);
        return Integer.parseInt(result);
    }

    @Override
    public int rollback() {
        outputStream.println("rollback");
        String result = inputStream.nextLine();
        System.out.println(result);
        return Integer.parseInt(result);
    }

    @Override
    public int getNumberOfUncommittedChanges() {
        outputStream.println("use " + name);
        String result = inputStream.nextLine();
        if (result.equals("using " + name)) {
            return 0;
        } else {
            return Integer.parseInt(result.split(" ")[0]);
        }
    }

    @Override
    public int getColumnsCount() {
        outputStream.println("describe " + name);
        String signature = inputStream.nextLine();
        return signature.split(" ").length;
    }

    @Override
    public Class<?> getColumnType(int columnIndex) throws IndexOutOfBoundsException {
        outputStream.println("describe " + name);
        String signature = inputStream.nextLine();
        try {
            List<Class<?>> types = TypesParser.getListFromString(signature);
            return types.get(columnIndex);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Storeable get(String key) {
        if (key == null) {
            throw new IllegalArgumentException();
        }
        outputStream.println("get " + key);
        String result = inputStream.nextLine();
        System.out.println(result);
        if (result.equals("not found")) {
            return null;
        } else {
            String value = inputStream.nextLine();
            try {
                return provider.deserialize(this, value);
            } catch (ParseException e) {
                return null;
            }
        }
    }

    @Override
    public void close() throws IOException {
        rollback();
        socket.close();
    }
}
