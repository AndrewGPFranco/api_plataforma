ALTER TABLE projects
    ADD COLUMN localhost_url VARCHAR(100) NOT NULL DEFAULT 'http://localhost:8000';
