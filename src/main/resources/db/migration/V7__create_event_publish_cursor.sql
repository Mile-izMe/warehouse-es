CREATE TABLE event_publish_cursor (
    worker_id                 VARCHAR(50) PRIMARY KEY,
    last_processed_event_id   BIGINT NOT NULL
);

INSERT INTO event_publish_cursor (worker_id, last_processed_event_id) VALUES ('kafka_main_relay', 0);