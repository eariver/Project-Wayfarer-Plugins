CREATE TABLE wf_main_repair_operation (
    repair_id CHAR(36) CHARACTER SET ascii NOT NULL,
    idempotency_key VARCHAR(191) CHARACTER SET ascii NOT NULL,
    player_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    tool_id CHAR(36) CHARACTER SET ascii NOT NULL,
    instance_epoch BIGINT NOT NULL,
    amount_waymark BIGINT NOT NULL,
    state VARCHAR(32) CHARACTER SET ascii NOT NULL,
    transaction_id CHAR(36) CHARACTER SET ascii NULL,
    refund_operation_id VARCHAR(191) CHARACTER SET ascii NULL,
    failure_code VARCHAR(96) CHARACTER SET ascii NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (repair_id),
    UNIQUE KEY uq_wf_main_repair_idempotency (idempotency_key),
    UNIQUE KEY uq_wf_main_repair_refund_operation (refund_operation_id),
    KEY ix_wf_main_repair_player_state (player_uuid, state, updated_at),
    KEY ix_wf_main_repair_transaction (transaction_id),
    CONSTRAINT fk_wf_main_repair_tool
        FOREIGN KEY (tool_id) REFERENCES wf_main_growth_tool(tool_id),
    CONSTRAINT ck_wf_main_repair_epoch CHECK (instance_epoch >= 1),
    CONSTRAINT ck_wf_main_repair_amount CHECK (amount_waymark > 0),
    CONSTRAINT ck_wf_main_repair_lock CHECK (lock_version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
