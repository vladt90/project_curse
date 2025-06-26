package com.olineshop.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.olineshop.controller.LoginController;

//Класс представления окна входа в систему
public class LoginView {
    private LoginController controller;

    //Запустить окно входа
    //primaryStage главное окно
    public void start(Stage primaryStage) {
        this.controller = new LoginController(this, primaryStage);

        primaryStage.setTitle("Интернет-магазин - Вход в систему");

        // Создаем основной контейнер с отступами
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Заголовок с тенью
        Text sceneTitle = new Text("Добро пожаловать");
        sceneTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        sceneTitle.setFill(Color.web("#2c3e50"));
        
        // Добавляем эффект тени
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(3.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.color(0.4, 0.4, 0.4, 0.3));
        sceneTitle.setEffect(dropShadow);

        // Создаем форму для входа
        VBox loginForm = new VBox(15);
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setPadding(new Insets(20));
        loginForm.setMaxWidth(350);
        loginForm.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // Поле для логина
        TextField loginTextField = new TextField();
        loginTextField.setPromptText("Введите логин");
        loginTextField.setPrefHeight(40);
        loginTextField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #dcdcdc; -fx-border-width: 1;");

        // Поле для пароля
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");
        passwordField.setPrefHeight(40);
        passwordField.setStyle("-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #dcdcdc; -fx-border-width: 1;");

        // Кнопка входа
        Button loginButton = new Button("Войти");
        loginButton.setPrefHeight(40);
        loginButton.setPrefWidth(320);
        loginButton.setDefaultButton(true);
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Эффект при наведении на кнопку
        loginButton.setOnMouseEntered(e -> loginButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));
        loginButton.setOnMouseExited(e -> loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));

        // Кнопка регистрации
        Button registerButton = new Button("Регистрация");
        registerButton.setPrefHeight(40);
        registerButton.setPrefWidth(320);
        registerButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-border-color: #3498db; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        // Эффект при наведении на кнопку регистрации
        registerButton.setOnMouseEntered(e -> registerButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #3498db; -fx-border-color: #3498db; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;"));
        registerButton.setOnMouseExited(e -> registerButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-border-color: #3498db; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;"));

        // Текст для сообщений об ошибках
        final Text actionTarget = new Text();
        actionTarget.setFill(Color.FIREBRICK);

        // Добавляем все элементы в форму
        loginForm.getChildren().addAll(
                new Label("Логин:"), loginTextField,
                new Label("Пароль:"), passwordField,
                loginButton,
                registerButton,
                actionTarget
        );

        // Обработчики событий
        loginButton.setOnAction(e -> {
            actionTarget.setText("");
            controller.handleLogin(loginTextField.getText(), passwordField.getText());
        });
        
        registerButton.setOnAction(e -> {
            actionTarget.setText("");
            controller.showRegistrationWindow();
        });

        // Добавляем все в основной контейнер
        mainContainer.getChildren().addAll(sceneTitle, loginForm);

        Scene scene = new Scene(mainContainer, 450, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //Показать диалоговое окно с сообщением
    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Стилизация диалогового окна
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-width: 1;");
        
        // Стилизация кнопок
        dialogPane.getButtonTypes().forEach(buttonType -> {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            button.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5;");
        });
        
        alert.showAndWait();
    }
} 