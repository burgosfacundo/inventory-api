CREATE TABLE product_suppliers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    supplier_id BIGINT NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT uk_product_supplier
        UNIQUE (product_id, supplier_id),

    CONSTRAINT fk_product_suppliers_product
        FOREIGN KEY (product_id)
            REFERENCES products(id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_product_suppliers_supplier
        FOREIGN KEY (supplier_id)
            REFERENCES suppliers(id)
            ON DELETE RESTRICT
);

CREATE INDEX idx_product_suppliers_product_id
    ON product_suppliers(product_id);

CREATE INDEX idx_product_suppliers_supplier_id
    ON product_suppliers(supplier_id);