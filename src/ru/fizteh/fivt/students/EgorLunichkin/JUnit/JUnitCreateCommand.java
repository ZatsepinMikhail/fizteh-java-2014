package ru.fizteh.fivt.students.EgorLunichkin.JUnit;

public class JUnitCreateCommand implements JUnitCommand {
    public JUnitCreateCommand(MyTableProvider mtp, String name) {
        this.tableName = name;
        this.myTableProvider = mtp;
    }

    private MyTableProvider myTableProvider;
    private String tableName;

    @Override
    public void run() {
        if (myTableProvider.createTable(tableName) == null) {
            System.out.println(tableName + " exists");
        } else {
            System.out.println("created");
        }
    }
}
