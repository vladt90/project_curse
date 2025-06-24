package com.olineshop.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
        
        HBox topPanel = createTopPanel();
        borderPane.setTop(topPanel);
        
        TabPane tabPane = new TabPane();
        
        Tab catalogTab = new Tab("Каталог товаров");
        catalogTab.setClosable(false);
        VBox catalogBox = createCatalogTab();
        catalogTab.setContent(catalogBox);
        
        Tab cartTab = new Tab("Корзина");
        cartTab.setClosable(false);
        VBox cartBox = createCartTab();
        cartTab.setContent(cartBox);
        
        Tab orderHistoryTab = new Tab("История заказов");
        orderHistoryTab.setClosable(false);
        VBox orderHistoryBox = createOrderHistoryTab();
        orderHistoryTab.setContent(orderHistoryBox);
        
        tabPane.getTabs().addAll(catalogTab, cartTab, orderHistoryTab);
        
        borderPane.setCenter(tabPane);

        Scene scene = new Scene(borderPane, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        controller.loadProducts();
        controller.loadOrderHistory();
    }

    //Создать верхнюю панель с информацией о пользователе
    //return панель с информацией о пользователе
    private HBox createTopPanel() {
        HBox topPanel = new HBox(10);
        topPanel.setPadding(new Insets(10, 10, 10, 10));
        
        Label userLabel = new Label("Пользователь: " + currentUser.getFullName());
        userLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        
        Label discountLabel = new Label("Ваша скидка: " + (currentUser.getDiscount() * 100) + "%");
        discountLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
        
        Button logoutButton = new Button("Выйти");
        logoutButton.setOnAction(e -> controller.handleLogout());
        
        topPanel.getChildren().addAll(userLabel, discountLabel, logoutButton);
        
        return topPanel;
    }

    //Создать вкладку "Каталог товаров"
    //return контейнер с содержимым вкладки "Каталог товаров"
    private VBox createCatalogTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        Text title = new Text("Доступные товары");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        
        HBox searchPanel = new HBox(10);
        searchPanel.setPadding(new Insets(0, 0, 10, 0));
        
        Label searchLabel = new Label("Поиск:");
        TextField searchField = new TextField();
        searchField.setPrefWidth(200);
        Button searchButton = new Button("Найти");
        searchButton.setOnAction(e -> controller.searchProducts(searchField.getText()));
        
        Label priceLabel = new Label("Цена от:");
        TextField minPriceField = new TextField();
        minPriceField.setPrefWidth(80);
        Label toLabel = new Label("до:");
        TextField maxPriceField = new TextField();
        maxPriceField.setPrefWidth(80);
        Button filterButton = new Button("Применить");
        filterButton.setOnAction(e -> {
            try {
                double minPrice = minPriceField.getText().isEmpty() ? 0 : Double.parseDouble(minPriceField.getText());
                double maxPrice = maxPriceField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(maxPriceField.getText());
                
                if (minPrice < 0 || maxPrice < 0) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Цена не может быть отрицательной");
                    return;
                }
                
                if (minPrice > maxPrice) {
                    showAlert(Alert.AlertType.ERROR, "Ошибка", "Минимальная цена не может быть больше максимальной");
                    return;
                }
                
                controller.filterProductsByPrice(minPrice, maxPrice);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректный формат числа");
            }
        });
        
        Button resetButton = new Button("Сбросить");
        resetButton.setOnAction(e -> {
            searchField.clear();
            minPriceField.clear();
            maxPriceField.clear();
            controller.loadProducts();
        });
        
        searchPanel.getChildren().addAll(searchLabel, searchField, searchButton, priceLabel, minPriceField, toLabel, maxPriceField, filterButton, resetButton);
        
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
        
        Label quantityLabel = new Label("Количество:");
        Spinner<Integer> quantitySpinner = new Spinner<>(1, 100, 1);
        quantitySpinner.setEditable(true);
        
        Button addToCartButton = new Button("Добавить в корзину");
        addToCartButton.setOnAction(e -> {
            Product selectedProduct = productTable.getSelectionModel().getSelectedItem();
            if (selectedProduct != null) {
                int quantity = quantitySpinner.getValue();
                controller.addToCart(selectedProduct, quantity);
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите товар для добавления в корзину");
            }
        });
        
        buttonPanel.getChildren().addAll(quantityLabel, quantitySpinner, addToCartButton);
        
        vbox.getChildren().addAll(title, searchPanel, productTable, buttonPanel);
        
        return vbox;
    }

    //Создать вкладку "Корзина"
    //return контейнер с содержимым вкладки "Корзина"
    private VBox createCartTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        Text title = new Text("Товары в корзине");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        
        cartTable = new TableView<>();
        
        TableColumn<OrderItem, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getProduct().getName());
        });
        nameColumn.setPrefWidth(200);
        
        TableColumn<OrderItem, Double> priceColumn = new TableColumn<>("Цена (руб.)");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        TableColumn<OrderItem, Integer> quantityColumn = new TableColumn<>("Количество");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        TableColumn<OrderItem, Double> subtotalColumn = new TableColumn<>("Сумма (руб.)");
        subtotalColumn.setCellValueFactory(cellData -> {
            double subtotal = cellData.getValue().getPrice() * cellData.getValue().getQuantity();
            return new javafx.beans.property.SimpleDoubleProperty(subtotal).asObject();
        });
        
        cartTable.getColumns().addAll(nameColumn, priceColumn, quantityColumn, subtotalColumn);
        
        HBox bottomPanel = new HBox(10);
        
        totalPriceLabel = new Label("Итого: 0.00 руб.");
        totalPriceLabel.setFont(Font.font("Tahoma", FontWeight.BOLD, 14));
        
        Button removeFromCartButton = new Button("Удалить из корзины");
        removeFromCartButton.setOnAction(e -> {
            OrderItem selectedItem = cartTable.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                controller.removeFromCart(selectedItem);
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите товар для удаления из корзины");
            }
        });
        
        Button checkoutButton = new Button("Оформить заказ");
        checkoutButton.setOnAction(e -> controller.showOrderConfirmationDialog());
        
        bottomPanel.getChildren().addAll(totalPriceLabel, removeFromCartButton, checkoutButton);
        
        vbox.getChildren().addAll(title, cartTable, bottomPanel);
        
        return vbox;
    }

    //Создать вкладку "История заказов"
    //return контейнер с содержимым вкладки "История заказов"
    private VBox createOrderHistoryTab() {
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        Text title = new Text("История заказов");
        title.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
        
        orderHistoryTable = new TableView<>();
        
        TableColumn<Order, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Order, String> orderDateColumn = new TableColumn<>("Дата заказа");
        orderDateColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getOrderDate().toString());
        });
        orderDateColumn.setPrefWidth(150);
        
        TableColumn<Order, String> deliveryDateColumn = new TableColumn<>("Дата доставки");
        deliveryDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime deliveryDate = cellData.getValue().getDeliveryDate();
            return new javafx.beans.property.SimpleStringProperty(
                    deliveryDate != null ? deliveryDate.toString() : "Не указана");
        });
        deliveryDateColumn.setPrefWidth(150);
        
        TableColumn<Order, Double> totalCostColumn = new TableColumn<>("Сумма (руб.)");
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        TableColumn<Order, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        orderHistoryTable.getColumns().addAll(idColumn, orderDateColumn, deliveryDateColumn, totalCostColumn, statusColumn);
        
        Button detailsButton = new Button("Подробности заказа");
        detailsButton.setOnAction(e -> {
            Order selectedOrder = orderHistoryTable.getSelectionModel().getSelectedItem();
            if (selectedOrder != null) {
                controller.showOrderDetails(selectedOrder);
            } else {
                showAlert(Alert.AlertType.WARNING, "Предупреждение", "Выберите заказ для просмотра подробностей");
            }
        });
        
        vbox.getChildren().addAll(title, orderHistoryTable, detailsButton);
        
        return vbox;
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
    //alertType тип диалогового окна
    //title заголовок окна
    //message сообщение
    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 