-- =========================================================
-- Inventory API - Demo Data
-- Loaded only when the "demo" Spring profile is active.
-- =========================================================


-- =========================================================
-- Categories
-- =========================================================

INSERT INTO categories (id, name, description)
VALUES
    (1, 'Electronics', 'Computer peripherals and electronic accessories'),
    (2, 'Office Furniture', 'Furniture and equipment for office workspaces')
    ON DUPLICATE KEY UPDATE id = id;


-- =========================================================
-- Products
-- =========================================================

INSERT INTO products (
    id,
    sku,
    name,
    description,
    sale_price,
    active,
    category_id
)
VALUES
    (
        1,
        'KEYBOARD-001',
        'Mechanical Keyboard',
        'Mechanical keyboard with RGB backlight',
        120.00,
        TRUE,
        1
    ),
    (
        2,
        'MOUSE-001',
        'Wireless Mouse',
        'Ergonomic wireless mouse',
        45.00,
        TRUE,
        1
    ),
    (
        3,
        'CHAIR-001',
        'Ergonomic Office Chair',
        'Adjustable ergonomic chair with lumbar support',
        350.00,
        TRUE,
        2
    ),
    (
        4,
        'DESK-001',
        'Standing Desk',
        'Height-adjustable office desk',
        700.00,
        FALSE,
        2
    )
    ON DUPLICATE KEY UPDATE id = id;


-- =========================================================
-- Suppliers
-- =========================================================

INSERT INTO suppliers (
    id,
    name,
    email,
    phone,
    description,
    active
)
VALUES
    (
        1,
        'Tech Distribution S.A.',
        'sales@techdistribution.demo',
        '+54 11 5555-0101',
        'Technology products distributor',
        TRUE
    ),
    (
        2,
        'Office Supply S.A.',
        'sales@officesupply.demo',
        '+54 11 5555-0102',
        'Office furniture supplier',
        TRUE
    ),
    (
        3,
        'Import Solutions S.A.',
        'sales@importsolutions.demo',
        '+54 11 5555-0103',
        'Imported technology products supplier',
        FALSE
    )
    ON DUPLICATE KEY UPDATE id = id;


-- =========================================================
-- Product / Supplier associations
-- =========================================================

INSERT INTO product_suppliers (
    id,
    product_id,
    supplier_id,
    purchase_price
)
VALUES
    (1, 1, 1, 80.00),
    (2, 1, 3, 75.00),
    (3, 2, 1, 25.00),
    (4, 3, 2, 230.00),
    (5, 4, 2, 480.00)
    ON DUPLICATE KEY UPDATE id = id;


-- =========================================================
-- Warehouses
-- =========================================================

INSERT INTO warehouses (
    id,
    code,
    name,
    street,
    street_number,
    postal_code,
    city,
    province,
    country_code,
    latitude,
    longitude
)
VALUES
    (
        1,
        'MDQ-01',
        'Mar del Plata Main Warehouse',
        'Avenida Independencia',
        '1234',
        'B7600',
        'Mar del Plata',
        'Buenos Aires',
        'AR',
        -38.0055000,
        -57.5426000
    ),
    (
        2,
        'BUE-01',
        'Buenos Aires Warehouse',
        'Avenida Corrientes',
        '1234',
        'C1043',
        'Buenos Aires',
        'Ciudad Autónoma de Buenos Aires',
        'AR',
        -34.6037220,
        -58.3815920
    )
    ON DUPLICATE KEY UPDATE id = id;


-- =========================================================
-- Stocks
--
-- Quantities are consistent with the movement history below.
-- =========================================================

INSERT INTO stocks (
    id,
    product_id,
    warehouse_id,
    quantity,
    minimum_stock
)
VALUES
    -- 30 IN - 8 transferred OUT = 22
    (1, 1, 1, 22, 10),

    -- 8 transferred IN = 8
    (2, 1, 2, 8, 5),

    -- 15 IN - 3 OUT = 12
    (3, 2, 1, 12, 4),

    -- 8 IN, minimum 10 -> low stock example
    (4, 3, 2, 8, 10),

    -- 12 IN
    (5, 4, 2, 12, 3)
    ON DUPLICATE KEY UPDATE id = id;


-- =========================================================
-- Inventory Movements
-- =========================================================

INSERT INTO inventory_movements (
    id,
    product_id,
    warehouse_id,
    type,
    quantity,
    created_at
)
VALUES
    -- Initial stock entries
    (
        1,
        1,
        1,
        'IN',
        30,
        DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 7 DAY)
    ),
    (
        2,
        2,
        1,
        'IN',
        15,
        DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 6 DAY)
    ),
    (
        3,
        3,
        2,
        'IN',
        8,
        DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 6 DAY)
    ),
    (
        4,
        4,
        2,
        'IN',
        12,
        DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 5 DAY)
    ),

    -- Transfer KEYBOARD-001: MDQ -> BUE
    (
        5,
        1,
        1,
        'OUT',
        8,
        DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY)
    ),
    (
        6,
        1,
        2,
        'IN',
        8,
        DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY)
    ),

    -- Regular outbound movement
    (
        7,
        2,
        1,
        'OUT',
        3,
        DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 2 DAY)
    )
    ON DUPLICATE KEY UPDATE id = id;


-- =========================================================
-- Stock Transfers
-- =========================================================

INSERT INTO stock_transfers (
    id,
    product_id,
    source_warehouse_id,
    destination_warehouse_id,
    quantity,
    created_at
)
VALUES
    (
        1,
        1,
        1,
        2,
        8,
        DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 4 DAY)
    )
    ON DUPLICATE KEY UPDATE id = id;