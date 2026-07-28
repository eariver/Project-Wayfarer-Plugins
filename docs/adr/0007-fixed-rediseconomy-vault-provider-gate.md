# ADR 0007: Keep the Fixed RedisEconomy/Vault Surface Behind an Owner Gate

- Status: Owner decision required
- Date: 2026-07-29
- Plugin repository base: `16ce48dd0cdf905c5fcff5430b54459a72173e4a`
- Project authority source: `344eedc738d75954daa43facfeef302944f2963a`
- Supersedes: none; this record resolves the source questions left by ADR 0006

## Context

The Project authority fixes these runtime artifacts:

| Artifact | Fixed version | SHA-256 |
|---|---|---|
| RedisEconomy | `4.5.12-wayfarer.1` | `AB00270CD970A909F54F6EE7C2C47151FB90DB0EA36FA6AB68AC59D939CFCA47` |
| VaultUnlocked | `2.20.2` | `BD9E7A31F1B2D31A591497174887EEA7AE7E632C6B179DA13E4F0AD732DE2DF7` |

Both Main and Frontier copies matched these values in the one permitted hash check. The fixed
RedisEconomy JAR bytecode was also inspected to ensure the behavior below is present in the binary,
not merely in a nearby source tree.

The designated RedisEconomy source was at commit
`581091a121ac155a830b927b3a8af6cbc4de2946`. Relevant immutable blobs are:

| Source | Git blob |
|---|---|
| `Currency.java` | `46a5cd87791c83577f073592739ad7a5c7acf679` |
| `RedisManager.java` | `fbe842ae6469a8c66a3f9fc13dcc9e909b4054e1` |
| `CurrenciesManager.java` | `f157a7dad5abb57bb85c377450bb1d1143b19c2e` |
| `RedisEconomyPlugin.java` | `9256847b66e75da0e774a9cf371224e80ea93e30` |
| `plugin.yml` | `54be051a757e6dcdaf4f5d979eec4120966dbfbb` |

The designated source worktree also contained an uncommitted two-file compatibility patch in
`build.gradle` and `Langs.java`, with stable Git patch ID
`edd629de328edf205ddce5a6e2692fbf8c9a8c3b`. It changes the version/Paper dependency and the
Paper 1.21.11 message path, but not the economy methods assessed here. The patch is determinable,
but commit `581091a` alone is not the immutable source of the fixed JAR's compatibility changes.
Neither the designated source nor Project files were modified.

## Observed fixed-source behavior

### Discovery and load order

VaultUnlocked and RedisEconomy both declare `STARTUP`; RedisEconomy has a hard dependency on
`Vault`. RedisEconomy obtains the `Vault` plugin and constructs `CurrenciesManager` during
`onEnable`. `CurrenciesManager` loads the currency caches, unregisters existing `Economy`
providers, and registers its default `Currency` through Bukkit `ServicesManager` before
RedisEconomy `onEnable` returns. The service registration is owned by the Vault plugin even though
the provider object is RedisEconomy's `Currency`.

Core's existing `softdepend: [Vault, RedisEconomy, LuckPerms]` is sufficient to place Core after
successful RedisEconomy enable. It does not supply a safe effect contract.

### Balance, debit, refund, and failure

- `getBalance(UUID)` reads RedisEconomy's `ConcurrentHashMap` cache.
- Vault exposes only player-name and `OfflinePlayer` overloads. It has no UUID overload.
- RedisEconomy has public UUID overloads, but those are RedisEconomy-specific API.
- `withdrawPlayer` performs an account/balance check, calculates a new balance, calls
  `updateAccount`, starts a separate transaction-history write, and immediately returns Vault
  `SUCCESS`. Insufficient funds returns Vault `FAILURE` before the update.
- `depositPlayer` follows the same immediate-success pattern.
- `updateAccount` schedules Redis `MULTI`/`EXEC` work on a RedisEconomy-owned executor and then
  updates the local cache. The returned future is discarded.
- Redis timeout/connection failures are retried and eventually logged inside RedisEconomy. They
  are not returned through `EconomyResponse`.
- RedisEconomy's separate transaction-history `CompletionStage` is also discarded by
  debit/refund.

Therefore a Vault `SUCCESS` means that the local cache mutation was accepted for later persistence.
It does not mean the Redis balance update or provider transaction record completed. A Core timeout
around the Vault call cannot bound or cancel the later Redis effect.

The debit read/check/write is not one atomic operation. `ConcurrentHashMap` makes individual reads
and writes thread-safe, but the balance test and replacement are separate. The synchronization in
`updateAccountCloudCache` serializes enqueueing, not the preceding balance calculation or a
cross-server authoritative conditional debit.

### Crash, resolve, and shutdown

The Vault surface cannot accept a Wayfarer operation ID and exposes no formal effect lookup.
RedisEconomy's fixed API provides no lookup by a caller operation ID. A balance comparison cannot
prove which concurrent effect occurred, so `resolve` would always be `UNKNOWN`.

On disable, RedisEconomy closes Redis before unregistering the Economy provider and terminating its
currency executors. The executor termination code does not establish a completed durable drain for
previously accepted balance work. This is another window where Vault has already returned
`SUCCESS` but durable effect completion is unknown.

## Decision

Do not implement or nominate a Vault-backed concrete provider from the fixed surface. Do not use
the current RedisEconomy UUID overload as an unapproved default. Keep production Waymark disabled
and fail closed as already implemented.

This is Gate B because Vault cannot provide the required UUID-only, atomic, completion/failure, and
timeout contract. It is Gate C because avoiding `OfflinePlayer` and adding the missing effect
contract requires RedisEconomy-specific API. It is Gate D because treating Vault `SUCCESS` as a
durable applied result can commit a Wayfarer transaction while Redis persistence later fails.
The source provenance mismatch for the compatibility patch must also be corrected when a new fixed
artifact is produced.

## Owner decision request

### Question

May the fixed RedisEconomy integration be replaced by a formally approved RedisEconomy-specific
Waymark API and a new fixed artifact that provides atomic UUID debit/refund, completion after the
authoritative effect, caller operation IDs, and effect lookup?

### Current requirement

Custom plugins use Vault or a formally approved Waymark Service Adapter; RedisEconomy internal keys
must not be accessed. Main-thread synchronous Redis I/O is forbidden. Provider ambiguity must
become `UNKNOWN`, and duplicate debit/refund must not be authorized.

### Observed fixed-source behavior

Vault returns synchronously after a local cache mutation while Redis persistence continues on an
unobserved executor. Failure is not returned, compound debit is not atomic, UUID-only calls are
outside Vault, and no operation-ID/effect lookup exists.

### Proposed change

Approve a RedisEconomy-owned public Waymark API that:

1. accepts UUID, exact amount, effect kind, and the caller's stable operation ID;
2. performs an atomic authoritative apply-or-return-existing operation without exposing internal
   keys;
3. completes only when the authoritative result is known;
4. returns a safe provider reference and supports lookup by operation ID/reference;
5. documents off-main execution, timeout/cancellation, disable/drain, and failure behavior.

Publish it in a newly versioned, clean fixed-source commit and JAR with new owner-fixed hashes.
After that approval, implement the adapter inside Core so Vault/RedisEconomy types remain private.

### Reason

This is the smallest contract that lets the existing MariaDB state machine distinguish
`APPLIED`, `NOT_APPLIED`, and `UNKNOWN` without guessing or accessing RedisEconomy keys.

### Alternatives

1. Explicitly accept current Vault local-cache success semantics. This is not recommended: Core
   can commit after a response even if the later Redis write fails.
2. Approve the current RedisEconomy UUID overload only. This removes the `OfflinePlayer` problem
   but does not solve completion, atomicity, operation identity, or lookup.
3. Keep Waymark disabled. This preserves safety but leaves the V0.0.1 concrete-provider
   requirement blocked.
4. Add a separate adapter plugin. This does not create the missing provider guarantees and
   reintroduces the classloader/load-order problem recorded by ADR 0006.

### Gameplay impact

The proposed API enables safe balance/debit/refund acceptance. Until approved, Waymark
transactions remain unavailable and provider-independent Core services remain available.

### Threading impact

The proposed provider operation must run off the Paper main thread with bounded completion.
Callbacks may return through Core's existing main-thread bridge. No Bukkit player object crosses
the worker boundary.

### Transaction / crash impact

An acknowledged provider result can be persisted as applied; ambiguity remains `UNKNOWN` and is
resolved by the provider lookup. Existing operation IDs and MariaDB state transitions remain
unchanged.

### Security impact

No secret, URI, Redis key, raw exception, or raw provider response crosses the public or command
boundary. The adapter exposes only safe IDs/codes and JDK types.

### Compatibility impact

RedisEconomy, its source authority, and its JAR hash change. Vault remains available for ordinary
economy consumers. Core gains only a private compile/runtime integration with the approved API.
No Core migration or public SPI change is proposed.

### Rollback

Disable Waymark and restore the previously fixed RedisEconomy artifact/configuration. Core remains
fail closed and provider-independent services continue to load.

### Owner decision required

Approve or reject the proposed RedisEconomy-specific Waymark API and new fixed artifact. If
rejected, state whether current Vault local-cache success semantics are explicitly accepted despite
the documented crash and outage ambiguity, or keep the V0.0.1 provider requirement blocked.

## Consequences

Concrete implementation, provider tests, standalone debit/refund acceptance, candidate fixing, and
Draft PR publication are blocked. Provider-independent documentation and regression validation may
proceed. No source API/config/migration change, runtime operation, or candidate claim is permitted
until the decision is recorded.
