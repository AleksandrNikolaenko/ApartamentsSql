package com.optimus;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Utils {

    static List<String[]> getTableList(ResultSetMetaData metaData, ResultSet resultSet) throws SQLException {
        List<String[]> tableList = new LinkedList<>();
        int columnCount = metaData.getColumnCount();
        String[] rowData = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            rowData[i] = metaData.getColumnName(i + 1);
        }
        tableList.add(rowData);
        while (resultSet.next()) {
            rowData = new String[columnCount];
            for (int i = 0; i < columnCount; i++) {
                rowData[i] = resultSet.getString(i + 1);
            }
            tableList.add(rowData);
        }
        return tableList;
    }

    static void printInConsole(int tableSize, Map<String, Integer> columnSize, List<String[]> tableList) {
        for (int i = 0; i < tableSize; i++) System.out.print("-");
        System.out.println();
        for (int i = 0; i < tableList.size(); i++) {
            String[] rowForPrint = new String[tableList.get(0).length];
            for (int j = 0; j < tableList.get(0).length; j++) rowForPrint[j] = tableList.get(i)[j];
            while (isPrint(rowForPrint)) {
                System.out.print("|");
                for (int j = 0; j < rowForPrint.length; j++) {
                    String key = tableList.get(0)[j];
                    String cellValue = rowForPrint[j];
                    if (cellValue != null) {
                        if (cellValue.length() > columnSize.get(key)) {
                            int spaceIndex = columnSize.get(key) - 1;
                            while (cellValue.charAt(spaceIndex) != ' ') spaceIndex--;
                            cellValue = rowForPrint[j].substring(0, spaceIndex + 1);
                            rowForPrint[j] = rowForPrint[j].substring(spaceIndex + 1);
                            System.out.print(cellValue);
                            for (int k = 0; k < columnSize.get(key) - cellValue.length(); k++) System.out.print(" ");
                            System.out.print("|");
                        } else {
                            System.out.print(cellValue);
                            for (int k = 0; k < columnSize.get(key) - cellValue.length(); k++) System.out.print(" ");
                            System.out.print("|");
                            rowForPrint[j] = null;
                        }
                    } else {
                        for (int k = 0; k < columnSize.get(key); k++) System.out.print(" ");
                        System.out.print("|");
                    }
                }
                System.out.println();
            }
            for (int j = 0; j < tableSize; j++) System.out.print("-");
            System.out.println();
        }
    }

    private static boolean isPrint(String[] rowForPrint) {
        for (String cell : rowForPrint) if (cell != null) return true;
        return false;
    }

    static int setSizesForPrintInConsole(List<String[]> tableList, Map<String, Integer> columnSize, Map<String, Integer> maxColumnSize) {
        int tableSize = 1;
        for (int i = 0; i < tableList.get(0).length; i++) {
            String key = tableList.get(0)[i];
            int maxSize = 0;
            for (int j = 0; j < tableList.size(); j++) {
                String[] row = tableList.get(j);
                if (row[i].length() > maxSize) maxSize = row[i].length();
            }
            columnSize.put(key, maxSize);
            if (columnSize.get(key) > maxColumnSize.get(key)) columnSize.put(key, maxColumnSize.get(key));
            tableSize += columnSize.get(key) + 1;
        }
        return tableSize;
    }
}
