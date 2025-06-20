package com.olineshop.model;

/**
 * Класс, представляющий позицию товара в заказе
 */
public class OrderItem {
    private Order order;
    private Product product;
    private int quantity;
    private double price; // Цена на момент покупки

    /**
     * Конструктор по умолчанию
     */
    public OrderItem() {
    }

    /**
     * Конструктор с параметрами
     * 
     * @param order    заказ
     * @param product  товар
     * @param quantity количество товара
     * @param price    цена товара на момент покупки
     */
    public OrderItem(Order order, Product product, int quantity, double price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    /**
     * Конструктор для создания позиции заказа из товара
     * 
     * @param product  товар
     * @param quantity количество товара
     */
    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice(); // Берем текущую цену товара
    }

    /**
     * Получить заказ
     * 
     * @return заказ
     */
    public Order getOrder() {
        return order;
    }

    /**
     * Установить заказ
     * 
     * @param order заказ
     */
    public void setOrder(Order order) {
        this.order = order;
    }

    /**
     * Получить товар
     * 
     * @return товар
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Установить товар
     * 
     * @param product товар
     */
    public void setProduct(Product product) {
        this.product = product;
    }

    /**
     * Получить количество товара
     * 
     * @return количество товара
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Установить количество товара
     * 
     * @param quantity количество товара
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Получить цену товара на момент покупки
     * 
     * @return цена товара на момент покупки
     */
    public double getPrice() {
        return price;
    }

    /**
     * Установить цену товара на момент покупки
     * 
     * @param price цена товара на момент покупки
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Получить стоимость позиции заказа (цена * количество)
     * 
     * @return стоимость позиции заказа
     */
    public double getSubtotal() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return product.getName() + " x " + quantity + " = " + getSubtotal() + " руб.";
    }
} 