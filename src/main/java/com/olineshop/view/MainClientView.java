package com.olineshop.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.olineshop.controller.ClientController;
import com.olineshop.model.Order;
import com.olineshop.model.OrderItem;
import com.olineshop.model.Product;
import com.olineshop.model.User;

import java.time.LocalDateTime;

//Класс представления главного окна клиентской части приложения
public class MainClientView {
    private ClientController controller;
    private User currentUser;
    private TableView<Product> productTable;
    private TableView<OrderItem> cartTable;
    private TableView<Order> orderHistoryTable;
    private Label totalPriceLabel;
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

    //Конструктор класса
    //user текущий пользователь
    public MainClientView(User user) {
        this.currentUser = user;
    }

    //Запустить главное окно клиентской части
    //primaryStage главное окно приложения
    public void start(Stage primaryStage) {
        this.controller = new ClientController(this, primaryStage, currentUser);

        primaryStage.setTitle("Интернет-магазин - Клиент: " + currentUser.getFullName());

        BorderPane borderPane = new BorderPane();
        borderPane.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        HBox topPanel = createTopPanel();
        borderPane.setTop(topPanel);
        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");
        
        // Создаем вкладки
        Tab catalogTab = createTab("Каталог товаров", "\uf07a"); // иконка корзины
        VBox catalogBox = createCatalogTab();
        catalogTab.setContent(catalogBox);
        
        Tab cartTab = createTab("Корзина", "\uf07a"); // иконка корзины
        VBox cartBox = createCartTab();
        cartTab.setContent(cartBox);
        
        Tab orderHistoryTab = createTab("История заказов", "\uf1da"); // иконка истории
        VBox orderHistoryBox = createOrderHistoryTab();
        orderHistoryTab.setContent(orderHistoryBox);
        
        tabPane.getTabs().addAll(catalogTab, cartTab, orderHistoryTab);
        
        // Добавляем обработчик события переключения вкладок для актуализации данных
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == catalogTab) {
                controller.loadProducts();
            } else if (newTab == cartTab) {
                controller.updateCart();
            } else if (newTab == orderHistoryTab) {
                controller.loadOrderHistory();
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
        
        controller.loadProducts();
        controller.loadOrderHistory();
    }
    
    // Создание вкладки с иконкой
    private Tab createTab(String title, String iconCode) {
        Tab tab = new Tab();
        
        Label label = new Label(title);
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        
        tab.setGraphic(label);
        
        return tab;
    }

    //Создать верхнюю панель с информацией о пользователе
    //return панель с информацией о пользователе
    private HBox createTopPanel() {
        HBox topPanel = new HBox();
        topPanel.setPadding(new Insets(15));
        topPanel.setAlignment(Pos.CENTER_LEFT);
        topPanel.setSpacing(20);
        topPanel.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";");
        
        Label userLabel = new Label("Пользователь: " + currentUser.getFullName());
        userLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        userLabel.setTextFill(Color.WHITE);
        
        Label discountLabel = new Label("Ваша скидка: " + (currentUser.getDiscount() * 100) + "%");
        discountLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        discountLabel.setTextFill(Color.WHITE);
        
        HBox spacer = new HBox();
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button editProfileButton = new Button("Изменить данные");
        editProfileButton.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        editProfileButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        editProfileButton.setOnMouseEntered(e -> editProfileButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;"));
        editProfileButton.setOnMouseExited(e -> editProfileButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;"));
        
        editProfileButton.setOnAction(e -> controller.showEditProfileDialog());
        
        Button logoutButton = new Button("Выйти");
        logoutButton.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        logoutButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;");
        
        logoutButton.setOnMouseEntered(e -> logoutButton.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;"));
        logoutButton.setOnMouseExited(e -> logoutButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;"));
        
        logoutButton.setOnAction(e -> controller.handleLogout());
        
        topPanel.getChildren().addAll(userLabel, discountLabel, spacer, editProfileButton, logoutButton);
        
        return topPanel;
    }

    //Создать вкладку "Каталог товаров"
    //return контейнер с содержимым вкладки "Каталог товаров"
    private VBox createCatalogTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_CENTER);
        
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Text title = new Text("Доступные товары");
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
        nameColumn.setPrefWidth(300);
        
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
        
        // Панель с элементами управления
        HBox controlPanel = new HBox(15);
        controlPanel.setAlignment(Pos.CENTER_LEFT);
        controlPanel.setPadding(new Insets(10, 0, 0, 0));
        
        Label quantityLabel = new Label("Количество:");
        quantityLabel.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 100, 1);
        quantitySpinner.setEditable(true);
        quantitySpinner.setPrefWidth(100);
        quantitySpinner.getEditor().setStyle("-fx-font-size: 14px;");
        
        Button addToCartButton = createStyledButton("Добавить в корзину", BUTTON_STYLE, BUTTON_HOVER_STYLE);
        addToCartButton.setOnAction(e -> {
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                int quantity = quantitySpinner.getValue();
                controller.addToCart(selectedProduct, quantity);
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите товар для добавления в корзину");
            }
        });
        
        controlPanel.getChildren().addAll(quantityLabel, quantitySpinner, addToCartButton);
        
        vbox.getChildren().addAll(titleBox, productTable, controlPanel);
        
        return vbox;
    }

    //Создать вкладку "Корзина"
    //return контейнер с содержимым вкладки "Корзина"
    private VBox createCartTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_CENTER);
        
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Text title = new Text("Товары в корзине");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setFill(Color.web(PRIMARY_COLOR));
        
        titleBox.getChildren().add(title);
        
        // Создаем таблицу корзины с современным стилем
        cartTable = new TableView<>();
        cartTable.setStyle(TABLE_STYLE);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<OrderItem, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getProduct().getName());
        });
        nameColumn.setPrefWidth(300);
        
        TableColumn<OrderItem, Double> priceColumn = new TableColumn<>("Цена (руб.)");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceColumn.setMaxWidth(120);
        
        TableColumn<OrderItem, Integer> quantityColumn = new TableColumn<>("Количество");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        quantityColumn.setMaxWidth(100);
        
        TableColumn<OrderItem, Double> subtotalColumn = new TableColumn<>("Сумма (руб.)");
        subtotalColumn.setCellValueFactory(cellData -> {
            double subtotal = cellData.getValue().getPrice() * cellData.getValue().getQuantity();
            return new javafx.beans.property.SimpleDoubleProperty(subtotal).asObject();
        });
        subtotalColumn.setMaxWidth(120);
        
        cartTable.getColumns().addAll(nameColumn, priceColumn, quantityColumn, subtotalColumn);
        
        // Панель с итоговой суммой и кнопками
        HBox bottomPanel = new HBox(15);
        bottomPanel.setAlignment(Pos.CENTER_RIGHT);
        bottomPanel.setPadding(new Insets(10, 0, 0, 0));
        
        totalPriceLabel = new Label("Итого: 0.00 руб.");
        totalPriceLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        totalPriceLabel.setTextFill(Color.web(PRIMARY_COLOR));
        
        HBox spacer = new HBox();
        spacer.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button removeFromCartButton = createStyledButton("Удалить из корзины", 
            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;", 
            "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        removeFromCartButton.setOnAction(e -> {
            OrderItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                controller.removeFromCart(selectedItem);
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите товар для удаления из корзины");
            }
        });
        
        Button checkoutButton = createStyledButton("Оформить заказ", 
            "-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;", 
            "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        
        checkoutButton.setOnAction(e -> controller.checkout());
        
        bottomPanel.getChildren().addAll(totalPriceLabel, spacer, removeFromCartButton, checkoutButton);
        
        vbox.getChildren().addAll(titleBox, cartTable, bottomPanel);
        
        return vbox;
    }

    //Создать вкладку "История заказов"
    //return контейнер с содержимым вкладки "История заказов"
    private VBox createOrderHistoryTab() {
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_CENTER);
        
        HBox titleBox = new HBox();
        titleBox.setAlignment(Pos.CENTER_LEFT);
        
        Text title = new Text("История заказов");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        title.setFill(Color.web(PRIMARY_COLOR));
        
        titleBox.getChildren().add(title);
        
        // Создаем таблицу истории заказов с современным стилем
        orderHistoryTable = new TableView<>();
        orderHistoryTable.setStyle(TABLE_STYLE);
        orderHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Order, Integer> idColumn = new TableColumn<>("№");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setMaxWidth(50);
        
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
        totalCostColumn.setMaxWidth(120);
        
        TableColumn<Order, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> {
            return new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (item == null || empty) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        
                        // Стилизация ячейки в зависимости от статуса
                        switch (item) {
                            case "Новый":
                                setStyle("-fx-text-fill: #3498db;");
                                break;
                            case "В обработке":
                                setStyle("-fx-text-fill: #f39c12;");
                                break;
                            case "Отправлен":
                                setStyle("-fx-text-fill: #27ae60;");
                                break;
                            case "Доставлен":
                                setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                                break;
                            case "Отменен":
                                setStyle("-fx-text-fill: #e74c3c;");
                                break;
                            default:
                                setStyle("");
                                break;
                        }
                    }
                }
            };
        });
        statusColumn.setPrefWidth(120);
        
        TableColumn<Order, Void> detailsColumn = new TableColumn<>("Действия");
        detailsColumn.setPrefWidth(100);
        detailsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button detailsButton = createStyledButton("Подробнее", 
                "-fx-background-color: " + ACCENT_COLOR + "; -fx-text-fill: white; -fx-background-radius: 3;", 
                "-fx-background-color: " + LIGHT_ACCENT_COLOR + "; -fx-text-fill: white; -fx-background-radius: 3;");
            
            {
                detailsButton.setOnAction(event -> {
                    Order order = getTableView().getItems().get(getIndex());
                    controller.showOrderDetails(order);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsButton);
                }
            }
        });
        
        orderHistoryTable.getColumns().addAll(idColumn, orderDateColumn, deliveryDateColumn, totalCostColumn, statusColumn, detailsColumn);
        
        vbox.getChildren().addAll(titleBox, orderHistoryTable);
        
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

    //Обновить таблицу корзины
    //cartItems список товаров в корзине
    //totalPrice общая стоимость товаров в корзине
    public void updateCartTable(ObservableList<OrderItem> cartItems, double totalPrice) {
        cartTable.setItems(cartItems);
        totalPriceLabel.setText(String.format("Итого: %.2f руб.", totalPrice));
    }

    //Обновить таблицу истории заказов
    //orders список заказов
    public void updateOrderHistoryTable(ObservableList<Order> orders) {
        orderHistoryTable.setItems(orders);
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

    //Обновить информацию о пользователе
    //user обновленный пользователь
    public void updateUserInfo(User user) {
        this.currentUser = user;
        // Обновляем верхнюю панель
        BorderPane borderPane = (BorderPane) ((StackPane) totalPriceLabel.getScene().getRoot()).getChildren().get(0);
        borderPane.setTop(createTopPanel());
    }
    
    //Показать или скрыть индикатор загрузки
    //show true - показать, false - скрыть
    public void showLoadingIndicator(boolean show) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(show);
        }
    }
} 