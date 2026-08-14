ALTER TABLE product_suppliers
    ADD COLUMN purchase_price DECIMAL(15,2) NOT NULL,
    ADD CONSTRAINT chk_product_suppliers_purchase_price
        CHECK (purchase_price >= 0);