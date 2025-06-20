package com.olineshop.model;

/**
 * Класс, представляющий пользователя в системе
 */
public class User {
    private int id;
    private String login;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private double discount;
    private Role role;

    /**
     * Конструктор по умолчанию
     */
    public User() {
        this.discount = 0.0; // По умолчанию скидка 0%
    }

    /**
     * Конструктор с параметрами
     * 
     * @param id           идентификатор пользователя
     * @param login        логин
     * @param passwordHash хеш пароля
     * @param firstName    имя
     * @param lastName     фамилия
     * @param email        электронная почта
     * @param phone        телефон
     * @param discount     скидка (в виде десятичной дроби, например 0.02 для 2%)
     * @param role         роль пользователя
     */
    public User(int id, String login, String passwordHash, String firstName, String lastName, String email, String phone,
            double discount, Role role) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.discount = discount;
        this.role = role;
    }

    /**
     * Получить идентификатор пользователя
     * 
     * @return идентификатор пользователя
     */
    public int getId() {
        return id;
    }

    /**
     * Установить идентификатор пользователя
     * 
     * @param id идентификатор пользователя
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Получить логин пользователя
     * 
     * @return логин пользователя
     */
    public String getLogin() {
        return login;
    }

    /**
     * Установить логин пользователя
     * 
     * @param login логин пользователя
     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * Получить хеш пароля пользователя
     * 
     * @return хеш пароля пользователя
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Установить хеш пароля пользователя
     * 
     * @param passwordHash хеш пароля пользователя
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Получить имя пользователя
     * 
     * @return имя пользователя
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Установить имя пользователя
     * 
     * @param firstName имя пользователя
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Получить фамилию пользователя
     * 
     * @return фамилия пользователя
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Установить фамилию пользователя
     * 
     * @param lastName фамилия пользователя
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Получить полное имя пользователя
     * 
     * @return полное имя пользователя (имя + фамилия)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Получить электронную почту пользователя
     * 
     * @return электронная почта пользователя
     */
    public String getEmail() {
        return email;
    }

    /**
     * Установить электронную почту пользователя
     * 
     * @param email электронная почта пользователя
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Получить телефон пользователя
     * 
     * @return телефон пользователя
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Установить телефон пользователя
     * 
     * @param phone телефон пользователя
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Получить скидку пользователя
     * 
     * @return скидка пользователя (в виде десятичной дроби)
     */
    public double getDiscount() {
        return discount;
    }

    /**
     * Установить скидку пользователя
     * 
     * @param discount скидка пользователя (в виде десятичной дроби)
     */
    public void setDiscount(double discount) {
        this.discount = discount;
    }

    /**
     * Получить роль пользователя
     * 
     * @return роль пользователя
     */
    public Role getRole() {
        return role;
    }

    /**
     * Установить роль пользователя
     * 
     * @param role роль пользователя
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Проверить, является ли пользователь администратором
     * 
     * @return true, если пользователь является администратором, иначе false
     */
    public boolean isAdmin() {
        return role != null && "Администратор".equals(role.getName());
    }

    @Override
    public String toString() {
        return getFullName();
    }
} 