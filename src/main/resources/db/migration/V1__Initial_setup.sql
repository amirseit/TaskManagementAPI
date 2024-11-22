-- Create users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL -- Use VARCHAR instead of ENUM for role
);

-- Create tasks table
CREATE TABLE tasks (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL, -- Use VARCHAR instead of ENUM for status
    priority VARCHAR(50) NOT NULL, -- Use VARCHAR instead of ENUM for priority
    author_id INT NOT NULL REFERENCES users(id),
    assignee_id INT REFERENCES users(id)
);

-- Create comments table
CREATE TABLE comments (
    id SERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    task_id INT NOT NULL REFERENCES tasks(id),
    author_id INT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add index to improve performance when fetching comments by task
CREATE INDEX idx_comments_task_id ON comments(task_id);
