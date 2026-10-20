-- liquibase formatted sql
-- changeset ronlievens:schema-1
CREATE TABLE donors
(
    id                    BIGINT                 NOT NULL AUTO_INCREMENT,
    name                  VARCHAR(50)            NOT NULL,
    reporting             BOOLEAN  DEFAULT FALSE NOT NULL,

    email                 VARCHAR(255),
    mobile                CHAR(10),
    telephone             CHAR(10),

    postal_code           VARCHAR(6),
    street                VARCHAR(1024),
    house_number          INT,
    house_number_addition VARCHAR(4),
    city                  VARCHAR(1024),

    comments              TEXT,
    registered            DATETIME DEFAULT NOW() NOT NULL,
    unregistered          DATETIME,
    msaccess              JSON,
    CONSTRAINT donors_pk PRIMARY KEY (id)
) ENGINE = InnoDB
  CHARACTER SET 'utf8'
  COLLATE 'utf8_unicode_ci';
