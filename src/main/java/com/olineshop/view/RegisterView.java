package com.olineshop.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.olineshop.controller.RegisterController;

/**
 * Класс представления окна регистрации пользователя
 */
public class RegisterView {
    private RegisterController controller;

    /**
     * Запустить окно регистрации
     * 
     * @param primaryStage главное окно приложения
     */
    public void start(Stage primaryStage) {
        this.controller = new RegisterController(this, primaryStage);

        primaryStage.setTitle("Интернет-магазин - Регистрация");

        // Создаем контейнер для размещения элементов
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Заголовок
        Text sceneTitle = new Text("Регистрация нового пользователя");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        // Поле для ввода логина
        Label loginLabel = new Label("Логин:");
        grid.add(loginLabel, 0, 1);

        TextField loginTextField = new TextField();
        grid.add(loginTextField, 1, 1);

        // Поле для ввода пароля
        Label passwordLabel = new Label("Пароль:");
        grid.add(passwordLabel, 0, 2);

        PasswordField passwordField = new PasswordField();
        grid.add(passwordField, 1, 2);

        // Поле для подтверждения пароля
        Label confirmPasswordLabel = new Label("Подтверждение пароля:");
        grid.add(confirmPasswordLabel, 0, 3);

        PasswordField confirmPasswordField = new PasswordField();
        grid.add(confirmPasswordField, 1, 3);

        // Поле для ввода имени
        Label firstNameLabel = new Label("Имя:");
        grid.add(firstNameLabel, 0, 4);

        TextField firstNameTextField = new TextField();
        grid.add(firstNameTextField, 1, 4);

        // Поле для ввода фамилии
        Label lastNameLabel = new Label("Фамилия:");
        grid.add(lastNameLabel, 0, 5);

        TextField lastNameTextField = new TextField();
        grid.add(lastNameTextField, 1, 5);

        // Поле для ввода email
        Label emailLabel = new Label("Email:");
        grid.add(emailLabel, 0, 6);

        TextField emailTextField = new TextField();
        grid.add(emailTextField, 1, 6);

        // Поле для ввода телефона
        Label phoneLabel = new Label("Телефон:");
        grid.add(phoneLabel, 0, 7);

        TextField phoneTextField = new TextField();
        grid.add(phoneTextField, 1, 7);

        // Кнопки
        Button registerButton = new Button("Зарегистрироваться");
        registerButton.setDefaultButton(true);
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(registerButton);
        grid.add(hbBtn, 1, 9);

        Button backButton = new Button("Назад");
        HBox hbBackBtn = new HBox(10);
        hbBackBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbBackBtn.getChildren().add(backButton);
        grid.add(hbBackBtn, 0, 9);

        // Текст для отображения ошибок
        final Text actionTarget = new Text();
        grid.add(actionTarget, 1, 11);

        // Привязка действий к контроллеру
        registerButton.setOnAction(e -> {
            actionTarget.setText("");
            controller.handleRegister(
                    loginTextField.getText(),
                    passwordField.getText(),
                    confirmPasswordField.getText(),
                    firstNameTextField.getText(),
                    lastNameTextField.getText(),
                    emailTextField.getText(),
                    phoneTextField.getText());
        });
        
        backButton.setOnAction(e -> {
            controller.showLoginWindow();
        });

        // Создаем сцену
        Scene scene = new Scene(grid, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Показать диалоговое окно с сообщением
     * 
     * @param alertType тип диалогового окна
     * @param title заголовок окна
     * @param message сообщение
     */
    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 