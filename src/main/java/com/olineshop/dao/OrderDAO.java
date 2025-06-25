package com.olineshop.dao;

import com.olineshop.model.Order;
import com.olineshop.model.OrderItem;
import com.olineshop.model.Product;
import com.olineshop.model.User;
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
        String sql = "SELECT * FROM orders";
        System.out.println("Получение всех заказов из базы данных");

        // Сбрасываем соединение перед выполнением запроса
        com.olineshop.util.DatabaseManager.resetConnectionStatus();

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            if (conn == null) {
                System.out.println("Ошибка: не удалось получить соединение с базой данных");
                return orders;
            }

            System.out.println("Выполнение SQL-запроса: " + sql);
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                loadOrderItems(order);
                orders.add(order);
            }
            System.out.println("Всего найдено заказов: " + orders.size());
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
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Order order = extractOrderFromResultSet(rs);
                    loadOrderItems(order);
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    //Получить заказ по идентификатору
    //id идентификатор заказа
    //return заказ или null, если заказ не найден
    public Order getOrderById(int id) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Order order = extractOrderFromResultSet(rs);
                    loadOrderItems(order);
                    return order;
                }
            }
        } catch (SQLException e) {
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
        
        // Используем PreparedStatement для вставки товаров
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
    //orderId идентификатор заказа
    //status новый статус
    //return true, если статус успешно обновлен, иначе false
    public boolean updateOrderStatus(int orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, orderId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Обновить дату доставки заказа
    //orderId идентификатор заказа
    //deliveryDate новая дата доставки
    //return true, если дата успешно обновлена, иначе false
    public boolean updateDeliveryDate(int orderId, LocalDateTime deliveryDate) {
        String sql = "UPDATE orders SET delivery_date = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (deliveryDate != null) {
                pstmt.setTimestamp(1, Timestamp.valueOf(deliveryDate));
            } else {
                pstmt.setNull(1, Types.TIMESTAMP);
            }
            
            pstmt.setInt(2, orderId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Удалить заказ из базы данных
    //id идентификатор заказа для удаления
    //return true, если заказ успешно удален, иначе false
    public boolean deleteOrder(int id) {
        // Сначала удаляем товары из заказа (из-за внешнего ключа)
        String sqlItems = "DELETE FROM order_items WHERE order_id = ?";
        String sqlOrder = "DELETE FROM orders WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try (PreparedStatement pstmtItems = conn.prepareStatement(sqlItems)) {
                pstmtItems.setInt(1, id);
                pstmtItems.executeUpdate();
                
                try (PreparedStatement pstmtOrder = conn.prepareStatement(sqlOrder)) {
                    pstmtOrder.setInt(1, id);
                    int affectedRows = pstmtOrder.executeUpdate();
                    
                    conn.commit();
                    return affectedRows > 0;
                }
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                return false;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Загрузить товары для заказа
    //order заказ
    private void loadOrderItems(Order order) {
        String sql = "SELECT oi.*, p.name, p.unit FROM order_items oi " +
                     "JOIN products p ON oi.product_id = p.id " +
                     "WHERE oi.order_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, order.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int productId = rs.getInt("product_id");
                    Product product = productDAO.getProductById(productId);
                    
                    if (product != null) {
                        OrderItem item = new OrderItem(
                                order,
                                product,
                                rs.getInt("quantity"),
                                rs.getDouble("price"));
                        
                        order.getItems().add(item);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Извлечь заказ из результата запроса
    //rs результат запроса
    //return заказ
    //throws SQLException если произошла ошибка при работе с базой данных
    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        User user = userDAO.getUserById(rs.getInt("user_id"));
        
        Order order = new Order(
                rs.getInt("id"),
                user,
                rs.getTimestamp("order_date").toLocalDateTime(),
                rs.getTimestamp("delivery_date") != null ? rs.getTimestamp("delivery_date").toLocalDateTime() : null,
                rs.getDouble("total_cost"),
                rs.getString("status"));
        
        return order;
    }
} 