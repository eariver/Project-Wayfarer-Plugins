# V0.0.2 Command and Permission Reference

These command surfaces are registered only after Core capability, module migration, and
persistence initialization succeed. A failed module remains fail-closed.

## Main

- `/wayfarer-main` opens the holder-bound Growth Tool status GUI.
- The Main GUI exposes status/material/evolution/progress/next threshold/enchant/branch/
  durability, then a separate repair preview with Confirm and Cancel.
- `/wayfarer-main status`
- `/wayfarer-main inspect tool <player-uuid>`
- `/wayfarer-main inspect repair <repair-uuid>`
- `/wayfarer-main grant|delivery <player-uuid>`
- `/wayfarer-main reissue|revoke <player-uuid> confirm`
- `/wayfarer-main reconcile <repair-uuid>`
- `/wayfarer-main repair|branch ...` (player workflow)
- `/wayfarer-main debug <progress-next|durability-one|repair-free>`
- alias: `/wfmain`
- `wayfarer.main.use`: default true
- `wayfarer.main.admin`: default operator
- `wayfarer.main.debug`: default operator and additionally requires config enablement

## Frontier

- `/wayfarer-frontier open` opens the holder-bound Navigation GUI with Loadout, Shop, Help, and
  an explicitly unavailable Waystone action.
- `/wayfarer-frontier status|shop <offer>`
- `/wayfarer-frontier loadout inspect <player-uuid>`
- `/wayfarer-frontier loadout reissue <player-uuid> <elytra|grappling_hook|navigation> confirm`
- `/wayfarer-frontier delivery <inspect|retry> <player-uuid>`
- `/wayfarer-frontier launchpad inspect <launchpad-uuid>`
- `/wayfarer-frontier launchpad <remove|reconcile> <launchpad-uuid> confirm`
- `/wayfarer-frontier transaction|audit inspect <purchase-uuid>`
- alias: `/wffrontier`
- `wayfarer.frontier.use`: default true
- `wayfarer.frontier.admin`: default operator

Player output must use sanitized status/failure codes. Raw UUIDs may appear only in authorized
admin evidence where required; secrets, provider references, and exception text remain hidden.
