package ru.fizteh.fivt.students.SibgatullinDamir.storeable;

/**
 * Created by Lenovo on 01.10.2014.
 */
public interface Commands {
    void execute(String[] args, MyTable table) throws MyException;
    String getName();
}
