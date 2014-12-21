package ru.fizteh.fivt.students.YaronskayaLiubov.JUnit;

import ru.fizteh.fivt.storage.strings.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luba_yaronskaya on 26.10.14.
 */
public class JUnitTable extends FileMap implements Table {

    @Override
    public String getName() {
        return curDB.getName();
    }

    public JUnitTable(String path) {
        super(path);
    }

    @Override
    protected void finalize() throws Throwable {
        this.save();
        super.finalize();
    }

    @Override
    public String get(String key) throws IllegalArgumentException {
        CheckParameters.checkKey(key);
        if (deltaRemoved.contains(key)) {
            return null;
        }
        String value = deltaAdded.get(key);
        if (value != null) {
            return value;
        }
        value = deltaChanged.get(key);
        if (value != null) {
            return value;
        }
        return super.get(key);
    }

    @Override
    public String put(String key, String value) throws IllegalArgumentException {
        CheckParameters.checkKey(key);
        CheckParameters.checkValue(value);
        String oldValue = null;

        if (deltaChanged.containsKey(key)) {
            oldValue = deltaChanged.put(key, value);
        } else {
            if (super.containsKey(key)) {
                oldValue = super.get(key);
                deltaChanged.put(key, value);
            } else {
                oldValue = deltaAdded.put(key, value);
            }
        }
        return oldValue;
    }

    @Override
    public String remove(String key) {
        CheckParameters.checkKey(key);
        delta.add(key);
        String value = null;
        /*if (deltaAdded.containsKey(key)) {
            value = deltaAdded.remove(key);
        } else {
            if (deltaChanged.containsKey(key)) {
                value = deltaChanged.remove(key);
            } else {
                value = super.remove(key);
            }
        }
        deltaRemoved.add(key);
        Storeable value = null;*/
        if (super.containsKey(key)) {
            if (deltaChanged.containsKey(key)) {
                value = deltaChanged.remove(key);
            } else {
                deltaRemoved.add(key);
            }
        } else {
            if (deltaAdded.containsKey(key)) {
                value = deltaAdded.remove(key);
            }
        }
        //return value;
        return value;
    }

    @Override
    public int size() {
        return super.size() + deltaAdded.size() - deltaRemoved.size();
    }

    @Override
    public int commit() {
        int deltaCount = deltaAdded.size() + deltaChanged.size() + deltaRemoved.size();
        super.putAll(deltaAdded);
        super.putAll(deltaChanged);
        super.keySet().removeAll(deltaRemoved);
        deltaAdded.clear();
        deltaChanged.clear();
        deltaRemoved.clear();
        return deltaCount;

    }

    @Override
    public int rollback() {
        int deltaCount = deltaAdded.size() + deltaChanged.size() + deltaRemoved.size();
        deltaAdded.clear();
        deltaChanged.clear();
        deltaRemoved.clear();
        return deltaCount;
    }

    @Override
    public List<String> list() {
        List<String> keys = new ArrayList<>(keySet());
        keys.removeAll(deltaRemoved);
        keys.addAll(deltaAdded.keySet());
        return keys;
    }

    public int unsavedChangesCount() {
        return deltaAdded.size() + deltaChanged.size() + deltaRemoved.size();
    }
}
