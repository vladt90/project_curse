package com.olineshop.model;

/**
 * Класс, представляющий роль пользователя в системе
 */
public class Role {
    private int id;
    private String name;

    /**
     * Конструктор по умолчанию
     */
    public Role() {
    }

    /**
     * Конструктор с параметрами
     * 
     * @param id   идентификатор роли
     * @param name название роли
     */
    public Role(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Получить идентификатор роли
     * 
     * @return идентификатор роли
     */
    public int getId() {
        return id;
    }

    /**
     * Установить идентификатор роли
     * 
     * @param id идентификатор роли
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Получить название роли
     * 
     * @return название роли
     */
    public String getName() {
        return name;
    }

    /**
     * Установить название роли
     * 
     * @param name название роли
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
} 