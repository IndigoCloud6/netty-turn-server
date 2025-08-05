-- Insert default admin user
-- Note: In production, you should use proper password hashing (BCrypt)
-- This is a placeholder - the application should handle proper password encoding
INSERT OR IGNORE INTO users (username, password, enabled, created_at, updated_at) 
VALUES ('admin', '$2a$10$dXJ3SW6G7P9wd0FkZNKZr.e9E9GzVz4GxWvOwV5d.D3qWx1PzWC8K', 1, datetime('now'), datetime('now'));

-- Insert test user for development/testing
INSERT OR IGNORE INTO users (username, password, enabled, created_at, updated_at)
VALUES ('testuser', '$2a$10$dXJ3SW6G7P9wd0FkZNKZr.e9E9GzVz4GxWvOwV5d.D3qWx1PzWC8K', 1, datetime('now'), datetime('now'));

-- Note: The password hash above corresponds to 'admin123' using BCrypt
-- In a real application, these would be generated during user creation