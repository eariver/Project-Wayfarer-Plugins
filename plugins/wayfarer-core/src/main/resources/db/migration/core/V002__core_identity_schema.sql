CREATE TABLE wf_core_player_identity (
    player_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    last_known_name VARCHAR(16) CHARACTER SET ascii NOT NULL,
    first_seen_at TIMESTAMP(3) NOT NULL,
    last_seen_at TIMESTAMP(3) NOT NULL,
    last_server_id VARCHAR(64) CHARACTER SET ascii NOT NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (player_uuid),
    KEY ix_wf_core_player_identity_last_seen (last_seen_at),
    KEY ix_wf_core_player_identity_name (last_known_name),
    CONSTRAINT ck_wf_core_player_identity_lock CHECK (lock_version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE wf_core_item_identity (
    item_instance_id CHAR(36) CHARACTER SET ascii NOT NULL,
    item_type VARCHAR(96) CHARACTER SET ascii NOT NULL,
    owner_uuid CHAR(36) CHARACTER SET ascii NOT NULL,
    instance_epoch BIGINT NOT NULL,
    schema_version INT NOT NULL,
    display_revision INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP(3) NOT NULL,
    updated_at TIMESTAMP(3) NOT NULL,
    lock_version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (item_instance_id),
    KEY ix_wf_core_item_identity_owner_type (owner_uuid, item_type),
    KEY ix_wf_core_item_identity_type_updated (item_type, updated_at),
    CONSTRAINT ck_wf_core_item_identity_epoch CHECK (instance_epoch >= 0),
    CONSTRAINT ck_wf_core_item_identity_schema CHECK (schema_version > 0),
    CONSTRAINT ck_wf_core_item_identity_display CHECK (display_revision >= 0),
    CONSTRAINT ck_wf_core_item_identity_lock CHECK (lock_version >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
