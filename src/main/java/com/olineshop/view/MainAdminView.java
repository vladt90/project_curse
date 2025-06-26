package com.olineshop.view;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.olineshop.controller.AdminController;
import com.olineshop.model.Order;
import com.olineshop.model.Product;
import com.olineshop.model.User;
import javafx.scene.control.ProgressIndicator;

//Класс представления главного окна административной части приложения
public class MainAdminView {
    private AdminController controller;
    private TableView<Product> productTable;
    private TableView<User> userTable;
    private TableView<Order> orderTable;
    private ProgressIndicator loadingIndicator;
    
    // Цвета и стили
    private final String PRIMARY_COLOR = "#2c3e50";
    private final String ACCENT_COLOR = "#3498db";
    private final String LIGHT_ACCENT_COLOR = "#5dade2";
    private final String BACKGROUND_COLOR = "#f5f5f5";
    private final String WHITE_COLOR = "#ffffff";
    private final String BORDER_COLOR = "#dcdcdc";
    
    private final String BUTTON_STYLE = String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-weight: bold; -fx-background-radius: 5;",
            ACCENT_COLOR, WHITE_COLOR);
    
    private final String BUTTON_HOVER_STYLE = String.format(
            "-fx-background-color: %s; -fx-text-fill: %s; -fx-font-weight: bold; -fx-background-radius: 5;",
            LIGHT_ACCENT_COLOR, WHITE_COLOR);
            
    private final String TABLE_STYLE = "-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);";

    //Запустить главное окно административной части
    //primaryStage главное окно приложения
    public void start(Stage primaryStage) {
        this.controller = new AdminController(this, primaryStage);

        primaryStage.setTitle("Интернет-магазин - Администратор");

        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        HBox topPanel = createTopPanel();
        borderPane.setTop(topPanel);
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");
        
        // Создаем вкладки
        Tab productsTab = createTab("Управление товарами", "\uf07a"); // иконка корзины
        VBox productsBox = createProductsTab();
        productsTab.setContent(productsBox);
        
        Tab usersTab = createTab("Управление пользователями", "\uf007"); // иконка пользователя
        VBox usersBox = createUsersTab();
        usersTab.setContent(usersBox);
        
        Tab ordersTab = createTab("Управление заказами", "\uf0d1"); // иконка грузовика
        VBox ordersBox = createOrdersTab();
        ordersTab.setContent(ordersBox);
        
        tabPane.getTabs().addAll(productsTab, usersTab, ordersTab);
        
        // Добавляем обработчик события переключения вкладок
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == productsTab) {
                controller.loadProducts();
            } else if (newTab == usersTab) {
                controller.loadUsers();
            } else if (newTab == ordersTab) {
                controller.loadOrders();
            }
        });
        
        borderPane.setCenter(tabPane);
        
        // Создаем индикатор загрузки
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setMaxSize(100, 100);
        loadingIndicator.setStyle("-fx-progress-color: " + ACCENT_COLOR + ";");
        
        // Создаем контейнер для индикатора загрузки
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(borderPane, loadingIndicator);
        StackPane.setAlignment(loadingIndicator, Pos.CENTER);

        Scene scene = new Scene(stackPane, 1000, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Загружаем данные только для активной вкладки при запуске
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == productsTab) {
            controller.loadProducts();
        } else if (selectedTab == usersTab) {
            controller.loadUsers();
        } else if (selectedTab == ordersTab) {
            controller.loadOrders();
        }
    }
    
    // Создание вкладки с иконкой
    private Tab createTab(String title, String iconCode) {
        Tab tab = new Tab();
        
        Label label = new Label(title);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        
        tab.setGraphic(label);
        
        return tab;
    }

    //Создать верхнюю панель
    //return верхняя панель
    private HBox createTopPanel() {
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(15));
        topPanel.setAlignment(Pos.CENTER_LEFT);
        topPanel.setSpacing(20);
        topPanel.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";");
        
        Label adminLabel = new Label("Панель администратора");
        adminLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        adminLabel.setTextFill(Color.WHITE);
        
        HBox spacer = new HBox();
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button logoutButton = new Button("Выйти");
        logoutButton.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        logoutButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;"));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;"));
        
        logoutButton.setOnAction(e -> controller.handleLogout());
        
        topPanel.getChildren().addAll(adminLabel, spacer, logoutButton);
        
        return topPanel;
    }

    //Создать вкладку "Управление товарами"
    //return контейнер с содержимым вкладки "Управление товарами"
    private VBox createProductsTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_CENTER);
        
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Text title = new Text("Управление товарами");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setFill(Color.web(PRIMARY_COLOR));
        
        titleBox.getChildren().add(title);
        
        // Создаем таблицу товаров с современным стилем
        productTable = new TableView<>();
        productTable.setStyle(TABLE_STYLE);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Product, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setMaxWidth(50);
        
        TableColumn<Product, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(200);
        
        TableColumn<Product, Double> priceColumn = new TableColumn<>("Цена (руб.)");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setMaxWidth(120);
        
        TableColumn<Product, String> unitColumn = new TableColumn<>("Ед. изм.");
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitColumn.setMaxWidth(80);
        
        TableColumn<Product, Integer> stockColumn = new TableColumn<>("В наличии");
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stockQuantity"));
        stockColumn.setMaxWidth(100);
        
        productTable.getColumns().addAll(idColumn, nameColumn, priceColumn, unitColumn, stockColumn);
        
        // Панель с кнопками
        HBox buttonPanel = new HBox(10);
        buttonPanel.setAlignment(Pos.CENTER_RIGHT);
        
        Button deleteButton = createStyledButton("Удалить", BUTTON_STYLE, BUTTON_HOVER_STYLE);
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
        
        buttonPanel.getChildren().add(deleteButton);
        
        vbox.getChildren().addAll(titleBox, productTable, buttonPanel);
        
        return vbox;
    }

    //Создать вкладку "Управление пользователями"
    //return контейнер с содержимым вкладки "Управление пользователями"
    private VBox createUsersTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_CENTER);
        
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Text title = new Text("Управление пользователями");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setFill(Color.web(PRIMARY_COLOR));
        
        titleBox.getChildren().add(title);
        
        // Создаем таблицу пользователей с современным стилем
        userTable = new TableView<>();
        userTable.setStyle(TABLE_STYLE);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<User, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setMaxWidth(50);
        
        TableColumn<User, String> loginColumn = new TableColumn<>("Логин");
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        loginColumn.setPrefWidth(100);
        
        TableColumn<User, String> firstNameColumn = new TableColumn<>("Имя");
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        firstNameColumn.setPrefWidth(120);
        
        TableColumn<User, String> lastNameColumn = new TableColumn<>("Фамилия");
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        lastNameColumn.setPrefWidth(120);
        
        TableColumn<User, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setPrefWidth(150);
        
        TableColumn<User, String> phoneColumn = new TableColumn<>("Телефон");
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        phoneColumn.setPrefWidth(120);
        
        TableColumn<User, Double> discountColumn = new TableColumn<>("Скидка (%)");
        discountColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleDoubleProperty(
                    cellData.getValue().getDiscount() * 100).asObject();
        });
        discountColumn.setMaxWidth(100);
        
        TableColumn<User, String> roleColumn = new TableColumn<>("Роль");
        roleColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getRole().getName());
        });
        roleColumn.setMaxWidth(100);
        
        userTable.getColumns().addAll(idColumn, loginColumn, firstNameColumn, lastNameColumn, 
                emailColumn, phoneColumn, discountColumn, roleColumn);
        
        vbox.getChildren().addAll(titleBox, userTable);
        
        return vbox;
    }

    //Создать вкладку "Управление заказами"
    //return контейнер с содержимым вкладки "Управление заказами"
    private VBox createOrdersTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_CENTER);
        
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Text title = new Text("Управление заказами");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setFill(Color.web(PRIMARY_COLOR));
        
        titleBox.getChildren().add(title);
        
        // Создаем таблицу заказов с современным стилем
        orderTable = new TableView<>();
        orderTable.setStyle(TABLE_STYLE);
        orderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Order, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setMaxWidth(50);
        
        TableColumn<Order, String> userColumn = new TableColumn<>("Пользователь");
        userColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getUser().getFullName());
        });
        userColumn.setPrefWidth(150);
        
        TableColumn<Order, String> orderDateColumn = new TableColumn<>("Дата заказа");
        orderDateColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getOrderDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
        });
        orderDateColumn.setPrefWidth(150);
        
        TableColumn<Order, String> deliveryDateColumn = new TableColumn<>("Дата доставки");
        deliveryDateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue().getDeliveryDate() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getDeliveryDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));
            } else {
                return new javafx.beans.property.SimpleStringProperty("Не указана");
            }
        });
        deliveryDateColumn.setPrefWidth(150);
        
        TableColumn<Order, Double> totalCostColumn = new TableColumn<>("Сумма (руб.)");
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        totalCostColumn.setMaxWidth(100);
        
        TableColumn<Order, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setPrefWidth(120);
        
        TableColumn<Order, Void> actionsColumn = new TableColumn<>("Действия");
        actionsColumn.setPrefWidth(200);
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button detailsButton = createStyledButton("Подробнее", 
                "-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; -fx-background-radius: 3;", 
                "-fx-background-color: " + LIGHT_ACCENT_COLOR + "; -fx-text-fill: white; -fx-background-radius: 3;");
                
            private final Button deleteButton = createStyledButton("Удалить", 
                "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3;", 
                "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 3;");
                
            {
                detailsButton.setPrefWidth(90);
                deleteButton.setPrefWidth(90);
                
                detailsButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    controller.showOrderDetails(order);
                });
                
                deleteButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    controller.deleteOrder(order.getId());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttonsBox = new HBox(5);
                    buttonsBox.getChildren().addAll(detailsButton, deleteButton);
                    setGraphic(buttonsBox);
                }
            }
        });
        
        orderTable.getColumns().addAll(idColumn, userColumn, orderDateColumn, deliveryDateColumn, totalCostColumn, statusColumn, actionsColumn);
        
        vbox.getChildren().addAll(titleBox, orderTable);
        
        return vbox;
    }
    
    // Создание стилизованной кнопки
    private Button createStyledButton(String text, String style, String hoverStyle) {
        Button button = new Button(text);
        button.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        button.setStyle(style);
        
        // Эффекты при наведении
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(style));
        
        return button;
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
        
        // Стилизация диалогового окна
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-width: 1;");
        
        // Стилизация кнопок
        dialogPane.getButtonTypes().forEach(buttonType -> {
            Button button = (Button) dialogPane.lookupButton(buttonType);
            button.setStyle("-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; -fx-background-radius: 5;");
        });
        
        alert.showAndWait();
    }

    //Показать или скрыть индикатор загрузки
    //show true - показать, false - скрыть
    public void showLoadingIndicator(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
    }
} 