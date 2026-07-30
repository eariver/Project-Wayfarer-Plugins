# V0.0.2 Mainline Handoff

Draft PR #14 is review-only. The implementation packet is anchored by
`ddc6711e358067414d180d0780eac490faf00dff`.

## Review order

1. Review corrected CI/headless evidence.
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
- Waystone production template/safe-arrival work;
- Order 13 decision for any EliteMobs–MVI adapter;
- Order 25 preserve/reset authority.

Main and Frontier acceptance must remain independently reversible. No stable tag, release,
deployment, migration, server restart, or roadmap completion is implied by this handoff.
