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
import com.olineshop.controller.LoginController;

/**
 * Класс представления окна входа в систему
 */
public class LoginView {
    private LoginController controller;

    /**
     * Запустить окно входа
     * 
     * @param primaryStage главное окно приложения
     */
    public void start(Stage primaryStage) {
        this.controller = new LoginController(this, primaryStage);

        primaryStage.setTitle("Интернет-магазин - Вход в систему");

        // Создаем контейнер для размещения элементов
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        // Заголовок
        Text sceneTitle = new Text("Добро пожаловать");
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

        // Кнопки
        Button loginButton = new Button("Войти");
        loginButton.setDefaultButton(true);
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(loginButton);
        grid.add(hbBtn, 1, 4);

        Button registerButton = new Button("Регистрация");
        HBox hbRegBtn = new HBox(10);
        hbRegBtn.setAlignment(Pos.BOTTOM_LEFT);
        hbRegBtn.getChildren().add(registerButton);
        grid.add(hbRegBtn, 0, 4);

        // Текст для отображения ошибок
        final Text actionTarget = new Text();
        grid.add(actionTarget, 1, 6);

        // Привязка действий к контроллеру
        loginButton.setOnAction(e -> {
            actionTarget.setText("");
            controller.handleLogin(loginTextField.getText(), passwordField.getText());
        });
        
        registerButton.setOnAction(e -> {
            actionTarget.setText("");
            controller.showRegistrationWindow();
        });

        // Создаем сцену
        Scene scene = new Scene(grid, 400, 300);
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