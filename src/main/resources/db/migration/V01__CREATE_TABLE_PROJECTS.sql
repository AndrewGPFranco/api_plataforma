CREATE TABLE projects
(
    id               UUID        NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    title            TEXT        NOT NULL,
    description      TEXT,
    started_at       DATE        NOT NULL,
    repo_front_url   TEXT,
    repo_back_url    TEXT,
    deploy_front_url TEXT,
    deploy_back_url  TEXT,
    has_deploy       BOOLEAN     NOT NULL GENERATED ALWAYS AS (
        deploy_front_url IS NOT NULL OR deploy_back_url IS NOT NULL
        ) STORED,
    created_at       TIMESTAMPTZ NOT NULL             DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL             DEFAULT now()
);
