package com.optimus;

import java.sql.SQLException;

public class DemoApartment {

    public static void main(String[] args) {
        String connection = "jdbc:mysql://localhost:3306/optimus?serverTimezone=Europe/Kiev";
        String user = "root";
        String password = "mysqlpassword";
        try {
            new Apartment(connection, user, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
