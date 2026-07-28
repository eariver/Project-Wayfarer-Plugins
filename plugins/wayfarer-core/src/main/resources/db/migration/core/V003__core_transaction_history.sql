ALTER TABLE wf_core_transaction
    CHANGE COLUMN provider_reference debit_provider_reference VARCHAR(191) NULL,
    ADD COLUMN debit_operation_id VARCHAR(191) CHARACTER SET ascii NULL
        AFTER debit_provider_reference,
    ADD COLUMN refund_operation_id VARCHAR(191) CHARACTER SET ascii NULL
        AFTER debit_operation_id,
    ADD COLUMN refund_provider_reference VARCHAR(191) NULL
        AFTER refund_operation_id,
    ADD COLUMN refund_terminal_state VARCHAR(40) CHARACTER SET ascii NULL
        AFTER refund_provider_reference,
    ADD COLUMN recovery_claim_id CHAR(36) CHARACTER SET ascii NULL
        AFTER refund_terminal_state,
    ADD COLUMN recovery_claim_until TIMESTAMP(3) NULL
        AFTER recovery_claim_id,
    ADD COLUMN reconcile_attempts INT NOT NULL DEFAULT 0 AFTER failure_code,
    ADD KEY ix_wf_core_transaction_recovery (
        state, recovery_claim_until, updated_at
    );

CREATE TABLE wf_core_transaction_event (
    event_id BIGINT NOT NULL AUTO_INCREMENT,
    transaction_id CHAR(36) CHARACTER SET ascii NOT NULL,
    from_state VARCHAR(40) CHARACTER SET ascii NULL,
    to_state VARCHAR(40) CHARACTER SET ascii NOT NULL,
    debit_operation_id VARCHAR(191) CHARACTER SET ascii NULL,
    debit_provider_reference VARCHAR(191) NULL,
    refund_operation_id VARCHAR(191) CHARACTER SET ascii NULL,
    refund_provider_reference VARCHAR(191) NULL,
    refund_terminal_state VARCHAR(40) CHARACTER SET ascii NULL,
    recovery_claim_id CHAR(36) CHARACTER SET ascii NULL,
    recovery_claim_until TIMESTAMP(3) NULL,
    failure_code VARCHAR(96) CHARACTER SET ascii NULL,
    transaction_lock_version BIGINT NOT NULL,
    occurred_at TIMESTAMP(3) NOT NULL,
    PRIMARY KEY (event_id),
    KEY ix_wf_core_transaction_event_transaction (transaction_id, event_id),
    CONSTRAINT fk_wf_core_transaction_event_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES wf_core_transaction (transaction_id)
        ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
