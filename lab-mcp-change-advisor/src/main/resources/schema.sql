create table if not exists change_ticket (
    id uuid primary key,
    created_at timestamp with time zone not null,
    screen_path varchar(100) not null,
    title varchar(120) not null,
    request_text varchar(3000) not null,
    state varchar(20) not null default 'OPEN',
    assignee varchar(60),
    reply varchar(3000),
    analysis_state varchar(20) not null default 'QUEUED',
    analysis_json clob
);
