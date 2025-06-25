package com.olineshop.util;

/**
 * Класс для пересоздания базы данных
 */
public class DatabaseRecreator {
    
    /**
     * Точка входа для запуска пересоздания базы данных
     */
    public static void main(String[] args) {
        System.out.println("Начало пересоздания базы данных...");
        DatabaseCleaner.recreateDatabase();
        System.out.println("Пересоздание базы данных завершено");
    }
} 