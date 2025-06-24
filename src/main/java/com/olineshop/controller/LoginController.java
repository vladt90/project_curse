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
            
            // Создаем объект администратора
            Role adminRole = roleDAO.getRoleById(1); // Роль администратора (ID = 1)
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
            

            primaryStage.close();
            
            System.out.println("Открываем окно администратора");
            MainAdminView adminView = new MainAdminView();
            adminView.start(new Stage());
            return;
        }
        
        System.out.println("Проверка учетных данных в базе данных...");
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