package com.optimus;

import java.sql.*;
import java.util.*;

public class Apartment {

    private final Map<String, Integer> maxColumnSize = new HashMap<>();

    private Connection connection;

    public Apartment(String connect, String user, String password) throws SQLException {
        this.connection = connectDB(connect, user, password);
        maxColumnSize.put("id", 5);
        maxColumnSize.put("district", 30);
        maxColumnSize.put("address", 30);
        maxColumnSize.put("area", 6);
        maxColumnSize.put("rooms", 5);
        maxColumnSize.put("price", 9);
        maxColumnSize.put("discription", 50);
        goWorkRoom();
    }

    private String questAnswer(String quest) {
        System.out.print(quest);
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }

    private Connection connectDB(String connect, String user, String password) throws SQLException {
        connection = DriverManager.getConnection(connect, user, password);
        Statement statement = connection.createStatement();
        statement.execute(
                "CREATE TABLE IF NOT EXISTS apartment (" +
                        "id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                        "district VARCHAR(30) NOT NULL," +
                        "address VARCHAR(100) NOT NULL," +
                        "area INT NOT NULL," +
                        "rooms INT NOT NULL," +
                        "price INT NOT NULL," +
                        "discription VARCHAR(1000) DEFAULT 'be away')");
        return connection;
    }

    public void goWorkRoom() throws SQLException{
        while (true) {
            System.out.println(
                    "Что будем делать?\n" +
                            "1. Добавить квартиру\n" +
                            "2. Удалить квартиру\n" +
                            "3. Изменить данные по квартире\n" +
                            "4. Сделать выборку\n" +
                            "0. Выход");
            String command = questAnswer("-> ");
            switch(command) {
                case "1" -> addApartment();
                case "2" -> delApartment();
                case "3" -> update();
                case "4" -> goSelectRoom();
                case "0" -> {
                    if (connection != null) connection.close();
                    System.exit(0);
                }
                default -> System.out.println("Не разборчиво... повторите");
            }
        }
    }

    private void addApartment() throws SQLException{
        System.out.println("Добавляем квартиру, требуются данные");
        String district = questAnswer("Район: ");
        if (district.length() > 30) {
            System.out.println("Так много букв не влезет. Не больше 30 символов");
            goWorkRoom();
        }
        String address = questAnswer("Адрес: ");
        if (address.length() > 50) {
            System.out.println("Не не не слишкрм длинно. Не более 50 символов");
        }
        int area = 0;
        int rooms = 0;
        int price = 0;
        try {
            area = Integer.parseInt(questAnswer("Площадь (кв.м): "));
            rooms = Integer.parseInt(questAnswer("Колличество комнат: "));
            price = Integer.parseInt(questAnswer("Цена: "));
        } catch (NumberFormatException e){
            System.out.println("Китайский не понимаю... отмена");
            goWorkRoom();
        }
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO Apartment (district, address, area, rooms, price) VALUES (?, ?, ?, ?, ?)");
        statement.setString(1, district);
        statement.setString(2, address);
        statement.setInt(3, area);
        statement.setInt(4, rooms);
        statement.setInt(5, price);
        statement.executeUpdate();
        statement.close();
    }

    private void delApartment() throws SQLException {
        String id = questAnswer("Введите id квартиры для удаления:\n-> ");
        PreparedStatement statement = connection.prepareStatement("DELETE FROM apartment WHERE id = ?");
        statement.setString(1, id);
        statement.executeUpdate();
        statement.close();
    }

    private void goSelectRoom() throws SQLException{
        System.out.println(
                "Что интересует?\n" +
                        "1. Всё\n"  +
                        "2. Площадь (кв.м)\n" +
                        "3. Количество комнат\n" +
                        "4. Цена\n" +
                        "5. Создать свой запрос\n" +
                        "0. Вернутся назад");
        String command = questAnswer("-> ");
        switch (command) {
            case "1" -> printSelectInConsole("*", "");
            case "2" -> setSelect("*", "area");
            case "3" -> setSelect("*", "rooms");
            case "4" -> setSelect("*", "price");
            case "5" -> setMultiSelect();
            case "0" -> goWorkRoom();
            default -> System.out.println("Не разборчиво... повторите");
        }

    }

    private void printSelectInConsole(String from, String where) throws SQLException{

        PreparedStatement dataStatement = connection.prepareStatement("SELECT " + from + " FROM apartment " + where);
        ResultSet resultSet = dataStatement.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        if (metaData.getColumnCount() != 0) {
            List<String[]> tableList = Utils.getTableList(metaData, resultSet);
            Map<String, Integer> columnSize = new HashMap<>();
            int tableSize = Utils.setSizesForPrintInConsole(tableList, columnSize, maxColumnSize);
            Utils.printInConsole(tableSize, columnSize, tableList);
        } else {
            System.out.println("Не найденно");
        }
        resultSet.close();
        dataStatement.close();
    }

    private void setSelect(String from, String where) throws SQLException{
        String whereWithRange = "";
        System.out.println("Диапазон будем задавать?");
        String yesNo = questAnswer("-> Y/N : ");
        if (yesNo.equalsIgnoreCase("Y")) {
            whereWithRange = setSelectRange(where);
            if (!whereWithRange.equals("")) whereWithRange = " WHERE " + whereWithRange;
            printSelectInConsole(from, whereWithRange);
            goWorkRoom();
        }
        if (yesNo.equalsIgnoreCase("N")) {
            printSelectInConsole(from, whereWithRange);
            goWorkRoom();
        }
        System.out.println("Ваш выбор мне не понятен");
        goSelectRoom();
    }

    private void setMultiSelect() throws SQLException{
        String from = "id, district, address";
        String where = "";
        String yesNo;
        String[] columnNames = new String[]{"area", "rooms", "price"};
        for (int i = 0; i < columnNames.length; i++) {
            yesNo = questAnswer(columnNames[i] + " ставим в запрос?\n-> Y/N : ");
            if (yesNo.equalsIgnoreCase("Y")) {
                from += ", " + columnNames[i];
                yesNo = questAnswer("Диапазон задаём?\n-> Y/N : ");
                if (yesNo.equalsIgnoreCase("Y")) {
                    if (where.equals("")) where += setSelectRange(columnNames[i]);
                    else where += " && " + setSelectRange(columnNames[i]);
                    continue;
                }
                if (!yesNo.equalsIgnoreCase("N")) {
                    System.out.println("кыр мыр быр... пишите яснее... отмена");
                    goSelectRoom();
                }
            }
        }
        if (!where.equals("")) where = " WHERE " + where;
        yesNo = questAnswer("Добавляем описание?\n-> Y/N : ");
        if (yesNo.equalsIgnoreCase("Y")) {
            from += ", discription";
            printSelectInConsole(from, where);
        }
        if (!yesNo.equalsIgnoreCase("N")) {
            System.out.println("кыр мыр быр... пишите яснее... отмена");
            goSelectRoom();
        }
        printSelectInConsole(from, where);
    }

    private String setSelectRange(String where) throws SQLException{
        String whereWithRange = "";
        System.out.println("Укажите от и до. Можно указать только один параметр.");
        int fromQ = 0;
        int toQ = 0;
        try {
            String answer = questAnswer(where + " от: ");
            if (!answer.equals("")) fromQ = Integer.parseInt(answer);
            answer = questAnswer(where + " до: ");
            if (!answer.equals("")) toQ = Integer.parseInt(answer);
        } catch (NumberFormatException e) {
            System.out.println("Муть какая то... отмена");
            goSelectRoom();
        }
        if (fromQ > 0) whereWithRange = where + " >= " + fromQ;
        if (toQ > 0 && !whereWithRange.equals("")) whereWithRange += " && " + where + " <= " + toQ;
        if (toQ > 0 && whereWithRange.equals("")) whereWithRange = where + " <= " + toQ;
        return whereWithRange;
    }

    private void update() throws SQLException{
        String updateText = "UPDATE apartment SET ";
        int id = -1;
        try {
            id = Integer.parseInt(questAnswer("Ввведите id квартиры:\n-> "));
        } catch (NumberFormatException e) {
            System.out.println("У меня от вас голова болит... отмена");
        }
        System.out.println(
                "Что будем менять?\n" +
                        "1. Район\n" +
                        "2. Адрес\n" +
                        "3. Площадь (кв.м)\n" +
                        "4. Количество комнат\n" +
                        "5. Цена\n" +
                        "6. Описание\n" +
                        "0. Вернутся назад");
        String command = questAnswer("-> ");
        try {
            switch (command) {
                case "1" -> {
                    String answer = questAnswer("Введите новый район\n-> ");
                    if (answer.length() > 30) {
                        System.out.println("Слишком длинно. Не более 30 символов");
                        goWorkRoom();
                    } else updateText += "district = '" + answer + "'";
                }
                case "2" -> {
                    String answer = questAnswer("Введите новый адрес\n-> ");
                    if (answer.length() > 50) {
                        System.out.println("Слишком длинно. Не более 50 символов");
                        goWorkRoom();
                    } else updateText += "address = '" + answer + "'";
                }
                case "3" -> updateText += "area = " + Integer.parseInt(questAnswer("Введите новую площадь\n-> "));
                case "4" -> updateText += "rooms = " + Integer.parseInt(questAnswer("Введите новое количество комнат\n-> "));
                case "5" -> updateText += "price = " + Integer.parseInt(questAnswer("Введите новую цену\n-> "));
                case "6" -> {
                    String answer = questAnswer("Введите новое описание\n-> ");
                    if (answer.length() > 1000) {
                        System.out.println("Слишком длинно. Не более 1000 символов");
                        goWorkRoom();
                    } else updateText += "discription = '" + answer + "'";
                }
                case "0" -> goWorkRoom();
                default -> System.out.println("Не разборчиво... повторите");
            }
        }catch (NumberFormatException e) {
            System.out.println("Бланк испортил... отмена");
            goWorkRoom();
        }
        PreparedStatement statement = connection.prepareStatement(updateText + " WHERE id = " + id);
        statement.executeUpdate();
        statement.close();
    }
}


