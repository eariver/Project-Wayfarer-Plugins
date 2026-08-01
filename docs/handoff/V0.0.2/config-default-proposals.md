# V0.0.2 Configuration Defaults and Accepted Presentation

These are the current Plugin-side defaults at product anchor
`7faf79081572df028a5ec19ccfbc820123180fc7`. Owner-resolved presentation outcomes are recorded
separately from the immutable requirement; later tuning is deferred.

| Module / key | Current value or boundary | Phase 07 state |
|---|---|---|
| Main `runtime-role` | `MAIN` | Implemented |
| Main progress worlds | `resource`, `resource_nether`, `resource_end` | Implemented |
| Main fixed-point scale | `1000` | Implemented |
| Main checkpoint / drain | `300 s / 15 s` | Implemented |
| Main GUI | Current English layout/status flow and separate repair confirmation | Accepted for V0.0.2; later tuning deferred |
| Growth Tool presentation | Current English name/lore with sanitized Player text | Accepted for V0.0.2; later tuning deferred |
| Pending Delivery notice | Current sanitized Player message | Accepted for V0.0.2 |
| Frontier `runtime-role` / world | `FRONTIER` / exact `frontier_iris` | Implemented |
| Missing Frontier world | Current behavior remains `FRONT-D01` review scope | `PLUGIN_REVIEW_REQUIRED` |
| Navigation GUI | Current English presentation with available implemented actions only | Accepted for V0.0.2; later tuning deferred |
| Launchpad performance | Current config controls horizontal/vertical velocity, cooldown, auto Elytra, and expiration extension | Adopted; no full immutable performance snapshot claim |
| Launchpad direction | Current Player view direction at use time | Adopted |
| Launchpad durable record | ID/location, placer, use count, max uses at creation, timestamps, definition, state, schema/lock, reserved yaw | Adopted; yaw is non-authoritative |
| Frontier shop | Current Launchpad and Flight Duration 3 Rocket offers; no Waystone | Implemented; client later |
| Module DB pools | Bounded module-local pools and separate histories under ADR 0009 | Approved/implemented |
| Debug commands | Disabled by default and gated by permission plus config | Implemented |

All database values remain environment-variable references. This file contains no resolved
credential, provider object/reference, runtime address, Project configuration, or secret.
