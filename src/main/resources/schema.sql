DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;

CREATE TABLE orders (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_email VARCHAR(255)   NOT NULL,
    total_amount   DECIMAL(10,2)  NOT NULL,
    status         VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP      DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE order_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    unit_price   DECIMAL(10,2)  NOT NULL,
    quantity     INT            NOT NULL
);
