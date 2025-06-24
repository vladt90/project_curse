package com.olineshop.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.olineshop.dao.OrderDAO;
import com.olineshop.dao.ProductDAO;
import com.olineshop.dao.UserDAO;
import com.olineshop.model.Order;
import com.olineshop.model.OrderItem;
import com.olineshop.model.Product;
import com.olineshop.model.User;
import com.olineshop.view.LoginView;
import com.olineshop.view.MainClientView;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


 //Конт для клиентов
 
public class ClientController {
    private final MainClientView view;
    private final Stage primaryStage;
    private final User currentUser;
    
    private final ProductDAO productDAO;
    private final OrderDAO orderDAO;
    private final UserDAO userDAO;
    
    private final ObservableList<Product> products;
    private final ObservableList<OrderItem> cartItems;
    private final ObservableList<Order> orders;
    
    
    
     //view клиентская часть
     //primaryStage окно
     //currentUser ттек пользователь
    public ClientController(MainClientView view, Stage primaryStage, User currentUser) {
        this.view = view;
        this.primaryStage = primaryStage;
        this.currentUser = currentUser;
        
        this.productDAO = new ProductDAO();
        this.orderDAO = new OrderDAO();
        this.userDAO = new UserDAO();
        
        this.products = FXCollections.observableArrayList();
        this.cartItems = FXCollections.observableArrayList();
        this.orders = FXCollections.observableArrayList();
    }

    //загрузка товаров(сп)
    public void loadProducts() {
        products.clear();
        products.addAll(productDAO.getAllProducts());
        view.updateProductTable(products);
    }
    
    //поиск товаров по названию
     //searchTerm строка для поиска
    public void searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            //показ всех товаров
            loadProducts();
            return;
        }
        
        products.clear();
        products.addAll(productDAO.searchProductsByName(searchTerm));
        view.updateProductTable(products);
    }
    
    //фильтр цены
     //minPrice мин цена
     //maxPrice мак цена
    public void filterProductsByPrice(double minPrice, double maxPrice) {
        products.clear();
        products.addAll(productDAO.filterProductsByPrice(minPrice, maxPrice));
        view.updateProductTable(products);
    }

    // для истории бож (главное не забыть!!!!)
    public void loadOrderHistory() {
        orders.clear();
        orders.addAll(orderDAO.getOrdersByUser(currentUser.getId()));
        view.updateOrderHistoryTable(orders);
    }

    //добавление в корзину
    //product товар
    //quantity количество
    public void addToCart(Product product, int quantity) {
        // есть ли на складе
        if (product.getStockQuantity() < quantity) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Недостаточно товара на складе");
            return;
        }
        
        // есть ли в корзине
        for (OrderItem item : cartItems) {
            if (item.getProduct().getId() == product.getId()) {
                // Товар уже есть
                int newQuantity = item.getQuantity() + quantity;
                
                // хватает ли на складе
                if (product.getStockQuantity() < newQuantity) {
                    view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Недостаточно товара на складе");
                    return;
                }
                
                item.setQuantity(newQuantity);
                updateCartView();
                return;
            }
        }
        
        // Товара нет в корзине
        OrderItem newItem = new OrderItem();
        newItem.setProduct(product);
        newItem.setQuantity(quantity);
        newItem.setPrice(product.getPrice());
        
        cartItems.add(newItem);
        updateCartView();
    }

    //Делитнуть из корзины
    //item товар кот хотим удалить
    public void removeFromCart(OrderItem item) {
        cartItems.remove(item);
        updateCartView();
    }

    //обнова корзины
    private void updateCartView() {
        double totalPrice = calculateTotalPrice();
        view.updateCartTable(cartItems, totalPrice);
    }

    //рассчет суммы
    private double calculateTotalPrice() {
        double total = 0.0;
        
        for (OrderItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }
        
        // делаем скидку
        total = total * (1 - currentUser.getDiscount());
        
        // применяем доп скидку 
        if (total > 5000) {
            // поменять!!!!
            total = total * 0.95;
        }
        
        return total;
    }
    
    //окно подтверждение заказа
    public void showOrderConfirmationDialog() {
        if (cartItems.isEmpty()) {
            view.showAlert(Alert.AlertType.WARNING, "Предупреждение", "Корзина пуста");
            return;
        }
        
        // Создание окна
        Stage confirmStage = new Stage();
        confirmStage.setTitle("Подтверждение заказа");
        confirmStage.initModality(Modality.WINDOW_MODAL);
        confirmStage.initOwner(primaryStage);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        // Инфа о заказе
        Label titleLabel = new Label("Подтверждение заказа");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Таблица товаров в зак
        TableView<OrderItem> itemsTable = new TableView<>();
        
        // Столбец с названием товара
        TableColumn<OrderItem, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getProduct().getName());
        });
        nameColumn.setPrefWidth(200);
        
        // Столбец с ценой товара
        TableColumn<OrderItem, Double> priceColumn = new TableColumn<>("Цена (руб.)");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        // Столбец с количеством товара
        TableColumn<OrderItem, Integer> quantityColumn = new TableColumn<>("Количество");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        // Столбец с суммой
        TableColumn<OrderItem, Double> subtotalColumn = new TableColumn<>("Сумма (руб.)");
        subtotalColumn.setCellValueFactory(cellData -> {
            double subtotal = cellData.getValue().getPrice() * cellData.getValue().getQuantity();
            return new javafx.beans.property.SimpleDoubleProperty(subtotal).asObject();
        });
        
        // add солбцы в табл
        itemsTable.getColumns().addAll(nameColumn, priceColumn, quantityColumn, subtotalColumn);
        
        // Заполняем таблицу 
        itemsTable.setItems(FXCollections.observableArrayList(cartItems));
        
        // Инфа о скидке и сумме в итоге
        double totalPrice = calculateTotalPrice();
        double originalPrice = 0.0;
        for (OrderItem item : cartItems) {
            originalPrice += item.getPrice() * item.getQuantity();
        }
        
        Label discountLabel = new Label(String.format("Скидка: %.2f%%", currentUser.getDiscount() * 100));
        Label additionalDiscountLabel = new Label();
        if (originalPrice > 5000) {
            additionalDiscountLabel.setText("Дополнительная скидка за заказ от 5000 руб.: 5%");
        } else {
            additionalDiscountLabel.setText("Для получения дополнительной скидки 5% сумма заказа должна быть более 5000 руб.");
        }
        
        Label totalLabel = new Label(String.format("Итоговая сумма: %.2f руб.", totalPrice));
        totalLabel.setStyle("-fx-font-weight: bold;");
        
        // Кнопки
        Button confirmButton = new Button("Подтвердить заказ");
        confirmButton.setOnAction(e -> {
            checkout();
            confirmStage.close();
        });
        
        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> confirmStage.close());
        
        vbox.getChildren().addAll(
                titleLabel, 
                itemsTable, 
                discountLabel, 
                additionalDiscountLabel, 
                totalLabel, 
                new javafx.scene.layout.HBox(10, confirmButton, cancelButton)
        );
        
        Scene scene = new Scene(vbox, 600, 400);
        confirmStage.setScene(scene);
        confirmStage.show();
    }

    //оформить заказ
    public void checkout() {
        if (cartItems.isEmpty()) {
            view.showAlert(Alert.AlertType.WARNING, "Предупреждение", "Корзина пуста");
            return;
        }
        
        // создатьь новый заказ
        Order order = new Order();
        order.setUser(currentUser);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalCost(calculateTotalPrice());
        order.setStatus("В обработке");
        
        // Добавить товары в заказ
        List<OrderItem> items = new ArrayList<>(cartItems);
        for (OrderItem item : items) {
            item.setOrder(order);
        }
        
        order.setItems(items);
        
        // сохраняем заказ в базе данных
        boolean success = orderDAO.addOrder(order);
        
        if (success) {
            // проверка скидки
            if (order.getTotalCost() > 5000 && currentUser.getDiscount() == 0) {
                // становится постоянным клиентом и скидку
                currentUser.setDiscount(0.02);
                userDAO.updateUser(currentUser);
            }
            
            view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Заказ успешно оформлен");
            
            // del корзину
            cartItems.clear();
            updateCartView();
            
            // Обновляем историю заказов
            loadOrderHistory();
        } else {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось оформить заказ");
        }
    }

    //подробности заказа
    //order заказ для просм
    public void showOrderDetails(Order order) {
        // окно для отображения подробностей заказа
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Подробности заказа №" + order.getId());
        detailsStage.initModality(Modality.WINDOW_MODAL);
        detailsStage.initOwner(primaryStage);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        // Инфа о заказе
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(0, 0, 10, 0));
        
        infoGrid.add(new Label("Номер заказа:"), 0, 0);
        infoGrid.add(new Label(String.valueOf(order.getId())), 1, 0);
        
        infoGrid.add(new Label("Дата заказа:"), 0, 1);
        infoGrid.add(new Label(order.getOrderDate().toString()), 1, 1);
        
        infoGrid.add(new Label("Дата доставки:"), 0, 2);
        infoGrid.add(new Label(order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : "Не указана"), 1, 2);
        
        infoGrid.add(new Label("Статус:"), 0, 3);
        infoGrid.add(new Label(order.getStatus()), 1, 3);
        
        infoGrid.add(new Label("Итоговая стоимость:"), 0, 4);
        infoGrid.add(new Label(String.format("%.2f руб.", order.getTotalCost())), 1, 4);
        
        // Таблица товаров в зак
        TableView<OrderItem> itemsTable = new TableView<>();
        
        // солбц с названием товара
        TableColumn<OrderItem, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getProduct().getName());
        });
        nameColumn.setPrefWidth(200);
        
        // солбц с ценой товара
        TableColumn<OrderItem, Double> priceColumn = new TableColumn<>("Цена (руб.)");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        // солбц с количеством товара
        TableColumn<OrderItem, Integer> quantityColumn = new TableColumn<>("Количество");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        // солбц с суммой
        TableColumn<OrderItem, Double> subtotalColumn = new TableColumn<>("Сумма (руб.)");
        subtotalColumn.setCellValueFactory(cellData -> {
            double subtotal = cellData.getValue().getPrice() * cellData.getValue().getQuantity();
            return new javafx.beans.property.SimpleDoubleProperty(subtotal).asObject();
        });
        
        // Добавляем солбцы в таблицу
        itemsTable.getColumns().addAll(nameColumn, priceColumn, quantityColumn, subtotalColumn);
        
        // Заполняем таблицу
        itemsTable.setItems(FXCollections.observableArrayList(order.getItems()));
        
        // кнопка закрыть
        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> detailsStage.close());
        
        vbox.getChildren().addAll(infoGrid, new Label("Товары в заказе:"), itemsTable, closeButton);
        
        Scene scene = new Scene(vbox, 600, 400);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    //выход из системы
    public void handleLogout() {
        // закрываем главное окно
        primaryStage.close();
        
        // открываем окно входа
        LoginView loginView = new LoginView();
        loginView.start(new Stage());
    }
} 