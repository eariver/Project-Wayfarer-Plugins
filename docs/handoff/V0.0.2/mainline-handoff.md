# V0.0.2 Mainline Handoff

Draft PR #14 is review-only. The implementation packet is anchored by
`981e425a4af619340b64b2060c0cb9ac7219cdd2`; later documentation commits do not change that
runtime proposal.

## Review order

1. Decide ADR 0009 module-local pool/Flyway ownership.
2. Decide B-004 gameplay-domain completion versus Core transaction completion.
3. Review LeafGrapple safe capability and supported protection hooks.
4. Resolve Owner presentation decisions.
5. Complete production wiring, headless evidence, candidate fixation, then bounded client tests.

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
