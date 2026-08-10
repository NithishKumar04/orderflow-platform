# ADR 0002: Start with a modular monolith

- Status: Accepted
- Date: 2026-08-10

## Context

Catalog, orders, inventory, and event processing could be separate services.
For this portfolio application, that topology would multiply deployment and
failure modes before independent scaling or ownership exists.

## Decision

Keep one deployable Spring Boot application with package-level domain
boundaries and transport interfaces. Use PostgreSQL schemas and service
contracts that do not require shared mutable objects across modules.

## Consequences

- Local execution, integration testing, and transactional behavior stay
  simple and observable.
- The project demonstrates service boundaries without presenting process
  count as architecture quality.
- A boundary can be extracted when throughput, ownership, or release cadence
  creates a measurable reason.
