-- Initialize multiple logical databases and users for local development
-- This script runs automatically when the official MySQL image initializes

CREATE DATABASE IF NOT EXISTS orders_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS inventory_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS payments_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS shipping_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS palpay_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Orders user
CREATE USER IF NOT EXISTS 'orders_user'@'%' IDENTIFIED BY 'orders_password';
GRANT ALL PRIVILEGES ON `orders_db`.* TO 'orders_user'@'%';

-- Inventory user
CREATE USER IF NOT EXISTS 'inventory_user'@'%' IDENTIFIED BY 'inventory_password';
GRANT ALL PRIVILEGES ON `inventory_db`.* TO 'inventory_user'@'%';

-- Payments user
CREATE USER IF NOT EXISTS 'payments_user'@'%' IDENTIFIED BY 'payments_password';
GRANT ALL PRIVILEGES ON `payments_db`.* TO 'payments_user'@'%';

-- Shipping user
CREATE USER IF NOT EXISTS 'shipping_user'@'%' IDENTIFIED BY 'shipping_password';
GRANT ALL PRIVILEGES ON `shipping_db`.* TO 'shipping_user'@'%';

-- Palpay user
CREATE USER IF NOT EXISTS 'palpay_user'@'%' IDENTIFIED BY 'palpay_password';
GRANT ALL PRIVILEGES ON `palpay_db`.* TO 'palpay_user'@'%';

FLUSH PRIVILEGES;

