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

//Класс представления окна регистрации пользователя
public class RegisterView {
    private RegisterController controller;

    //Запустить окно регистрации
    //primaryStage главное окно приложения
    public void start(Stage primaryStage) {
        this.controller = new RegisterController(this, primaryStage);

        primaryStage.setTitle("Интернет-магазин - Регистрация");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Text sceneTitle = new Text("Регистрация нового пользователя");
        sceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(sceneTitle, 0, 0, 2, 1);

        Label loginLabel = new Label("Логин:");
        grid.add(loginLabel, 0, 1);

        TextField loginTextField = new TextField();
        grid.add(loginTextField, 1, 1);

        Label passwordLabel = new Label("Пароль:");
        grid.add(passwordLabel, 0, 2);

        PasswordField passwordField = new PasswordField();
        grid.add(passwordField, 1, 2);

        Label confirmPasswordLabel = new Label("Подтверждение пароля:");
        grid.add(confirmPasswordLabel, 0, 3);

        PasswordField confirmPasswordField = new PasswordField();
        grid.add(confirmPasswordField, 1, 3);

        Label firstNameLabel = new Label("Имя:");
        grid.add(firstNameLabel, 0, 4);

        TextField firstNameTextField = new TextField();
        grid.add(firstNameTextField, 1, 4);

        Label lastNameLabel = new Label("Фамилия:");
        grid.add(lastNameLabel, 0, 5);

        TextField lastNameTextField = new TextField();
        grid.add(lastNameTextField, 1, 5);

        Label emailLabel = new Label("Email:");
        grid.add(emailLabel, 0, 6);

        TextField emailTextField = new TextField();
        grid.add(emailTextField, 1, 6);

        Label phoneLabel = new Label("Телефон:");
        grid.add(phoneLabel, 0, 7);

        TextField phoneTextField = new TextField();
        grid.add(phoneTextField, 1, 7);

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

        final Text actionTarget = new Text();
        grid.add(actionTarget, 1, 11);

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

        Scene scene = new Scene(grid, 500, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //Показать диалоговое окно с ссообщением
    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 