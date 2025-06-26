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
import com.olineshop.controller.RegisterController;

//Класс представления окна регистрации пользователя
public class RegisterView {
    private RegisterController controller;

    //Запустить окно регистрации
    //primaryStage главное окно приложения
    public void start(Stage primaryStage) {
        this.controller = new RegisterController(this, primaryStage);

        primaryStage.setTitle("Интернет-магазин - Регистрация");

        // Создаем основной контейнер с отступами
        VBox mainContainer = new VBox(20);
        mainContainer.setPadding(new Insets(30));
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: #f5f5f5;");

        // Заголовок с тенью
        Text sceneTitle = new Text("Регистрация нового пользователя");
        sceneTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        sceneTitle.setFill(Color.web("#2c3e50"));
        
        // Добавляем эффект тени
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(3.0);
        dropShadow.setOffsetX(2.0);
        dropShadow.setOffsetY(2.0);
        dropShadow.setColor(Color.color(0.4, 0.4, 0.4, 0.3));
        sceneTitle.setEffect(dropShadow);

        // Создаем форму для регистрации
        VBox registerForm = new VBox(12);
        registerForm.setAlignment(Pos.CENTER);
        registerForm.setPadding(new Insets(25));
        registerForm.setMaxWidth(450);
        registerForm.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // Стиль для всех текстовых полей
        String textFieldStyle = "-fx-background-radius: 5; -fx-border-radius: 5; -fx-border-color: #dcdcdc; -fx-border-width: 1; -fx-padding: 8;";

        // Поле для логина
        Label loginLabel = new Label("Логин:");
        loginLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        TextField loginTextField = new TextField();
        loginTextField.setPromptText("Введите логин");
        loginTextField.setStyle(textFieldStyle);

        // Поле для пароля
        Label passwordLabel = new Label("Пароль:");
        passwordLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Введите пароль");
        passwordField.setStyle(textFieldStyle);

        // Поле для подтверждения пароля
        Label confirmPasswordLabel = new Label("Подтверждение пароля:");
        confirmPasswordLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Подтвердите пароль");
        confirmPasswordField.setStyle(textFieldStyle);

        // Поле для имени
        Label firstNameLabel = new Label("Имя:");
        firstNameLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        TextField firstNameTextField = new TextField();
        firstNameTextField.setPromptText("Введите имя");
        firstNameTextField.setStyle(textFieldStyle);

        // Поле для фамилии
        Label lastNameLabel = new Label("Фамилия:");
        lastNameLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        TextField lastNameTextField = new TextField();
        lastNameTextField.setPromptText("Введите фамилию");
        lastNameTextField.setStyle(textFieldStyle);

        // Поле для email
        Label emailLabel = new Label("Email:");
        emailLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        TextField emailTextField = new TextField();
        emailTextField.setPromptText("Введите email");
        emailTextField.setStyle(textFieldStyle);

        // Поле для телефона
        Label phoneLabel = new Label("Телефон:");
        phoneLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        TextField phoneTextField = new TextField();
        phoneTextField.setPromptText("Введите телефон");
        phoneTextField.setStyle(textFieldStyle);

        // Кнопки
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        // Кнопка регистрации
        Button registerButton = new Button("Зарегистрироваться");
        registerButton.setPrefHeight(40);
        registerButton.setPrefWidth(200);
        registerButton.setDefaultButton(true);
        registerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        // Эффект при наведении на кнопку
        registerButton.setOnMouseEntered(e -> registerButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));
        registerButton.setOnMouseExited(e -> registerButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;"));

        // Кнопка назад
        Button backButton = new Button("Назад");
        backButton.setPrefHeight(40);
        backButton.setPrefWidth(120);
        backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-border-color: #3498db; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        // Эффект при наведении на кнопку назад
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: #f5f5f5; -fx-text-fill: #3498db; -fx-border-color: #3498db; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #3498db; -fx-border-color: #3498db; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;"));

        buttonBox.getChildren().addAll(backButton, registerButton);

        // Текст для сообщений об ошибках
        final Text actionTarget = new Text();
        actionTarget.setFill(Color.FIREBRICK);

        // Добавляем все элементы в форму
        registerForm.getChildren().addAll(
                loginLabel, loginTextField,
                passwordLabel, passwordField,
                confirmPasswordLabel, confirmPasswordField,
                firstNameLabel, firstNameTextField,
                lastNameLabel, lastNameTextField,
                emailLabel, emailTextField,
                phoneLabel, phoneTextField,
                buttonBox,
                actionTarget
        );

        // Обработчики событий
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

        // Добавляем все в основной контейнер
        mainContainer.getChildren().addAll(sceneTitle, registerForm);

        Scene scene = new Scene(mainContainer, 550, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //Показать диалоговое окно с ссообщением
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