CREATE TABLE company (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    ruc         VARCHAR(20)  NOT NULL UNIQUE,
    enabled     BIT(1)       NOT NULL DEFAULT 1,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    created_by  VARCHAR(255),
    updated_by  VARCHAR(255)
);

INSERT INTO company (name, ruc, enabled, created_at, updated_at)
VALUES ('TUPACK', '20613601296', 1, NOW(), NOW());

ALTER TABLE invoice ADD COLUMN company_id BIGINT NULL;

ALTER TABLE invoice
    ADD CONSTRAINT fk_invoice_company FOREIGN KEY (company_id) REFERENCES company (id);

UPDATE invoice SET company_id = 1;

ALTER TABLE invoice MODIFY COLUMN company_id BIGINT NOT NULL;
