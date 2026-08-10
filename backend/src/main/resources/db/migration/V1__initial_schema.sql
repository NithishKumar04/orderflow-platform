CREATE TABLE products (
    id UUID PRIMARY KEY,
    sku VARCHAR(64) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    category VARCHAR(80) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    inventory INTEGER NOT NULL,
    featured BOOLEAN NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_products_category ON products(category);

CREATE TABLE orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(40) NOT NULL UNIQUE,
    user_id VARCHAR(200) NOT NULL,
    status VARCHAR(40) NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_orders_user_created ON orders(user_id, created_at);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    sku VARCHAR(64) NOT NULL,
    product_name VARCHAR(160) NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    quantity INTEGER NOT NULL
);

CREATE TABLE order_timeline (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    status VARCHAR(40) NOT NULL,
    title VARCHAR(120) NOT NULL,
    description VARCHAR(500) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,
    user_id VARCHAR(200) NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    order_id UUID NOT NULL REFERENCES orders(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_idempotency_user_key UNIQUE(user_id, idempotency_key)
);

CREATE TABLE outbox_events (
    id UUID PRIMARY KEY,
    aggregate_id UUID NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    payload VARCHAR(2000) NOT NULL,
    status VARCHAR(30) NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    last_error VARCHAR(1000)
);

CREATE INDEX idx_outbox_pending ON outbox_events(status, next_attempt_at, created_at);

CREATE TABLE processed_events (
    event_id UUID PRIMARY KEY,
    processed_at TIMESTAMP WITH TIME ZONE NOT NULL
);
