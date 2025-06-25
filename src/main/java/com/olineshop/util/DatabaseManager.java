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
import java.sql.DatabaseMetaData;

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
                        
                        // Проверяем и исправляем структуру базы данных
                        checkAndFixDatabaseStructure(connection);
                        
                        // Выводим полную информацию о структуре базы данных
                        printDatabaseInfo(connection);
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

    public static void testConnection() {
        System.out.println("Тестирование соединения с базой данных...");
        
        // Пробуем подключиться на стандартном порту 3306
        try {
            System.out.println("Попытка подключения к MySQL на порту 3306...");
            Connection testConn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC",
                USER, PASSWORD
            );
            System.out.println("Успешное подключение к MySQL на порту 3306");
            testConn.close();
        } catch (SQLException e) {
            System.out.println("Не удалось подключиться к MySQL на порту 3306: " + e.getMessage());
        }
        
        // Пробуем подключиться на альтернативном порту 3307 (XAMPP)
        try {
            System.out.println("Попытка подключения к MySQL на порту 3307...");
            Connection testConn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3307?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC",
                USER, PASSWORD
            );
            System.out.println("Успешное подключение к MySQL на порту 3307");
            testConn.close();
        } catch (SQLException e) {
            System.out.println("Не удалось подключиться к MySQL на порту 3307: " + e.getMessage());
        }
        
        // Проверяем наличие базы данных на порту 3306
        try {
            System.out.println("Проверка наличия базы данных " + DB_NAME + " на порту 3306...");
            Connection testConn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC",
                USER, PASSWORD
            );
            
            ResultSet rs = testConn.getMetaData().getCatalogs();
            boolean dbExists = false;
            while (rs.next()) {
                String dbName = rs.getString(1);
                if (DB_NAME.equals(dbName)) {
                    dbExists = true;
                    break;
                }
            }
            System.out.println("База данных " + DB_NAME + (dbExists ? " существует" : " не существует") + " на порту 3306");
            
            // Если база данных существует, проверяем структуру таблиц
            if (dbExists) {
                checkDatabaseStructure(testConn, DB_NAME);
            }
            
            testConn.close();
        } catch (SQLException e) {
            System.out.println("Не удалось проверить наличие базы данных на порту 3306: " + e.getMessage());
        }
        
        // Проверяем наличие базы данных на порту 3307
        try {
            System.out.println("Проверка наличия базы данных " + DB_NAME + " на порту 3307...");
            Connection testConn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3307?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC",
                USER, PASSWORD
            );
            
            ResultSet rs = testConn.getMetaData().getCatalogs();
            boolean dbExists = false;
            while (rs.next()) {
                String dbName = rs.getString(1);
                if (DB_NAME.equals(dbName)) {
                    dbExists = true;
                    break;
                }
            }
            System.out.println("База данных " + DB_NAME + (dbExists ? " существует" : " не существует") + " на порту 3307");
            
            // Если база данных существует, проверяем структуру таблиц
            if (dbExists) {
                checkDatabaseStructure(testConn, DB_NAME);
            }
            
            testConn.close();
        } catch (SQLException e) {
            System.out.println("Не удалось проверить наличие базы данных на порту 3307: " + e.getMessage());
        }
    }

    private static void checkDatabaseStructure(Connection conn, String dbName) {
        try {
            System.out.println("Проверка структуры таблиц в базе данных " + dbName + "...");
            
            // Переключаемся на нужную базу данных
            Statement stmt = conn.createStatement();
            stmt.execute("USE " + dbName);
            
            // Проверяем наличие таблиц
            String[] tables = {"roles", "users", "products", "orders", "order_items"};
            for (String table : tables) {
                ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE '" + table + "'");
                boolean tableExists = rs.next();
                System.out.println("Таблица '" + table + "' " + (tableExists ? "существует" : "НЕ существует"));
                
                if (tableExists) {
                    // Проверяем структуру таблицы
                    ResultSet columns = stmt.executeQuery("DESCRIBE " + table);
                    System.out.println("Структура таблицы '" + table + "':");
                    while (columns.next()) {
                        String columnName = columns.getString("Field");
                        String columnType = columns.getString("Type");
                        String isNull = columns.getString("Null");
                        String key = columns.getString("Key");
                        String defaultValue = columns.getString("Default");
                        System.out.println("  - " + columnName + " (" + columnType + ")" + 
                                          ", Null: " + isNull + ", Key: " + key + 
                                          ", Default: " + defaultValue);
                    }
                    
                    // Проверяем количество записей в таблице
                    ResultSet count = stmt.executeQuery("SELECT COUNT(*) FROM " + table);
                    if (count.next()) {
                        System.out.println("  Количество записей в таблице '" + table + "': " + count.getInt(1));
                    }
                }
            }
            
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке структуры базы данных: " + e.getMessage());
        }
    }

    // Метод для вывода полной информации о структуре базы данных
    private static void printDatabaseInfo(Connection conn) {
        try {
            System.out.println("\n=== ПОЛНАЯ ИНФОРМАЦИЯ О СТРУКТУРЕ БАЗЫ ДАННЫХ ===");
            
            // Получаем метаданные
            DatabaseMetaData metaData = conn.getMetaData();
            
            // Информация о базе данных
            System.out.println("Драйвер БД: " + metaData.getDriverName() + " " + metaData.getDriverVersion());
            System.out.println("URL подключения: " + metaData.getURL());
            System.out.println("Имя пользователя: " + metaData.getUserName());
            System.out.println("Продукт БД: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            
            // Получаем список таблиц
            System.out.println("\n--- ТАБЛИЦЫ ---");
            try (ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("\nТаблица: " + tableName);
                    
                    // Получаем список столбцов для таблицы
                    try (ResultSet columns = metaData.getColumns(null, null, tableName, null)) {
                        System.out.println("  Столбцы:");
                        while (columns.next()) {
                            String columnName = columns.getString("COLUMN_NAME");
                            String typeName = columns.getString("TYPE_NAME");
                            int columnSize = columns.getInt("COLUMN_SIZE");
                            String nullable = columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable ? "NULL" : "NOT NULL";
                            String defaultValue = columns.getString("COLUMN_DEF");
                            
                            System.out.println("    " + columnName + " - " + typeName + 
                                              "(" + columnSize + ") " + nullable + 
                                              (defaultValue != null ? " DEFAULT " + defaultValue : ""));
                        }
                    }
                    
                    // Получаем первичный ключ
                    try (ResultSet primaryKeys = metaData.getPrimaryKeys(null, null, tableName)) {
                        if (primaryKeys.next()) {
                            System.out.println("  Первичный ключ: " + primaryKeys.getString("COLUMN_NAME"));
                        }
                    }
                    
                    // Получаем внешние ключи
                    try (ResultSet foreignKeys = metaData.getImportedKeys(null, null, tableName)) {
                        if (foreignKeys.next()) {
                            System.out.println("  Внешние ключи:");
                            do {
                                String fkColumnName = foreignKeys.getString("FKCOLUMN_NAME");
                                String pkTableName = foreignKeys.getString("PKTABLE_NAME");
                                String pkColumnName = foreignKeys.getString("PKCOLUMN_NAME");
                                
                                System.out.println("    " + fkColumnName + " -> " + 
                                                  pkTableName + "." + pkColumnName);
                            } while (foreignKeys.next());
                        }
                    }
                    
                    // Получаем количество записей
                    try (Statement stmt = conn.createStatement();
                         ResultSet count = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
                        if (count.next()) {
                            System.out.println("  Количество записей: " + count.getInt(1));
                        }
                    }
                }
            }
            
            System.out.println("\n=== КОНЕЦ ИНФОРМАЦИИ О СТРУКТУРЕ БАЗЫ ДАННЫХ ===\n");
        } catch (SQLException e) {
            System.out.println("Ошибка при получении информации о структуре базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Метод для проверки и исправления структуры базы данных
    private static void checkAndFixDatabaseStructure(Connection conn) {
        System.out.println("\n=== ПРОВЕРКА И ИСПРАВЛЕНИЕ СТРУКТУРЫ БАЗЫ ДАННЫХ ===");
        
        try {
            // Проверяем существование таблиц
            String[] tables = {"roles", "users", "products", "orders", "order_items"};
            boolean allTablesExist = true;
            
            for (String table : tables) {
                try (ResultSet rs = conn.getMetaData().getTables(null, null, table, null)) {
                    if (!rs.next()) {
                        System.out.println("Таблица '" + table + "' не существует!");
                        allTablesExist = false;
                    } else {
                        System.out.println("Таблица '" + table + "' существует");
                    }
                }
            }
            
            if (!allTablesExist) {
                System.out.println("Не все необходимые таблицы существуют. Выполняем скрипт создания схемы...");
                executeSqlScript(conn, "db/schema.sql");
                System.out.println("Скрипт создания схемы выполнен");
                return;
            }
            
            // Проверяем структуру таблицы order_items
            System.out.println("Проверка структуры таблицы order_items...");
            
            // Проверяем наличие внешних ключей
            boolean hasOrderFk = false;
            boolean hasProductFk = false;
            
            try (ResultSet fks = conn.getMetaData().getImportedKeys(null, null, "order_items")) {
                while (fks.next()) {
                    String pkTableName = fks.getString("PKTABLE_NAME");
                    String fkColumnName = fks.getString("FKCOLUMN_NAME");
                    
                    if ("orders".equals(pkTableName) && "order_id".equals(fkColumnName)) {
                        hasOrderFk = true;
                        System.out.println("Внешний ключ order_id -> orders.id существует");
                    }
                    
                    if ("products".equals(pkTableName) && "product_id".equals(fkColumnName)) {
                        hasProductFk = true;
                        System.out.println("Внешний ключ product_id -> products.id существует");
                    }
                }
            }
            
            // Если отсутствуют внешние ключи, пересоздаем таблицу
            if (!hasOrderFk || !hasProductFk) {
                System.out.println("Отсутствуют необходимые внешние ключи в таблице order_items. Пересоздаем таблицу...");
                
                // Создаем временную таблицу для сохранения данных
                try (Statement stmt = conn.createStatement()) {
                    // Создаем временную таблицу
                    stmt.execute("CREATE TABLE IF NOT EXISTS `order_items_temp` (" +
                                 "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                                 "`order_id` INT NOT NULL," +
                                 "`product_id` INT NOT NULL," +
                                 "`quantity` INT NOT NULL," +
                                 "`price` DECIMAL(10, 2) NOT NULL)");
                    
                    // Копируем данные
                    stmt.execute("INSERT INTO `order_items_temp` SELECT * FROM `order_items`");
                    
                    // Удаляем старую таблицу
                    stmt.execute("DROP TABLE IF EXISTS `order_items`");
                    
                    // Создаем новую таблицу с правильными внешними ключами
                    stmt.execute("CREATE TABLE IF NOT EXISTS `order_items` (" +
                                 "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                                 "`order_id` INT NOT NULL," +
                                 "`product_id` INT NOT NULL," +
                                 "`quantity` INT NOT NULL," +
                                 "`price` DECIMAL(10, 2) NOT NULL," +
                                 "FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE," +
                                 "FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE)");
                    
                    // Восстанавливаем данные
                    stmt.execute("INSERT INTO `order_items` SELECT * FROM `order_items_temp`");
                    
                    // Удаляем временную таблицу
                    stmt.execute("DROP TABLE IF EXISTS `order_items_temp`");
                    
                    System.out.println("Таблица order_items успешно пересоздана с корректными внешними ключами");
                }
            }
            
            System.out.println("=== ПРОВЕРКА И ИСПРАВЛЕНИЕ СТРУКТУРЫ БАЗЫ ДАННЫХ ЗАВЕРШЕНЫ ===\n");
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке и исправлении структуры базы данных: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
    }
} 