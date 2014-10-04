package ru.fizteh.fivt.students.ZatsepinMikhail.FileMap;

import java.util.HashMap;

public abstract class Command<T> {
    protected String name;
    protected int numberOfArguments;

    public abstract boolean run(T object, String[] args);

    @Override
    public final String toString() {
        return name;
    }
}
