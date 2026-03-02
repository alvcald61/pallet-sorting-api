create table if not exists document
(
    required      bit          null,
    document_id   bigint auto_increment
    primary key,
    document_name varchar(255) null,
    storage_path  varchar(255) null
    );

create table if not exists notifications
(
    enabled             bit                                                                                                                                                                    not null,
    is_read             bit                                                                                                                                                                    not null,
    created_at          datetime(6)                                                                                                                                                            null,
    id                  bigint auto_increment
    primary key,
    read_at             datetime(6)                                                                                                                                                            null,
    updated_at          datetime(6)                                                                                                                                                            null,
    related_entity_type varchar(50)                                                                                                                                                            null,
    created_by          varchar(255)                                                                                                                                                           null,
    message             text                                                                                                                                                                   not null,
    related_entity_id   varchar(255)                                                                                                                                                           null,
    title               varchar(255)                                                                                                                                                           not null,
    updated_by          varchar(255)                                                                                                                                                           null,
    user_id             varchar(255)                                                                                                                                                           not null,
    metadata            json                                                                                                                                                                   null,
    type                enum ('ORDER_APPROVED', 'ORDER_CREATED', 'ORDER_DENIED', 'ORDER_DOCUMENT_PENDING', 'ORDER_STATUS_CHANGED', 'SYSTEM_ALERT', 'TRANSPORT_DELIVERED', 'TRANSPORT_STARTED') not null
    );

create index idx_user_read_created
    on notifications (user_id, is_read, created_at);

create table if not exists pallet
(
    amount     int          null,
    enabled    bit          not null,
    height     double       null,
    length     double       null,
    width      double       null,
    created_at datetime(6)  null,
    id         bigint auto_increment
    primary key,
    updated_at datetime(6)  null,
    created_by varchar(255) null,
    type       varchar(255) null,
    updated_by varchar(255) null
    );

create table if not exists price_condition
(
    enabled            bit          not null,
    max_volume         double       null,
    max_weight         double       null,
    min_volume         double       null,
    min_weight         double       null,
    created_at         datetime(6)  null,
    price_condition_id bigint auto_increment
    primary key,
    updated_at         datetime(6)  null,
    created_by         varchar(255) null,
    currency           varchar(255) null,
    updated_by         varchar(255) null
    );

create table if not exists roles
(
    enabled     bit          not null,
    created_at  datetime(6)  null,
    id          bigint auto_increment
    primary key,
    updated_at  datetime(6)  null,
    name        varchar(50)  not null,
    created_by  varchar(255) null,
    permissions varchar(255) null,
    updated_by  varchar(255) null,
    constraint uk_roles_name
    unique (name)
    );

create table if not exists users
(
    enabled    bit          not null,
    created_at datetime(6)  null,
    id         bigint auto_increment
    primary key,
    updated_at datetime(6)  null,
    first_name varchar(150) not null,
    last_name  varchar(150) not null,
    email      varchar(190) not null,
    created_by varchar(255) null,
    password   varchar(255) not null,
    updated_by varchar(255) null
    );

create table if not exists client
(
    enabled       bit                       not null,
    trust         bit                       not null,
    created_at    datetime(6)               null,
    id            bigint auto_increment
    primary key,
    updated_at    datetime(6)               null,
    user_id       bigint                    null,
    address       varchar(255)              null,
    business_name varchar(255)              null,
    created_by    varchar(255)              null,
    phone         varchar(255)              null,
    ruc           varchar(255)              null,
    updated_by    varchar(255)              null,
    client_type   enum ('BASIC', 'PREMIUM') null,
    constraint UK1ixfyfepst9sjbo9op1v65fg0
    unique (user_id),
    constraint FKbxisi412kym1baqfr00rxd8yo
    foreign key (user_id) references users (id)
    );

create table if not exists dispatcher
(
    enabled    bit          not null,
    client_id  bigint       null,
    created_at datetime(6)  null,
    id         bigint auto_increment
    primary key,
    updated_at datetime(6)  null,
    created_by varchar(255) null,
    first_name varchar(255) null,
    last_name  varchar(255) null,
    phone      varchar(255) null,
    updated_by varchar(255) null,
    constraint FK6up19otxo1xiyon6q38vr7j43
    foreign key (client_id) references client (id)
    );

create table if not exists driver
(
    enabled        bit          not null,
    created_at     datetime(6)  null,
    driver_id      bigint auto_increment
    primary key,
    truck_id       bigint       null,
    updated_at     datetime(6)  null,
    user_id        bigint       null,
    created_by     varchar(255) null,
    dni            varchar(255) null,
    driver_licence varchar(255) null,
    phone          varchar(255) null,
    updated_by     varchar(255) null,
    constraint UK1q06vc1wajnp5j574ixw7driq
    unique (truck_id),
    constraint UKg3oju5uudgl1cct873m6f2bfy
    unique (user_id),
    constraint FKcmqtox7705hqk8hahof0ikmba
    foreign key (user_id) references users (id)
    );

create table if not exists order_templates
(
    client_id   bigint       not null,
    created_at  datetime(6)  not null,
    id          bigint auto_increment
    primary key,
    last_used   datetime(6)  null,
    order_type  varchar(50)  not null,
    name        varchar(100) not null,
    description varchar(500) null,
    items_json  text         null,
    route_json  text         null,
    constraint FKs0gn452gtlaa9x6sptd5iq1nx
    foreign key (client_id) references client (id)
    );

create table if not exists truck
(
    area             double                                                                        null,
    enabled          bit                                                                           not null,
    height           double                                                                        null,
    length           double                                                                        null,
    multiplayer      double                                                                        null,
    volume           double                                                                        null,
    weight           double                                                                        null,
    width            double                                                                        null,
    created_at       datetime(6)                                                                   null,
    driver_driver_id bigint                                                                        null,
    id               bigint auto_increment
    primary key,
    updated_at       datetime(6)                                                                   null,
    created_by       varchar(255)                                                                  null,
    license_plate    varchar(255)                                                                  null,
    truck_type       varchar(255)                                                                  null,
    type             varchar(255)                                                                  null,
    updated_by       varchar(255)                                                                  null,
    status           enum ('ASSIGNED', 'AVAILABLE', 'IN_TRANSIT', 'MAINTENANCE', 'OUT_OF_SERVICE') null,
    constraint UKobn60ixofwvj6tqbl018m3qh8
    unique (driver_driver_id),
    constraint FK5pppm0k7ihnde14dfcy97p8jr
    foreign key (driver_driver_id) references driver (driver_id)
    );

alter table driver
    add constraint FKcyntrio0y6yc4ms2jqfd1qvo7
        foreign key (truck_id) references truck (id);

create table if not exists users_roles
(
    role_id bigint not null,
    user_id bigint not null,
    primary key (role_id, user_id),
    constraint fk_users_roles_role
    foreign key (role_id) references roles (id),
    constraint fk_users_roles_user
    foreign key (user_id) references users (id)
    );

create table if not exists warehouse
(
    warehouse_id  bigint auto_increment
    primary key,
    address       varchar(255) null,
    city          varchar(255) null,
    district      varchar(255) null,
    location_link varchar(255) null,
    name          varchar(255) null,
    phone         varchar(255) null,
    state         varchar(255) null
    );

create table if not exists warehouse_documents
(
    document_id  bigint not null,
    warehouse_id bigint not null,
    primary key (document_id, warehouse_id),
    constraint FKok0vesou8t9cvuuu8hctt0433
    foreign key (warehouse_id) references warehouse (warehouse_id),
    constraint FKoyemxo6r8qxjngergrbikkwy7
    foreign key (document_id) references document (document_id)
    );

create table if not exists zone
(
    enabled           bit          not null,
    created_at        datetime(6)  null,
    id                bigint auto_increment
    primary key,
    max_delivery_time bigint       null,
    updated_at        datetime(6)  null,
    city              varchar(255) null,
    created_by        varchar(255) null,
    district          varchar(255) null,
    name              varchar(255) null,
    state             varchar(255) null,
    updated_by        varchar(255) null,
    zone_name         varchar(255) null
    );

create table if not exists price
(
    enabled                            bit            not null,
    price                              decimal(38, 2) null,
    client_id                          bigint         null,
    created_at                         datetime(6)    null,
    price_condition_price_condition_id bigint         null,
    price_zone_id                      bigint auto_increment
    primary key,
    updated_at                         datetime(6)    null,
    zone_id                            bigint         null,
    created_by                         varchar(255)   null,
    updated_by                         varchar(255)   null,
    constraint FK4q8agp9s1aausef6rm7dhjhj4
    foreign key (zone_id) references zone (id),
    constraint FK7cngcl9in68y65f6ts1op4th9
    foreign key (price_condition_price_condition_id) references price_condition (price_condition_id),
    constraint FKkfu7js91u530pgxehuw16bm2a
    foreign key (client_id) references client (id)
    );

create table if not exists transport_order
(
    amount                  decimal(38, 2)                                                                                                                                                                                          null,
    enabled                 bit                                                                                                                                                                                                     not null,
    is_document_pending     bit                                                                                                                                                                                                     not null,
    total_volume            decimal(38, 2)                                                                                                                                                                                          null,
    total_weight            decimal(38, 2)                                                                                                                                                                                          null,
    client_id               bigint                                                                                                                                                                                                  not null,
    created_at              datetime(6)                                                                                                                                                                                             null,
    dispatcher_id           bigint                                                                                                                                                                                                  null,
    id                      bigint auto_increment
    primary key,
    pickup_date             datetime(6)                                                                                                                                                                                             null,
    projected_delivery_date datetime(6)                                                                                                                                                                                             null,
    real_delivery_date      datetime(6)                                                                                                                                                                                             null,
    truck_id                bigint                                                                                                                                                                                                  null,
    updated_at              datetime(6)                                                                                                                                                                                             null,
    warehouse_id            bigint                                                                                                                                                                                                  null,
    zone_id                 bigint                                                                                                                                                                                                  not null,
    address_link            varchar(255)                                                                                                                                                                                            null,
    created_by              varchar(255)                                                                                                                                                                                            null,
    from_address            varchar(255)                                                                                                                                                                                            null,
    gps_link                varchar(255)                                                                                                                                                                                            null,
    solution                text                                                                                                                                                                                                    null,
    solution_image_url      varchar(255)                                                                                                                                                                                            null,
    sunat_document_path     varchar(255)                                                                                                                                                                                            null,
    to_address              varchar(255)                                                                                                                                                                                            null,
    updated_by              varchar(255)                                                                                                                                                                                            null,
    order_status            enum ('APPROVED', 'DELIVERED', 'DENIED', 'DOCUMENT_PENDING', 'IN_PROGRESS', 'PRE_APPROVED', 'REVIEW')                                                                                                   null,
    order_type              enum ('BULK', 'THREE_DIMENSIONAL', 'TWO_DIMENSIONAL')                                                                                                                                                   null,
    transport_status        enum ('ARRIVED_AT_DESTINATION', 'ARRIVED_AT_WAREHOUSE', 'DELIVERED', 'EN_ROUTE_TO_DESTINATION', 'EN_ROUTE_TO_WAREHOUSE', 'LOADING', 'LOADING_COMPLETED', 'PENDING', 'UNLOADING', 'UNLOADING_COMPLETED') null,
    constraint FK3p3n6jw17860oic1mihxbvnnf
    foreign key (zone_id) references zone (id),
    constraint FK4m4f8xygnqrg1ctanrak8v8ud
    foreign key (warehouse_id) references warehouse (warehouse_id),
    constraint FKckucdxue1aun0vjdjrmj6t8kd
    foreign key (truck_id) references truck (id),
    constraint FKs31badw5ungxtat29i3vyue9v
    foreign key (client_id) references client (id),
    constraint FKt5urqc5qkwhvsjj6xjh9gehdj
    foreign key (dispatcher_id) references dispatcher (id)
    );

create table if not exists bulk
(
    enabled    bit          not null,
    height     double       not null,
    volume     double       not null,
    weight     double       not null,
    created_at datetime(6)  null,
    id         bigint auto_increment
    primary key,
    order_id   bigint       null,
    quantity   bigint       not null,
    updated_at datetime(6)  null,
    created_by varchar(255) null,
    updated_by varchar(255) null,
    constraint FKt3r8l1h6593fwspxlwbe77dvt
    foreign key (order_id) references transport_order (id)
    );

create table if not exists order_document
(
    document_id bigint       not null,
    order_id    bigint       not null,
    link        varchar(255) null,
    primary key (document_id, order_id),
    constraint FKivobyxod8bdy1at7jyebadjk9
    foreign key (document_id) references document (document_id),
    constraint FKn44nuubarx3lvmhlb7q93dnhs
    foreign key (order_id) references transport_order (id)
    );

create table if not exists order_pallet
(
    quantity  int            not null,
    weight    decimal(38, 2) null,
    id        bigint auto_increment
    primary key,
    order_id  bigint         null,
    pallet_id bigint         null,
    constraint FKdoim03yskiam8xjtkhw0nex9s
    foreign key (pallet_id) references pallet (id),
    constraint FKll5hqqpqn11eppdgle17mdw6f
    foreign key (order_id) references transport_order (id)
    );

create table if not exists order_status_update
(
    enabled      bit                                                                                                   not null,
    created_at   datetime(6)                                                                                           null,
    id           bigint auto_increment
    primary key,
    order_id     bigint                                                                                                null,
    updated_at   datetime(6)                                                                                           null,
    created_by   varchar(255)                                                                                          null,
    updated_by   varchar(255)                                                                                          null,
    order_status enum ('APPROVED', 'DELIVERED', 'DENIED', 'DOCUMENT_PENDING', 'IN_PROGRESS', 'PRE_APPROVED', 'REVIEW') null,
    constraint FK97a9tkwhdcgnrmo1eud0gto99
    foreign key (order_id) references transport_order (id)
    );

create table if not exists transport_status_updates
(
    location_latitude  double                                                                                                                                                                                                  null,
    location_longitude double                                                                                                                                                                                                  null,
    id                 bigint auto_increment
    primary key,
    order_id           bigint                                                                                                                                                                                                  not null,
    timestamp          datetime(6)                                                                                                                                                                                             not null,
    updated_by         varchar(100)                                                                                                                                                                                            null,
    location_address   varchar(500)                                                                                                                                                                                            null,
    photo_url          varchar(500)                                                                                                                                                                                            null,
    signature_url      varchar(500)                                                                                                                                                                                            null,
    notes              varchar(1000)                                                                                                                                                                                           null,
    status             enum ('ARRIVED_AT_DESTINATION', 'ARRIVED_AT_WAREHOUSE', 'DELIVERED', 'EN_ROUTE_TO_DESTINATION', 'EN_ROUTE_TO_WAREHOUSE', 'LOADING', 'LOADING_COMPLETED', 'PENDING', 'UNLOADING', 'UNLOADING_COMPLETED') not null,
    constraint FK180724g8kbelklhapmof2au7q
    foreign key (order_id) references transport_order (id)
    );

