package com.olineshop.controller;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import com.olineshop.dao.RoleDAO;
import com.olineshop.dao.UserDAO;
import com.olineshop.model.Role;
import com.olineshop.model.User;
import com.olineshop.view.LoginView;
import com.olineshop.view.RegisterView;

/**
 * Контроллер для окна регистрации пользователей
 */
public class RegisterController {
    private final RegisterView view;
    private final Stage primaryStage;
    private final UserDAO userDAO;
    private final RoleDAO roleDAO;

    /**
     * Конструктор контроллера
     * 
     * @param view представление окна регистрации
     * @param primaryStage главное окно приложения
     */
    public RegisterController(RegisterView view, Stage primaryStage) {
        this.view = view;
        this.primaryStage = primaryStage;
        this.userDAO = new UserDAO();
        this.roleDAO = new RoleDAO();
    }

    /**
     * Обработать нажатие на кнопку "Зарегистрироваться"
     * 
     * @param login логин пользователя
     * @param password пароль пользователя
     * @param confirmPassword подтверждение пароля
     * @param firstName имя пользователя
     * @param lastName фамилия пользователя
     * @param email электронная почта пользователя
     * @param phone телефон пользователя
     */
    public void handleRegister(String login, String password, String confirmPassword,
            String firstName, String lastName, String email, String phone) {
        // Проверяем, что обязательные поля не пустые
        if (login == null || login.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите логин");
            return;
        }
        
        if (password == null || password.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите пароль");
            return;
        }
        
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Подтвердите пароль");
            return;
        }
        
        if (firstName == null || firstName.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите имя");
            return;
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите фамилию");
            return;
        }
        
        if (email == null || email.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите email");
            return;
        }
        
        // Проверяем, что пароли совпадают
        if (!password.equals(confirmPassword)) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Пароли не совпадают");
            return;
        }
        
        // Проверяем, что логин не занят
        User existingUser = userDAO.getUserByLogin(login);
        if (existingUser != null) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Логин уже занят");
            return;
        }
        
        // Получаем роль "Клиент" (id = 2)
        Role clientRole = roleDAO.getRoleById(2);
        if (clientRole == null) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось получить роль пользователя");
            return;
        }
        
        // Создаем нового пользователя
        User newUser = new User();
        newUser.setLogin(login);
        newUser.setPasswordHash(password); // В реальном приложении здесь должно быть хеширование пароля
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setEmail(email);
        newUser.setPhone(phone);
        newUser.setDiscount(0.0); // Начальная скидка 0%
        newUser.setRole(clientRole);
        
        // Добавляем пользователя в базу данных
        boolean success = userDAO.addUser(newUser);
        
        if (success) {
            view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Регистрация выполнена успешно");
            showLoginWindow();
        } else {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось зарегистрировать пользователя");
        }
    }

    /**
     * Показать окно входа
     */
    public void showLoginWindow() {
        // Закрываем окно регистрации
        primaryStage.close();
        
        // Открываем окно входа
        LoginView loginView = new LoginView();
        loginView.start(new Stage());
    }
} 