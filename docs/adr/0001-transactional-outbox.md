# ADR 0001: Use a transactional outbox

- Status: Accepted
- Date: 2026-08-10

## Context

Checkout must persist an order and initiate asynchronous processing. Writing
the database and publishing to a broker are two independent operations. A
crash between them can produce either an event without an order or an order
that is never processed.

## Decision

Persist the order and an outbox event in one PostgreSQL transaction. A
scheduled relay publishes committed events and marks them delivered only
after transport acknowledgement. Publication uses a bounded retry budget and
dead-letter state.

## Consequences

- Database commit is the single acceptance point for checkout.
- Publication is at least once, so consumers must be idempotent.
- Relay delay adds a small amount of processing latency.
- The outbox table needs retention and dead-letter operations in production.
