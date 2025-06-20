package com.olineshop.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
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
import com.olineshop.view.MainAdminView;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Контроллер для административной части приложения
 */
public class AdminController {
    private final MainAdminView view;
    private final Stage primaryStage;
    
    private final ProductDAO productDAO;
    private final UserDAO userDAO;
    private final OrderDAO orderDAO;
    
    private final ObservableList<Product> products;
    private final ObservableList<User> users;
    private final ObservableList<Order> orders;
    
    /**
     * Конструктор контроллера
     * 
     * @param view представление административной части
     * @param primaryStage главное окно приложения
     */
    public AdminController(MainAdminView view, Stage primaryStage) {
        this.view = view;
        this.primaryStage = primaryStage;
        
        this.productDAO = new ProductDAO();
        this.userDAO = new UserDAO();
        this.orderDAO = new OrderDAO();
        
        this.products = FXCollections.observableArrayList();
        this.users = FXCollections.observableArrayList();
        this.orders = FXCollections.observableArrayList();
    }

    /**
     * Загрузить список товаров
     */
    public void loadProducts() {
        products.clear();
        products.addAll(productDAO.getAllProducts());
        view.updateProductTable(products);
    }

    /**
     * Загрузить список пользователей
     */
    public void loadUsers() {
        users.clear();
        users.addAll(userDAO.getAllUsers());
        view.updateUserTable(users);
    }

    /**
     * Загрузить список заказов
     */
    public void loadOrders() {
        orders.clear();
        orders.addAll(orderDAO.getAllOrders());
        view.updateOrderTable(orders);
    }

    /**
     * Добавить новый товар
     * 
     * @param name название товара
     * @param price цена товара
     * @param unit единица измерения
     * @param quantity количество на складе
     */
    public void addProduct(String name, double price, String unit, int quantity) {
        // Проверяем, что поля не пустые
        if (name == null || name.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите название товара");
            return;
        }
        
        if (unit == null || unit.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите единицу измерения");
            return;
        }
        
        // Проверяем, что цена и количество положительные
        if (price <= 0) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Цена должна быть положительной");
            return;
        }
        
        if (quantity < 0) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Количество не может быть отрицательным");
            return;
        }
        
        // Создаем новый товар
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setUnit(unit);
        product.setStockQuantity(quantity);
        
        // Добавляем товар в базу данных
        boolean success = productDAO.addProduct(product);
        
        if (success) {
            view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Товар успешно добавлен");
            loadProducts();
        } else {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить товар");
        }
    }

    /**
     * Обновить существующий товар
     * 
     * @param id идентификатор товара
     * @param name новое название товара
     * @param price новая цена товара
     * @param unit новая единица измерения
     * @param quantity новое количество на складе
     */
    public void updateProduct(int id, String name, double price, String unit, int quantity) {
        // Проверяем, что поля не пустые
        if (name == null || name.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите название товара");
            return;
        }
        
        if (unit == null || unit.trim().isEmpty()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Введите единицу измерения");
            return;
        }
        
        // Проверяем, что цена и количество положительные
        if (price <= 0) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Цена должна быть положительной");
            return;
        }
        
        if (quantity < 0) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Количество не может быть отрицательным");
            return;
        }
        
        // Получаем товар из базы данных
        Product product = productDAO.getProductById(id);
        
        if (product == null) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Товар не найден");
            return;
        }
        
        // Обновляем данные товара
        product.setName(name);
        product.setPrice(price);
        product.setUnit(unit);
        product.setStockQuantity(quantity);
        
        // Обновляем товар в базе данных
        boolean success = productDAO.updateProduct(product);
        
        if (success) {
            view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Товар успешно обновлен");
            loadProducts();
        } else {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось обновить товар");
        }
    }

    /**
     * Удалить товар
     * 
     * @param id идентификатор товара для удаления
     */
    public void deleteProduct(int id) {
        // Запрашиваем подтверждение удаления
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText(null);
        alert.setContentText("Вы действительно хотите удалить товар?");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Удаляем товар из базы данных
            boolean success = productDAO.deleteProduct(id);
            
            if (success) {
                view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Товар успешно удален");
                loadProducts();
            } else {
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить товар");
            }
        }
    }

    /**
     * Показать подробную информацию о пользователе
     * 
     * @param user пользователь для просмотра
     */
    public void showUserDetails(User user) {
        // Создаем новое окно для отображения подробностей пользователя
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Информация о пользователе: " + user.getFullName());
        detailsStage.initModality(Modality.WINDOW_MODAL);
        detailsStage.initOwner(primaryStage);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        // Информация о пользователе
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(0, 0, 10, 0));
        
        infoGrid.add(new Label("ID:"), 0, 0);
        infoGrid.add(new Label(String.valueOf(user.getId())), 1, 0);
        
        infoGrid.add(new Label("Логин:"), 0, 1);
        infoGrid.add(new Label(user.getLogin()), 1, 1);
        
        infoGrid.add(new Label("Имя:"), 0, 2);
        infoGrid.add(new Label(user.getFirstName()), 1, 2);
        
        infoGrid.add(new Label("Фамилия:"), 0, 3);
        infoGrid.add(new Label(user.getLastName()), 1, 3);
        
        infoGrid.add(new Label("Email:"), 0, 4);
        infoGrid.add(new Label(user.getEmail()), 1, 4);
        
        infoGrid.add(new Label("Телефон:"), 0, 5);
        infoGrid.add(new Label(user.getPhone() != null ? user.getPhone() : "Не указан"), 1, 5);
        
        infoGrid.add(new Label("Скидка:"), 0, 6);
        infoGrid.add(new Label(String.format("%.2f%%", user.getDiscount() * 100)), 1, 6);
        
        infoGrid.add(new Label("Роль:"), 0, 7);
        infoGrid.add(new Label(user.getRole().getName()), 1, 7);
        
        // Форма для изменения скидки
        HBox discountBox = new HBox(10);
        discountBox.setPadding(new Insets(10, 0, 10, 0));
        
        Label discountLabel = new Label("Изменить скидку (%):");
        TextField discountField = new TextField(String.format("%.2f", user.getDiscount() * 100));
        Button updateDiscountButton = new Button("Обновить");
        
        updateDiscountButton.setOnAction(e -> {
            try {
                double discount = Double.parseDouble(discountField.getText()) / 100.0;
                
                if (discount < 0 || discount > 1) {
                    view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Скидка должна быть от 0 до 100%");
                    return;
                }
                
                user.setDiscount(discount);
                boolean success = userDAO.updateUser(user);
                
                if (success) {
                    view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Скидка успешно обновлена");
                    loadUsers();
                } else {
                    view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось обновить скидку");
                }
            } catch (NumberFormatException ex) {
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректный формат числа");
            }
        });
        
        discountBox.getChildren().addAll(discountLabel, discountField, updateDiscountButton);
        
        // Таблица заказов пользователя
        Label ordersLabel = new Label("Заказы пользователя:");
        
        TableView<Order> ordersTable = new TableView<>();
        
        // Столбец с ID заказа
        TableColumn<Order, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        // Столбец с датой заказа
        TableColumn<Order, String> orderDateColumn = new TableColumn<>("Дата заказа");
        orderDateColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getOrderDate().toString());
        });
        orderDateColumn.setPrefWidth(150);
        
        // Столбец с общей стоимостью
        TableColumn<Order, Double> totalCostColumn = new TableColumn<>("Сумма (руб.)");
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        // Столбец со статусом
        TableColumn<Order, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Добавляем столбцы в таблицу
        ordersTable.getColumns().addAll(idColumn, orderDateColumn, totalCostColumn, statusColumn);
        
        // Заполняем таблицу данными
        ordersTable.setItems(FXCollections.observableArrayList(orderDAO.getOrdersByUser(user.getId())));
        
        // Кнопка "Закрыть"
        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> detailsStage.close());
        
        vbox.getChildren().addAll(infoGrid, discountBox, ordersLabel, ordersTable, closeButton);
        
        Scene scene = new Scene(vbox, 600, 500);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    /**
     * Удалить пользователя
     * 
     * @param id идентификатор пользователя для удаления
     */
    public void deleteUser(int id) {
        // Проверяем, что пользователь не является администратором
        User user = userDAO.getUserById(id);
        
        if (user == null) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Пользователь не найден");
            return;
        }
        
        if (user.isAdmin()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Нельзя удалить администратора");
            return;
        }
        
        // Запрашиваем подтверждение удаления
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText(null);
        alert.setContentText("Вы действительно хотите удалить пользователя " + user.getFullName() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Удаляем пользователя из базы данных
            boolean success = userDAO.deleteUser(id);
            
            if (success) {
                view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Пользователь успешно удален");
                loadUsers();
            } else {
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить пользователя");
            }
        }
    }

    /**
     * Показать подробную информацию о заказе
     * 
     * @param order заказ для просмотра
     */
    public void showOrderDetails(Order order) {
        // Создаем новое окно для отображения подробностей заказа
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Подробности заказа №" + order.getId());
        detailsStage.initModality(Modality.WINDOW_MODAL);
        detailsStage.initOwner(primaryStage);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        // Информация о заказе
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(0, 0, 10, 0));
        
        infoGrid.add(new Label("Номер заказа:"), 0, 0);
        infoGrid.add(new Label(String.valueOf(order.getId())), 1, 0);
        
        infoGrid.add(new Label("Пользователь:"), 0, 1);
        infoGrid.add(new Label(order.getUser().getFullName()), 1, 1);
        
        infoGrid.add(new Label("Email:"), 0, 2);
        infoGrid.add(new Label(order.getUser().getEmail()), 1, 2);
        
        infoGrid.add(new Label("Телефон:"), 0, 3);
        infoGrid.add(new Label(order.getUser().getPhone() != null ? order.getUser().getPhone() : "Не указан"), 1, 3);
        
        infoGrid.add(new Label("Дата заказа:"), 0, 4);
        infoGrid.add(new Label(order.getOrderDate().toString()), 1, 4);
        
        infoGrid.add(new Label("Дата доставки:"), 0, 5);
        infoGrid.add(new Label(order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : "Не указана"), 1, 5);
        
        infoGrid.add(new Label("Статус:"), 0, 6);
        infoGrid.add(new Label(order.getStatus()), 1, 6);
        
        infoGrid.add(new Label("Итоговая стоимость:"), 0, 7);
        infoGrid.add(new Label(String.format("%.2f руб.", order.getTotalCost())), 1, 7);
        
        // Таблица товаров в заказе
        Label itemsLabel = new Label("Товары в заказе:");
        
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
        
        // Добавляем столбцы в таблицу
        itemsTable.getColumns().addAll(nameColumn, priceColumn, quantityColumn, subtotalColumn);
        
        // Заполняем таблицу данными
        itemsTable.setItems(FXCollections.observableArrayList(order.getItems()));
        
        // Кнопки
        HBox buttonBox = new HBox(10);
        
        Button changeStatusButton = new Button("Изменить статус");
        changeStatusButton.setOnAction(e -> {
            showChangeStatusDialog(order);
            detailsStage.close();
        });
        
        Button setDeliveryDateButton = new Button("Указать дату доставки");
        setDeliveryDateButton.setOnAction(e -> {
            showSetDeliveryDateDialog(order);
            detailsStage.close();
        });
        
        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> detailsStage.close());
        
        buttonBox.getChildren().addAll(changeStatusButton, setDeliveryDateButton, closeButton);
        
        vbox.getChildren().addAll(infoGrid, itemsLabel, itemsTable, buttonBox);
        
        Scene scene = new Scene(vbox, 600, 500);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    /**
     * Показать диалог изменения статуса заказа
     * 
     * @param order заказ для изменения статуса
     */
    public void showChangeStatusDialog(Order order) {
        // Создаем новое окно для изменения статуса заказа
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Изменение статуса заказа №" + order.getId());
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        // Информация о текущем статусе
        Label currentStatusLabel = new Label("Текущий статус: " + order.getStatus());
        
        // Выбор нового статуса
        Label newStatusLabel = new Label("Новый статус:");
        
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("В обработке", "Отправлен", "Доставлен", "Отменен");
        statusComboBox.setValue(order.getStatus());
        
        // Кнопки
        HBox buttonBox = new HBox(10);
        
        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(e -> {
            String newStatus = statusComboBox.getValue();
            
            if (newStatus != null && !newStatus.equals(order.getStatus())) {
                boolean success = orderDAO.updateOrderStatus(order.getId(), newStatus);
                
                if (success) {
                    view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Статус заказа успешно изменен");
                    loadOrders();
                    dialogStage.close();
                } else {
                    view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось изменить статус заказа");
                }
            } else {
                dialogStage.close();
            }
        });
        
        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> dialogStage.close());
        
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        
        vbox.getChildren().addAll(currentStatusLabel, newStatusLabel, statusComboBox, buttonBox);
        
        Scene scene = new Scene(vbox, 300, 150);
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    /**
     * Показать диалог установки даты доставки заказа
     * 
     * @param order заказ для установки даты доставки
     */
    public void showSetDeliveryDateDialog(Order order) {
        // Создаем новое окно для установки даты доставки заказа
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Установка даты доставки заказа №" + order.getId());
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        // Информация о текущей дате доставки
        Label currentDateLabel = new Label("Текущая дата доставки: " + 
                (order.getDeliveryDate() != null ? order.getDeliveryDate().toString() : "Не указана"));
        
        // Выбор новой даты доставки
        Label newDateLabel = new Label("Новая дата доставки (ГГГГ-ММ-ДД):");
        
        TextField dateField = new TextField();
        if (order.getDeliveryDate() != null) {
            dateField.setText(order.getDeliveryDate().toLocalDate().toString());
        }
        
        // Кнопки
        HBox buttonBox = new HBox(10);
        
        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(e -> {
            String dateStr = dateField.getText();
            
            if (dateStr != null && !dateStr.trim().isEmpty()) {
                try {
                    java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                    LocalDateTime deliveryDate = date.atTime(12, 0); // Устанавливаем время на 12:00
                    
                    boolean success = orderDAO.updateDeliveryDate(order.getId(), deliveryDate);
                    
                    if (success) {
                        view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Дата доставки успешно установлена");
                        loadOrders();
                        dialogStage.close();
                    } else {
                        view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось установить дату доставки");
                    }
                } catch (Exception ex) {
                    view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Некорректный формат даты. Используйте формат ГГГГ-ММ-ДД");
                }
            } else {
                // Если поле пустое, устанавливаем null (удаляем дату доставки)
                boolean success = orderDAO.updateDeliveryDate(order.getId(), null);
                
                if (success) {
                    view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Дата доставки успешно удалена");
                    loadOrders();
                    dialogStage.close();
                } else {
                    view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить дату доставки");
                }
            }
        });
        
        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> dialogStage.close());
        
        buttonBox.getChildren().addAll(saveButton, cancelButton);
        
        vbox.getChildren().addAll(currentDateLabel, newDateLabel, dateField, buttonBox);
        
        Scene scene = new Scene(vbox, 350, 150);
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    /**
     * Удалить заказ
     * 
     * @param id идентификатор заказа для удаления
     */
    public void deleteOrder(int id) {
        // Запрашиваем подтверждение удаления
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText(null);
        alert.setContentText("Вы действительно хотите удалить заказ №" + id + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Удаляем заказ из базы данных
            boolean success = orderDAO.deleteOrder(id);
            
            if (success) {
                view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Заказ успешно удален");
                loadOrders();
            } else {
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить заказ");
            }
        }
    }

    /**
     * Обработать выход из системы
     */
    public void handleLogout() {
        // Закрываем главное окно
        primaryStage.close();
        
        // Открываем окно входа
        LoginView loginView = new LoginView();
        loginView.start(new Stage());
    }
} 