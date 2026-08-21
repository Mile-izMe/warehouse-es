CREATE TABLE product (
    id          UUID PRIMARY KEY,
    sku         VARCHAR(50) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT NULL,
    unit        VARCHAR(20) NOT NULL,
    min_stock   DECIMAL(19, 3) NOT NULL DEFAULT 0,
    status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NULL,
    updated_at TIMESTAMPTZ NULL,
    updated_by UUID NULL,
    deleted_at TIMESTAMPTZ NULL,
    deleted_by UUID NULL,

    CONSTRAINT ck_product_min_stock
        CHECK (min_stock >= 0),
    CONSTRAINT ck_product_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE UNIQUE INDEX uk_product_sku_active
    ON product (sku)
    WHERE deleted_at IS NULL;