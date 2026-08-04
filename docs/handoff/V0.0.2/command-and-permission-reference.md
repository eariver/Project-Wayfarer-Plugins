# V0.0.2 Command and Permission Reference

Commands are exposed only after Core capability, module migration, persistence initialization,
and recovery gates succeed. A failed module remains fail-closed. The current command surface is
documented exactly; later presentation tuning is not a V0.0.2 blocker.

## Main routes

```text
/wayfarer-main
/wayfarer-main status
/wayfarer-main tool reissue [confirm]
/wayfarer-main repair
/wayfarer-main branch <FORTUNE|SILK_TOUCH>
/wayfarer-main inspect <tool|repair|reissue> <uuid>
/wayfarer-main grant <player-uuid>
/wayfarer-main delivery <player-uuid>
/wayfarer-main reissue <player-uuid> confirm
/wayfarer-main revoke <player-uuid> confirm
/wayfarer-main reconcile <repair-uuid>
/wayfarer-main reconcile <reissue-uuid> confirm-payment confirm
/wayfarer-main reconcile <reissue-uuid> resume-payment confirm
/wayfarer-main reconcile <reissue-uuid> resume-rotation confirm
/wayfarer-main reconcile <reissue-uuid> mark-failed <FAILURE_CODE> confirm
/wayfarer-main debug <progress-next|durability-one|repair-free>
```

No arguments and `status` are Admin read routes; they do not open the Player GUI. Player
`repair` and `tool reissue [confirm]` use `wayfarer.main.use`. The Player reissue quote costs
broken repair plus full repair, rejects an authoritative current item or Pending Delivery before
debit, rotates to a new physical instance/epoch on success, and leaves physical delivery pending
when inventory is unavailable. Later delivery retry is free.

## Frontier routes

```text
/wayfarer-frontier open
/wayfarer-frontier shop <offer>
/wayfarer-frontier
/wayfarer-frontier status
/wayfarer-frontier loadout inspect <player-uuid>
/wayfarer-frontier loadout reissue <player-uuid> <elytra|grappling_hook|navigation> confirm
/wayfarer-frontier delivery inspect <player-uuid>
/wayfarer-frontier delivery retry <player-uuid>
/wayfarer-frontier launchpad inspect <launchpad-uuid>
/wayfarer-frontier launchpad remove <launchpad-uuid> confirm
/wayfarer-frontier launchpad reconcile <launchpad-uuid> [confirm]
/wayfarer-frontier transaction inspect <purchase-uuid>
/wayfarer-frontier audit inspect <purchase-uuid>
```

Player `open` and `shop <offer>` use `wayfarer.frontier.use`. Permanent Frontier items use typed
durable Pending Delivery and same identity/epoch free redelivery; Launchpad and Rocket are not
permanent free-redelivery items.

## Phase 06 leaf mapping

| Route | Permission |
|---|---|
| Main no arguments / `status` / `inspect ...` | `wayfarer.main.admin.read` |
| Main `grant`, `delivery`, Admin `reissue ... confirm` | `wayfarer.main.admin.delivery` |
| Main Admin `revoke ... confirm`, `branch ...` | `wayfarer.main.admin.modify` |
| Main `reconcile ...` | `wayfarer.main.admin.reconcile` |
| Main `debug ...` plus enabled debug config | `wayfarer.main.debug` |
| Frontier no arguments / `status` / read `inspect` routes | `wayfarer.frontier.admin.read` |
| Frontier `loadout reissue ...`, `delivery retry ...` | `wayfarer.frontier.admin.delivery` |
| Frontier `launchpad remove ... confirm` | `wayfarer.frontier.admin.launchpad` |
| Frontier `launchpad reconcile ...` | `wayfarer.frontier.admin.reconcile` |

## Permission descriptors and Project behavior

Main declares:

```text
wayfarer.main.use
wayfarer.main.admin.read
wayfarer.main.admin.delivery
wayfarer.main.admin.modify
wayfarer.main.admin.reconcile
wayfarer.main.admin.*
wayfarer.main.debug
```

Frontier declares:

```text
wayfarer.frontier.use
wayfarer.frontier.admin.read
wayfarer.frontier.admin.delivery
wayfarer.frontier.admin.launchpad
wayfarer.frontier.admin.reconcile
wayfarer.frontier.admin.*
wayfarer.frontier.debug
```

`use` defaults true. Each `admin.*` umbrella defaults to OP and has exactly its four Phase 06
leaf children; each leaf defaults false. Debug is separate from the umbrella and defaults to OP,
but the configured debug gate is also required. There is no top-level command `permission:`
descriptor. The old exact broad nodes `wayfarer.main.admin` and `wayfarer.frontier.admin` are
inactive and are not compatibility promises.

Project Admin remains temporary LuckPerms `wayfarer_admin` with global `*`; Project OP is disabled.
No Project permission group, context, backend, or Runtime configuration was changed here.

Player output remains sanitized: no secrets, provider references, raw exception text, or
unnecessary internal IDs.
