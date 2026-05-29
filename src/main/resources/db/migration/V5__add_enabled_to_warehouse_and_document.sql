alter table warehouse
    add column enabled bit not null default 1;

alter table document
    add column enabled bit not null default 1;
