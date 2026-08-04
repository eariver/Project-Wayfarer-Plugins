ALTER TABLE wf_main_repair_operation
    ADD COLUMN operation_kind VARCHAR(16) CHARACTER SET ascii NOT NULL DEFAULT 'REPAIR'
        AFTER idempotency_key,
    ADD COLUMN config_revision VARCHAR(64) CHARACTER SET ascii NULL,
    ADD COLUMN evolution_count INT NULL,
    ADD COLUMN expected_item_instance_id CHAR(36) CHARACTER SET ascii NULL,
    ADD COLUMN new_item_instance_id CHAR(36) CHARACTER SET ascii NULL,
    ADD COLUMN payment_committed_at TIMESTAMP(3) NULL,
    ADD COLUMN active_guard CHAR(36) CHARACTER SET ascii NULL,
    ADD UNIQUE KEY uq_wf_main_operation_active_guard (active_guard),
    ADD KEY ix_wf_main_operation_kind_state (operation_kind, state, updated_at),
    ADD CONSTRAINT ck_wf_main_operation_kind
        CHECK (operation_kind IN ('REPAIR', 'REISSUE')),
    ADD CONSTRAINT ck_wf_main_operation_repair_state
        CHECK (operation_kind <> 'REPAIR'
            OR state IN ('PREPARED', 'PAYMENT_PENDING', 'PAYMENT_COMMITTED',
                         'DOMAIN_COMMITTED', 'REFUND_PENDING', 'REFUNDED',
                         'UNKNOWN', 'FAILED')),
    ADD CONSTRAINT ck_wf_main_operation_reissue_state
        CHECK (operation_kind <> 'REISSUE'
            OR state IN ('PREPARED', 'PAYMENT_PENDING', 'PAYMENT_COMMITTED',
                         'PENDING_DELIVERY', 'DELIVERED', 'FAILED', 'ABANDONED',
                         'UNKNOWN')),
    ADD CONSTRAINT ck_wf_main_operation_repair_guard
        CHECK (operation_kind <> 'REPAIR' OR active_guard IS NULL),
    ADD CONSTRAINT ck_wf_main_operation_guard_state
        CHECK (active_guard IS NULL
            OR (operation_kind = 'REISSUE'
                AND active_guard = tool_id
                AND state IN ('PREPARED', 'PAYMENT_PENDING', 'PAYMENT_COMMITTED',
                              'UNKNOWN'))),
    ADD CONSTRAINT ck_wf_main_operation_reissue_guard_required
        CHECK (operation_kind <> 'REISSUE'
            OR state NOT IN ('PREPARED', 'PAYMENT_PENDING', 'PAYMENT_COMMITTED',
                             'UNKNOWN')
            OR (active_guard IS NOT NULL AND active_guard = tool_id)),
    ADD CONSTRAINT ck_wf_main_operation_reissue_snapshot
        CHECK (operation_kind <> 'REISSUE'
            OR (config_revision IS NOT NULL
                AND evolution_count IS NOT NULL
                AND evolution_count >= 0
                AND expected_item_instance_id IS NOT NULL
                AND new_item_instance_id IS NOT NULL)),
    ADD CONSTRAINT ck_wf_main_operation_paid_state
        CHECK (operation_kind <> 'REISSUE'
            OR state NOT IN ('PAYMENT_COMMITTED', 'PENDING_DELIVERY', 'DELIVERED')
            OR (transaction_id IS NOT NULL AND payment_committed_at IS NOT NULL)),
    ADD CONSTRAINT ck_wf_main_operation_unpaid_state
        CHECK (operation_kind <> 'REISSUE'
            OR state NOT IN ('PREPARED', 'PAYMENT_PENDING', 'FAILED', 'ABANDONED')
            OR (payment_committed_at IS NULL
                AND (state NOT IN ('PREPARED', 'PAYMENT_PENDING', 'ABANDONED')
                     OR transaction_id IS NULL))),
    ADD CONSTRAINT ck_wf_main_operation_payment_marker
        CHECK (payment_committed_at IS NULL OR transaction_id IS NOT NULL);
