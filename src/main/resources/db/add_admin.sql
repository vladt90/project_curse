USE `online_store_db`;

-- Проверяем, существует ли уже администратор
SET @admin_exists = (SELECT COUNT(*) FROM `users` WHERE `login` = 'admin');

-- Получаем ID роли администратора
SET @admin_role_id = (SELECT `id` FROM `roles` WHERE `name` = 'Администратор' LIMIT 1);

-- Добавляем администратора, если он еще не существует
INSERT INTO `users` (`login`, `password_hash`, `first_name`, `last_name`, `email`, `phone`, `discount`, `role_id`) 
SELECT 'admin', 'admin', 'Администратор', 'Системы', 'admin@example.com', '89123456789', 0.00, @admin_role_id
WHERE @admin_exists = 0 AND @admin_role_id IS NOT NULL; 