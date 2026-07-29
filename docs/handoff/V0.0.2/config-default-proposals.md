# V0.0.2 Proposed Defaults

| Module / key | Proposed value | Reason | Owner confirmation |
|---|---|---|---|
| Main `runtime-role` | `MAIN` | exact placement gate | No |
| Main progress worlds | `resource`, `resource_nether`, `resource_end` | requirement baseline | No |
| Main fixed-point scale | 1000 | deterministic fractional progress | No |
| Main checkpoint / drain | 300 s / 15 s | bounded loss and shutdown | No |
| Main GUI | Japanese 27-slot status + separate repair confirmation | small, explicit mutation boundary | MAIN-D04 |
| Growth Tool presentation | Japanese concise owner-safe lore | readable without internal IDs | MAIN-D05 |
| Pending-delivery notice | two sanitized Japanese chat lines | no raw IDs or exception text | Plugin review MAIN-D06 |
| Frontier `runtime-role` / world | `FRONTIER` / exact `frontier_iris` | fail-closed placement/world authority | No |
| Missing Frontier world | admin health only; gameplay disabled | no world creation and observable failure | FRONT-D01 |
| Navigation GUI | Japanese 27-slot Loadout/Shop/Help; Waystone unavailable | exposes only authorized actions | FRONT-D05 |
| Launchpad creation | 3 successful uses, 30-day rolling expiry, 2-second cooldown, horizontal 2.5, vertical 1.2, auto-Elytra | bounded baseline and immutable snapshot | Plugin review FRONT-D03 |
| Frontier shop | Launchpad ×1 / 30 WM; Flight Duration 3 rocket ×1 / 200 WM | requirement baseline; no Waystone | No |
| Module DB pool | max 3 per installed module; 5000 ms timeout | bounded proposed option B | Plugin review ADR 0009 |
| Debug commands | disabled | least privilege and sanitized default | No |

All database values remain environment-variable references. This table contains no resolved
credential, provider object/reference, runtime address, or Project configuration.
