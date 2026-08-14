CREATE TABLE stock_transfers(
    id BIGINT NOT NULL AUTO_INCREMENT ,

    product_id BIGINT NOT NULL,
    source_warehouse_id BIGINT NOT NULL,
    destination_warehouse_id BIGINT NOT NULL,

    quantity INT NOT NULL,
    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY(id),

    CONSTRAINT fk_stock_transfers_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
            ON DELETE RESTRICT,


    CONSTRAINT fk_stock_transfers_source_warehouse
        FOREIGN KEY (source_warehouse_id)
            REFERENCES warehouses(id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_stock_transfers_destination_warehouse
        FOREIGN KEY (destination_warehouse_id)
            REFERENCES warehouses(id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_stock_transfers_quantity
            CHECK (quantity > 0),

    CONSTRAINT chk_stock_transfers_different_warehouses
            CHECK (source_warehouse_id <> destination_warehouse_id)
);

CREATE INDEX idx_stock_transfers_product_id
    ON stock_transfers(product_id);

CREATE INDEX idx_stock_transfers_source_warehouse_id
    ON stock_transfers(source_warehouse_id);

CREATE INDEX idx_stock_transfers_destination_warehouse_id
    ON stock_transfers(destination_warehouse_id);

CREATE INDEX idx_stock_transfers_created_at
    ON stock_transfers(created_at);