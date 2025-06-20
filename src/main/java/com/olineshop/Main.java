package com.olineshop;

import javafx.application.Application;
import javafx.stage.Stage;
import com.olineshop.view.LoginView;
import com.olineshop.util.DatabaseManager;

/**
 * Главный класс приложения
 */
public class Main extends Application {

    /**
     * Метод запуска JavaFX приложения
     * 
     * @param primaryStage главное окно приложения
     */
    @Override
    public void start(Stage primaryStage) {
        // Запускаем окно входа
        LoginView loginView = new LoginView();
        loginView.start(primaryStage);
    }

    /**
     * Метод, вызываемый при закрытии приложения
     */
    @Override
    public void stop() {
        // Закрываем соединение с БД при выходе из приложения
        DatabaseManager.closeConnection();
    }

    /**
     * Точка входа в приложение
     * 
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        launch(args);
    }
} 