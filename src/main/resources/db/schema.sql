CREATE DATABASE IF NOT EXISTS online_store_db;
USE online_store_db;

CREATE TABLE IF NOT EXISTS `roles` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS `users` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `login` VARCHAR(50) NOT NULL UNIQUE,
  `password_hash` VARCHAR(255) NOT NULL,
  `first_name` VARCHAR(50) NOT NULL,
  `last_name` VARCHAR(50) NOT NULL,
  `email` VARCHAR(100) NOT NULL,
  `phone` VARCHAR(20),
  `discount` DOUBLE DEFAULT 0,
  `role_id` INT NOT NULL,
  FOREIGN KEY (`role_id`) REFERENCES `roles`(`id`)
);

CREATE TABLE IF NOT EXISTS `products` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  `price` DECIMAL(10, 2) NOT NULL,
  `unit` VARCHAR(20) NOT NULL DEFAULT 'шт',
  `stock_quantity` INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS `orders` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `order_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `delivery_date` DATETIME NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'Новый',
  `total_price` DECIMAL(10, 2) NOT NULL,
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)
);

CREATE TABLE IF NOT EXISTS `order_items` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `order_id` INT NOT NULL,
  `product_id` INT NOT NULL,
  `quantity` INT NOT NULL,
  `price` DECIMAL(10, 2) NOT NULL,
  FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`),
  FOREIGN KEY (`product_id`) REFERENCES `products`(`id`)
);

INSERT IGNORE INTO `roles` (`id`, `name`) VALUES 
(1, 'Администратор'),
(2, 'Клиент');

INSERT IGNORE INTO `users` (`login`, `password_hash`, `first_name`, `last_name`, `email`, `role_id`) VALUES 
('admin', 'admin', 'Администратор', 'Системы', 'admin@example.com', 1);


SET @product_count = (SELECT COUNT(*) FROM `products`);

INSERT IGNORE INTO `products` (`name`, `price`, `unit`, `stock_quantity`)
SELECT 'Смартфон', 3000.00, 'шт', 15 FROM dual WHERE @product_count = 0
UNION ALL SELECT 'Утюг', 1500.00, 'шт', 8 FROM dual WHERE @product_count = 0
UNION ALL SELECT 'Телевизор', 6000.00, 'шт', 5 FROM dual WHERE @product_count = 0
UNION ALL SELECT 'Кофемашина', 800.50, 'шт', 12 FROM dual WHERE @product_count = 0
UNION ALL SELECT 'Пульт', 150.00, 'шт', 7 FROM dual WHERE @product_count = 0
UNION ALL SELECT 'Микроволновка', 3500.99, 'шт', 20 FROM dual WHERE @product_count = 0
UNION ALL SELECT 'Холодильник', 7000.00, 'шт', 3 FROM dual WHERE @product_count = 0
UNION ALL SELECT 'Стиральная машина', 4500.00, 'шт', 6 FROM dual WHERE @product_count = 0
UNION ALL SELECT 'Планшет', 1500.00, 'шт', 10 FROM dual WHERE @product_count = 0
UNION ALL SELECT 'Фотоаппарат', 4800.00, 'шт', 4 FROM dual WHERE @product_count = 0; 