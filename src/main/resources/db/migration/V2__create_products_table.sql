CREATE TABLE products (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sku VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    sale_price DECIMAL(10, 2) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_products_sku
        UNIQUE (sku),
    CONSTRAINT chk_products_sale_price
        CHECK (sale_price >= 0),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_products_category_id
    ON products(category_id);