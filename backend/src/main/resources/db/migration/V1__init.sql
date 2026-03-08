-- Student Approval System - Database Schema
-- PostgreSQL Migration Script

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    avatar_url VARCHAR(255),
    department VARCHAR(100),
    student_id VARCHAR(50),
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles enumeration
-- STUDENT - Student who can submit applications
-- APPROVER - Approver who can review applications
-- ADMIN - Administrator with full access
-- PRODUCT_AGENT - Product Manager Agent
-- ARCHITECT_AGENT - Architecture Agent
-- BACKEND_AGENT - Backend Developer Agent
-- FRONTEND_AGENT - Frontend Developer Agent
-- TEST_AGENT - Testing Agent
-- SECURITY_AGENT - Security Agent

-- Applications table
CREATE TABLE applications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    applicant_id UUID NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(20) DEFAULT 'NORMAL',
    attachment_urls TEXT[],
    metadata JSONB DEFAULT '{}',
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    withdrawn BOOLEAN DEFAULT false,
    withdrawn_at TIMESTAMP,
    completed_at TIMESTAMP
);

-- Approvals table
CREATE TABLE approvals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    approver_id UUID NOT NULL REFERENCES users(id),
    agent_role VARCHAR(50),
    action VARCHAR(20) NOT NULL,
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(application_id, approver_id)
);

-- Workflow definitions table
CREATE TABLE workflows (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    app_type VARCHAR(50) NOT NULL,
    definition JSONB NOT NULL,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Workflow instances table
CREATE TABLE workflow_instances (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    workflow_id UUID NOT NULL REFERENCES workflows(id),
    current_node_id VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'RUNNING',
    context JSONB DEFAULT '{}',
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

-- Workflow nodes table
CREATE TABLE workflow_nodes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    workflow_id UUID NOT NULL REFERENCES workflows(id) ON DELETE CASCADE,
    node_id VARCHAR(50) NOT NULL,
    node_name VARCHAR(100) NOT NULL,
    node_type VARCHAR(20) NOT NULL,
    config JSONB DEFAULT '{}',
    position INTEGER DEFAULT 0
);

-- Agent actions log table
CREATE TABLE agent_actions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    application_id UUID NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    agent_role VARCHAR(50) NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    description TEXT,
    result JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Refresh tokens table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_applications_applicant ON applications(applicant_id);
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_approvals_application ON approvals(application_id);
CREATE INDEX idx_approvals_approver ON approvals(approver_id);
CREATE INDEX idx_workflow_instances_application ON workflow_instances(application_id);
CREATE INDEX idx_agent_actions_application ON agent_actions(application_id);
CREATE INDEX idx_refresh_tokens_user ON refresh_tokens(user_id);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password_hash, display_name, role, department)
VALUES ('admin', 'admin@example.com', '$2a$10$rOq7lFqQh5MqjdN9x.7bWuqR8kqJvJzKcN8x.7bWuqR8kqJvJzKc', 'System Administrator', 'ADMIN', 'IT');

-- Insert default approver user (password: approver123)
INSERT INTO users (username, email, password_hash, display_name, role, department)
VALUES ('approver', 'approver@example.com', '$2a$10$rOq7lFqQh5MqjdN9x.7bWuqR8kqJvJzKcN8x.7bWuqR8kqJvJzKc', 'Default Approver', 'APPROVER', 'Academic Affairs');

-- Insert default workflow for student approvals
INSERT INTO workflows (name, description, app_type, definition) VALUES (
    'Standard Student Approval',
    'Standard approval workflow with multiple agent review',
    'GENERAL',
    '{
        "nodes": [
            {"id": "submit", "name": "Submit", "type": "START", "agents": []},
            {"id": "product_review", "name": "Product Review", "type": "AGENT_REVIEW", "agents": ["PRODUCT_AGENT"]},
            {"id": "security_review", "name": "Security Review", "type": "AGENT_REVIEW", "agents": ["SECURITY_AGENT"]},
            {"id": "final_approval", "name": "Final Approval", "type": "MANUAL_APPROVAL", "agents": []},
            {"id": "completed", "name": "Completed", "type": "END", "agents": []}
        ],
        "transitions": [
            {"from": "submit", "to": "product_review", "condition": "submitted"},
            {"from": "product_review", "to": "security_review", "condition": "approved"},
            {"from": "security_review", "to": "final_approval", "condition": "approved"},
            {"from": "final_approval", "to": "completed", "condition": "approved"},
            {"from": "*", "to": "submit", "condition": "rejected"}
        ]
    }'
);
