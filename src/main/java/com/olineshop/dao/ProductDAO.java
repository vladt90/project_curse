package com.olineshop.dao;

import com.olineshop.model.Product;
import com.olineshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

//Класс для работы с товарами в базе данных

public class ProductDAO {

    //Получить все товары из базы данных
    //return список товаров
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY id DESC";
        System.out.println("Получение всех товаров из базы данных");
        
        // Сбрасываем соединение перед выполнением запроса
        com.olineshop.util.DatabaseManager.resetConnectionStatus();

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                System.out.println("Ошибка: соединение с базой данных не установлено");
                return products;
            }
            
            // Устанавливаем ограничение на количество возвращаемых строк для улучшения производительности
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setFetchSize(100); // Оптимизация для больших наборов данных
                
                System.out.println("Выполнение SQL-запроса: " + sql);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        products.add(extractProductFromResultSet(rs));
                    }
                }
            }
            System.out.println("Всего найдено товаров: " + products.size());
        } catch (SQLException e) {
            System.out.println("Ошибка при получении всех товаров: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    //Получить все товары, которые есть в наличии
    //return список товаров в наличии
    public List<Product> getAvailableProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE stock_quantity > 0";
        System.out.println("Получение доступных товаров");

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
            System.out.println("Найдено доступных товаров: " + products.size());
        } catch (SQLException e) {
            System.out.println("Ошибка при получении доступных товаров: " + e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    //Получить товар по идентификатору
    //id идентификатор товара
    //return товар или null, если товар не найден
    public Product getProductById(int id) {
        String sql = "SELECT * FROM products WHERE id = ?";
        System.out.println("Получение товара по ID: " + id);
        
        // Сбрасываем соединение перед выполнением запроса
        com.olineshop.util.DatabaseManager.resetConnectionStatus();

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных при получении товара по ID");
                return null;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                System.out.println("Выполнение SQL-запроса: " + sql + " с параметром id=" + id);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        Product product = extractProductFromResultSet(rs);
                        System.out.println("Товар найден: ID=" + product.getId() + ", Название=" + product.getName());
                        return product;
                    } else {
                        System.out.println("Товар с ID=" + id + " не найден");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении товара по ID=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    //Получить товар по идентификатору, используя существующее соединение
    //id идентификатор товара
    //conn существующее соединение с базой данных
    //return товар или null, если товар не найден
    public Product getProductByIdWithConnection(int id, Connection conn) {
        if (conn == null) {
            System.out.println("Ошибка: передано null-соединение при получении товара по ID");
            return null;
        }
        
        String sql = "SELECT * FROM products WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractProductFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении товара по ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    //Добавить новый товар в базу данных
    //product товар для добавления
    //return true, если товар успешно добавлен, иначе false
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (name, price, unit, stock_quantity) VALUES (?, ?, ?, ?)";
        System.out.println("Добавление товара: " + product.getName() + ", Цена: " + product.getPrice() + 
                          ", Ед.изм.: " + product.getUnit() + ", Количество: " + product.getStockQuantity());

        // Сбрасываем соединение перед добавлением товара
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                System.out.println("Ошибка: соединение с базой данных не установлено");
                return false;
            }

            // Проверка метаданных соединения
            System.out.println("URL подключения: " + conn.getMetaData().getURL());
            System.out.println("Пользователь БД: " + conn.getMetaData().getUserName());
            System.out.println("Версия БД: " + conn.getMetaData().getDatabaseProductVersion());
            
            // Проверяем структуру таблицы products
            if (!checkProductsTableStructure(conn)) {
                System.out.println("Ошибка структуры таблицы products");
                return false;
            }
            
            // Проверяем существование таблицы products
            try (ResultSet tables = conn.getMetaData().getTables(null, null, "products", null)) {
                if (!tables.next()) {
                    System.out.println("Ошибка: таблица 'products' не существует в базе данных");
                    return false;
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, product.getName());
                pstmt.setDouble(2, product.getPrice());
                pstmt.setString(3, product.getUnit());
                pstmt.setInt(4, product.getStockQuantity());

                System.out.println("Выполнение SQL-запроса: " + sql);
                System.out.println("Параметры: 1=" + product.getName() + ", 2=" + product.getPrice() + 
                                  ", 3=" + product.getUnit() + ", 4=" + product.getStockQuantity());
                
                // Добавляем дополнительную проверку
                try {
                    int affectedRows = pstmt.executeUpdate();
                    System.out.println("Затронуто строк: " + affectedRows);
                    
                    if (affectedRows > 0) {
                        try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                int id = generatedKeys.getInt(1);
                                product.setId(id);
                                System.out.println("Товар успешно добавлен с ID: " + id);
                                return true;
                            } else {
                                System.out.println("Не удалось получить ID добавленного товара");
                            }
                        }
                    }
                    System.out.println("Товар не был добавлен");
                    return false;
                } catch (SQLException e) {
                    System.out.println("Ошибка при выполнении запроса: " + e.getMessage());
                    System.out.println("SQL State: " + e.getSQLState());
                    System.out.println("Error Code: " + e.getErrorCode());
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении товара: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    // Метод для проверки структуры таблицы products
    private boolean checkProductsTableStructure(Connection conn) {
        try {
            System.out.println("Проверка структуры таблицы products...");
            
            // Проверяем существование таблицы
            try (ResultSet tables = conn.getMetaData().getTables(null, null, "products", null)) {
                if (!tables.next()) {
                    System.out.println("Таблица 'products' не существует в базе данных");
                    
                    // Попробуем создать таблицу
                    try (Statement stmt = conn.createStatement()) {
                        String createTableSQL = "CREATE TABLE IF NOT EXISTS `products` (" +
                                              "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                                              "`name` VARCHAR(100) NOT NULL," +
                                              "`price` DECIMAL(10, 2) NOT NULL," +
                                              "`unit` VARCHAR(20) NOT NULL DEFAULT 'шт'," +
                                              "`stock_quantity` INT NOT NULL DEFAULT 0)";
                        System.out.println("Создание таблицы products: " + createTableSQL);
                        stmt.execute(createTableSQL);
                        System.out.println("Таблица products создана");
                    }
                    return true;
                } else {
                    System.out.println("Таблица 'products' существует в базе данных");
                }
            }
            
            // Проверяем структуру таблицы
            try (ResultSet columns = conn.getMetaData().getColumns(null, null, "products", null)) {
                boolean hasId = false;
                boolean hasName = false;
                boolean hasPrice = false;
                boolean hasUnit = false;
                boolean hasStockQuantity = false;
                
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    
                    System.out.println("Колонка: " + columnName + ", Тип: " + columnType + ", Размер: " + columnSize);
                    
                    switch (columnName.toLowerCase()) {
                        case "id":
                            hasId = true;
                            break;
                        case "name":
                            hasName = true;
                            break;
                        case "price":
                            hasPrice = true;
                            break;
                        case "unit":
                            hasUnit = true;
                            break;
                        case "stock_quantity":
                            hasStockQuantity = true;
                            break;
                    }
                }
                
                // Проверяем, все ли необходимые колонки присутствуют
                if (!hasId || !hasName || !hasPrice || !hasUnit || !hasStockQuantity) {
                    System.out.println("Структура таблицы products некорректна. Отсутствуют колонки:");
                    if (!hasId) System.out.println("- id");
                    if (!hasName) System.out.println("- name");
                    if (!hasPrice) System.out.println("- price");
                    if (!hasUnit) System.out.println("- unit");
                    if (!hasStockQuantity) System.out.println("- stock_quantity");
                    
                    // Пробуем пересоздать таблицу
                    try (Statement stmt = conn.createStatement()) {
                        // Сначала удаляем таблицу, если она существует
                        String dropTableSQL = "DROP TABLE IF EXISTS products";
                        System.out.println("Удаление таблицы products: " + dropTableSQL);
                        stmt.execute(dropTableSQL);
                        
                        // Затем создаем таблицу заново
                        String createTableSQL = "CREATE TABLE `products` (" +
                                              "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                                              "`name` VARCHAR(100) NOT NULL," +
                                              "`price` DECIMAL(10, 2) NOT NULL," +
                                              "`unit` VARCHAR(20) NOT NULL DEFAULT 'шт'," +
                                              "`stock_quantity` INT NOT NULL DEFAULT 0)";
                        System.out.println("Пересоздание таблицы products: " + createTableSQL);
                        stmt.execute(createTableSQL);
                        System.out.println("Таблица products пересоздана");
                    }
                    return true;
                }
                
                System.out.println("Структура таблицы products корректна");
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке структуры таблицы products: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    //Обновить товар в базе данных
    //product товар для обновления
    //return true, если товар успешно обновлен, иначе false
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, price = ?, unit = ?, stock_quantity = ? WHERE id = ?";
        System.out.println("Обновление товара: ID=" + product.getId() + ", Название=" + product.getName() + 
                          ", Цена=" + product.getPrice() + ", Ед.изм.=" + product.getUnit() + 
                          ", Количество=" + product.getStockQuantity());

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.out.println("Ошибка: соединение с базой данных не установлено");
                return false;
            }

            pstmt.setString(1, product.getName());
            pstmt.setDouble(2, product.getPrice());
            pstmt.setString(3, product.getUnit());
            pstmt.setInt(4, product.getStockQuantity());
            pstmt.setInt(5, product.getId());

            System.out.println("Выполнение SQL-запроса: " + sql);
            System.out.println("Параметры: 1=" + product.getName() + ", 2=" + product.getPrice() + 
                              ", 3=" + product.getUnit() + ", 4=" + product.getStockQuantity() + 
                              ", 5=" + product.getId());
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Затронуто строк: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении товара: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //Обновить количество товара на складе
    //productId идентификатор товара
    //newQuantity новое количество товара
    //return true, если количество успешно обновлено, иначе false
    public boolean updateProductQuantity(int productId, int newQuantity) {
        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        System.out.println("Обновление количества товара: ID=" + productId + ", Новое количество=" + newQuantity);

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.out.println("Ошибка: соединение с базой данных не установлено");
                return false;
            }

            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, productId);

            System.out.println("Выполнение SQL-запроса: " + sql);
            System.out.println("Параметры: 1=" + newQuantity + ", 2=" + productId);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Затронуто строк: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении количества товара: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //Обновить количество товара на складе с использованием существующего соединения
    //productId идентификатор товара
    //newQuantity новое количество товара
    //conn существующее соединение с базой данных
    //return true, если количество успешно обновлено, иначе false
    public boolean updateProductQuantityWithConnection(int productId, int newQuantity, Connection conn) {
        if (conn == null) {
            System.out.println("Ошибка: передано null-соединение при обновлении количества товара");
            return false;
        }
        
        // Проверка на валидный ID товара
        if (productId <= 0) {
            System.out.println("Ошибка: неверный ID товара (" + productId + ") при обновлении количества");
            return false;
        }
        
        // Проверка на валидное количество товара
        if (newQuantity < 0) {
            System.out.println("Ошибка: отрицательное количество товара (" + newQuantity + ") при обновлении");
            return false;
        }
        
        // Сначала проверяем существование товара с указанным ID
        String checkSql = "SELECT id FROM products WHERE id = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, productId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Ошибка: товар с ID=" + productId + " не найден в базе данных");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке существования товара: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
        
        String sql = "UPDATE products SET stock_quantity = ? WHERE id = ?";
        System.out.println("Обновление количества товара с существующим соединением: ID=" + productId + ", Новое количество=" + newQuantity);

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, productId);

            System.out.println("Выполнение SQL-запроса: " + sql);
            System.out.println("Параметры: 1=" + newQuantity + ", 2=" + productId);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Затронуто строк: " + affectedRows);
            
            if (affectedRows == 0) {
                System.out.println("Предупреждение: не обновлено ни одной строки при обновлении количества товара с ID=" + productId);
                return false;
            }
            
            return true;
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении количества товара: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    //Удалить товар из базы данных
    //id идентификатор товара для удаления
    //return true, если товар успешно удален, иначе false
    public boolean deleteProduct(int id) {
        String sql = "DELETE FROM products WHERE id = ?";
        System.out.println("Удаление товара с ID=" + id);
        
        // Сбрасываем соединение перед удалением
        com.olineshop.util.DatabaseManager.resetConnectionStatus();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.out.println("Ошибка: соединение с базой данных не установлено");
                return false;
            }

            pstmt.setInt(1, id);
            
            System.out.println("Выполнение SQL-запроса: " + sql);
            System.out.println("Параметр: id=" + id);
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Затронуто строк: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Ошибка при удалении товара: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    //Извлечь товар из результата запроса
    //rs результат запроса
    //return товар
    //throws SQLException если произошла ошибка при работе с базой данных
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        String unit = rs.getString("unit");
        int stockQuantity = rs.getInt("stock_quantity");
        
        System.out.println("Извлечен товар: ID=" + id + ", Название=" + name + 
                          ", Цена=" + price + ", Ед.изм.=" + unit + ", Количество=" + stockQuantity);
        
        return new Product(id, name, price, unit, stockQuantity);
    }

    //Получить список товаров по их идентификаторам
    //productIds список идентификаторов товаров
    //return список найденных товаров
    public List<Product> getProductsByIds(List<Integer> productIds) {
        List<Product> products = new ArrayList<>();
        if (productIds == null || productIds.isEmpty()) {
            return products;
        }
        
        // Создаем SQL запрос с параметрами IN
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE id IN (");
        for (int i = 0; i < productIds.size(); i++) {
            sql.append("?");
            if (i < productIds.size() - 1) {
                sql.append(",");
            }
        }
        sql.append(")");
        
        System.out.println("Получение товаров по ID: " + productIds);
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // Устанавливаем параметры запроса
            for (int i = 0; i < productIds.size(); i++) {
                pstmt.setInt(i + 1, productIds.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Product product = extractProductFromResultSet(rs);
                    products.add(product);
                    System.out.println("Найден товар: ID=" + product.getId() + 
                                      ", Название=" + product.getName() + 
                                      ", Количество=" + product.getStockQuantity());
                }
            }
            
            System.out.println("Всего найдено товаров: " + products.size());
        } catch (SQLException e) {
            System.out.println("Ошибка при получении товаров по ID: " + e.getMessage());
            e.printStackTrace();
        }
        
        return products;
    }
} 