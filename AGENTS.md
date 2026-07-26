# Codex Instructions

1. Read `docs/specifications/Project_Wayfarer_Plugin_Work_Order_and_Design_Specification_v0.0.1.md` before editing.
2. Preserve module, threading, persistence, identity, transaction and fail-closed boundaries.
3. Do not create `Wayfarer_Frontier_EliteMobsMVI` unless a Project-side Decision Report says `ADAPTER_REQUIRED`.
4. Never commit JARs, secrets, runtime configs, worlds, databases, logs or caches.
5. Do not perform Project Wayfarer Runtime changes from this repository.
6. Run the narrowest relevant tests, then `check`.
7. Applied Flyway migrations are immutable.
8. Main and Frontier may depend on Core; Core must not depend on Main or Frontier.
9. Normal inventories are owned by the Minecraft backend/MVI, never by this repository's MariaDB schema.
10. Report changed files, tests, limitations and any authority conflict.
