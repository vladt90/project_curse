package com.olineshop.dao;

import com.olineshop.model.Order;
import com.olineshop.model.OrderItem;
import com.olineshop.model.Product;
import com.olineshop.model.User;
import com.olineshop.model.Role;
import com.olineshop.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//Класс для работы с заказами в базе данных

public class OrderDAO {
    private UserDAO userDAO = new UserDAO();
    private ProductDAO productDAO = new ProductDAO();

    //Получить все заказы из базы данных
    //return список заказов
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.login, u.first_name, u.last_name, u.email, u.phone, u.discount, " +
                     "u.role_id, r.name as role_name " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.id " +
                     "JOIN roles r ON u.role_id = r.id " +
                     "ORDER BY o.order_date DESC";
        System.out.println("Получение всех заказов из базы данных");

        // Сбрасываем соединение перед выполнением запроса
        com.olineshop.util.DatabaseManager.resetConnectionStatus();

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных");
                return orders;
            }

            System.out.println("Выполнение SQL-запроса: " + sql);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                while (rs.next()) {
                    // Создаем объект Role
                    Role role = new Role(
                        rs.getInt("role_id"),
                        rs.getString("role_name")
                    );
                    
                    // Создаем объект User
                    User user = new User(
                        rs.getInt("user_id"),
                        rs.getString("login"),
                        "", // Пароль не загружаем из соображений безопасности
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getDouble("discount"),
                        role
                    );
                    
                    // Создаем объект Order
                    Order order = new Order(
                        rs.getInt("id"),
                        user,
                        rs.getTimestamp("order_date").toLocalDateTime(),
                        rs.getTimestamp("delivery_date") != null ? rs.getTimestamp("delivery_date").toLocalDateTime() : null,
                        rs.getDouble("total_cost"),
                        rs.getString("status")
                    );
                    
                    // Инициализируем пустой список товаров
                    order.setItems(new ArrayList<>());
                    
                    orders.add(order);
                }
            }
            
            System.out.println("Всего загружено заказов: " + orders.size());
        } catch (SQLException e) {
            System.out.println("Ошибка при получении всех заказов: " + e.getMessage());
            e.printStackTrace();
        }
        return orders;
    }

    //Получить заказы пользователя
    //userId идентификатор пользователя
    //return список заказов пользователя
    public List<Order> getOrdersByUser(int userId) {
        System.out.println("Получение заказов для пользователя с ID: " + userId);
        List<Order> orders = new ArrayList<>();
        
        // Сначала получаем все ID заказов пользователя
        String idSql = "SELECT id FROM orders WHERE user_id = ?";
        List<Integer> orderIds = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных");
                return orders;
            }
            
            System.out.println("Соединение с базой данных получено успешно");
            
            try (PreparedStatement pstmt = conn.prepareStatement(idSql)) {
                pstmt.setInt(1, userId);
                System.out.println("Выполнение SQL-запроса: " + idSql + " с параметром user_id=" + userId);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int orderId = rs.getInt("id");
                        orderIds.add(orderId);
                        System.out.println("Найден ID заказа: " + orderId);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении ID заказов пользователя: " + e.getMessage());
            e.printStackTrace();
            return orders;
        }
        
        System.out.println("Найдено ID заказов: " + orderIds.size());
        
        // Теперь для каждого ID получаем полную информацию о заказе
        for (Integer orderId : orderIds) {
            try {
                Order order = getOrderById(orderId);
                if (order != null) {
                    orders.add(order);
                    System.out.println("Добавлен заказ в список: ID=" + order.getId());
                }
            } catch (Exception e) {
                System.out.println("Ошибка при получении заказа по ID=" + orderId + ": " + e.getMessage());
            }
        }
        
        System.out.println("Всего загружено заказов: " + orders.size());
        return orders;
    }

    //Получить заказ по идентификатору
    //id идентификатор заказа
    //return заказ или null, если заказ не найден
    public Order getOrderById(int id) {
        System.out.println("Получение заказа по ID: " + id);
        String sql = "SELECT * FROM orders WHERE id = ?";
        
        // Сбрасываем соединение перед выполнением запроса
        com.olineshop.util.DatabaseManager.resetConnectionStatus();

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных");
                return null;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                System.out.println("Выполнение SQL-запроса: " + sql + " с параметром id=" + id);
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        // Извлекаем данные из ResultSet
                        int orderId = rs.getInt("id");
                        int userId = rs.getInt("user_id");
                        Timestamp orderDateTimestamp = rs.getTimestamp("order_date");
                        Timestamp deliveryDateTimestamp = rs.getTimestamp("delivery_date");
                        double totalCost = rs.getDouble("total_cost");
                        String status = rs.getString("status");
                        
                        // Закрываем ResultSet перед тем, как делать другие запросы
                        rs.close();
                        
                        // Получаем пользователя
                        User user = userDAO.getUserById(userId);
                        
                        // Создаем объект заказа
                        Order order = new Order(
                            orderId,
                            user,
                            orderDateTimestamp.toLocalDateTime(),
                            deliveryDateTimestamp != null ? deliveryDateTimestamp.toLocalDateTime() : null,
                            totalCost,
                            status
                        );
                        
                        // Инициализируем список товаров
                        order.setItems(new ArrayList<>());
                        
                        System.out.println("Заказ найден: ID=" + orderId + 
                                          ", Пользователь=" + (user != null ? user.getLogin() : "null") + 
                                          ", Дата=" + orderDateTimestamp + 
                                          ", Сумма=" + totalCost + 
                                          ", Статус=" + status);
                        
                        // Загружаем товары для заказа в отдельном соединении
                        try {
                            loadOrderItems(order);
                        } catch (Exception e) {
                            System.out.println("Ошибка при загрузке товаров для заказа ID=" + order.getId() + ": " + e.getMessage());
                            e.printStackTrace();
                        }
                        
                        return order;
                    } else {
                        System.out.println("Заказ с ID=" + id + " не найден");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении заказа по ID=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    //Добавить новый заказ в базу данных
    //order заказ для добавления
    //return true, если заказ успешно добавлен, иначе false
    public boolean addOrder(Order order) {
        // Проверка входных данных
        if (order == null) {
            System.out.println("Ошибка: передан null-заказ");
            return false;
        }
        
        if (order.getUser() == null) {
            System.out.println("Ошибка: пользователь не указан в заказе");
            return false;
        }
        
        if (order.getItems() == null || order.getItems().isEmpty()) {
            System.out.println("Ошибка: список товаров в заказе пуст");
            return false;
        }
        
        // Сбрасываем соединение перед добавлением заказа
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        // Добавляем заказ в базу данных
        String sql = "INSERT INTO orders (user_id, order_date, delivery_date, total_cost, status) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        
        try {
            System.out.println("Получение соединения с базой данных для добавления заказа...");
            conn = DatabaseManager.getConnection();
            
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных");
                return false;
            }
            
            System.out.println("Соединение с базой данных получено успешно");
            
            // Начинаем транзакцию
            System.out.println("Начало транзакции...");
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, order.getUser().getId());
                pstmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
                
                if (order.getDeliveryDate() != null) {
                    pstmt.setTimestamp(3, Timestamp.valueOf(order.getDeliveryDate()));
                } else {
                    pstmt.setNull(3, Types.TIMESTAMP);
                }
                
                // getTotalCost для total_cost в БД
                pstmt.setDouble(4, order.getTotalCost());
                pstmt.setString(5, order.getStatus());

                System.out.println("Выполнение SQL-запроса для добавления заказа");
                
                int affectedRows = pstmt.executeUpdate();
                System.out.println("Затронуто строк при добавлении заказа: " + affectedRows);
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int orderId = generatedKeys.getInt(1);
                            order.setId(orderId);
                            System.out.println("Заказ добавлен с ID: " + orderId);
                            
                            // Добавляем товары в заказ
                            System.out.println("Добавление товаров в заказ...");
                            boolean itemsAdded = addOrderItems(order, conn);
                            
                            if (itemsAdded) {
                                // Если все успешно, фиксируем транзакцию
                                System.out.println("Товары успешно добавлены в заказ, фиксация транзакции...");
                                conn.commit();
                                System.out.println("Транзакция успешно зафиксирована");
                                return true;
                            } else {
                                // Если не удалось добавить товары, откатываем транзакцию
                                System.out.println("Не удалось добавить товары в заказ, откат транзакции...");
                                conn.rollback();
                                System.out.println("Транзакция отменена: не удалось добавить товары в заказ");
                                return false;
                            }
                        } else {
                            // Если не удалось получить ID заказа, откатываем транзакцию
                            System.out.println("Не удалось получить ID добавленного заказа, откат транзакции...");
                            conn.rollback();
                            System.out.println("Транзакция отменена: не удалось получить ID добавленного заказа");
                            return false;
                        }
                    }
                } else {
                    // Если не удалось добавить заказ, откатываем транзакцию
                    System.out.println("Не удалось добавить заказ, откат транзакции...");
                    conn.rollback();
                    System.out.println("Заказ не был добавлен");
                    return false;
                }
            } catch (SQLException e) {
                // В случае ошибки откатываем транзакцию
                if (conn != null) {
                    try {
                        conn.rollback();
                        System.out.println("Транзакция отменена из-за ошибки: " + e.getMessage());
                    } catch (SQLException ex) {
                        System.out.println("Ошибка при откате транзакции: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                
                System.out.println("Ошибка при добавлении заказа: " + e.getMessage());
                System.out.println("SQL State: " + e.getSQLState());
                System.out.println("Error Code: " + e.getErrorCode());
                e.printStackTrace();
                return false;
            } finally {
                // Восстанавливаем автоматическое подтверждение транзакций
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        // Не закрываем соединение здесь, так как оно будет закрыто автоматически
                    } catch (SQLException e) {
                        System.out.println("Ошибка при восстановлении autoCommit: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при получении соединения с базой данных: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //Добавить товары в заказ
    //order заказ с товарами
    //conn активное соединение с базой данных
    //return true, если товары успешно добавлены, иначе false
    private boolean addOrderItems(Order order, Connection conn) {
        System.out.println("Добавление товаров для заказа ID=" + order.getId());
        
        // Проверка на null значения
        if (order == null) {
            System.out.println("Ошибка: передан null-заказ при добавлении товаров");
            return false;
        }
        
        if (conn == null) {
            System.out.println("Ошибка: передано null-соединение при добавлении товаров в заказ");
            return false;
        }
        
        // Проверка на валидный ID заказа
        if (order.getId() <= 0) {
            System.out.println("Ошибка: неверный ID заказа (" + order.getId() + ") при добавлении товаров");
            return false;
        }
        
        // Проверка на наличие товаров в заказе
        if (order.getItems() == null || order.getItems().isEmpty()) {
            System.out.println("Ошибка: список товаров в заказе пуст");
            return false;
        }
        
        System.out.println("Количество товаров в заказе: " + order.getItems().size());
        
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price_per_item) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Добавляем каждый товар в пакет запросов
            for (OrderItem item : order.getItems()) {
                // Проверка на null значения
                if (item == null) {
                    System.out.println("Ошибка: null-элемент в списке товаров заказа");
                    continue;
                }
                
                if (item.getProduct() == null) {
                    System.out.println("Ошибка: товар не определен в элементе заказа");
                    return false;
                }
                
                // Проверка на валидный ID товара
                if (item.getProduct().getId() <= 0) {
                    System.out.println("Ошибка: неверный ID товара (" + item.getProduct().getId() + ") при добавлении в заказ");
                    return false;
                }
                
                // Проверка на валидное количество товара
                if (item.getQuantity() <= 0) {
                    System.out.println("Ошибка: неверное количество товара (" + item.getQuantity() + ") при добавлении в заказ");
                    return false;
                }
                
                // Получаем актуальную информацию о товаре из базы данных, используя то же соединение
                Product freshProduct = productDAO.getProductByIdWithConnection(item.getProduct().getId(), conn);
                if (freshProduct == null) {
                    System.out.println("Ошибка: не удалось получить актуальную информацию о товаре с ID: " + item.getProduct().getId());
                    return false;
                }
                
                // Проверяем наличие на складе
                if (freshProduct.getStockQuantity() < item.getQuantity()) {
                    System.out.println("Ошибка: недостаточно товара на складе. ID=" + freshProduct.getId() + 
                                      ", Доступно=" + freshProduct.getStockQuantity() + 
                                      ", Требуется=" + item.getQuantity());
                    return false;
                }
                
                // Обновляем информацию о товаре в элементе заказа
                item.setProduct(freshProduct);
                
                System.out.println("Подготовка добавления товара: ID=" + item.getProduct().getId() + 
                                  ", Название=" + item.getProduct().getName() + 
                                  ", Количество=" + item.getQuantity() + 
                                  ", Цена=" + item.getPrice());
                
                // Устанавливаем параметры запроса
                pstmt.setInt(1, order.getId());
                pstmt.setInt(2, item.getProduct().getId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getPrice());
                pstmt.addBatch();
                
                System.out.println("Товар добавлен в пакет запросов: ID=" + item.getProduct().getId() + 
                                  ", Название=" + item.getProduct().getName() + 
                                  ", Количество=" + item.getQuantity() + 
                                  ", Цена=" + item.getPrice());
                
                // Уменьшаем количество товара на складе
                Product product = item.getProduct();
                int newQuantity = product.getStockQuantity() - item.getQuantity();
                
                if (newQuantity < 0) {
                    System.out.println("Ошибка: недостаточное количество товара на складе. ID=" + 
                                      product.getId() + ", Доступно=" + product.getStockQuantity() + 
                                      ", Требуется=" + item.getQuantity());
                    return false;
                }
                
                // Обновляем количество товара на складе
                boolean updated = productDAO.updateProductQuantityWithConnection(product.getId(), newQuantity, conn);
                if (!updated) {
                    System.out.println("Ошибка: не удалось обновить количество товара на складе. ID=" + product.getId());
                    return false;
                }
                
                // Обновляем количество в объекте товара в памяти
                product.setStockQuantity(newQuantity);
                System.out.println("Количество товара в памяти обновлено: ID=" + product.getId() + 
                                  ", Новое количество=" + product.getStockQuantity());
            }
            
            // Выполняем пакет запросов на добавление товаров
            System.out.println("Выполнение пакета запросов на добавление товаров...");
            int[] results = pstmt.executeBatch();
            
            // Проверяем результаты выполнения
            boolean allSuccessful = true;
            for (int i = 0; i < results.length; i++) {
                if (results[i] <= 0 && results[i] != Statement.SUCCESS_NO_INFO) {
                    allSuccessful = false;
                    System.out.println("Ошибка при добавлении товара #" + (i+1) + " в заказ");
                }
            }
            
            if (allSuccessful) {
                System.out.println("Все товары успешно добавлены в заказ");
                return true;
            } else {
                System.out.println("Ошибка: не все товары были добавлены в заказ");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при добавлении товаров в заказ: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        }
    }

    //Обновить статус заказа
    //id идентификатор заказа
    //status новый статус заказа
    //return true, если статус успешно обновлен, иначе false
    public boolean updateOrderStatus(int id, String status) {
        System.out.println("Обновление статуса заказа ID=" + id + " на " + status);
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        
        // Сбрасываем соединение перед выполнением запроса
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных при обновлении статуса заказа");
                return false;
            }
            
            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            
            System.out.println("Выполнение SQL-запроса: " + sql);
            System.out.println("Параметры: 1=" + status + ", 2=" + id);
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Затронуто строк: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении статуса заказа: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //Обновить дату доставки заказа
    //id идентификатор заказа
    //deliveryDate новая дата доставки
    //return true, если дата успешно обновлена, иначе false
    public boolean updateDeliveryDate(int id, LocalDateTime deliveryDate) {
        System.out.println("Обновление даты доставки заказа ID=" + id + " на " + deliveryDate);
        String sql = "UPDATE orders SET delivery_date = ? WHERE id = ?";
        
        // Сбрасываем соединение перед выполнением запроса
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных при обновлении даты доставки заказа");
                return false;
            }
            
            if (deliveryDate != null) {
                pstmt.setTimestamp(1, Timestamp.valueOf(deliveryDate));
            } else {
                pstmt.setNull(1, java.sql.Types.TIMESTAMP);
            }
            pstmt.setInt(2, id);
            
            System.out.println("Выполнение SQL-запроса: " + sql);
            System.out.println("Параметры: 1=" + deliveryDate + ", 2=" + id);
            
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Затронуто строк: " + affectedRows);
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.out.println("Ошибка при обновлении даты доставки заказа: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //Удалить заказ из базы данных
    //id идентификатор заказа для удаления
    //return true, если заказ успешно удален, иначе false
    public boolean deleteOrder(int id) {
        System.out.println("Попытка удаления заказа с ID=" + id);
        
        // Сбрасываем соединение перед удалением
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        // Сначала удаляем товары из заказа (из-за внешнего ключа)
        String sqlItems = "DELETE FROM order_items WHERE order_id = ?";
        String sqlOrder = "DELETE FROM orders WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных при удалении заказа");
                return false;
            }
            
            // Начинаем транзакцию
            conn.setAutoCommit(false);
            
            try {
                // Удаляем товары заказа
                try (PreparedStatement pstmtItems = conn.prepareStatement(sqlItems)) {
                    pstmtItems.setInt(1, id);
                    System.out.println("Выполнение SQL-запроса: " + sqlItems + " с параметром order_id=" + id);
                    int itemsDeleted = pstmtItems.executeUpdate();
                    System.out.println("Удалено товаров заказа: " + itemsDeleted);
                }
                
                // Удаляем сам заказ
                try (PreparedStatement pstmtOrder = conn.prepareStatement(sqlOrder)) {
                    pstmtOrder.setInt(1, id);
                    System.out.println("Выполнение SQL-запроса: " + sqlOrder + " с параметром id=" + id);
                    int result = pstmtOrder.executeUpdate();
                    
                    // Если заказ успешно удален, фиксируем транзакцию
                    if (result > 0) {
                        conn.commit();
                        System.out.println("Заказ с ID=" + id + " успешно удален");
                        return true;
                    } else {
                        // Если заказ не найден, откатываем транзакцию
                        conn.rollback();
                        System.out.println("Заказ с ID=" + id + " не найден");
                        return false;
                    }
                }
            } catch (SQLException e) {
                // В случае ошибки откатываем транзакцию
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.out.println("Ошибка при откате транзакции: " + ex.getMessage());
                    ex.printStackTrace();
                }
                System.out.println("Ошибка при удалении заказа с ID=" + id + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            } finally {
                // Восстанавливаем автоматическую фиксацию транзакций
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    System.out.println("Ошибка при восстановлении автоматической фиксации транзакций: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при подключении к базе данных: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //Загрузить товары для заказа
    //order заказ
    private void loadOrderItems(Order order) {
        System.out.println("Загрузка товаров для заказа ID=" + order.getId());
        
        if (order.getItems() == null) {
            System.out.println("Инициализация списка товаров для заказа ID=" + order.getId());
            order.setItems(new ArrayList<>());
        } else {
            // Очищаем существующий список товаров
            order.getItems().clear();
        }
        
        // Используем оптимизированный запрос, который загружает все необходимые данные за один раз
        String sql = "SELECT oi.*, p.* FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "WHERE oi.order_id = ?";

        // Сбрасываем соединение перед выполнением запроса
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        try (Connection conn = DatabaseManager.getConnection()) {
            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных при загрузке товаров для заказа");
                return;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, order.getId());
                System.out.println("Выполнение SQL-запроса для загрузки товаров заказа: " + sql + " с параметром order_id=" + order.getId());
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    int itemCount = 0;
                    while (rs.next()) {
                        // Создаем объект Product из данных ResultSet
                        Product product = new Product(
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getString("unit"),
                            rs.getInt("stock_quantity")
                        );
                        
                        // Создаем объект OrderItem
                        OrderItem item = new OrderItem(
                            order,
                            product,
                            rs.getInt("quantity"),
                            rs.getDouble("price_per_item")
                        );
                        
                        // Добавляем товар в заказ
                        order.getItems().add(item);
                        itemCount++;
                        
                        System.out.println("Найден товар в заказе: ID=" + product.getId() + 
                                          ", Название=" + product.getName() + 
                                          ", Количество=" + item.getQuantity() + 
                                          ", Цена=" + item.getPrice());
                    }
                    System.out.println("Загружено товаров для заказа ID=" + order.getId() + ": " + itemCount);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при загрузке товаров для заказа ID=" + order.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Вспомогательный класс для хранения данных о товарах заказа
    private static class OrderItemData {
        int productId;
        int quantity;
        double price;
        String productName;
        String productUnit;
        
        OrderItemData(int productId, int quantity, double price, String productName, String productUnit) {
            this.productId = productId;
            this.quantity = quantity;
            this.price = price;
            this.productName = productName;
            this.productUnit = productUnit;
        }
    }

    //Извлечь заказ из результата запроса
    //rs результат запроса
    //return заказ
    //throws SQLException если произошла ошибка при работе с базой данных
    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        try {
            int userId = rs.getInt("user_id");
            
            // Получаем пользователя
            User user = userDAO.getUserById(userId);
            
            Order order = new Order(
                    rs.getInt("id"),
                    user,
                    rs.getTimestamp("order_date").toLocalDateTime(),
                    rs.getTimestamp("delivery_date") != null ? rs.getTimestamp("delivery_date").toLocalDateTime() : null,
                    rs.getDouble("total_cost"),
                    rs.getString("status"));
            
            // Инициализируем список товаров
            order.setItems(new ArrayList<>());
            
            return order;
        } catch (SQLException e) {
            System.out.println("Ошибка при извлечении заказа из ResultSet: " + e.getMessage());
            throw e;
        }
    }
} 