# V0.0.2 Command and Permission Reference

These command surfaces are proposed and declared in plugin descriptors. Mutation handlers are not
registered while ADR 0009 keeps the plugins fail-closed.

## Main

- `/wayfarer-main status`
- `/wayfarer-main inspect|grant|reissue|repair|branch|revoke|reconcile|delivery`
- alias: `/wfmain`
- `wayfarer.main.use`: default true
- `wayfarer.main.admin`: default operator
- `wayfarer.main.debug`: default operator and additionally requires config enablement

## Frontier

- `/wayfarer-frontier open|status`
- `/wayfarer-frontier loadout|delivery|launchpad|transaction|audit`
- alias: `/wffrontier`
- `wayfarer.frontier.use`: default true
- `wayfarer.frontier.admin`: default operator

Player output must use sanitized status/failure codes. Raw UUIDs may appear only in authorized
admin evidence where required; secrets, provider references, and exception text remain hidden.
