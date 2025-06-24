package com.olineshop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

//Singleton
public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/online_store_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    private DatabaseManager() {
    }

    public static Connection getConnection() {
        if (connection == null) {
            try {

                System.out.println("Подключение к базе данных: " + URL);
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Подключение успешно установлено");
            } catch (SQLException e) {
                System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
                e.printStackTrace();

                throw new RuntimeException("Не удалось подключиться к базе данных!", e);
            }
        }
        return connection;
    }


    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
} 