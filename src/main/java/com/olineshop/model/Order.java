package com.olineshop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс, представляющий заказ в системе
 */
public class Order {
    private int id;
    private User user;
    private LocalDateTime orderDate;
    private LocalDateTime deliveryDate;
    private double totalCost;
    private String status;
    private List<OrderItem> items;

    /**
     * Конструктор по умолчанию
     */
    public Order() {
        this.items = new ArrayList<>();
        this.orderDate = LocalDateTime.now();
        this.status = "В обработке";
    }

    /**
     * Конструктор с параметрами
     * 
     * @param id           идентификатор заказа
     * @param user         пользователь, оформивший заказ
     * @param orderDate    дата оформления заказа
     * @param deliveryDate дата доставки заказа
     * @param totalCost    общая стоимость заказа
     * @param status       статус заказа
     */
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

    /**
     * Получить идентификатор заказа
     * 
     * @return идентификатор заказа
     */
    public int getId() {
        return id;
    }

    /**
     * Установить идентификатор заказа
     * 
     * @param id идентификатор заказа
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Получить пользователя, оформившего заказ
     * 
     * @return пользователь, оформивший заказ
     */
    public User getUser() {
        return user;
    }

    /**
     * Установить пользователя, оформившего заказ
     * 
     * @param user пользователь, оформивший заказ
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Получить дату оформления заказа
     * 
     * @return дата оформления заказа
     */
    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    /**
     * Установить дату оформления заказа
     * 
     * @param orderDate дата оформления заказа
     */
    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    /**
     * Получить дату доставки заказа
     * 
     * @return дата доставки заказа
     */
    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * Установить дату доставки заказа
     * 
     * @param deliveryDate дата доставки заказа
     */
    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * Получить общую стоимость заказа
     * 
     * @return общая стоимость заказа
     */
    public double getTotalCost() {
        return totalCost;
    }

    /**
     * Установить общую стоимость заказа
     * 
     * @param totalCost общая стоимость заказа
     */
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    /**
     * Получить статус заказа
     * 
     * @return статус заказа
     */
    public String getStatus() {
        return status;
    }

    /**
     * Установить статус заказа
     * 
     * @param status статус заказа
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Получить список товаров в заказе
     * 
     * @return список товаров в заказе
     */
    public List<OrderItem> getItems() {
        return items;
    }

    /**
     * Установить список товаров в заказе
     * 
     * @param items список товаров в заказе
     */
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    /**
     * Добавить товар в заказ
     * 
     * @param item товар для добавления в заказ
     */
    public void addItem(OrderItem item) {
        items.add(item);
        recalculateTotalCost();
    }

    /**
     * Удалить товар из заказа
     * 
     * @param item товар для удаления из заказа
     * @return true, если товар был удален, иначе false
     */
    public boolean removeItem(OrderItem item) {
        boolean removed = items.remove(item);
        if (removed) {
            recalculateTotalCost();
        }
        return removed;
    }

    /**
     * Пересчитать общую стоимость заказа на основе товаров в заказе и скидки
     * пользователя
     */
    public void recalculateTotalCost() {
        double subtotal = 0.0;
        for (OrderItem item : items) {
            subtotal += item.getPrice() * item.getQuantity();
        }

        // Применяем скидку пользователя
        double discount = user != null ? user.getDiscount() : 0.0;
        
        // Применяем дополнительную скидку в зависимости от суммы заказа
        double additionalDiscount = 0.0;
        if (subtotal > 10000) {
            additionalDiscount = 0.05; // 5% скидка при заказе от 10000 руб.
        } else if (subtotal > 5000) {
            additionalDiscount = 0.03; // 3% скидка при заказе от 5000 руб.
        }

        // Итоговая скидка (не может быть больше 10%)
        double totalDiscount = Math.min(discount + additionalDiscount, 0.1);
        
        // Применяем скидку к итоговой стоимости
        this.totalCost = subtotal * (1 - totalDiscount);
    }

    @Override
    public String toString() {
        return "Заказ #" + id + " от " + orderDate + " (" + status + ")";
    }
} 