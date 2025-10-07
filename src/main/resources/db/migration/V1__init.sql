-- Simple initial schema for Flight Advisor (minimal)
-- Adjust as needed; production DB should use a proper migration set

CREATE TABLE IF NOT EXISTS "user" (
    id UUID PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS role (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

-- Add other domain tables as needed
