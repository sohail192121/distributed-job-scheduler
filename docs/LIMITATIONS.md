# Prototype boundaries

Workers require unique per-attempt identities to avoid the same worker ID reusing a stale lease. A production implementation must use monotonic fencing tokens and database compare-and-swap. This is not a distributed scheduler yet.
