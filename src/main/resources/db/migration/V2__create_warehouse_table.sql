CREATE TABLE warehouse (
    id                    UUID PRIMARY KEY,
    warehouse_code        VARCHAR(50) NOT NULL,
    name                  VARCHAR(255) NOT NULL,
    address               VARCHAR(500),
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by UUID NULL,
    updated_at TIMESTAMPTZ NULL,
    updated_by UUID NULL,
    deleted_at TIMESTAMPTZ NULL,
    deleted_by UUID NULL,

    CONSTRAINT ck_warehouse_status
        CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

CREATE UNIQUE INDEX uk_warehouse_code_active
    ON warehouse (warehouse_code)
    WHERE deleted_at IS NULL;