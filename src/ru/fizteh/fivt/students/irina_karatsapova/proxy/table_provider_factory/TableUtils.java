package ru.fizteh.fivt.students.irina_karatsapova.proxy.table_provider_factory;

import ru.fizteh.fivt.students.irina_karatsapova.proxy.exceptions.ColumnFormatException;
import ru.fizteh.fivt.students.irina_karatsapova.proxy.interfaces.Storeable;
import ru.fizteh.fivt.students.irina_karatsapova.proxy.interfaces.Table;
import ru.fizteh.fivt.students.irina_karatsapova.proxy.utils.TypeTransformer;

public class TableUtils {
    public static void checkColumnsFormat(Table table, Storeable value)
                                                        throws ColumnFormatException, IndexOutOfBoundsException {
        for (int columnIndex = 0; columnIndex < table.getColumnsCount(); ++columnIndex) {
            Object columnValue = value.getColumnAt(columnIndex);
            if (columnValue != null && !table.getColumnType(columnIndex).equals(columnValue.getClass())) {
                throw new ColumnFormatException("column index " + columnIndex + ": "
                        + "table type - " + TypeTransformer.getStringByType(table.getColumnType(columnIndex)) + ", "
                        + "given type - " + TypeTransformer.getStringByType(columnValue.getClass()));
            }
        }
        try {
            value.getColumnAt(table.getColumnsCount() + 1);
            throw new ColumnFormatException("value contains more columns than table, there should be "
                    + table.getColumnsCount() + "columns");
        } catch (IndexOutOfBoundsException e) {
            // that's ok if it is caught
        }
    }
}
