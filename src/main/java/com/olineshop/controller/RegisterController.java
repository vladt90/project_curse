package com.olineshop.controller;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.olineshop.dao.RoleDAO;
import com.olineshop.dao.UserDAO;
import com.olineshop.model.Role;
import com.olineshop.model.User;
import com.olineshop.util.DatabaseManager;
import com.olineshop.view.LoginView;
import com.olineshop.view.RegisterView;

import java.util.List;

//Контроллер для окна регистрации пользователей

public class RegisterController {
    private final RegisterView view;
    private final Stage primaryStage;
    private final UserDAO userDAO;
    private final RoleDAO roleDAO;

    //Конструктор контроллера
    //view представление окна регистрации
    //primaryStage главное окно приложения
    public RegisterController(RegisterView view, Stage primaryStage) {
        this.view = view;
        this.primaryStage = primaryStage;
        this.userDAO = new UserDAO();
        this.roleDAO = new RoleDAO();
    }

    //нажатие на кнопку Зарегистрироваться
    public void handleRegister(String login, String password, String confirmPassword,
            String firstName, String lastName, String email, String phone) {
        System.out.println("Начало регистрации");
        
        // Проверяем подключение к базе данных
        if (DatabaseManager.isConnectionFailed()) {
            System.out.println("Ошибка подключения к базе данных: " + DatabaseManager.getLastErrorMessage());
            view.showAlert(Alert.AlertType.ERROR, "Ошибка подключения", 
                    "Не удалось подключиться к базе данных. Проверьте настройки подключения.");
            return;
        }
        
        // поля не пустые?
        if (login == null || login.trim().isEmpty()) {
            System.out.println("Ошибка: пустой логин");
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите логин");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            System.out.println("Ошибка: пустой пароль");
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите пароль");
            return;
        }
        
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            System.out.println("Ошибка: пустое подтверждение пароля");
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Подтвердите пароль");
            return;
        }
        
        if (firstName == null || firstName.trim().isEmpty()) {
            System.out.println("Ошибка: пустое имя");
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите имя");
            return;
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            System.out.println("Ошибка: пустая фамилия");
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите фамилию");
            return;
        }
        
        if (email == null || email.trim().isEmpty()) {
            System.out.println("Ошибка: пустой email");
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите email");
            return;
        }
        
        // пароли совпадают?
        if (!password.equals(confirmPassword)) {
            System.out.println("Ошибка: пароли не совпадают");
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Пароли не совпадают");
            return;
        }
        
        System.out.println("Проверка существующего пользователя");
        User existingUser = userDAO.getUserByLogin(login);
        if (existingUser != null) {
            System.out.println("Ошибка: логин уже занят");
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Логин уже занят");
            return;
        }
        
        System.out.println("Получение роли клиента");
        
       
        DatabaseManager.resetConnectionStatus();

        // Получаем роль "Клиент" напрямую из базы данных
        Role clientRole = roleDAO.getRoleByName("Клиент");
        
        if (clientRole == null) {
            System.out.println("Не удалось найти роль 'Клиент', пробуем получить все роли");
            
            // Пробуем создать стандартные роли, если они не существуют
            roleDAO.createDefaultRoles();
            
            // Повторно пытаемся получить роль клиента
            clientRole = roleDAO.getRoleByName("Клиент");
            
            if (clientRole == null) {
                List<Role> allRoles = roleDAO.getAllRoles();
                if (allRoles == null || allRoles.isEmpty()) {
                    System.out.println("Ошибка: не удалось получить список ролей из базы данных");
                    view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось получить список ролей. Проверьте подключение к базе данных.");
                    return;
                }
                
                System.out.println("Доступные роли:");
                for (Role role : allRoles) {
                    System.out.println("ID: " + role.getId() + ", Название: " + role.getName());
                }
                
                // пробуем по ID
                clientRole = roleDAO.getRoleById(2);
                
                if (clientRole == null && !allRoles.isEmpty()) {
                    System.out.println("Не удалось найти роль клиента по ID. Пробуем найти подходящую роль.");
                    for (Role role : allRoles) {
                        if (role.getName().toLowerCase().contains("клиент") || 
                            role.getName().toLowerCase().contains("client") ||
                            role.getName().toLowerCase().contains("user")) {
                            clientRole = role;
                            System.out.println("Найдена подходящая роль: " + role.getName());
                            break;
                        }
                    }
                    
                    // Если не нашли подходящую, берем первую доступную
                    if (clientRole == null) {
                        clientRole = allRoles.get(0);
                        System.out.println("Используем первую доступную роль: " + clientRole.getName());
                    }
                }
            }
        }
        
        if (clientRole == null) {
            System.out.println("Ошибка: не удалось получить роль пользователя");
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось получить роль пользователя. Проверьте настройки базы данных.");
            return;
        }
        
        System.out.println("Создание нового пользователя с ролью: ID=" + clientRole.getId() + ", Название=" + clientRole.getName());
        User newUser = new User();
        newUser.setLogin(login);
        newUser.setPasswordHash(password);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setDiscount(0.0);
        newUser.setRole(clientRole);
        
        System.out.println("Добавление пользователя в базу данных");
        boolean success = userDAO.addUser(newUser);
        
        System.out.println("Результат регистрации: " + (success ? "успешно" : "ошибка"));
        if (success) {
            view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Регистрация выполнена успешно");
            showLoginWindow();
        } else {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось зарегистрировать пользователя");
        }
    }

    public void showLoginWindow() {
        primaryStage.close();
        
        LoginView loginView = new LoginView();
        loginView.start(new Stage());
    }
} 