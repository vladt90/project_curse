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

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        } catch (SQLException e) {
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
        String sql = "INSERT INTO orders (user_id, order_date, delivery_date, total_cost, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, order.getUser().getId());
            pstmt.setTimestamp(2, Timestamp.valueOf(order.getOrderDate()));
            
            if (order.getDeliveryDate() != null) {
                pstmt.setTimestamp(3, Timestamp.valueOf(order.getDeliveryDate()));
            } else {
                pstmt.setNull(3, Types.TIMESTAMP);
            }
            
            pstmt.setDouble(4, order.getTotalCost());
            pstmt.setString(5, order.getStatus());

            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int orderId = generatedKeys.getInt(1);
                        order.setId(orderId);
                        
                        // Добавляем товары в заказ
                        return addOrderItems(order);
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Добавить товары в заказ
    //order заказ с товарами
    //return true, если товары успешно добавлены, иначе false
    private boolean addOrderItems(Order order) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price_per_item) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (OrderItem item : order.getItems()) {
                pstmt.setInt(1, order.getId());
                pstmt.setInt(2, item.getProduct().getId());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setDouble(4, item.getPrice());
                pstmt.addBatch();
                
                // Уменьшаем количество товара на складе
                Product product = item.getProduct();
                int newQuantity = product.getStockQuantity() - item.getQuantity();
                productDAO.updateProductQuantity(product.getId(), newQuantity);
            }
            
            int[] affectedRows = pstmt.executeBatch();
            return affectedRows.length == order.getItems().size();
        } catch (SQLException e) {
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
                                rs.getDouble("price_per_item"));
                        
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
        int userId = rs.getInt("user_id");
        User user = userDAO.getUserById(userId);
        
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