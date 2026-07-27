# Wayfarer Item Identity Contract

The public contract is asynchronous and JDK-only. MariaDB is the identity authority; Paper PDC is
a raw claim that must be validated against it.

The six required claims are `wayfarer:item_type`, `wayfarer:owner_uuid`,
`wayfarer:instance_epoch`, `wayfarer:schema_version`, `wayfarer:item_instance_id`, and
`wayfarer:display_revision`. Missing values, non-canonical UUIDs, negative epoch/display revision,
non-positive schema, unknown type/schema, absent records, and type/owner/epoch/schema/display
mismatches fail closed with the corresponding `FailureReason`.

`create` completes only after durable insert. `find` and `validate` are asynchronous. Invalid
claims return an invalid domain result only after durable
`ITEM_IDENTITY_VALIDATION_FAILED` audit succeeds; audit failure is exceptional. The audit never
stores a raw malformed UUID, full PDC/NBT dump, inventory, lore, display name, or material-only
evidence.
