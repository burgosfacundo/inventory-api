CREATE TABLE suppliers (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(100) NOT NULL,
                        phone VARCHAR(25),
                        description VARCHAR(255),
                        active BOOLEAN NOT NULL DEFAULT TRUE,

                        PRIMARY KEY (id),

                        CONSTRAINT uk_suppliers_email
                              UNIQUE (email)
);