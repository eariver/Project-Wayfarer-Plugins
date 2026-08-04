CREATE TABLE wf_frontier_purchase (
    purchase_id CHAR(36) CHARACTER SET ascii NOT NULL,
    idempotency_key VARCHAR(191) CHARACTER SET ascii NOT NULL,
    player_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    theme_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    offer_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    item_type VARCHAR(64) CHARACTER SET ascii NOT NULL,
    quantity INT NOT NULL,
    price_waymark BIGINT NOT NULL,
    state VARCHAR(32) CHARACTER SET ascii NOT NULL,
    transaction_id CHAR(36) CHARACTER SET ascii NULL,
    delivery_id CHAR(36) CHARACTER SET ascii NULL,
    failure_code VARCHAR(96) CHARACTER SET ascii NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (purchase_id),
    UNIQUE KEY uq_wf_frontier_purchase_idempotency (idempotency_key),
    UNIQUE KEY uq_wf_frontier_purchase_transaction (transaction_id),
    KEY ix_wf_frontier_purchase_player_state (player_uuid, state, updated_at),
    CONSTRAINT fk_wf_frontier_purchase_delivery
        FOREIGN KEY (delivery_id) REFERENCES wf_frontier_pending_delivery(delivery_id),
    CONSTRAINT ck_wf_frontier_purchase_quantity CHECK (quantity > 0),
    CONSTRAINT ck_wf_frontier_purchase_price CHECK (price_waymark >= 0),
    CONSTRAINT ck_wf_frontier_purchase_lock CHECK (lock_version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

ALTER TABLE wf_frontier_launchpad
    ADD COLUMN use_claim_token CHAR(36) CHARACTER SET ascii NULL AFTER state,
    ADD COLUMN use_claim_until TIMESTAMP(3) NULL AFTER use_claim_token,
    ADD COLUMN failure_code VARCHAR(96) CHARACTER SET ascii NULL AFTER use_claim_until,
    ADD KEY ix_wf_frontier_launchpad_claim (state, use_claim_until);

CREATE TABLE wf_frontier_launchpad_history (
    history_id BIGINT NOT NULL AUTO_INCREMENT,
    launchpad_id CHAR(36) CHARACTER SET ascii NOT NULL,
    event_type VARCHAR(64) CHARACTER SET ascii NOT NULL,
    actor_uuid CHAR(36) CHARACTER SET ascii NULL,
    audit_event_id CHAR(36) CHARACTER SET ascii NULL,
    occurred_at TIMESTAMP(3) NOT NULL,
    details_json LONGTEXT NULL,
    PRIMARY KEY (history_id),
    KEY ix_wf_frontier_launchpad_history (launchpad_id, occurred_at),
    CONSTRAINT ck_wf_frontier_launchpad_history_details
        CHECK (details_json IS NULL OR JSON_VALID(details_json))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
