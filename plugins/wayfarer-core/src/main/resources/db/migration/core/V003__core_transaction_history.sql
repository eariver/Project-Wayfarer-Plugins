ALTER TABLE wf_core_transaction
    ADD COLUMN provider_operation_id VARCHAR(191) CHARACTER SET ascii NULL AFTER provider_reference,
    ADD COLUMN reconcile_attempts INT NOT NULL DEFAULT 0 AFTER failure_code;

CREATE TABLE wf_core_transaction_event (
    event_id BIGINT NOT NULL AUTO_INCREMENT,
    transaction_id CHAR(36) CHARACTER SET ascii NOT NULL,
    from_state VARCHAR(40) CHARACTER SET ascii NULL,
    to_state VARCHAR(40) CHARACTER SET ascii NOT NULL,
    provider_reference VARCHAR(191) NULL,
    failure_code VARCHAR(96) CHARACTER SET ascii NULL,
    occurred_at TIMESTAMP(3) NOT NULL,
    PRIMARY KEY (event_id),
    KEY ix_wf_core_transaction_event_transaction (transaction_id, event_id),
    CONSTRAINT fk_wf_core_transaction_event_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES wf_core_transaction (transaction_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
