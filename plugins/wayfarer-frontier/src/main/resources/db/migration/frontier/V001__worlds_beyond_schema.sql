CREATE TABLE wf_frontier_theme_player_state (
    player_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    theme_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    first_joined_at TIMESTAMP(3) NULL,
    initial_launchpad_granted BOOLEAN NOT NULL DEFAULT FALSE,
    initial_launchpad_granted_at TIMESTAMP(3) NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (player_uuid, theme_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wf_frontier_item_instance (
    player_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    theme_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    item_type VARCHAR(64) CHARACTER SET ascii NOT NULL,
    instance_epoch BIGINT NOT NULL DEFAULT 1,
    state VARCHAR(24) CHARACTER SET ascii NOT NULL,
    issued_at TIMESTAMP(3) NOT NULL,
    invalidated_at TIMESTAMP(3) NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (player_uuid, theme_id, item_type),
    CONSTRAINT ck_wf_frontier_item_epoch CHECK (instance_epoch >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wf_frontier_pending_delivery (
    delivery_id CHAR(36) CHARACTER SET ascii NOT NULL,
    player_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    theme_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    item_type VARCHAR(64) CHARACTER SET ascii NOT NULL,
    quantity INT NOT NULL,
    idempotency_key VARCHAR(191) CHARACTER SET ascii NOT NULL,
    payload_json LONGTEXT NULL,
    state VARCHAR(24) CHARACTER SET ascii NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    last_error VARCHAR(191) NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (delivery_id),
    UNIQUE KEY uq_wf_frontier_delivery_idempotency (idempotency_key),
    KEY ix_wf_frontier_delivery_player_state (player_uuid, state, created_at),
    CONSTRAINT ck_wf_frontier_delivery_quantity CHECK (quantity > 0),
    CONSTRAINT ck_wf_frontier_delivery_payload CHECK (payload_json IS NULL OR JSON_VALID(payload_json))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wf_frontier_launchpad (
    launchpad_id CHAR(36) CHARACTER SET ascii NOT NULL,
    world_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    z INT NOT NULL,
    yaw FLOAT NOT NULL,
    placer_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    successful_use_count INT NOT NULL DEFAULT 0,
    max_uses_at_creation INT NOT NULL,
    created_at TIMESTAMP(3) NOT NULL,
    last_used_at TIMESTAMP(3) NULL,
    expires_at TIMESTAMP(3) NOT NULL,
    definition_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    state VARCHAR(24) CHARACTER SET ascii NOT NULL,
    schema_version INT NOT NULL DEFAULT 1,
    lock_version BIGINT NOT NULL DEFAULT 0,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (launchpad_id),
    UNIQUE KEY uq_wf_frontier_launchpad_location (world_id, x, y, z),
    KEY ix_wf_frontier_launchpad_expiry (state, expires_at),
    KEY ix_wf_frontier_launchpad_placer (placer_uuid, state),
    CONSTRAINT ck_wf_frontier_launchpad_uses CHECK (
        successful_use_count >= 0 AND max_uses_at_creation > 0
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wf_frontier_waystone (
    waystone_id CHAR(36) CHARACTER SET ascii NOT NULL,
    theme_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    world_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    z INT NOT NULL,
    rotation SMALLINT NOT NULL,
    template_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    founder_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    founder_name_at_creation VARCHAR(64) NOT NULL,
    maintainer_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    biome_key VARCHAR(191) CHARACTER SET ascii NOT NULL,
    biome_display_name VARCHAR(96) NOT NULL,
    founder_biome_sequence BIGINT NOT NULL,
    display_name VARCHAR(191) NOT NULL,
    state VARCHAR(24) CHARACTER SET ascii NOT NULL,
    protected_until TIMESTAMP(3) NULL,
    active_until TIMESTAMP(3) NULL,
    dormant_at TIMESTAMP(3) NULL,
    ruin_after TIMESTAMP(3) NULL,
    ruined_at TIMESTAMP(3) NULL,
    created_at TIMESTAMP(3) NOT NULL,
    last_maintained_at TIMESTAMP(3) NOT NULL,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    lock_version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (waystone_id),
    UNIQUE KEY uq_wf_frontier_waystone_name (display_name),
    KEY ix_wf_frontier_waystone_spatial (world_id, state, x, z),
    KEY ix_wf_frontier_waystone_protected (state, protected_until),
    KEY ix_wf_frontier_waystone_active (state, active_until),
    KEY ix_wf_frontier_waystone_ruin (state, ruin_after),
    KEY ix_wf_frontier_waystone_maintainer (maintainer_uuid, state),
    KEY ix_wf_frontier_waystone_founder (founder_uuid, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wf_frontier_waystone_sequence (
    founder_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    biome_key VARCHAR(191) CHARACTER SET ascii NOT NULL,
    last_sequence BIGINT NOT NULL,
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (founder_uuid, biome_key),
    CONSTRAINT ck_wf_frontier_waystone_sequence CHECK (last_sequence >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wf_frontier_waystone_discovery (
    waystone_id CHAR(36) CHARACTER SET ascii NOT NULL,
    player_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    discovered_at TIMESTAMP(3) NOT NULL,
    last_interacted_at TIMESTAMP(3) NOT NULL,
    last_teleported_at TIMESTAMP(3) NULL,
    teleport_count BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (waystone_id, player_uuid),
    KEY ix_wf_frontier_discovery_player (player_uuid, discovered_at),
    CONSTRAINT fk_wf_frontier_discovery_waystone
        FOREIGN KEY (waystone_id) REFERENCES wf_frontier_waystone(waystone_id),
    CONSTRAINT ck_wf_frontier_discovery_count CHECK (teleport_count >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wf_frontier_waystone_history (
    history_id BIGINT NOT NULL AUTO_INCREMENT,
    waystone_id CHAR(36) CHARACTER SET ascii NOT NULL,
    event_type VARCHAR(64) CHARACTER SET ascii NOT NULL,
    actor_uuid CHAR(36) CHARACTER SET ascii NULL,
    previous_maintainer_uuid CHAR(36) CHARACTER SET ascii NULL,
    new_maintainer_uuid CHAR(36) CHARACTER SET ascii NULL,
    wm_cost BIGINT NOT NULL DEFAULT 0,
    transaction_id CHAR(36) CHARACTER SET ascii NULL,
    occurred_at TIMESTAMP(3) NOT NULL,
    details_json LONGTEXT NULL,
    PRIMARY KEY (history_id),
    KEY ix_wf_frontier_waystone_history_waystone (waystone_id, occurred_at),
    KEY ix_wf_frontier_waystone_history_actor (actor_uuid, occurred_at),
    CONSTRAINT fk_wf_frontier_history_waystone
        FOREIGN KEY (waystone_id) REFERENCES wf_frontier_waystone(waystone_id),
    CONSTRAINT ck_wf_frontier_history_cost CHECK (wm_cost >= 0),
    CONSTRAINT ck_wf_frontier_history_details CHECK (details_json IS NULL OR JSON_VALID(details_json))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wf_frontier_placement_transaction (
    placement_transaction_id CHAR(36) CHARACTER SET ascii NOT NULL,
    placement_type VARCHAR(24) CHARACTER SET ascii NOT NULL,
    player_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    item_instance_id CHAR(36) CHARACTER SET ascii NOT NULL,
    world_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    x INT NOT NULL,
    y INT NOT NULL,
    z INT NOT NULL,
    rotation SMALLINT NOT NULL,
    state VARCHAR(32) CHARACTER SET ascii NOT NULL,
    domain_id CHAR(36) CHARACTER SET ascii NULL,
    failure_code VARCHAR(96) CHARACTER SET ascii NULL,
    payload_json LONGTEXT NULL,
    created_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    updated_at TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
    lock_version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (placement_transaction_id),
    UNIQUE KEY uq_wf_frontier_placement_item (item_instance_id),
    KEY ix_wf_frontier_placement_state (state, updated_at),
    CONSTRAINT ck_wf_frontier_placement_payload CHECK (payload_json IS NULL OR JSON_VALID(payload_json))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
