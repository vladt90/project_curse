package com.olineshop;

import javafx.application.Application;
import javafx.stage.Stage;
import com.olineshop.view.LoginView;
import com.olineshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class Main extends Application {


     //primaryStage главное окно

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Запуск приложения...");
        
        try {
            System.out.println("Проверка подключения к базе данных...");
            
            // Запускаем расширенное тестирование соединения
            DatabaseManager.testConnection();
            
            // Сбрасываем соединение перед основным подключением
            DatabaseManager.resetConnectionStatus();
            System.out.println("Соединение сброшено, пробуем подключиться заново...");
            
            Connection connection = DatabaseManager.getConnection();
            if (connection != null && !connection.isClosed()) {
                System.out.println("Подключение к базе данных успешно установлено");
                
                // Проверяем, существуют ли все необходимые таблицы
                checkDatabaseTables(connection);
                
                // Проверяем наличие данных в таблицах
                checkDatabaseData(connection);
                
                // Обновляем структуру базы данных
                updateDatabaseSchema(connection);
                
                // Выполняем тестовый запрос для проверки работы с базой данных
                testDatabaseQuery(connection);
            } else {
                System.out.println("Ошибка: не удалось подключиться к базе данных");
                showDatabaseErrorAlert();
                return;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке подключения к базе данных: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            showDatabaseErrorAlert();
            return;
        }
        
        System.out.println("Запуск окна входа...");
        LoginView loginView = new LoginView();
        loginView.start(primaryStage);
        System.out.println("Окно входа запущено");
    }
    

    private void checkDatabaseTables(Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String[] tables = {"roles", "users", "products", "orders", "order_items"};
            
            System.out.println("Проверка существования таблиц в базе данных:");
            boolean allTablesExist = true;
            
            for (String table : tables) {
                ResultSet rs = metaData.getTables(null, null, table, null);
                if (rs.next()) {
                    System.out.println("Таблица '" + table + "' существует");
                } else {
                    System.out.println("Таблица '" + table + "' НЕ существует");
                    allTablesExist = false;
                }
                rs.close();
            }
            
            // Если не все таблицы существуют, создаем их
            if (!allTablesExist) {
                System.out.println("Создание недостающих таблиц...");
                createTables(connection);
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке таблиц: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createTables(Connection connection) {
        try {
            Statement stmt = connection.createStatement();
            
            // Создаем таблицу roles, если она не существует
            String createRolesTable = 
                "CREATE TABLE IF NOT EXISTS `roles` (" +
                "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `name` VARCHAR(50) NOT NULL UNIQUE" +
                ")";
            stmt.executeUpdate(createRolesTable);
            System.out.println("Таблица roles создана или уже существует");
            
            // Создаем таблицу users, если она не существует
            String createUsersTable = 
                "CREATE TABLE IF NOT EXISTS `users` (" +
                "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `login` VARCHAR(50) NOT NULL UNIQUE," +
                "  `password_hash` VARCHAR(255) NOT NULL," +
                "  `first_name` VARCHAR(50) NOT NULL," +
                "  `last_name` VARCHAR(50) NOT NULL," +
                "  `email` VARCHAR(100) NOT NULL," +
                "  `phone` VARCHAR(20)," +
                "  `discount` DOUBLE DEFAULT 0," +
                "  `role_id` INT NOT NULL," +
                "  FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`)" +
                ")";
            stmt.executeUpdate(createUsersTable);
            System.out.println("Таблица users создана или уже существует");
            
            // Создаем таблицу products, если она не существует
            String createProductsTable = 
                "CREATE TABLE IF NOT EXISTS `products` (" +
                "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `name` VARCHAR(100) NOT NULL UNIQUE," +
                "  `price` DECIMAL(10, 2) NOT NULL," +
                "  `unit` VARCHAR(20) NOT NULL DEFAULT 'шт'," +
                "  `stock_quantity` INT NOT NULL DEFAULT 0" +
                ")";
            stmt.executeUpdate(createProductsTable);
            System.out.println("Таблица products создана или уже существует");
            
            // Создаем таблицу orders, если она не существует
            String createOrdersTable = 
                "CREATE TABLE IF NOT EXISTS `orders` (" +
                "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `user_id` INT NOT NULL," +
                "  `order_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "  `delivery_date` DATETIME NULL," +
                "  `status` VARCHAR(20) NOT NULL DEFAULT 'Новый'," +
                "  `total_cost` DECIMAL(10, 2) NOT NULL," +
                "  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)" +
                ")";
            stmt.executeUpdate(createOrdersTable);
            System.out.println("Таблица orders создана или уже существует");
            
            // Создаем таблицу order_items, если она не существует
            String createOrderItemsTable = 
                "CREATE TABLE IF NOT EXISTS `order_items` (" +
                "  `id` INT AUTO_INCREMENT PRIMARY KEY," +
                "  `order_id` INT NOT NULL," +
                "  `product_id` INT NOT NULL," +
                "  `quantity` INT NOT NULL," +
                "  `price_per_item` DECIMAL(10, 2) NOT NULL," +
                "  FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`)," +
                "  FOREIGN KEY (`product_id`) REFERENCES `products`(`id`)" +
                ")";
            stmt.executeUpdate(createOrderItemsTable);
            System.out.println("Таблица order_items создана или уже существует");
            
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Ошибка при создании таблиц: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void checkDatabaseData(Connection connection) {
        try {
            // Проверяем наличие данных в таблице products
            java.sql.Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products");
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Количество товаров в базе данных: " + count);
                if (count == 0) {
                    System.out.println("Внимание: таблица products пуста");
                    // Добавляем тестовые товары
                    addTestProducts(connection);
                }
            }
            
            // Проверяем наличие ролей
            rs = stmt.executeQuery("SELECT COUNT(*) FROM roles");
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Количество ролей в базе данных: " + count);
                if (count == 0) {
                    System.out.println("Внимание: таблица roles пуста");
                    // Добавляем базовые роли
                    stmt.executeUpdate("INSERT INTO roles (id, name) VALUES (1, 'Администратор'), (2, 'Клиент')");
                    System.out.println("Добавлены базовые роли");
                }
            }
            
            // Проверяем наличие администратора
            rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role_id = 1");
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Количество администраторов в базе данных: " + count);
                if (count == 0) {
                    System.out.println("Внимание: нет администратора в базе данных");
                    // Добавляем администратора
                    stmt.executeUpdate("INSERT INTO users (login, password_hash, first_name, last_name, email, role_id) " +
                                      "VALUES ('admin', 'admin', 'Администратор', 'Системы', 'admin@example.com', 1)");
                    System.out.println("Добавлен администратор по умолчанию");
                }
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке данных в базе данных: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addTestProducts(Connection connection) {
        try {
            Statement stmt = connection.createStatement();
            
            // Добавляем тестовые товары
            String insertProducts = 
                "INSERT INTO products (name, price, unit, stock_quantity) VALUES " +
                "('Смартфон', 3000.00, 'шт', 15), " +
                "('Утюг', 1500.00, 'шт', 8), " +
                "('Телевизор', 6000.00, 'шт', 5), " +
                "('Кофемашина', 800.50, 'шт', 12), " +
                "('Пульт', 150.00, 'шт', 7), " +
                "('Микроволновка', 3500.99, 'шт', 20), " +
                "('Холодильник', 7000.00, 'шт', 3), " +
                "('Стиральная машина', 4500.00, 'шт', 6), " +
                "('Планшет', 1500.00, 'шт', 10), " +
                "('Фотоаппарат', 4800.00, 'шт', 4)";
            
            int rowsAffected = stmt.executeUpdate(insertProducts);
            System.out.println("Добавлено " + rowsAffected + " тестовых товаров");
            
            stmt.close();
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении тестовых товаров: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showDatabaseErrorAlert() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Ошибка базы данных");
            alert.setHeaderText("Ошибка подключения к базе данных");
            alert.setContentText("Не удалось подключиться к базе данных.\n\n" +
                               "Пожалуйста, убедитесь, что:\n" +
                               "1. Сервер MySQL запущен\n" +
                               "2. База данных 'online_store_db' существует\n" +
                               "3. Пользователь 'root' имеет доступ к базе данных\n\n" +
                               "Приложение будет закрыто.");
            alert.showAndWait();
            javafx.application.Platform.exit();
        });
    }

    @Override
    public void stop() {
        System.out.println("Завершение работы приложения...");
       DatabaseManager.closeConnection();
        System.out.println("Соединение с базой данных закрыто");
    }

    // Метод для тестирования запросов к базе данных
    private void testDatabaseQuery(Connection connection) {
        System.out.println("\n=== ТЕСТИРОВАНИЕ ЗАПРОСОВ К БАЗЕ ДАННЫХ ===");
        
        try {
            // Тестируем запрос к таблице products
            String sql = "SELECT * FROM products LIMIT 1";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    int stockQuantity = rs.getInt("stock_quantity");
                    
                    System.out.println("Тестовый запрос к таблице products успешно выполнен");
                    System.out.println("Получен товар: ID=" + id + ", Название=" + name + 
                                      ", Цена=" + price + ", Количество=" + stockQuantity);
                } else {
                    System.out.println("Тестовый запрос к таблице products выполнен, но товары не найдены");
                }
            }
            
            // Тестируем запрос к таблице orders
            sql = "SELECT COUNT(*) FROM orders";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Тестовый запрос к таблице orders успешно выполнен");
                    System.out.println("Количество заказов в базе данных: " + count);
                }
            }
            
            // Тестируем запрос к таблице order_items
            sql = "SELECT COUNT(*) FROM order_items";
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("Тестовый запрос к таблице order_items успешно выполнен");
                    System.out.println("Количество элементов заказов в базе данных: " + count);
                }
            }
            
            System.out.println("=== ТЕСТИРОВАНИЕ ЗАПРОСОВ К БАЗЕ ДАННЫХ ЗАВЕРШЕНО ===\n");
        } catch (SQLException e) {
            System.out.println("Ошибка при тестировании запросов к базе данных: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
    }

    private void updateDatabaseSchema(Connection connection) {
        System.out.println("Обновление структуры базы данных...");
        
        try {
            // Проверяем, существует ли столбец total_price в таблице orders
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "orders", "total_price");
            
            if (columns.next()) {
                // Столбец total_price существует, меняем его на total_cost
                try (Statement stmt = connection.createStatement()) {
                    String alterOrdersTable = "ALTER TABLE orders CHANGE COLUMN total_price total_cost DECIMAL(10, 2) NOT NULL";
                    stmt.executeUpdate(alterOrdersTable);
                    System.out.println("Таблица orders успешно обновлена (total_price -> total_cost)");
                } catch (SQLException e) {
                    System.out.println("Ошибка при обновлении таблицы orders: " + e.getMessage());
                }
            } else {
                System.out.println("Столбец total_price не найден в таблице orders, обновление не требуется");
            }
            columns.close();
            
            // Проверяем, существует ли столбец price в таблице order_items
            columns = metaData.getColumns(null, null, "order_items", "price");
            
            if (columns.next()) {
                // Столбец price существует, меняем его на price_per_item
                try (Statement stmt = connection.createStatement()) {
                    String alterOrderItemsTable = "ALTER TABLE order_items CHANGE COLUMN price price_per_item DECIMAL(10, 2) NOT NULL";
                    stmt.executeUpdate(alterOrderItemsTable);
                    System.out.println("Таблица order_items успешно обновлена (price -> price_per_item)");
                } catch (SQLException e) {
                    System.out.println("Ошибка при обновлении таблицы order_items: " + e.getMessage());
                }
            } else {
                System.out.println("Столбец price не найден в таблице order_items, обновление не требуется");
            }
            columns.close();
            
            System.out.println("Обновление структуры базы данных завершено");
        } catch (Exception e) {
            System.out.println("Ошибка при обновлении структуры базы данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Запуск JavaFX приложения...");
        launch(args);
    }
} 