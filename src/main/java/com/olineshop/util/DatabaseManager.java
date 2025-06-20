package com.olineshop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Класс для управления соединением с базой данных (Singleton)
 */
public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/online_store_db";
    private static final String USER = "root"; // Ваш пользователь
    private static final String PASSWORD = ""; // Ваш пароль

    private static Connection connection;

    /**
     * Приватный конструктор для предотвращения создания экземпляров класса
     */
    private DatabaseManager() {
    }

    /**
     * Получить соединение с базой данных
     * 
     * @return соединение с базой данных
     */
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Регистрировать драйвер не нужно для JDBC 4.0+
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                e.printStackTrace();
                // В реальном приложении здесь нужна более качественная обработка ошибок
                throw new RuntimeException("Не удалось подключиться к базе данных!", e);
            }
        }
        return connection;
    }

    /**
     * Закрыть соединение с базой данных
     */
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