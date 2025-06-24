USE `online_store_db`;

SET @admin_exists = (SELECT COUNT(*) FROM `users` WHERE `login` = 'admin');

SET @admin_role_id = (SELECT `id` FROM `roles` WHERE `name` = 'Администратор' LIMIT 1);

INSERT INTO `users` (`login`, `password_hash`, `first_name`, `last_name`, `email`, `phone`, `discount`, `role_id`) 
SELECT 'admin', 'admin', 'Иван', 'К', 'ivanK@mail.ru', '89123456789', 0.00, @admin_role_id
WHERE @admin_exists = 0; 