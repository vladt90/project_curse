package com.olineshop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private User user;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private double totalCost;
    private String status;
    private List<OrderItem> items;

    public Order() {
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
        this.status = "Новый";
    }


    public Order(int id, User user, LocalDateTime orderDate, LocalDateTime deliveryDate, double totalCost,
            String status) {
        this.id = id;
        this.user = user;
        this.orderDate = orderDate;
        this.deliveryDate = deliveryDate;
        this.totalCost = totalCost;
        this.status = status;
        this.items = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        recalculateTotalCost();
    }

    public boolean removeItem(OrderItem item) {
        boolean removed = items.remove(item);
        if (removed) {
            recalculateTotalCost();
        }
        return removed;
    }

    public void recalculateTotalCost() {
        double subtotal = 0.0;
        for (OrderItem item : items) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        double discount = user != null ? user.getDiscount() : 0.0;
        
        this.totalCost = subtotal * (1 - discount);
    }

    @Override
    public String toString() {
        return "Заказ #" + id + " от " + orderDate.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) + " (" + status + ")";
    }
} 