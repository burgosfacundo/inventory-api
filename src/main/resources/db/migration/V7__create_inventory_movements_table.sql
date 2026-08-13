CREATE TABLE inventory_movements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,

    type VARCHAR(10) NOT NULL,
    quantity INT NOT NULL,
    created_at DATETIME(6) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_inventory_movements_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_inventory_movements_warehouse
        FOREIGN KEY (warehouse_id)
            REFERENCES warehouses(id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_inventory_movements_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_inventory_movements_type
        CHECK (type IN ('IN', 'OUT'))
);

CREATE INDEX idx_inventory_movements_product_id
    ON inventory_movements(product_id);

CREATE INDEX idx_inventory_movements_warehouse_id
    ON inventory_movements(warehouse_id);

CREATE INDEX idx_inventory_movements_created_at
    ON inventory_movements(created_at);