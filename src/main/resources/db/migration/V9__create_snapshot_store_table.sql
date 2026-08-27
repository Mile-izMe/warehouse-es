CREATE TABLE snapshot_store (
    aggregate_id        VARCHAR(100)    PRIMARY KEY,
    snapshot_version    INT             NOT NULL DEFAULT 0,
    snapshot_payload    JSONB           NOT NULL,

    created_at          TIMESTAMPTZ     NOT NULL DEFAULT CURRENT_TIMESTAMP
);