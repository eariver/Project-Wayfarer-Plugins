# Module Boundaries

- Core publishes stable service interfaces through Bukkit ServicesManager.
- Main and Frontier hard-depend on Core.
- Main and Frontier never depend on each other.
- API contains contracts only.
- Common contains no server-specific gameplay.
- Testkit is test-only.
- LeafGrapple integration is isolated.
- EM–MVI adapter is absent until formally required.
