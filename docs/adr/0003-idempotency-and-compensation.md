# ADR 0003: Make delivery idempotent and failure compensating

- Status: Accepted
- Date: 2026-08-10

## Context

Clients retry timed-out checkout requests, and Kafka can redeliver records.
Payment can also fail after inventory has been reserved.

## Decision

- Require an `Idempotency-Key` on checkout and scope it to the authenticated
  user with a database uniqueness constraint.
- Carry the stable outbox event UUID in the broker payload and record it in
  `processed_events` in the workflow transaction.
- Model order transitions explicitly.
- Release reserved inventory when payment is declined.

## Consequences

- Sequential duplicate HTTP requests return the original order.
- Replayed broker events are no-ops.
- Compensation behavior is visible in the order timeline.
- External payment integration will require a longer-running saga rather than
  the current single database transaction.
