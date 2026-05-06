create table if not exists invoice
(
    enabled        bit            not null default 1,
    id             bigint auto_increment primary key,
    created_at     datetime(6)    null,
    updated_at     datetime(6)    null,
    created_by     varchar(255)   null,
    updated_by     varchar(255)   null,
    invoice_number varchar(50)    not null,
    issue_date     date           not null,
    due_date       date           null,
    client_ruc     varchar(20)    not null,
    client_name    varchar(255)   not null,
    currency       varchar(10)    not null,
    subtotal       decimal(12, 2) not null,
    igv            decimal(12, 2) not null,
    total          decimal(12, 2) not null,
    status         enum ('PENDING','PAID') not null default 'PENDING',
    client_id      bigint         null,
    paid_at        datetime(6)    null,
    constraint uk_invoice_number unique (invoice_number),
    constraint fk_invoice_client foreign key (client_id) references client (id)
);

create table if not exists payment_evidence
(
    enabled      bit          not null default 1,
    id           bigint auto_increment primary key,
    created_at   datetime(6)  null,
    updated_at   datetime(6)  null,
    created_by   varchar(255) null,
    updated_by   varchar(255) null,
    invoice_id   bigint       not null,
    file_url     varchar(500) not null,
    file_name    varchar(255) not null,
    uploaded_by  varchar(255) not null,
    constraint fk_payment_evidence_invoice foreign key (invoice_id) references invoice (id)
);
