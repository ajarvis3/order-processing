-- Inventory DB initialization: inventory table
-- Moved from inventory/src/main/resources/db/migration/V1__create_inventory_table.sql

CREATE TABLE IF NOT EXISTS inventory_db.inventory (
                                                      id BIGINT PRIMARY KEY,
                                                      sku VARCHAR(191),
    quantity_available INT
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Orders DB initialization: orders table
-- Moved from orders/src/main/resources/db/migration/V1__create_orders_table.sql

CREATE TABLE IF NOT EXISTS orders_db.orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(191),
    order_number VARCHAR(191),
    status VARCHAR(50),
    total_amount DOUBLE,
    order_quantity INT,
    palpay_order_id BIGINT,
    auth_id VARCHAR(191)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Palpay DB initialization: payments and palpay orders tables
-- Moved from palpay/src/main/resources/db/migration/V1__create_payments_table.sql

-- Creates payments table
CREATE TABLE IF NOT EXISTS palpay_db.payments (
    auth_id VARCHAR(191) NOT NULL PRIMARY KEY,
    order_id BIGINT,
    status VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Creates palpay orders table
CREATE TABLE IF NOT EXISTS palpay_db.orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    total_amount DOUBLE,
    payment_method_type VARCHAR(50),
    status VARCHAR(50),
    auth_id VARCHAR(191)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Shipping DB initialization: orders table used by shipping service
-- Moved from shipping/src/main/resources/db/migration/V1__create_shipping_orders_table.sql

CREATE TABLE IF NOT EXISTS shipping_db.shipping (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT,
    status VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

