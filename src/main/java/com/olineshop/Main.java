package com.olineshop;

import javafx.application.Application;
import javafx.stage.Stage;
import com.olineshop.view.LoginView;
import com.olineshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;


public class Main extends Application {


     //primaryStage главное окно

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Запуск приложения...");
        
        try {
            System.out.println("Проверка подключения к базе данных...");
            Connection connection = DatabaseManager.getConnection();
            if (connection != null && !connection.isClosed()) {
                System.out.println("Подключение к базе данных успешно установлено");
                
                checkDatabaseTables(connection);
            } else {
                System.out.println("Ошибка: не удалось подключиться к базе данных");
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке подключения к базе данных: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Запуск окна входа...");
        LoginView loginView = new LoginView();
        loginView.start(primaryStage);
        System.out.println("Окно входа запущено");
    }
    

    private void checkDatabaseTables(Connection connection) {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String[] tables = {"roles", "users", "products", "orders", "order_items"};
            
            System.out.println("Проверка существования таблиц в базе данных:");
            for (String table : tables) {
                ResultSet rs = metaData.getTables(null, null, table, null);
                if (rs.next()) {
                    System.out.println("Таблица '" + table + "' существует");
                    
                    if (table.equals("roles")) {
                        ResultSet roleCount = connection.createStatement().executeQuery("SELECT COUNT(*) FROM roles");
                        if (roleCount.next()) {
                            int count = roleCount.getInt(1);
                            System.out.println("Количество записей в таблице 'roles': " + count);
                        }
                        
                        ResultSet roleData = connection.createStatement().executeQuery("SELECT * FROM roles");
                        System.out.println("Содержимое таблицы 'roles':");
                        while (roleData.next()) {
                            int id = roleData.getInt("id");
                            String name = roleData.getString("name");
                            System.out.println("ID: " + id + ", Название: " + name);
                        }
                    }
                } else {
                    System.out.println("Таблица '" + table + "' НЕ существует");
                }
                rs.close();
            }
        } catch (SQLException e) {
            System.out.println("Ошибка при проверке таблиц: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.out.println("Завершение работы приложения...");
       DatabaseManager.closeConnection();
        System.out.println("Соединение с базой данных закрыто");
    }

    public static void main(String[] args) {
        System.out.println("Запуск JavaFX приложения...");
        launch(args);
    }
} 