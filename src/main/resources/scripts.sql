CREATE TABLE users
(
    id         VARCHAR(255) NOT NULL,
    created_at datetime     NULL,
    updated_at datetime     NULL,
    created_by VARCHAR(255) NULL,
    updated_by VARCHAR(255) NULL,
    enabled    VARCHAR(255) NULL,
    first_name VARCHAR(150) NOT NULL,
    last_name  VARCHAR(150) NOT NULL,
    email      VARCHAR(190) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

CREATE TABLE users_roles
(
    role_id VARCHAR(255) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_users_roles PRIMARY KEY (role_id, user_id)
);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_users_roles_user FOREIGN KEY (user_id) REFERENCES users (id);

ALTER TABLE users_roles
    ADD CONSTRAINT fk_users_roles_userxJ7Roz FOREIGN KEY (role_id) REFERENCES roles (id);


CREATE TABLE roles
(
    id         VARCHAR(255) NOT NULL,
    created_at datetime     NULL,
    updated_at datetime     NULL,
    created_by VARCHAR(255) NULL,
    updated_by VARCHAR(255) NULL,
    enabled    VARCHAR(255) NULL,
    name       VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
);

ALTER TABLE roles
    ADD CONSTRAINT uk_roles_name UNIQUE (name);

alter table users alter column enabled CREATE TABLE roles
    (
    id         VARCHAR(255) NOT NULL,
    created_at datetime     NULL,
    updated_at datetime     NULL,
    created_by VARCHAR(255) NULL,
    updated_by VARCHAR(255) NULL,
    enabled    VARCHAR(255) NULL,
    name       VARCHAR(50)  NOT NULL,
    CONSTRAINT pk_roles PRIMARY KEY (id)
    );

ALTER TABLE roles
    ADD CONSTRAINT uk_roles_name UNIQUE (name);