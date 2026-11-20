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

create table if not exists ticket_attachment (
    id uuid primary key,
    ticket_id uuid not null references change_ticket(id) on delete cascade,
    filename varchar(180) not null,
    media_type varchar(32) not null,
    size_bytes bigint not null,
    content blob not null
);

create index if not exists idx_ticket_attachment_ticket on ticket_attachment(ticket_id);

create table if not exists users (
    username varchar_ignorecase(50) primary key,
    password varchar(100) not null,
    enabled boolean not null
);

create table if not exists authorities (
    username varchar_ignorecase(50) not null references users(username),
    authority varchar(50) not null,
    primary key (username, authority)
);

alter table change_ticket add column if not exists owner varchar(50) default 'biz' not null;
create index if not exists idx_change_ticket_owner on change_ticket(owner, created_at);
