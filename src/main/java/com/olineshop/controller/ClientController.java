package com.olineshop.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
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
import java.sql.SQLException;


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
        // Сбрасываем соединение перед загрузкой товаров
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        try {
            products.clear();
            products.addAll(productDAO.getAllProducts());
            view.updateProductTable(products);
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке товаров: " + e.getMessage());
            e.printStackTrace();
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить товары: " + e.getMessage());
        }
    }
    
    //поиск товаров по названию
     //searchTerm строка для поиска
    public void searchProducts(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            //показ всех товаров
            loadProducts();
            return;
        }
        
        // Сбрасываем соединение перед поиском товаров
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        try {
            products.clear();
            products.addAll(productDAO.searchProductsByName(searchTerm));
            view.updateProductTable(products);
        } catch (Exception e) {
            System.out.println("Ошибка при поиске товаров: " + e.getMessage());
            e.printStackTrace();
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось найти товары: " + e.getMessage());
        }
    }
    
    //фильтр цены
     //minPrice мин цена
     //maxPrice мак цена
    public void filterProductsByPrice(double minPrice, double maxPrice) {
        // Сбрасываем соединение перед фильтрацией товаров
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        try {
            products.clear();
            products.addAll(productDAO.filterProductsByPrice(minPrice, maxPrice));
            view.updateProductTable(products);
        } catch (Exception e) {
            System.out.println("Ошибка при фильтрации товаров: " + e.getMessage());
            e.printStackTrace();
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось отфильтровать товары: " + e.getMessage());
        }
    }

    // для истории бож (главное не забыть!!!!)
    public void loadOrderHistory() {
        // Сбрасываем соединение перед загрузкой истории заказов
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        try {
            orders.clear();
            List<Order> userOrders = orderDAO.getOrdersByUser(currentUser.getId());
            
            // Сортировка заказов по дате (от новых к старым)
            userOrders.sort((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()));
            
            orders.addAll(userOrders);
            view.updateOrderHistoryTable(orders);
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке истории заказов: " + e.getMessage());
            e.printStackTrace();
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось загрузить историю заказов: " + e.getMessage());
        }
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
        double subtotal = 0.0;
        
        for (OrderItem item : cartItems) {
            subtotal += item.getPrice() * item.getQuantity();
        }
        
        System.out.println("Сумма заказа до скидок: " + subtotal + " руб.");
        
        // Применяем персональную скидку пользователя
        double discount = currentUser.getDiscount();
        
        // Убираем дополнительные скидки за большие заказы
        
        double discountAmount = subtotal * discount;
        System.out.println("Скидка (" + (discount * 100) + "%): " + discountAmount + " руб.");
        
        double total = subtotal * (1 - discount);
        System.out.println("Итоговая сумма заказа после скидки: " + total + " руб.");
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
            OrderItem item = cellData.getValue();
            if (item != null && item.getProduct() != null) {
                return new javafx.beans.property.SimpleStringProperty(item.getProduct().getName());
            }
            return new javafx.beans.property.SimpleStringProperty("Неизвестный товар");
        });
        nameColumn.setPrefWidth(200);
        
        // Столбец с ценой товара
        TableColumn<OrderItem, Double> priceColumn = new TableColumn<>("Цена (руб.)");
        priceColumn.setCellValueFactory(cellData -> {
            OrderItem item = cellData.getValue();
            if (item != null) {
                return new javafx.beans.property.SimpleDoubleProperty(item.getPrice()).asObject();
            }
            return new javafx.beans.property.SimpleDoubleProperty(0).asObject();
        });
        
        // Столбец с количеством товара
        TableColumn<OrderItem, Integer> quantityColumn = new TableColumn<>("Количество");
        quantityColumn.setCellValueFactory(cellData -> {
            OrderItem item = cellData.getValue();
            if (item != null) {
                return new javafx.beans.property.SimpleIntegerProperty(item.getQuantity()).asObject();
            }
            return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
        });
        
        // Столбец с суммой
        TableColumn<OrderItem, Double> subtotalColumn = new TableColumn<>("Сумма (руб.)");
        subtotalColumn.setCellValueFactory(cellData -> {
            OrderItem item = cellData.getValue();
            if (item != null) {
                double subtotal = item.getPrice() * item.getQuantity();
                return new javafx.beans.property.SimpleDoubleProperty(subtotal).asObject();
            }
            return new javafx.beans.property.SimpleDoubleProperty(0).asObject();
        });
        
        // add солбцы в табл
        itemsTable.getColumns().addAll(nameColumn, priceColumn, quantityColumn, subtotalColumn);
        
        // Проверяем наличие товаров в корзине
        if (cartItems != null && !cartItems.isEmpty()) {
            // Создаем копию списка товаров для отображения
            ObservableList<OrderItem> displayItems = FXCollections.observableArrayList(cartItems);
            // Заполняем таблицу 
            itemsTable.setItems(displayItems);
        } else {
            System.out.println("Предупреждение: корзина пуста при отображении окна подтверждения заказа");
            itemsTable.setItems(FXCollections.observableArrayList());
        }
        
        // Инфа о скидке и сумме в итоге
        double originalPrice = 0.0;
        for (OrderItem item : cartItems) {
            originalPrice += item.getPrice() * item.getQuantity();
        }
        
        // Создаем панель с информацией о скидках
        GridPane discountGrid = new GridPane();
        discountGrid.setHgap(10);
        discountGrid.setVgap(5);
        discountGrid.setPadding(new Insets(10, 0, 10, 0));
        
        int row = 0;
        
        // Сумма до скидок
        discountGrid.add(new Label("Сумма без скидок:"), 0, row);
        discountGrid.add(new Label(String.format("%.2f руб.", originalPrice)), 1, row);
        row++;
        
        // Персональная скидка пользователя
        if (currentUser.getDiscount() > 0) {
            double userDiscountAmount = originalPrice * currentUser.getDiscount();
            discountGrid.add(new Label(String.format("Персональная скидка (%.0f%%):", currentUser.getDiscount() * 100)), 0, row);
            discountGrid.add(new Label(String.format("-%.2f руб.", userDiscountAmount)), 1, row);
            row++;
        } else {
            discountGrid.add(new Label("У вас пока нет персональной скидки"), 0, row, 2, 1);
            discountGrid.add(new Label("При заказе на сумму более 5000 руб. вы получите скидку 2%"), 0, row+1, 2, 1);
            row += 2;
        }
        
        // Разделительная линия
        javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, 0, 300, 0);
        discountGrid.add(line, 0, row, 2, 1);
        row++;
        
        // Итоговая сумма
        double totalPrice = calculateTotalPrice();
        Label totalLabel = new Label("Итоговая сумма:");
        totalLabel.setStyle("-fx-font-weight: bold;");
        discountGrid.add(totalLabel, 0, row);
        
        Label totalValueLabel = new Label(String.format("%.2f руб.", totalPrice));
        totalValueLabel.setStyle("-fx-font-weight: bold;");
        discountGrid.add(totalValueLabel, 1, row);
        
        // Кнопки
        Button confirmButton = new Button("Подтвердить заказ");
        confirmButton.setOnAction(e -> {
            try {
                System.out.println("Нажата кнопка 'Подтвердить заказ'");
                checkout();
                confirmStage.close();
            } catch (Exception ex) {
                System.out.println("Ошибка при обработке нажатия кнопки 'Подтвердить заказ': " + ex.getMessage());
                ex.printStackTrace();
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", 
                        "Произошла ошибка при оформлении заказа: " + ex.getMessage());
            }
        });
        
        Button cancelButton = new Button("Отмена");
        cancelButton.setOnAction(e -> confirmStage.close());
        
        HBox buttonBox = new HBox(10, confirmButton, cancelButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        vbox.getChildren().addAll(
                titleLabel, 
                new Label("Товары в заказе:"),
                itemsTable, 
                discountGrid,
                buttonBox
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
        
        // Сбрасываем соединение перед оформлением заказа
        com.olineshop.util.DatabaseManager.resetConnectionStatus();
        
        try {
            System.out.println("Начало оформления заказа...");
            
            // Проверяем наличие товаров на складе перед оформлением заказа
            for (OrderItem item : cartItems) {
                System.out.println("Проверка товара: ID=" + item.getProduct().getId() + 
                                  ", Название=" + item.getProduct().getName() + 
                                  ", Количество в корзине=" + item.getQuantity());
                
                Product product = productDAO.getProductById(item.getProduct().getId());
                if (product == null) {
                    String errorMsg = "Товар " + item.getProduct().getName() + " больше не доступен";
                    System.out.println("Ошибка: " + errorMsg);
                    view.showAlert(Alert.AlertType.ERROR, "Ошибка", errorMsg);
                    return;
                }
                
                System.out.println("Товар найден в базе: ID=" + product.getId() + 
                                  ", Название=" + product.getName() + 
                                  ", Доступное количество=" + product.getStockQuantity());
                
                if (product.getStockQuantity() < item.getQuantity()) {
                    String errorMsg = "Недостаточно товара " + product.getName() + " на складе. " +
                                     "Доступно: " + product.getStockQuantity() + ", в корзине: " + item.getQuantity();
                    System.out.println("Ошибка: " + errorMsg);
                    view.showAlert(Alert.AlertType.ERROR, "Ошибка", errorMsg);
                    return;
                }
                
                // Обновляем информацию о товаре в корзине
                item.setProduct(product);
                System.out.println("Информация о товаре в корзине обновлена");
            }
            
            // создаем новый заказ
            System.out.println("Создание нового заказа...");
            Order order = new Order();
            order.setUser(currentUser);
            order.setOrderDate(LocalDateTime.now());
            
            // Пересчитываем итоговую стоимость с учетом актуальных скидок
            double totalPrice = calculateTotalPrice();
            order.setTotalCost(totalPrice);
            order.setStatus("Новый");
            
            System.out.println("Информация о заказе: Пользователь=" + currentUser.getLogin() + 
                              ", Дата=" + order.getOrderDate() + 
                              ", Сумма=" + totalPrice + 
                              ", Статус=" + order.getStatus());
            
            // Добавить товары в заказ
            System.out.println("Добавление товаров в заказ...");
            List<OrderItem> orderItems = new ArrayList<>();
            for (OrderItem cartItem : cartItems) {
                // Проверяем, что товар корректный
                Product product = cartItem.getProduct();
                if (product == null) {
                    System.out.println("Ошибка: товар не определен в элементе корзины");
                    continue;
                }
                
                // Проверяем, что товар имеет корректный ID
                if (product.getId() <= 0) {
                    System.out.println("Ошибка: некорректный ID товара: " + product.getId());
                    continue;
                }
                
                // Создаем новый объект OrderItem для заказа
                try {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(product);
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(product.getPrice());  // Используем текущую цену из объекта товара
                    
                    orderItems.add(orderItem);
                    System.out.println("Товар добавлен в заказ: " + orderItem.getProduct().getName() + 
                                      ", Количество=" + orderItem.getQuantity() + 
                                      ", Цена=" + orderItem.getPrice());
                } catch (Exception e) {
                    System.out.println("Ошибка при создании элемента заказа: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Проверяем, что хотя бы один товар был добавлен в заказ
            if (orderItems.isEmpty()) {
                System.out.println("Ошибка: не удалось добавить ни одного товара в заказ");
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось добавить товары в заказ");
                return;
            }

            order.setItems(orderItems);
            
            // сохраняем заказ в базе данных
            System.out.println("Сохранение заказа в базе данных...");

            // Дополнительная проверка перед сохранением заказа
            if (order.getUser() == null) {
                System.out.println("Ошибка: пользователь не установлен в заказе");
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось оформить заказ: пользователь не установлен");
                return;
            }

            if (order.getItems() == null || order.getItems().isEmpty()) {
                System.out.println("Ошибка: список товаров в заказе пуст");
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось оформить заказ: корзина пуста");
                return;
            }

            // Проверяем, что все товары в заказе имеют корректные ссылки
            boolean hasInvalidItems = false;
            for (OrderItem item : order.getItems()) {
                if (item == null) {
                    System.out.println("Ошибка: null-элемент в списке товаров заказа");
                    hasInvalidItems = true;
                    break;
                }
                if (item.getProduct() == null) {
                    System.out.println("Ошибка: товар не определен в элементе заказа");
                    hasInvalidItems = true;
                    break;
                }
                if (item.getQuantity() <= 0) {
                    System.out.println("Ошибка: некорректное количество товара (" + item.getQuantity() + 
                                      ") для " + item.getProduct().getName());
                    hasInvalidItems = true;
                    break;
                }
            }

            if (hasInvalidItems) {
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось оформить заказ: некорректные товары в корзине");
                return;
            }

            boolean success = orderDAO.addOrder(order);
            
            if (success) {
                System.out.println("Заказ успешно оформлен с ID: " + order.getId());
                
                // Проверяем сумму заказа для обновления статуса постоянного клиента
                double subtotal = 0.0;
                for (OrderItem item : cartItems) {
                    subtotal += item.getPrice() * item.getQuantity();
                }
                
                // Если сумма заказа превышает 5000 рублей и у пользователя еще нет скидки,
                // устанавливаем скидку 2% для постоянного клиента
                if (subtotal > 5000 && currentUser.getDiscount() == 0) {
                    System.out.println("Пользователь переходит в категорию постоянных клиентов");
                    currentUser.setDiscount(0.02); // 2% скидка
                    boolean discountUpdated = userDAO.updateUserDiscount(currentUser.getId(), 0.02);
                    if (discountUpdated) {
                        System.out.println("Скидка пользователя успешно обновлена до 2%");
                    } else {
                        System.out.println("Не удалось обновить скидку пользователя");
                    }
                }
                
                view.showAlert(Alert.AlertType.INFORMATION, "Успех", "Заказ успешно оформлен");
                
                // Очищаем корзину
                cartItems.clear();
                updateCartView();
                
                // Обновляем историю заказов
                loadOrderHistory();
                
                // Обновляем список товаров, чтобы отразить изменения в количестве на складе
                loadProducts();
            } else {
                System.out.println("Не удалось оформить заказ");
                view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Не удалось оформить заказ");
            }
        } catch (Exception e) {
            System.out.println("Ошибка при оформлении заказа: " + e.getMessage());
            e.printStackTrace();
            
            // Более детальная информация об ошибке
            if (e instanceof SQLException) {
                SQLException sqlEx = (SQLException) e;
                System.out.println("SQL State: " + sqlEx.getSQLState());
                System.out.println("Error Code: " + sqlEx.getErrorCode());
            }
            
            view.showAlert(Alert.AlertType.ERROR, "Ошибка", "Произошла ошибка при оформлении заказа: " + e.getMessage());
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
        
        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(15, 15, 15, 15));
        
        // Заголовок
        Label headerLabel = new Label("Информация о заказе №" + order.getId());
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Инфа о заказе
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(10);
        infoGrid.setPadding(new Insets(10, 10, 10, 10));
        infoGrid.setStyle("-fx-background-color: #f4f4f4; -fx-background-radius: 5;");
        
        // Стиль для меток
        String labelStyle = "-fx-font-weight: bold;";
        
        Label idLabel = new Label("Номер заказа:");
        idLabel.setStyle(labelStyle);
        infoGrid.add(idLabel, 0, 0);
        infoGrid.add(new Label(String.valueOf(order.getId())), 1, 0);
        
        Label orderDateLabel = new Label("Дата заказа:");
        orderDateLabel.setStyle(labelStyle);
        infoGrid.add(orderDateLabel, 0, 1);
        String formattedOrderDate = order.getOrderDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        infoGrid.add(new Label(formattedOrderDate), 1, 1);
        
        Label deliveryDateLabel = new Label("Дата доставки:");
        deliveryDateLabel.setStyle(labelStyle);
        infoGrid.add(deliveryDateLabel, 0, 2);
        String deliveryDateText;
        if (order.getDeliveryDate() != null) {
            deliveryDateText = order.getDeliveryDate().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        } else {
            deliveryDateText = "Не указана";
        }
        infoGrid.add(new Label(deliveryDateText), 1, 2);
        
        Label statusLabel = new Label("Статус:");
        statusLabel.setStyle(labelStyle);
        infoGrid.add(statusLabel, 0, 3);
        Label statusValueLabel = new Label(order.getStatus());
        
        // Устанавливаем цвет в зависимости от статуса
        switch (order.getStatus().toLowerCase()) {
            case "новый":
                statusValueLabel.setStyle("-fx-text-fill: blue;");
                break;
            case "в обработке":
                statusValueLabel.setStyle("-fx-text-fill: orange;");
                break;
            case "доставляется":
                statusValueLabel.setStyle("-fx-text-fill: purple;");
                break;
            case "доставлен":
                statusValueLabel.setStyle("-fx-text-fill: green;");
                break;
            case "отменен":
                statusValueLabel.setStyle("-fx-text-fill: red;");
                break;
            default:
                break;
        }
        
        infoGrid.add(statusValueLabel, 1, 3);
        
        Label costLabel = new Label("Итоговая стоимость:");
        costLabel.setStyle(labelStyle);
        infoGrid.add(costLabel, 0, 4);
        Label costValueLabel = new Label(String.format("%.2f руб.", order.getTotalCost()));
        costValueLabel.setStyle("-fx-font-weight: bold;");
        infoGrid.add(costValueLabel, 1, 4);
        
        // Заголовок для таблицы товаров
        Label itemsHeaderLabel = new Label("Товары в заказе:");
        itemsHeaderLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Таблица товаров в заказе
        TableView<OrderItem> itemsTable = new TableView<>();
        itemsTable.setMinHeight(200);
        
        // Столбец с названием товара
        TableColumn<OrderItem, String> nameColumn = new TableColumn<>("Название");
        nameColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getProduct().getName());
        });
        nameColumn.setPrefWidth(200);
        
        // Столбец с ценой товара
        TableColumn<OrderItem, String> priceColumn = new TableColumn<>("Цена (руб.)");
        priceColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                    String.format("%.2f", cellData.getValue().getPrice()));
        });
        
        // Столбец с количеством товара
        TableColumn<OrderItem, Integer> quantityColumn = new TableColumn<>("Количество");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        
        // Столбец с суммой
        TableColumn<OrderItem, String> subtotalColumn = new TableColumn<>("Сумма (руб.)");
        subtotalColumn.setCellValueFactory(cellData -> {
            double subtotal = cellData.getValue().getPrice() * cellData.getValue().getQuantity();
            return new javafx.beans.property.SimpleStringProperty(String.format("%.2f", subtotal));
        });
        
        // Добавляем столбцы в таблицу
        itemsTable.getColumns().addAll(nameColumn, priceColumn, quantityColumn, subtotalColumn);
        
        // Заполняем таблицу
        itemsTable.setItems(FXCollections.observableArrayList(order.getItems()));
        
        // Кнопка закрыть
        Button closeButton = new Button("Закрыть");
        closeButton.setPrefWidth(100);
        closeButton.setOnAction(e -> detailsStage.close());
        
        HBox buttonBox = new HBox(closeButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));
        
        vbox.getChildren().addAll(headerLabel, infoGrid, itemsHeaderLabel, itemsTable, buttonBox);
        
        Scene scene = new Scene(vbox, 650, 500);
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