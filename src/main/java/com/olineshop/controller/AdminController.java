package com.olineshop.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
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
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public class AdminController {
    private final MainAdminView view;
    private final Stage primaryStage;
    
    private final ProductDAO productDAO;
    private final UserDAO userDAO;
    private final OrderDAO orderDAO;
    
    private final ObservableList<Product> products;
    private final ObservableList<User> users;
    private final ObservableList<Order> orders;
    


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

    public void loadProducts() {
        // Показываем индикатор загрузки
        view.showLoadingIndicator(true);
        
        Task<List<Product>> task = new Task<>() {
            @Override
            protected List<Product> call() {
                // Сбрасываем соединение перед загрузкой товаров
                com.olineshop.util.DatabaseManager.resetConnectionStatus();
                return productDAO.getAllProducts();
            }
        };
        
        task.setOnSucceeded(event -> {
            products.clear();
            products.addAll(task.getValue());
            view.updateProductTable(products);
            view.showLoadingIndicator(false);
        });
        
        task.setOnFailed(event -> {
            System.out.println("Ошибка при загрузке товаров: " + task.getException().getMessage());
            task.getException().printStackTrace();
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить товары");
            view.showLoadingIndicator(false);
        });
        
        new Thread(task).start();
    }

    public void loadUsers() {
        // Показываем индикатор загрузки
        view.showLoadingIndicator(true);
        
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() {
                // Сбрасываем соединение перед загрузкой пользователей
                com.olineshop.util.DatabaseManager.resetConnectionStatus();
                return userDAO.getAllUsers();
            }
        };
        
        task.setOnSucceeded(event -> {
            users.clear();
            users.addAll(task.getValue());
            view.updateUserTable(users);
            view.showLoadingIndicator(false);
        });
        
        task.setOnFailed(event -> {
            System.out.println("Ошибка при загрузке пользователей: " + task.getException().getMessage());
            task.getException().printStackTrace();
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить пользователей");
            view.showLoadingIndicator(false);
        });
        
        new Thread(task).start();
    }

    public void loadOrders() {
        // Показываем индикатор загрузки
        view.showLoadingIndicator(true);
        
        Task<List<Order>> task = new Task<>() {
            @Override
            protected List<Order> call() {
                // Сбрасываем соединение перед загрузкой заказов
                com.olineshop.util.DatabaseManager.resetConnectionStatus();
                return orderDAO.getAllOrders();
            }
        };
        
        task.setOnSucceeded(event -> {
            orders.clear();
            orders.addAll(task.getValue());
            view.updateOrderTable(orders);
            view.showLoadingIndicator(false);
        });
        
        task.setOnFailed(event -> {
            System.out.println("Ошибка при загрузке заказов: " + task.getException().getMessage());
            task.getException().printStackTrace();
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить заказы");
            view.showLoadingIndicator(false);
        });
        
        new Thread(task).start();
    }

    public void deleteProduct(int id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText(null);
        alert.setContentText("Вы действительно хотите удалить товар?");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Показываем индикатор загрузки
            view.showLoadingIndicator(true);
            
            // Выполняем удаление товара в отдельном потоке
            Task<Boolean> deleteTask = new Task<>() {
                @Override
                protected Boolean call() {
                    // Сбрасываем соединение перед удалением
                    com.olineshop.util.DatabaseManager.resetConnectionStatus();
                    return productDAO.deleteProduct(id);
                }
            };
            
            deleteTask.setOnSucceeded(event -> {
                boolean success = deleteTask.getValue();
                if (success) {
                    view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Товар успешно удален");
                    // Перезагружаем список товаров после удаления
                    loadProducts();
                } else {
                    view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить товар");
                    view.showLoadingIndicator(false);
                }
            });
            
            deleteTask.setOnFailed(event -> {
                System.out.println("Ошибка при удалении товара: " + deleteTask.getException().getMessage());
                deleteTask.getException().printStackTrace();
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Произошла ошибка при удалении товара");
                view.showLoadingIndicator(false);
            });
            
            new Thread(deleteTask).start();
        }
    }

    public void showUserDetails(User user) {
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Информация о пользователе: " + user.getFullName());
        detailsStage.initModality(Modality.WINDOW_MODAL);
        detailsStage.initOwner(primaryStage);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
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
        
        Label ordersLabel = new Label("Заказы пользователя:");
        
        TableView<Order> ordersTable = new TableView<>();
        
        TableColumn<Order, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        
    
        TableColumn<Order, String> orderDateColumn = new TableColumn<>("Дата заказа");
        orderDateColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getOrderDate().toString());
        });
        orderDateColumn.setPrefWidth(150);
        
        TableColumn<Order, Double> totalCostColumn = new TableColumn<>("Сумма (руб.)");
        totalCostColumn.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        
        TableColumn<Order, String> statusColumn = new TableColumn<>("Статус");
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        ordersTable.getColumns().addAll(idColumn, orderDateColumn, totalCostColumn, statusColumn);
        
        ordersTable.setItems(FXCollections.observableArrayList(orderDAO.getOrdersByUser(user.getId())));
        
        Button closeButton = new Button("Закрыть");
        closeButton.setOnAction(e -> detailsStage.close());
        
        vbox.getChildren().addAll(infoGrid, discountBox, ordersLabel, ordersTable, closeButton);
        
        Scene scene = new Scene(vbox, 600, 500);
        detailsStage.setScene(scene);
        detailsStage.show();
    }

    public void deleteUser(int id) {
        User user = userDAO.getUserById(id);
        
        if (user == null) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Пользователь не найден");
            return;
        }
        
        if (user.isAdmin()) {
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Нельзя удалить администратора");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText(null);
        alert.setContentText("Вы действительно хотите удалить пользователя " + user.getFullName() + "?");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = userDAO.deleteUser(id);
            
            if (success) {
                view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Пользователь успешно удален");
                loadUsers();
            } else {
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить пользователя");
            }
        }
    }

    public void showOrderDetails(Order order) {
        System.out.println("Показ деталей заказа ID=" + order.getId());
        
        // Показываем индикатор загрузки
        view.showLoadingIndicator(true);
        
        // Сначала загружаем актуальные данные заказа с товарами
        Task<Order> loadOrderTask = new Task<>() {
            @Override
            protected Order call() {
                // Загружаем заказ с товарами
                return orderDAO.getOrderById(order.getId());
            }
        };
        
        loadOrderTask.setOnSucceeded(event -> {
            Order loadedOrder = loadOrderTask.getValue();
            if (loadedOrder != null) {
                // Показываем окно с деталями заказа
                showOrderDetailsWindow(loadedOrder);
            } else {
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить данные заказа");
            }
            view.showLoadingIndicator(false);
        });
        
        loadOrderTask.setOnFailed(event -> {
            System.out.println("Ошибка при загрузке заказа: " + loadOrderTask.getException().getMessage());
            loadOrderTask.getException().printStackTrace();
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Произошла ошибка при загрузке данных заказа");
            view.showLoadingIndicator(false);
        });
        
        new Thread(loadOrderTask).start();
    }
    
    // Вспомогательный метод для отображения окна с деталями заказа
    private void showOrderDetailsWindow(Order order) {
        // Создаем и показываем окно с деталями заказа
        Stage detailsStage = new Stage();
        detailsStage.setTitle("Подробности заказа №" + order.getId());
        detailsStage.initModality(Modality.WINDOW_MODAL);
        detailsStage.initOwner(primaryStage);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
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
        infoGrid.add(new Label(order.getOrderDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))), 1, 4);
        
        infoGrid.add(new Label("Дата доставки:"), 0, 5);
        infoGrid.add(new Label(order.getDeliveryDate() != null ? order.getDeliveryDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "Не указана"), 1, 5);
        
        infoGrid.add(new Label("Статус:"), 0, 6);
        infoGrid.add(new Label(order.getStatus()), 1, 6);
        
        infoGrid.add(new Label("Итоговая стоимость:"), 0, 7);
        infoGrid.add(new Label(String.format("%.2f руб.", order.getTotalCost())), 1, 7);

        Label itemsLabel = new Label("Товары в заказе:");
        
        TableView<OrderItem> itemsTable = new TableView<>();
        
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
        
        itemsTable.getColumns().addAll(nameColumn, priceColumn, quantityColumn, subtotalColumn);
        
        // Проверяем, загружены ли товары заказа
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            // Если товары уже загружены, показываем их
            itemsTable.setItems(FXCollections.observableArrayList(order.getItems()));
            System.out.println("Отображение " + order.getItems().size() + " товаров в заказе #" + order.getId());
        } else {
            System.out.println("Список товаров в заказе #" + order.getId() + " пуст или null");
            itemsTable.setItems(FXCollections.observableArrayList());
        }
        
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

    public void showChangeStatusDialog(Order order) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Изменение статуса заказа №" + order.getId());
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        Label currentStatusLabel = new Label("Текущий статус: " + order.getStatus());
        
        Label newStatusLabel = new Label("Новый статус:");
        
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("В обработке", "Отправлен", "Доставлен", "Отменен");
        statusComboBox.setValue(order.getStatus());
        
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

    public void showSetDeliveryDateDialog(Order order) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Установка даты доставки заказа №" + order.getId());
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        
        Label currentDateLabel = new Label("Текущая дата доставки: " + 
                (order.getDeliveryDate() != null ? order.getDeliveryDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "Не указана"));
        
        Label newDateLabel = new Label("Новая дата доставки (ГГГГ-ММ-ДД):");
        
        TextField dateField = new TextField();
        if (order.getDeliveryDate() != null) {
            dateField.setText(order.getDeliveryDate().toLocalDate().toString());
        }
        
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

    public void deleteOrder(int id) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText(null);
        alert.setContentText("Вы действительно хотите удалить заказ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Показываем индикатор загрузки
            view.showLoadingIndicator(true);
            
            // Выполняем удаление заказа в отдельном потоке
            Task<Boolean> deleteTask = new Task<>() {
                @Override
                protected Boolean call() {
                    // Сбрасываем соединение перед удалением
                    com.olineshop.util.DatabaseManager.resetConnectionStatus();
                    return orderDAO.deleteOrder(id);
                }
            };
            
            deleteTask.setOnSucceeded(event -> {
                boolean success = deleteTask.getValue();
                if (success) {
                    view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Заказ успешно удален");
                    // Перезагружаем список заказов после удаления
                    loadOrders();
                } else {
                    view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось удалить заказ");
                    view.showLoadingIndicator(false);
                }
            });
            
            deleteTask.setOnFailed(event -> {
                System.out.println("Ошибка при удалении заказа: " + deleteTask.getException().getMessage());
                deleteTask.getException().printStackTrace();
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Произошла ошибка при удалении заказа");
                view.showLoadingIndicator(false);
            });
            
            new Thread(deleteTask).start();
        }
    }

    public void handleLogout() {
        // Сброс статус соединения с бд перед выходом
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        primaryStage.close();
        
        LoginView loginView = new LoginView();
        loginView.start(new Stage());
    }


} 