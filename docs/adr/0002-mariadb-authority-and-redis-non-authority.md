# ADR 0002: MariaDB Authority and Redis Non-authority

Status: Accepted

MariaDB is authoritative for durable custom gameplay data. Redis may provide cache, lock and
messaging assistance but cannot be the sole source of truth.
