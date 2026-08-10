.PHONY: dev backend web test verify compose-up compose-down

dev:
	@echo "Run 'make backend' and 'make web' in separate terminals."

backend:
	cd backend && ./mvnw spring-boot:run

web:
	cd web && npm run dev

test:
	cd backend && ./mvnw test
	cd web && npm test

verify:
	cd backend && ./mvnw verify
	cd web && npm run lint
	cd web && npm test
	cd web && npm run build

compose-up:
	docker compose up --build

compose-down:
	docker compose down
