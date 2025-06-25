package com.olineshop.view;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.olineshop.controller.AdminController;
import com.olineshop.model.Order;
import com.olineshop.model.Product;
import com.olineshop.model.User;

//Класс представления главного окна административной части приложения
public class MainAdminView {
    private AdminController controller;
    private TableView<Product> productTable;
    private TableView<User> userTable;
    private TableView<Order> orderTable;

    //Запустить главное окно административной части
    //primaryStage главное окно приложения
    public void start(Stage primaryStage) {
        this.controller = new AdminController(this, primaryStage);

        primaryStage.setTitle("Интернет-магазин - Администратор");

        BorderPane borderPane = new BorderPane();
        
        HBox topPanel = createTopPanel();
        borderPane.setTop(topPanel);
        
        TabPane tabPane = new TabPane();
        
        Tab productsTab = new Tab("Управление товарами");
        productsTab.setClosable(false);
        VBox productsBox = createProductsTab();
        productsTab.setContent(productsBox);
        
        Tab usersTab = new Tab("Управление пользователями");
        usersTab.setClosable(false);
        VBox usersBox = createUsersTab();
        usersTab.setContent(usersBox);
        
        Tab ordersTab = new Tab("Управление заказами");
        ordersTab.setClosable(false);
        VBox ordersBox = createOrdersTab();
        ordersTab.setContent(ordersBox);
        
        tabPane.getTabs().addAll(productsTab, usersTab, ordersTab);
        
        borderPane.setCenter(tabPane);

        Scene scene = new Scene(borderPane, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        controller.loadProducts();
        controller.loadUsers();
        controller.loadOrders();
    }

    //Создать верхнюю панель
    //return верхняя панель
    private HBox createTopPanel() {
        HBox topPanel = new HBox(10);
        topPanel.setPadding(new Insets(10, 10, 10, 10));
        
        Label adminLabel = new Label("Панель администратора");
        adminLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));
        
        Button logoutButton = new Button("Выйти");
        logoutButton.setOnAction(e -> controller.handleLogout());
        
        topPanel.getChildren().addAll(adminLabel, logoutButton);
        
        return topPanel;
    }

    //Создать вкладку "Управление товарами"
    //return контейнер с содержимым вкладки "Управление товарами"
    private VBox createProductsTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        Text title = new Text("Управление товарами");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        
        productTable = new TableView<>();
        
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Product, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);
        
        TableColumn<Product, Double> priceColumn = new TableColumn<>("Цена (руб.)");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        TableColumn<Product, String> unitColumn = new TableColumn<>("Ед. изм.");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        
        TableColumn<Product, Integer> stockColumn = new TableColumn<>("В наличии");
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        productTable.getColumns().addAll(idColumn, nameColumn, priceColumn, unitColumn, stockColumn);
        
        HBox buttonPanel = new HBox(10);
        
        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> {
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                try {
                    controller.deleteProduct(selectedProduct.getId());
                } catch (Exception ex) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Произошла ошибка при удалении товара: " + ex.getMessage());
                    ex.printStackTrace();
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите товар для удаления");
            }
        });
        
        Button deleteAllButton = new Button("Удалить все товары");
        deleteAllButton.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white;");
        deleteAllButton.setOnAction(e -> {
            controller.deleteAllProducts();
        });
        
        buttonPanel.getChildren().addAll(deleteButton, deleteAllButton);
        
        vbox.getChildren().addAll(title, productTable, buttonPanel);
        
        return vbox;
    }

    //Создать вкладку "Управление пользователями"
    //return контейнер с содержимым вкладки "Управление пользователями"
    private VBox createUsersTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        Text title = new Text("Управление пользователями");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        
        userTable = new TableView<>();
        
        TableColumn<User, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<User, String> loginColumn = new TableColumn<>("Логин");
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        
        TableColumn<User, String> firstNameColumn = new TableColumn<>("Имя");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        TableColumn<User, String> lastNameColumn = new TableColumn<>("Фамилия");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        
        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setPrefWidth(150);
        
        TableColumn<User, String> phoneColumn = new TableColumn<>("Телефон");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        TableColumn<User, Double> discountColumn = new TableColumn<>("Скидка (%)");
        discountColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleDoubleProperty(
                    cellData.getValue().getDiscount() * 100).asObject();
        });
        
        TableColumn<User, String> roleColumn = new TableColumn<>("Роль");
        roleColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getRole().getName());
        });
        
        userTable.getColumns().addAll(idColumn, loginColumn, firstNameColumn, lastNameColumn, 
                emailColumn, phoneColumn, discountColumn, roleColumn);
        
        HBox buttonPanel = new HBox(10);
        
        Button viewDetailsButton = new Button("Подробнее");
        viewDetailsButton.setOnAction(e -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                controller.showUserDetails(selectedUser);
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите пользователя для просмотра");
            }
        });
        
        Button deleteUserButton = new Button("Удалить");
        deleteUserButton.setOnAction(e -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                controller.deleteUser(selectedUser.getId());
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите пользователя для удаления");
            }
        });
        
        buttonPanel.getChildren().addAll(viewDetailsButton, deleteUserButton);
        
        vbox.getChildren().addAll(title, userTable, buttonPanel);
        
        return vbox;
    }

    //Создать вкладку "Управление заказами"
    //return контейнер с содержимым вкладки "Управление заказами"
    private VBox createOrdersTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        Text title = new Text("Управление заказами");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        
        orderTable = new TableView<>();
        
        TableColumn<Order, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Order, String> userColumn = new TableColumn<>("Пользователь");
        userColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getUser().getFullName());
        });
        userColumn.setPrefWidth(150);
        
        TableColumn<Order, String> orderDateColumn = new TableColumn<>("Дата заказа");
        orderDateColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getOrderDate().toString());
        });
        orderDateColumn.setPrefWidth(150);
        
        TableColumn<Order, String> deliveryDateColumn = new TableColumn<>("Дата доставки");
        deliveryDateColumn.setCellValueFactory(cellData -> {
            java.time.LocalDateTime deliveryDate = cellData.getValue().getDeliveryDate();
            return new javafx.beans.property.SimpleStringProperty(
                    deliveryDate != null ? deliveryDate.toString() : "Не указана");
        });
        deliveryDateColumn.setPrefWidth(150);
        
        TableColumn<Order, Double> totalCostColumn = new TableColumn<>("Сумма (руб.)");
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        TableColumn<Order, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        orderTable.getColumns().addAll(idColumn, userColumn, orderDateColumn, 
                deliveryDateColumn, totalCostColumn, statusColumn);
        
        HBox buttonPanel = new HBox(10);
        
        Button viewDetailsButton = new Button("Подробнее");
        viewDetailsButton.setOnAction(e -> {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                controller.showOrderDetails(selectedOrder);
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите заказ для просмотра");
            }
        });
        
        Button changeStatusButton = new Button("Изменить статус");
        changeStatusButton.setOnAction(e -> {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                controller.showChangeStatusDialog(selectedOrder);
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите заказ для изменения статуса");
            }
        });
        
        Button deleteOrderButton = new Button("Удалить");
        deleteOrderButton.setOnAction(e -> {
            Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                controller.deleteOrder(selectedOrder.getId());
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите заказ для удаления");
            }
        });
        
        buttonPanel.getChildren().addAll(viewDetailsButton, changeStatusButton, deleteOrderButton);
        
        vbox.getChildren().addAll(title, orderTable, buttonPanel);
        
        return vbox;
    }

    //Обновить таблицу товаров
    //products список товаров
    public void updateProductTable(ObservableList<Product> products) {
        productTable.setItems(products);
    }

    //Обновить таблицу пользователей
    //users список пользователей
    public void updateUserTable(ObservableList<User> users) {
        userTable.setItems(users);
    }

    //Обновить таблицу заказов
    //orders список заказов
    public void updateOrderTable(ObservableList<Order> orders) {
        orderTable.setItems(orders);
    }

    //Показать диалоговое окно с сообщением
    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 