package com.olineshop.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderChecker {
    public static void main(String[] args) {
        System.out.println("Проверка заказов в базе данных...");
        
        try (Connection conn = DatabaseManager.getConnection()) {
            // Проверка всех заказов
            String sql = "SELECT * FROM orders";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                System.out.println("Все заказы в базе данных:");
                System.out.println("ID | User ID | Order Date | Delivery Date | Total Cost | Status");
                System.out.println("----------------------------------------------------------");
                
                boolean hasOrders = false;
                while (rs.next()) {
                    hasOrders = true;
                    int id = rs.getInt("id");
                    int userId = rs.getInt("user_id");
                    String orderDate = rs.getTimestamp("order_date").toString();
                    String deliveryDate = rs.getTimestamp("delivery_date") != null ? 
                                         rs.getTimestamp("delivery_date").toString() : "NULL";
                    double totalCost = rs.getDouble("total_cost");
                    String status = rs.getString("status");
                    
                    System.out.println(id + " | " + userId + " | " + orderDate + " | " + 
                                      deliveryDate + " | " + totalCost + " | " + status);
                }
                
                if (!hasOrders) {
                    System.out.println("Нет заказов в базе данных.");
                }
            }
            
            // Проверка заказов пользователя с ID = 3
            String userSql = "SELECT * FROM orders WHERE user_id = 3";
            try (PreparedStatement pstmt = conn.prepareStatement(userSql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                System.out.println("\nЗаказы пользователя с ID = 3:");
                System.out.println("ID | User ID | Order Date | Delivery Date | Total Cost | Status");
                System.out.println("----------------------------------------------------------");
                
                boolean hasOrders = false;
                while (rs.next()) {
                    hasOrders = true;
                    int id = rs.getInt("id");
                    int userId = rs.getInt("user_id");
                    String orderDate = rs.getTimestamp("order_date").toString();
                    String deliveryDate = rs.getTimestamp("delivery_date") != null ? 
                                         rs.getTimestamp("delivery_date").toString() : "NULL";
                    double totalCost = rs.getDouble("total_cost");
                    String status = rs.getString("status");
                    
                    System.out.println(id + " | " + userId + " | " + orderDate + " | " + 
                                      deliveryDate + " | " + totalCost + " | " + status);
                }
                
                if (!hasOrders) {
                    System.out.println("Нет заказов для пользователя с ID = 3.");
                }
            }
            
            // Проверка товаров в заказах
            String itemsSql = "SELECT oi.*, p.name FROM order_items oi JOIN products p ON oi.product_id = p.id";
            try (PreparedStatement pstmt = conn.prepareStatement(itemsSql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                System.out.println("\nТовары в заказах:");
                System.out.println("Order ID | Product ID | Product Name | Quantity | Price Per Item");
                System.out.println("----------------------------------------------------------");
                
                boolean hasItems = false;
                while (rs.next()) {
                    hasItems = true;
                    int orderId = rs.getInt("order_id");
                    int productId = rs.getInt("product_id");
                    String productName = rs.getString("name");
                    int quantity = rs.getInt("quantity");
                    double pricePerItem = rs.getDouble("price_per_item");
                    
                    System.out.println(orderId + " | " + productId + " | " + productName + " | " + 
                                      quantity + " | " + pricePerItem);
                }
                
                if (!hasItems) {
                    System.out.println("Нет товаров в заказах.");
                }
            }
            
            // Проверка пользователей
            String usersSql = "SELECT id, login FROM users";
            try (PreparedStatement pstmt = conn.prepareStatement(usersSql);
                 ResultSet rs = pstmt.executeQuery()) {
                
                System.out.println("\nПользователи:");
                System.out.println("ID | Login");
                System.out.println("----------------------------------------------------------");
                
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String login = rs.getString("login");
                    
                    System.out.println(id + " | " + login);
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке заказов: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 