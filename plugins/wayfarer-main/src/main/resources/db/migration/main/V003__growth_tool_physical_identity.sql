ALTER TABLE wf_main_growth_tool
    ADD COLUMN current_item_instance_id CHAR(36) CHARACTER SET ascii NULL
        AFTER tool_id;

UPDATE wf_main_growth_tool
SET current_item_instance_id = LOWER(UUID())
WHERE current_item_instance_id IS NULL;

ALTER TABLE wf_main_growth_tool
    MODIFY current_item_instance_id CHAR(36) CHARACTER SET ascii NOT NULL,
    ADD UNIQUE KEY uq_wf_main_growth_tool_physical_instance
        (current_item_instance_id);
