CREATE TABLE stocks (
    id BIGINT NOT NULL AUTO_INCREMENT,

    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,

    quantity INT NOT NULL,
    minimum_stock INT NOT NULL DEFAULT 0,

    PRIMARY KEY (id),

    CONSTRAINT uk_stocks_product_warehouse
        UNIQUE (product_id, warehouse_id),

    CONSTRAINT fk_stocks_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_stocks_warehouse
        FOREIGN KEY (warehouse_id)
            REFERENCES warehouses(id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_stocks_quantity
        CHECK (quantity >= 0)

    CONSTRAINT chk_stocks_minimum_stock
        CHECK (minimum_stock >= 0)
);

CREATE INDEX idx_stocks_product_id
    ON stocks(product_id);

CREATE INDEX idx_stocks_warehouse_id
    ON stocks(warehouse_id);