package com.olineshop.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

//Singleton
public class DatabaseManager {
    // Изменяем URL для поддержки стандартного порта MySQL и альтернативного порта XAMPP
    private static final String URL = "jdbc:mysql://localhost:3306/online_store_db";
    private static final String URL_WITHOUT_DB = "jdbc:mysql://localhost:3306/";
    private static final String ALTERNATIVE_URL = "jdbc:mysql://localhost:3307/online_store_db";
    private static final String ALTERNATIVE_URL_WITHOUT_DB = "jdbc:mysql://localhost:3307/";
    private static final String DB_NAME = "online_store_db";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private static Connection connection;
    private static boolean connectionFailed = false;
    private static String lastErrorMessage = "";
    private static boolean useAlternativePort = false;
    private static boolean isDbInitialized = false;

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

                // Проверяем существование базы данных и создаем ее при необходимости
                boolean isNewDatabase = ensureDatabaseExists();

                // Выбираем URL в зависимости от успешности подключения
                String currentUrl = useAlternativePort ? ALTERNATIVE_URL : URL;
                System.out.println("Подключение к базе данных: " + currentUrl);
                
                // Добавляем параметры для корректной работы с кириллицей
                String connectionUrl = currentUrl + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
                
                // Добавляем дополнительную информацию для отладки
                System.out.println("Пытаемся подключиться к: " + connectionUrl);
                System.out.println("Пользователь: " + USER);
                System.out.println("Пароль: " + (PASSWORD.isEmpty() ? "[пустой]" : "[установлен]"));
                
                try {
                    connection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
                    System.out.println("Подключение успешно установлено");
                    
                    // Проверяем соединение с базой данных
                    if (connection.isValid(5)) {
                        System.out.println("Соединение с БД действительно");
                    } else {
                        System.out.println("Соединение с БД недействительно");
                    }
                    
                    // Проверяем, инициализирована ли база данных
                    if (isNewDatabase || !isDatabaseInitialized()) {
                        System.out.println("Инициализация новой базы данных...");
                        executeSqlScript(connection, "db/schema.sql");
                        executeSqlScript(connection, "db/add_admin.sql");
                        isDbInitialized = true;
                    } else {
                        System.out.println("База данных уже инициализирована, пропускаем выполнение скриптов");
                    }
                    
                    connectionFailed = false;
                } catch (SQLException e) {
                    System.out.println("Ошибка подключения к базе данных: " + e.getMessage());
                    System.out.println("SQL State: " + e.getSQLState());
                    System.out.println("Error Code: " + e.getErrorCode());
                    
                    // Если не удалось подключиться и мы еще не пробовали альтернативный порт
                    if (!useAlternativePort) {
                        System.out.println("Пробуем подключиться через альтернативный порт 3307...");
                        useAlternativePort = true;
                        connection = null;
                        connectionFailed = false;
                        return getConnection(); // Рекурсивно пытаемся подключиться через альтернативный порт
                    }
                    
                    e.printStackTrace();
                    connectionFailed = true;
                    lastErrorMessage = "Не удалось подключиться к базе данных: " + e.getMessage();
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Непредвиденная ошибка при подключении к базе данных: " + e.getMessage());
                e.printStackTrace();
                
                connectionFailed = true;
                lastErrorMessage = "Непредвиденная ошибка: " + e.getMessage();
                return null;
            }
        }
        return connection;
    }

    private static boolean ensureDatabaseExists() {
        boolean isNewDatabase = false;
        try {
            // Выбираем URL в зависимости от настройки порта
            String currentUrlWithoutDb = useAlternativePort ? ALTERNATIVE_URL_WITHOUT_DB : URL_WITHOUT_DB;
            System.out.println("Проверка существования базы данных: " + DB_NAME + " на " + currentUrlWithoutDb);
            
            // Добавляем параметры для корректной работы с кириллицей
            String connectionUrl = currentUrlWithoutDb + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
            Connection rootConnection = DriverManager.getConnection(connectionUrl, USER, PASSWORD);
            Statement stmt = rootConnection.createStatement();
            
            // Проверяем существование базы данных
            ResultSet resultSet = stmt.executeQuery("SHOW DATABASES LIKE '" + DB_NAME + "'");
            isNewDatabase = !resultSet.next();
            
            // Создаем базу данных, если она не существует
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("База данных " + DB_NAME + (isNewDatabase ? " создана" : " уже существует"));
            
            // Закрываем соединение
            stmt.close();
            rootConnection.close();
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке/создании базы данных: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            
            // Если не удалось подключиться и мы еще не пробовали альтернативный порт
            if (!useAlternativePort) {
                System.out.println("Пробуем подключиться через альтернативный порт 3307...");
                useAlternativePort = true;
                return ensureDatabaseExists(); // Рекурсивно пытаемся подключиться через альтернативный порт
            }
            e.printStackTrace();
        }
        return isNewDatabase;
    }
    
    private static boolean isDatabaseInitialized() {
        try {
            Statement stmt = connection.createStatement();
            
            // Проверяем наличие таблицы products
            ResultSet tablesResult = stmt.executeQuery(
                "SELECT COUNT(*) FROM information_schema.tables " +
                "WHERE table_schema = '" + DB_NAME + "' AND table_name = 'products'"
            );
            
            if (tablesResult.next() && tablesResult.getInt(1) > 0) {
                // Проверяем наличие данных в таблице products
                ResultSet productsResult = stmt.executeQuery("SELECT COUNT(*) FROM products");
                if (productsResult.next() && productsResult.getInt(1) > 0) {
                    System.out.println("База данных уже инициализирована и содержит товары");
                    return true;
                }
            }
            
            System.out.println("База данных не инициализирована или не содержит товары");
            return false;
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке инициализации базы данных: " + e.getMessage());
            return false;
        }
    }
    
    private static void executeSqlScript(Connection conn, String resourcePath) {
        System.out.println("Выполнение SQL-скрипта: " + resourcePath);
        try {
            InputStream inputStream = DatabaseManager.class.getClassLoader().getResourceAsStream(resourcePath);
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

    public static boolean isConnectionFailed() {
        return connectionFailed;
    }
    
    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }
    
    public static void resetConnectionStatus() {
        System.out.println("Сброс состояния соединения с базой данных");
        connectionFailed = false;
        lastErrorMessage = "";
        
        // Закрываем соединение
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Текущее соединение закрыто");
            } catch (SQLException e) {
                System.out.println("Ошибка при закрытии соединения: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        connection = null;
        isDbInitialized = false;
        System.out.println("Состояние соединения сброшено");
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