# V0.0.2 Sanitized Configuration

## Main

- `config-version: 1`
- `runtime-role: MAIN`
- exact progress worlds: `resource`, `resource_nether`, `resource_end`
- fixed-point scale: 1000
- checkpoint: 300 seconds; bounded disable drain: 15 seconds
- database pool proposal: maximum 3; connection timeout 5000 ms
- database values are environment-variable references only:
  `WAYFARER_DB_URL`, `WAYFARER_DB_USERNAME`, `WAYFARER_DB_PASSWORD`
- debug commands: disabled

## Frontier

- `config-version: 1`
- `runtime-role: FRONTIER`
- exact world: `frontier_iris`
- expected LeafGrapple: `1.0.2`
- navigation Waystone state: `DEFERRED_BY_REQUIREMENT`
- Launchpad defaults: 2 initial, 3 successful uses, 30-day expiry, 2-second cooldown,
  horizontal 2.5, vertical 1.2, auto-Elytra
- shop: Launchpad ×1 / 30 WM; Flight Duration 3 rocket ×1 / 200 WM
- database references and pool proposal match Main

Resolved credentials, URLs containing credentials, provider references, and runtime-specific
values are intentionally absent.
