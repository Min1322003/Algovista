CREATE TABLE lessons (
    id          VARCHAR(50) PRIMARY KEY,
    title       VARCHAR(200) NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    position    INT NOT NULL,
    published   BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE activities (
    id              BIGSERIAL PRIMARY KEY,
    lesson_id       VARCHAR(50) NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    position        INT NOT NULL,                -- 0-based order inside the lesson
    type            VARCHAR(30) NOT NULL
        CHECK (type IN ('EXPLANATION', 'MULTIPLE_CHOICE', 'EDGE_SELECTION', 'NODE_SELECTION', 'CODE_COMPLETION')),
    title           VARCHAR(200) NOT NULL,
    instruction     TEXT NOT NULL,
    content         JSONB NOT NULL DEFAULT '{}'::jsonb,  -- what the client renders (graph, options, tokens...)
    answer          TEXT[],                               -- correct ids, NULL for EXPLANATION
    praise_message  TEXT NOT NULL DEFAULT '',
    clue_message    TEXT NOT NULL DEFAULT '',
    explanation     TEXT NOT NULL DEFAULT '',
    UNIQUE (lesson_id, position)
);

CREATE TABLE user_progress (
    id               BIGSERIAL PRIMARY KEY,
    user_id          VARCHAR(64) NOT NULL,
    lesson_id        VARCHAR(50) NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    current_position INT NOT NULL DEFAULT 0,
    status           VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS'
        CHECK (status IN ('IN_PROGRESS', 'COMPLETED')),
    started_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (user_id, lesson_id)
);
