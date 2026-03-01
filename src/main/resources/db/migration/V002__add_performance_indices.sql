-- Performance indices for optimizing dashboard and query performance
-- Created as part of FASE 4.2: Performance Optimization

-- Orders table indices
CREATE INDEX IF NOT EXISTS idx_order_status ON orders(order_status);
CREATE INDEX IF NOT EXISTS idx_order_pickup_date ON orders(pickup_date);
CREATE INDEX IF NOT EXISTS idx_order_client_id ON orders(client_id);
CREATE INDEX IF NOT EXISTS idx_order_truck_id ON orders(truck_id);
CREATE INDEX IF NOT EXISTS idx_order_warehouse_id ON orders(warehouse_id);

-- Composite indices for common query patterns
CREATE INDEX IF NOT EXISTS idx_order_status_pickup ON orders(order_status, pickup_date);
CREATE INDEX IF NOT EXISTS idx_order_client_status ON orders(client_id, order_status);

-- Truck table indices
CREATE INDEX IF NOT EXISTS idx_truck_driver_id ON truck(driver_id);
CREATE INDEX IF NOT EXISTS idx_truck_status ON truck(status);

-- User table indices
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);

-- Client table indices
CREATE INDEX IF NOT EXISTS idx_client_user_id ON client(user_id);
CREATE INDEX IF NOT EXISTS idx_client_enabled ON client(enabled);

-- Driver table indices
CREATE INDEX IF NOT EXISTS idx_driver_user_id ON driver(user_id);
CREATE INDEX IF NOT EXISTS idx_driver_dni ON driver(dni);
CREATE INDEX IF NOT EXISTS idx_driver_enabled ON driver(enabled);

-- Order status tracking
CREATE INDEX IF NOT EXISTS idx_order_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_updated_at ON orders(updated_at);
