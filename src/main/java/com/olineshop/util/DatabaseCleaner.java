package com.olineshop.util;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Класс для очистки базы данных
 */
public class DatabaseCleaner {
    
    /**
     * Очищает таблицу products
     */
    public static void clearProducts() {
        try {
            Connection connection = DatabaseManager.getConnection();
            if (connection == null) {
                System.out.println("Не удалось получить соединение с базой данных");
                return;
            }
            
            System.out.println("Очистка таблицы products...");
            executeSqlScript(connection, "db/clear_products.sql");
            System.out.println("Таблица products успешно очищена");
        } catch (Exception e) {
            System.out.println("Ошибка при очистке таблицы products: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Пересоздает всю базу данных
     */
    public static void recreateDatabase() {
        try {
            // Сбрасываем соединение
            DatabaseManager.resetConnectionStatus();
            
            Connection connection = DatabaseManager.getConnection();
            if (connection == null) {
                System.out.println("Не удалось получить соединение с базой данных");
                return;
            }
            
            System.out.println("Удаление существующих таблиц...");
            
            // Удаляем таблицы в обратном порядке зависимостей
            try (Statement stmt = connection.createStatement()) {
                // Отключаем проверку внешних ключей
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
                
                // Удаляем таблицы
                stmt.execute("DROP TABLE IF EXISTS order_items");
                stmt.execute("DROP TABLE IF EXISTS orders");
                stmt.execute("DROP TABLE IF EXISTS products");
                stmt.execute("DROP TABLE IF EXISTS users");
                stmt.execute("DROP TABLE IF EXISTS roles");
                
                // Включаем проверку внешних ключей
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1");
            } catch (SQLException e) {
                System.out.println("Ошибка при удалении таблиц: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Закрываем соединение
            connection.close();
            
            // Сбрасываем соединение
            DatabaseManager.resetConnectionStatus();
            
            // Получаем новое соединение
            connection = DatabaseManager.getConnection();
            if (connection == null) {
                System.out.println("Не удалось получить соединение с базой данных после удаления таблиц");
                return;
            }
            
            System.out.println("Создание новых таблиц...");
            executeSqlScript(connection, "db/schema.sql");
            executeSqlScript(connection, "db/add_admin.sql");
            
            System.out.println("База данных успешно пересоздана");
        } catch (Exception e) {
            System.out.println("Ошибка при пересоздании базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Выполняет SQL-скрипт
     */
    private static void executeSqlScript(Connection conn, String resourcePath) {
        System.out.println("Выполнение SQL-скрипта: " + resourcePath);
        try {
            InputStream inputStream = DatabaseCleaner.class.getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                System.out.println("Не удалось найти файл " + resourcePath + " в ресурсах");
                return;
            }
            
            String sql = new BufferedReader(new InputStreamReader(inputStream))
                    .lines().collect(Collectors.joining("\n"));
            
            String[] sqlCommands = sql.split(";");
            
            try (Statement stmt = conn.createStatement()) {
                for (String command : sqlCommands) {
                    if (!command.trim().isEmpty()) {
                        System.out.println("Выполнение SQL-команды: " + command);
                        stmt.execute(command);
                    }
                }
            }
            System.out.println("SQL-скрипт " + resourcePath + " выполнен успешно");
        } catch (Exception e) {
            System.out.println("Ошибка при выполнении SQL-скрипта " + resourcePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Точка входа для запуска очистки из командной строки
     */
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("recreate")) {
            recreateDatabase();
        } else {
            clearProducts();
        }
        
        DatabaseManager.closeConnection();
        System.out.println("Операция с базой данных завершена");
    }
} 