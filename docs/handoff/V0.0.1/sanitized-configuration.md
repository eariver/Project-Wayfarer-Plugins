# V0.0.1 Sanitized Configuration

- Config version: Pending lifecycle implementation
- Server ID: operator-defined, non-secret
- MariaDB URL/username/password: environment-variable references only
- Redis URI/credentials: environment-variable references only
- Migration locations: Core-only, pending final record
- Waymark provider: supported Vault/provider boundary, pending capability/thread verification
- Executor/audit/health/shutdown settings: pending validated sample

Never place secret values in this document, YAML samples, logs, audits, exceptions, or release
manifests. Record required environment variable names and validation behavior only.
