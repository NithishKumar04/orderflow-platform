# Testing strategy

## Automated checks

| Layer | Coverage |
| --- | --- |
| Domain | Allowed and rejected order-state transitions |
| Reliability | Retry-budget to dead-letter behavior |
| API integration | Login, public catalog, protected orders, correlation ID |
| Workflow integration | Idempotent checkout, successful confirmation |
| Compensation | Declined payment restores inventory |
| Database compatibility | Flyway migration and catalog seed on PostgreSQL via Testcontainers |
| Frontend unit | Cart quantity, inventory cap, line removal |
| Build | TypeScript compilation and production Vite bundle |
| Supply chain | NPM audit and Dependabot updates |
| Delivery | Container builds and a full-topology asynchronous checkout |

Run everything:

```bash
make verify
```

## Manual browser scenarios

The release checklist covers:

1. Sign in with the prepared account.
2. Search and category filtering.
3. Add, increment, decrement, and remove cart lines.
4. Approved checkout reaches `CONFIRMED`.
5. Declined checkout reaches `PAYMENT_FAILED` and inventory is restored.
6. Confirmed order cancellation releases inventory.
7. Desktop and narrow mobile layouts have no horizontal overflow.
8. Keyboard focus is visible for interactive controls.

## Full-topology testing

Docker is intentionally not required for the default test suite. When Docker is
available, the PostgreSQL compatibility test runs automatically. CI also builds
the images, starts PostgreSQL, Redis, Redpanda, the API, and the storefront, then
waits for an event-driven order to reach `CONFIRMED`.

Run the same topology locally with:

```bash
docker compose up --build
```

Before publishing performance claims, run the load scenario in
`ops/load/orderflow.js` against that topology and record the exact machine,
duration, concurrency, data shape, and percentile results.
