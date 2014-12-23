package ru.fizteh.fivt.students.deserg.parallel.commands;

import ru.fizteh.fivt.students.deserg.parallel.DbTableProvider;
import ru.fizteh.fivt.students.deserg.parallel.MyException;
import ru.fizteh.fivt.students.deserg.parallel.Serializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by deserg on 22.10.14.
 */
public class DbCreate implements Command {

    @Override
    public void execute(ArrayList<String> args, DbTableProvider db) {

        if (args.size() < 2) {
            throw new MyException("Not enough arguments");
        }
        if (args.size() >= 2) {

            String tableName = args.get(1);
            String typeString = "";
            for (int i = 2; i < args.size(); i++) {
                typeString += args.get(i) + " ";
            }

            String[] types = typeString.substring(1, typeString.length() - 2).split("\\s+");
            List<Class<?>> signature = Serializer.makeSignatureFromStrings(types);

            try {
                if (db.createTable(tableName, signature) == null) {
                    System.out.println(tableName + " exists");
                } else {
                    System.out.println("created");
                }
            } catch (IOException ex) {
                System.out.println("IO error while creating the table \"" + tableName + "\"");
                System.exit(1);
            }

        }
    }

}
