-- Users table
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    username VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    job VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    current_project_id UUID
);

-- Projects table
CREATE TABLE IF NOT EXISTS projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP WITH TIME ZONE,
    project_owner VARCHAR(255),
    address TEXT
);

-- User-Project relationships
CREATE TABLE IF NOT EXISTS user_project (
    user_id UUID REFERENCES users(id),
    project_id UUID REFERENCES projects(id),
    role VARCHAR(50) NOT NULL, -- 'ADMIN' or 'MEMBER'
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, project_id)
);

-- Drop tables if they exist
DROP TABLE IF EXISTS rfi_reply_attachments;
DROP TABLE IF EXISTS rfi_replies;
DROP TABLE IF EXISTS rfi_attachments;
DROP TABLE IF EXISTS rfis;

-- Create RFI table
CREATE TABLE rfis (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    due_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    project_id UUID NOT NULL REFERENCES projects(id),
    created_by UUID NOT NULL REFERENCES users(id),
    assigned_type VARCHAR(10) CHECK (assigned_type IN ('USER', 'GROUP')),
    assigned_user_id UUID REFERENCES users(id),
    assigned_group_id UUID REFERENCES groups(id),
    CONSTRAINT check_assignment CHECK (
        (assigned_type = 'USER' AND assigned_user_id IS NOT NULL AND assigned_group_id IS NULL) OR
        (assigned_type = 'GROUP' AND assigned_group_id IS NOT NULL AND assigned_user_id IS NULL) OR
        (assigned_type IS NULL AND assigned_user_id IS NULL AND assigned_group_id IS NULL)
    )
);

-- Create RFI Attachments table
CREATE TABLE rfi_attachments (
    id UUID PRIMARY KEY,
    rfi_id UUID NOT NULL REFERENCES rfis(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

-- Create RFI Replies table
CREATE TABLE rfi_replies (
    id UUID PRIMARY KEY,
    rfi_id UUID NOT NULL REFERENCES rfis(id) ON DELETE CASCADE,
    message VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    created_by UUID NOT NULL REFERENCES users(id)
);

-- Create RFI Reply Attachments table
CREATE TABLE rfi_reply_attachments (
    id UUID PRIMARY KEY,
    reply_id UUID NOT NULL REFERENCES rfi_replies(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_url TEXT NOT NULL,
    file_size BIGINT NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

-- Drawings
CREATE TABLE IF NOT EXISTS drawings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    project_id UUID REFERENCES projects(id),
    name VARCHAR(255) NOT NULL,
    file_id VARCHAR(255) NOT NULL,
    web_view_link TEXT,
    preview_link TEXT,
    thumbnail_link TEXT,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    size BIGINT
);

-- Groups table
CREATE TABLE IF NOT EXISTS groups (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    project_id UUID NOT NULL REFERENCES projects(id),
    created_by UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Group members table
CREATE TABLE IF NOT EXISTS group_members (
    group_id UUID REFERENCES groups(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id),
    PRIMARY KEY (group_id, user_id)
);

-- Create RFI Group Member Assignments table
CREATE TABLE rfi_group_assignments (
    id UUID PRIMARY KEY,
    rfi_id UUID NOT NULL REFERENCES rfis(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES users(id),
    group_id UUID NOT NULL REFERENCES groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(rfi_id, user_id)
);

-- User indices
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);

-- Project indices
CREATE INDEX IF NOT EXISTS idx_project_code ON projects(code);
CREATE INDEX IF NOT EXISTS idx_project_created_at ON projects(created_at);
CREATE INDEX IF NOT EXISTS idx_project_due_date ON projects(due_date);

-- User project indices
CREATE INDEX IF NOT EXISTS idx_user_project_user ON user_project(user_id);
CREATE INDEX IF NOT EXISTS idx_user_project_project ON user_project(project_id);

-- RFI indices
CREATE INDEX idx_rfis_project_id ON rfis(project_id);
CREATE INDEX idx_rfis_created_by ON rfis(created_by);
CREATE INDEX idx_rfis_assigned_user ON rfis(assigned_user_id) WHERE assigned_type = 'USER';
CREATE INDEX idx_rfis_assigned_group ON rfis(assigned_group_id) WHERE assigned_type = 'GROUP';
CREATE INDEX idx_rfis_due_date ON rfis(due_date);

-- RFI attachment indices
CREATE INDEX idx_rfi_attachments_rfi_id ON rfi_attachments(rfi_id);

-- RFI reply indices
CREATE INDEX idx_rfi_replies_rfi_id ON rfi_replies(rfi_id);
CREATE INDEX idx_rfi_replies_created_by ON rfi_replies(created_by);

-- RFI reply attachment indices
CREATE INDEX idx_rfi_reply_attachments_reply_id ON rfi_reply_attachments(reply_id);

-- Group indices
CREATE INDEX IF NOT EXISTS idx_group_project ON groups(project_id);
CREATE INDEX IF NOT EXISTS idx_group_created_by ON groups(created_by);
CREATE INDEX IF NOT EXISTS idx_group_members_group ON group_members(group_id);
CREATE INDEX IF NOT EXISTS idx_group_members_user ON group_members(user_id);

-- RFI group assignment indices
CREATE INDEX idx_rfi_group_assignments_rfi ON rfi_group_assignments(rfi_id);
CREATE INDEX idx_rfi_group_assignments_user ON rfi_group_assignments(user_id);
CREATE INDEX idx_rfi_group_assignments_group ON rfi_group_assignments(group_id); 