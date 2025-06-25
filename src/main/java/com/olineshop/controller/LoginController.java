package com.olineshop.controller;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.olineshop.dao.UserDAO;
import com.olineshop.model.User;
import com.olineshop.view.LoginView;
import com.olineshop.view.MainAdminView;
import com.olineshop.view.MainClientView;
import com.olineshop.view.RegisterView;
import com.olineshop.dao.RoleDAO;
import com.olineshop.model.Role;
import com.olineshop.util.DatabaseManager;

import java.util.List;

//Контроллер для окна входа в систему

public class LoginController {
    private final LoginView view;
    private final Stage primaryStage;
    private final UserDAO userDAO;
    private final RoleDAO roleDAO;
    
    // Жестко заданные учетные данные администратора
    private static final String ADMIN_LOGIN = "admin";
    private static final String ADMIN_PASSWORD = "admin";

    //Конструктор контроллера
    //view представление окна входа
    //primaryStage главное окно приложения
    public LoginController(LoginView view, Stage primaryStage) {
        this.view = view;
        this.primaryStage = primaryStage;
        this.userDAO = new UserDAO();
        this.roleDAO = new RoleDAO();
        System.out.println("LoginController инициализирован");
    }

    //нажатие на кнопку "Войти"
    public void handleLogin(String login, String password) {
        System.out.println("Попытка входа: логин = " + login);
        
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
        
        // Проверяем учетные данные администратора
        if (login.equals(ADMIN_LOGIN) && password.equals(ADMIN_PASSWORD)) {
            System.out.println("Вход выполнен с использованием жестко заданных учетных данных администратора");
            
            // Сбрасываем статус соединения с базой данных перед входом администратора
            DatabaseManager.resetConnectionStatus();
            
            // Получаем список всех ролей
            List<Role> allRoles = roleDAO.getAllRoles();
            System.out.println("Доступные роли:");
            for (Role role : allRoles) {
                System.out.println("ID: " + role.getId() + ", Название: " + role.getName());
            }
            
            // Создаем объект администратора
            Role adminRole = null;
            
            // Сначала пробуем найти роль по названию
            adminRole = roleDAO.getRoleByName("Администратор");
            
            // Если по названию не нашли, пробуем по ID
            if (adminRole == null) {
                System.out.println("Не удалось найти роль администратора по названию, пробуем найти по ID=1");
                adminRole = roleDAO.getRoleById(1);
            }
            
            // Если и по ID не нашли, пробуем взять первую подходящую роль из списка
            if (adminRole == null && !allRoles.isEmpty()) {
                System.out.println("Не удалось найти роль администратора ни по названию, ни по ID. Пробуем использовать доступную роль.");
                for (Role role : allRoles) {
                    if (role.getName().toLowerCase().contains("админ") || 
                        role.getName().toLowerCase().contains("admin")) {
                        adminRole = role;
                        System.out.println("Найдена подходящая роль: " + role.getName());
                        break;
                    }
                }
                
                // Если не нашли подходящую, берем первую доступную
                if (adminRole == null && !allRoles.isEmpty()) {
                    adminRole = allRoles.get(0);
                    System.out.println("Используем первую доступную роль: " + adminRole.getName());
                }
            }
            
            if (adminRole == null) {
                System.out.println("Ошибка: не удалось получить роль администратора");
                view.showAlert(Alert.AlertType.ERROR, "Ошибка входа", "Не удалось получить роль администратора");
                return;
            }
            
            User adminUser = new User();
            adminUser.setId(0); // Временный ID
            adminUser.setLogin(ADMIN_LOGIN);
            adminUser.setFirstName("Администратор");
            adminUser.setLastName("Системы");
            adminUser.setEmail("admin@example.com");
            adminUser.setRole(adminRole);
            
            System.out.println("Создан администратор с ролью: ID=" + adminRole.getId() + ", Название=" + adminRole.getName());
            primaryStage.close();
            
            System.out.println("Открываем окно администратора");
            MainAdminView adminView = new MainAdminView();
            adminView.start(new Stage());
            return;
        }
        
        System.out.println("Проверка учетных данных в базе данных...");

        // Сбрасываем статус соединения с базой данных перед проверкой учетных данных
        DatabaseManager.resetConnectionStatus();

        User user = userDAO.authenticate(login, password);
        
        if (user != null) {
            System.out.println("Вход успешен. Пользователь: " + user.getFirstName() + " " + user.getLastName() + ", роль: " + user.getRole().getName());
            

            primaryStage.close();

            if (user.isAdmin()) {
                System.out.println("Открываем окно администратора");

                MainAdminView adminView = new MainAdminView();
                adminView.start(new Stage());
            } else {
                System.out.println("Открываем окно клиента");

                MainClientView clientView = new MainClientView(user);
                clientView.start(new Stage());
            }
        } else {
            System.out.println("Ошибка входа: неверный логин или пароль");

            view.showAlert(Alert.AlertType.ERROR, "Ошибка входа", "Неверный логин или пароль");
        }
    }


    public void showRegistrationWindow() {
        System.out.println("Переход к окну регистрации");

        primaryStage.close();
        
        RegisterView registerView = new RegisterView();
        registerView.start(new Stage());
    }
} 