package com.olineshop.controller;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.olineshop.dao.UserDAO;
import com.olineshop.model.User;
import com.olineshop.view.LoginView;
import com.olineshop.view.MainAdminView;
import com.olineshop.view.MainClientView;
import com.olineshop.view.RegisterView;

/**
 * Контроллер для окна входа в систему
 */
public class LoginController {
    private final LoginView view;
    private final Stage primaryStage;
    private final UserDAO userDAO;

    /**
     * Конструктор контроллера
     * 
     * @param view представление окна входа
     * @param primaryStage главное окно приложения
     */
    public LoginController(LoginView view, Stage primaryStage) {
        this.view = view;
        this.primaryStage = primaryStage;
        this.userDAO = new UserDAO();
    }

    /**
     * Обработать нажатие на кнопку "Войти"
     * 
     * @param login логин пользователя
     * @param password пароль пользователя
     */
    public void handleLogin(String login, String password) {
        // Проверяем, что поля не пустые
        if (login == null || login.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите логин");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите пароль");
            return;
        }
        
        // Проверяем учетные данные пользователя
        User user = userDAO.authenticate(login, password);
        
        if (user != null) {
            // Закрываем окно входа
            primaryStage.close();
            
            // Открываем соответствующее окно в зависимости от роли пользователя
            if (user.isAdmin()) {
                // Пользователь - администратор
                MainAdminView adminView = new MainAdminView();
                adminView.start(new Stage());
            } else {
                // Пользователь - клиент
                MainClientView clientView = new MainClientView(user);
                clientView.start(new Stage());
            }
        } else {
            // Неверный логин или пароль
            view.showAlert(Alert.AlertType.ERROR, "Ошибка входа", "Неверный логин или пароль");
        }
    }

    /**
     * Показать окно регистрации
     */
    public void showRegistrationWindow() {
        // Закрываем окно входа
        primaryStage.close();
        
        // Открываем окно регистрации
        RegisterView registerView = new RegisterView();
        registerView.start(new Stage());
    }
} 