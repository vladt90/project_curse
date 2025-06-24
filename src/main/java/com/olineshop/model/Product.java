package com.olineshop.model;

public class Product {
    private int id;
    private String name;
    private double price;
    private String unit;
    private int stockQuantity;

    public Product() {
    }


    public Product(int id, String name, double price, String unit, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isInStock() {
        return stockQuantity > 0;
    }

    @Override
    public String toString() {
        return name + " (" + price + " руб./" + unit + ")";
    }
} 