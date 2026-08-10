# API examples

The interactive OpenAPI UI is available at `http://localhost:8080/docs`.

## Authenticate

```bash
curl -s http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@orderflow.dev","password":"Demo123!"}'
```

Store the returned token:

```bash
TOKEN="<token>"
```

## Browse products

```bash
curl -s http://localhost:8080/api/products
```

## Create an order

Replace the product UUID with one from the catalog response.

```bash
curl -i http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Idempotency-Key: demo-checkout-001" \
  -H 'Content-Type: application/json' \
  -d '{
    "items": [{"productId": "<product-uuid>", "quantity": 1}],
    "paymentMethod": "DEMO_APPROVED"
  }'
```

Repeat the request with the same user and key to receive the same order.
Set `paymentMethod` to `DEMO_DECLINED` to exercise compensation.

## Inspect orders

```bash
curl -s http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"
```

Every response includes `X-Correlation-Id`. Supplying a valid value in that
request header preserves it through the API response and logs.
