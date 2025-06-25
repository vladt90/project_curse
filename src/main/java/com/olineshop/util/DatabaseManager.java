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
    private static boolean connectionFailed = false;
    private static String lastErrorMessage = "";

    private DatabaseManager() {
    }

    public static Connection getConnection() {
        if (connection == null && !connectionFailed) {
            try {
                // Загружаем драйвер MySQL
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                    System.out.println("MySQL JDBC драйвер загружен успешно");
                } catch (ClassNotFoundException e) {
                    System.out.println("Ошибка загрузки MySQL JDBC драйвера: " + e.getMessage());
                    e.printStackTrace();
                    connectionFailed = true;
                    lastErrorMessage = "Не удалось загрузить драйвер MySQL: " + e.getMessage();
                    return null;
                }

                System.out.println("Подключение к базе данных: " + URL);
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Подключение успешно установлено");
                connectionFailed = false;
            } catch (SQLException e) {
                System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
                e.printStackTrace();
                
                connectionFailed = true;
                lastErrorMessage = "Не удалось подключиться к базе данных: " + e.getMessage();
                return null;
            }
        }
        return connection;
    }

    public static boolean isConnectionFailed() {
        return connectionFailed;
    }
    
    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }
    
    public static void resetConnectionStatus() {
        connectionFailed = false;
        lastErrorMessage = "";
        connection = null;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                connectionFailed = false;
                System.out.println("Соединение с базой данных закрыто");
            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии соединения: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
} 