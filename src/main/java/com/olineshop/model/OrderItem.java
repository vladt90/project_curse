package com.olineshop.model;

public class OrderItem {
    private Order order;
    private Product product;
    private int quantity;
    private double price;
    public OrderItem() {
    }


    public OrderItem(Order order, Product product, int quantity, double price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }


    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.price = product.getPrice();
    }


    public Order getOrder() {
        return order;
    }


    public void setOrder(Order order) {
        this.order = order;
    }


    public Product getProduct() {
        return product;
    }


    public void setProduct(Product product) {
        this.product = product;
    }


    public int getQuantity() {
        return quantity;
    }


    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    public double getPrice() {
        return price;
    }


    public void setPrice(double price) {
        this.price = price;
    }


    public double getSubtotal() {
        return price * quantity;
    }

    @Override
    public String toString() {
        return product.getName() + " x " + quantity + " = " + getSubtotal() + " руб.";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OrderItem other = (OrderItem) obj;
        // Сравниваем только по ID продукта
        return product != null && other.product != null && 
               product.getId() == other.product.getId();
    }
    
    @Override
    public int hashCode() {
        return product != null ? product.getId() : 0;
    }
} 