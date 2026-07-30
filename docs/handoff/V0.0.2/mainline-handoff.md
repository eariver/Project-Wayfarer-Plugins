# V0.0.2 Mainline Handoff

Draft PR #14 is review-only. The implementation packet is anchored by
`2114e3cd8f5d6fcd7b4aeb22fd4343290e297072`.

## Review order

1. Review second-review CI/headless and representative gameplay evidence.
2. Review LeafGrapple safe capability and supported protection hooks.
3. Resolve Owner presentation decisions.
4. Fix a candidate only after review, then run bounded client tests.

## Project-owned follow-up

After Plugin acceptance, transfer the following to the Project repository's deferred-design
record (for example `docs/11-deferred-design-items.md`) without changing Project Runtime from this
repository:

- Vault/RedisEconomy success is acceptance, not durable Redis completion or effect lookup;
- stronger guarantees must be a common economy-platform improvement, not a Wayfarer side channel;
- Frontier seed/border/generation, portal deny, gate coordinates, and MVI configuration;
- protection coverage for tools that bypass Bukkit and the public WorldEdit edit-session API;
- Waystone production template/safe-arrival work;
- Order 13 decision for any EliteMobs–MVI adapter;
- Order 25 preserve/reset authority.

Main and Frontier acceptance must remain independently reversible. No stable tag, release,
deployment, migration, server restart, or roadmap completion is implied by this handoff.
