-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT 1,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login TIMESTAMP
);

-- Create index for username lookup
CREATE UNIQUE INDEX IF NOT EXISTS idx_username ON users (username);

-- Create turn_sessions table  
CREATE TABLE IF NOT EXISTS turn_sessions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    session_id VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL,
    client_address VARCHAR(45) NOT NULL,
    client_port INTEGER NOT NULL,
    realm VARCHAR(255),
    nonce VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_activity TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    bytes_sent INTEGER NOT NULL DEFAULT 0,
    bytes_received INTEGER NOT NULL DEFAULT 0,
    packets_sent INTEGER NOT NULL DEFAULT 0,
    packets_received INTEGER NOT NULL DEFAULT 0
);

-- Create indexes for turn_sessions
CREATE UNIQUE INDEX IF NOT EXISTS idx_session_id ON turn_sessions (session_id);
CREATE INDEX IF NOT EXISTS idx_turn_username ON turn_sessions (username);
CREATE INDEX IF NOT EXISTS idx_client_address ON turn_sessions (client_address);

-- Create allocations table
CREATE TABLE IF NOT EXISTS allocations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    allocation_id VARCHAR(255) NOT NULL UNIQUE,
    session_id VARCHAR(255) NOT NULL,
    username VARCHAR(50) NOT NULL,
    relay_address VARCHAR(45) NOT NULL,
    relay_port INTEGER NOT NULL,
    client_address VARCHAR(45) NOT NULL,
    client_port INTEGER NOT NULL,
    transport_protocol VARCHAR(10) NOT NULL,
    lifetime_seconds INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    last_refresh TIMESTAMP,
    bytes_relayed INTEGER NOT NULL DEFAULT 0,
    packets_relayed INTEGER NOT NULL DEFAULT 0,
    permissions_count INTEGER NOT NULL DEFAULT 0
);

-- Create indexes for allocations
CREATE UNIQUE INDEX IF NOT EXISTS idx_allocation_id ON allocations (allocation_id);
CREATE INDEX IF NOT EXISTS idx_allocation_session_id ON allocations (session_id);
CREATE INDEX IF NOT EXISTS idx_relay_address ON allocations (relay_address);
CREATE INDEX IF NOT EXISTS idx_allocation_username ON allocations (username);