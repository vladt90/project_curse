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

/**
 * Класс представления главного окна административной части приложения
 */
public class MainAdminView {
    private AdminController controller;
    private TableView<Product> productTable;
    private TableView<User> userTable;
    private TableView<Order> orderTable;
    
    // Поля для добавления/редактирования товара
    private TextField productNameField;
    private TextField productPriceField;
    private TextField productUnitField;
    private TextField productQuantityField;

    /**
     * Запустить главное окно административной части
     * 
     * @param primaryStage главное окно приложения
     */
    public void start(Stage primaryStage) {
        this.controller = new AdminController(this, primaryStage);

        primaryStage.setTitle("Интернет-магазин - Администратор");

        // Создаем основной контейнер
        BorderPane borderPane = new BorderPane();
        
        // Верхняя панель с информацией
        HBox topPanel = createTopPanel();
        borderPane.setTop(topPanel);
        
        // Создаем вкладки
        TabPane tabPane = new TabPane();
        
        // Вкладка "Управление товарами"
        Tab productsTab = new Tab("Управление товарами");
        productsTab.setClosable(false);
        VBox productsBox = createProductsTab();
        productsTab.setContent(productsBox);
        
        // Вкладка "Управление пользователями"
        Tab usersTab = new Tab("Управление пользователями");
        usersTab.setClosable(false);
        VBox usersBox = createUsersTab();
        usersTab.setContent(usersBox);
        
        // Вкладка "Управление заказами"
        Tab ordersTab = new Tab("Управление заказами");
        ordersTab.setClosable(false);
        VBox ordersBox = createOrdersTab();
        ordersTab.setContent(ordersBox);
        
        // Добавляем вкладки в панель
        tabPane.getTabs().addAll(productsTab, usersTab, ordersTab);
        
        // Добавляем панель вкладок в центр основного контейнера
        borderPane.setCenter(tabPane);

        // Создаем сцену
        Scene scene = new Scene(borderPane, 900, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Загружаем данные
        controller.loadProducts();
        controller.loadUsers();
        controller.loadOrders();
    }

    /**
     * Создать верхнюю панель
     * 
     * @return верхняя панель
     */
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

    /**
     * Создать вкладку "Управление товарами"
     * 
     * @return контейнер с содержимым вкладки "Управление товарами"
     */
    private VBox createProductsTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        Text title = new Text("Управление товарами");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        
        // Создаем таблицу товаров
        productTable = new TableView<>();
        
        // Столбец с ID товара
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        // Столбец с названием товара
        TableColumn<Product, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);
        
        // Столбец с ценой товара
        TableColumn<Product, Double> priceColumn = new TableColumn<>("Цена (руб.)");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        // Столбец с единицей измерения
        TableColumn<Product, String> unitColumn = new TableColumn<>("Ед. изм.");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        
        // Столбец с количеством на складе
        TableColumn<Product, Integer> stockColumn = new TableColumn<>("В наличии");
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        
        // Добавляем столбцы в таблицу
        productTable.getColumns().addAll(idColumn, nameColumn, priceColumn, unitColumn, stockColumn);
        
        // Форма для добавления/редактирования товара
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setPadding(new Insets(10, 10, 10, 10));
        
        // Название товара
        Label nameLabel = new Label("Название:");
        formGrid.add(nameLabel, 0, 0);
        
        productNameField = new TextField();
        formGrid.add(productNameField, 1, 0);
        
        // Цена товара
        Label priceLabel = new Label("Цена (руб.):");
        formGrid.add(priceLabel, 0, 1);
        
        productPriceField = new TextField();
        formGrid.add(productPriceField, 1, 1);
        
        // Единица измерения
        Label unitLabel = new Label("Ед. изм. (шт, кг, л):");
        formGrid.add(unitLabel, 0, 2);
        
        productUnitField = new TextField();
        formGrid.add(productUnitField, 1, 2);
        
        // Количество на складе
        Label quantityLabel = new Label("Количество:");
        formGrid.add(quantityLabel, 0, 3);
        
        productQuantityField = new TextField();
        formGrid.add(productQuantityField, 1, 3);
        
        // Панель с кнопками
        HBox buttonPanel = new HBox(10);
        
        Button addButton = new Button("Добавить");
        addButton.setOnAction(e -> {
            try {
                String name = productNameField.getText();
                double price = Double.parseDouble(productPriceField.getText());
                String unit = productUnitField.getText();
                int quantity = Integer.parseInt(productQuantityField.getText());
                
                controller.addProduct(name, price, unit, quantity);
                clearProductForm();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректный формат числа");
            }
        });
        
        Button updateButton = new Button("Обновить");
        updateButton.setOnAction(e -> {
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                try {
                    String name = productNameField.getText();
                    double price = Double.parseDouble(productPriceField.getText());
                    String unit = productUnitField.getText();
                    int quantity = Integer.parseInt(productQuantityField.getText());
                    
                    controller.updateProduct(selectedProduct.getId(), name, price, unit, quantity);
                    clearProductForm();
                } catch (NumberFormatException ex) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректный формат числа");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите товар для обновления");
            }
        });
        
        Button deleteButton = new Button("Удалить");
        deleteButton.setOnAction(e -> {
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                controller.deleteProduct(selectedProduct.getId());
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите товар для удаления");
            }
        });
        
        Button clearButton = new Button("Очистить");
        clearButton.setOnAction(e -> clearProductForm());
        
        buttonPanel.getChildren().addAll(addButton, updateButton, deleteButton, clearButton);
        
        // Обработчик выбора товара в таблице
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                productNameField.setText(newSelection.getName());
                productPriceField.setText(String.valueOf(newSelection.getPrice()));
                productUnitField.setText(newSelection.getUnit());
                productQuantityField.setText(String.valueOf(newSelection.getStockQuantity()));
            }
        });
        
        vbox.getChildren().addAll(title, productTable, formGrid, buttonPanel);
        
        return vbox;
    }

    /**
     * Создать вкладку "Управление пользователями"
     * 
     * @return контейнер с содержимым вкладки "Управление пользователями"
     */
    private VBox createUsersTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        Text title = new Text("Управление пользователями");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        
        // Создаем таблицу пользователей
        userTable = new TableView<>();
        
        // Столбец с ID пользователя
        TableColumn<User, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        // Столбец с логином пользователя
        TableColumn<User, String> loginColumn = new TableColumn<>("Логин");
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        
        // Столбец с именем пользователя
        TableColumn<User, String> firstNameColumn = new TableColumn<>("Имя");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        
        // Столбец с фамилией пользователя
        TableColumn<User, String> lastNameColumn = new TableColumn<>("Фамилия");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        
        // Столбец с email пользователя
        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setPrefWidth(150);
        
        // Столбец с телефоном пользователя
        TableColumn<User, String> phoneColumn = new TableColumn<>("Телефон");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        
        // Столбец со скидкой пользователя
        TableColumn<User, Double> discountColumn = new TableColumn<>("Скидка (%)");
        discountColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleDoubleProperty(
                    cellData.getValue().getDiscount() * 100).asObject();
        });
        
        // Столбец с ролью пользователя
        TableColumn<User, String> roleColumn = new TableColumn<>("Роль");
        roleColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getRole().getName());
        });
        
        // Добавляем столбцы в таблицу
        userTable.getColumns().addAll(idColumn, loginColumn, firstNameColumn, lastNameColumn, 
                emailColumn, phoneColumn, discountColumn, roleColumn);
        
        // Панель с кнопками
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

    /**
     * Создать вкладку "Управление заказами"
     * 
     * @return контейнер с содержимым вкладки "Управление заказами"
     */
    private VBox createOrdersTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        Text title = new Text("Управление заказами");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        
        // Создаем таблицу заказов
        orderTable = new TableView<>();
        
        // Столбец с ID заказа
        TableColumn<Order, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        // Столбец с пользователем
        TableColumn<Order, String> userColumn = new TableColumn<>("Пользователь");
        userColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getUser().getFullName());
        });
        userColumn.setPrefWidth(150);
        
        // Столбец с датой заказа
        TableColumn<Order, String> orderDateColumn = new TableColumn<>("Дата заказа");
        orderDateColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getOrderDate().toString());
        });
        orderDateColumn.setPrefWidth(150);
        
        // Столбец с датой доставки
        TableColumn<Order, String> deliveryDateColumn = new TableColumn<>("Дата доставки");
        deliveryDateColumn.setCellValueFactory(cellData -> {
            java.time.LocalDateTime deliveryDate = cellData.getValue().getDeliveryDate();
            return new javafx.beans.property.SimpleStringProperty(
                    deliveryDate != null ? deliveryDate.toString() : "Не указана");
        });
        deliveryDateColumn.setPrefWidth(150);
        
        // Столбец с общей стоимостью
        TableColumn<Order, Double> totalCostColumn = new TableColumn<>("Сумма (руб.)");
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        // Столбец со статусом
        TableColumn<Order, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Добавляем столбцы в таблицу
        orderTable.getColumns().addAll(idColumn, userColumn, orderDateColumn, 
                deliveryDateColumn, totalCostColumn, statusColumn);
        
        // Панель с кнопками
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

    /**
     * Очистить форму товара
     */
    private void clearProductForm() {
        productNameField.clear();
        productPriceField.clear();
        productUnitField.clear();
        productQuantityField.clear();
        productTable.getSelectionModel().clearSelection();
    }

    /**
     * Обновить таблицу товаров
     * 
     * @param products список товаров
     */
    public void updateProductTable(ObservableList<Product> products) {
        productTable.setItems(products);
    }

    /**
     * Обновить таблицу пользователей
     * 
     * @param users список пользователей
     */
    public void updateUserTable(ObservableList<User> users) {
        userTable.setItems(users);
    }

    /**
     * Обновить таблицу заказов
     * 
     * @param orders список заказов
     */
    public void updateOrderTable(ObservableList<Order> orders) {
        orderTable.setItems(orders);
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