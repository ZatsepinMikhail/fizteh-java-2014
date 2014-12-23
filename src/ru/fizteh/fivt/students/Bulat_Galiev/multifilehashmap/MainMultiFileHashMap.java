package ru.fizteh.fivt.students.Bulat_Galiev.multifilehashmap;

public final class MainMultiFileHashMap {
    private MainMultiFileHashMap() {
        // Disable instantiation to this class.
    }

    public static void main(final String[] args) {
        try {
            String databaseDir = System.getProperty("fizteh.db.dir");
            if (databaseDir == null) {
                System.err.println("specify the path to fizteh.db.dir");
                System.exit(-1);
            }
            TableProvider provider = new TableProviderFactory()
                    .create(databaseDir);
            if (args.length == 0) {
                Modesfilemap.interactiveMode(provider);
            } else {
                Modesfilemap.batchMode(provider, args);
            }
        } catch (Exception e) {
            System.err.print(e.getMessage());
            System.exit(-1);
        }
    }
}
