# V0.0.2 Compatibility Matrix

| Consumer | Core requirement | Config | Migration | Upgrade from V0.0.1 | Downgrade / rollback |
|---|---|---|---|---|---|
| Wayfarer_Main current source | `>=0.0.1 <0.1.0` | current Main config | Main V001→V004 | Forward-only; separate-history tests and V004 reissue evidence | No down migration; disable/remove module without deleting state |
| Wayfarer_Frontier current source | `>=0.0.1 <0.1.0` | current Frontier config | Frontier V001→V002 | Forward-only; separate-history and Pending Delivery evidence | No down migration; preserve pending/reconcile state |
| Wayfarer_Core V0.0.1 | self | Core config v1 | Core V001–V003 | Unchanged and reused | Existing V0.0.1 authority |

Main and Frontier have no mutual dependency. Core has no dependency on either gameplay module.
Normal inventory/profile state remains Minecraft backend/MVI-owned. Old broad permission nodes are
not compatibility promises; Phase 06 leaf permissions are the current documented surface.
