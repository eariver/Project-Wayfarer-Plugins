package io.github.eariver.wayfarer.common.identity;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WayfarerItemIdentityKeysTest {
    @Test
    void exposesExactlyTheSixPortablePdcKeyStrings() {
        assertEquals(Set.of(
            "wayfarer:item_type",
            "wayfarer:owner_uuid",
            "wayfarer:instance_epoch",
            "wayfarer:schema_version",
            "wayfarer:item_instance_id",
            "wayfarer:display_revision"
        ), Set.of(
            WayfarerItemIdentityKeys.ITEM_TYPE,
            WayfarerItemIdentityKeys.OWNER_UUID,
            WayfarerItemIdentityKeys.INSTANCE_EPOCH,
            WayfarerItemIdentityKeys.SCHEMA_VERSION,
            WayfarerItemIdentityKeys.ITEM_INSTANCE_ID,
            WayfarerItemIdentityKeys.DISPLAY_REVISION
        ));
    }
}
