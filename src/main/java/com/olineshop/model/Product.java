package com.olineshop.model;

/**
 * Класс, представляющий товар в системе
 */
public class Product {
    private int id;
    private String name;
    private double price;
    private String unit;
    private int stockQuantity;

    /**
     * Конструктор по умолчанию
     */
    public Product() {
    }

    /**
     * Конструктор с параметрами
     * 
     * @param id            идентификатор товара
     * @param name          название товара
     * @param price         цена товара
     * @param unit          единица измерения (шт, кг, л)
     * @param stockQuantity количество товара на складе
     */
    public Product(int id, String name, double price, String unit, int stockQuantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.unit = unit;
        this.stockQuantity = stockQuantity;
    }

    /**
     * Получить идентификатор товара
     * 
     * @return идентификатор товара
     */
    public int getId() {
        return id;
    }

    /**
     * Установить идентификатор товара
     * 
     * @param id идентификатор товара
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Получить название товара
     * 
     * @return название товара
     */
    public String getName() {
        return name;
    }

    /**
     * Установить название товара
     * 
     * @param name название товара
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Получить цену товара
     * 
     * @return цена товара
     */
    public double getPrice() {
        return price;
    }

    /**
     * Установить цену товара
     * 
     * @param price цена товара
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Получить единицу измерения товара
     * 
     * @return единица измерения товара
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Установить единицу измерения товара
     * 
     * @param unit единица измерения товара
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Получить количество товара на складе
     * 
     * @return количество товара на складе
     */
    public int getStockQuantity() {
        return stockQuantity;
    }

    /**
     * Установить количество товара на складе
     * 
     * @param stockQuantity количество товара на складе
     */
    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    /**
     * Проверить, есть ли товар в наличии
     * 
     * @return true, если товар в наличии, иначе false
     */
    public boolean isInStock() {
        return stockQuantity > 0;
    }

    /**
     * Получить строковое представление товара с указанием единицы измерения
     * 
     * @return строковое представление товара
     */
    @Override
    public String toString() {
        return name + " (" + price + " руб./" + unit + ")";
    }
} 