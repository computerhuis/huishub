-- liquibase formatted sql
-- changeset ronlievens:schema-0
CREATE TABLE postal_codes
(
    code             CHAR(6)       NOT NULL,
    url              VARCHAR(2048) NOT NULL,
    city             VARCHAR(1024) NOT NULL,
    municipality     VARCHAR(1024) NOT NULL,
    province         VARCHAR(1024) NOT NULL,
    neighbourhood    VARCHAR(1024),
    district         VARCHAR(1024),
    street           VARCHAR(2048),
    house_number_min INT,
    house_number_max INT,
    pobox            BOOLEAN       NOT NULL DEFAULT false,
    CONSTRAINT postal_codes_pk PRIMARY KEY (code)
) ENGINE = InnoDB
  CHARACTER SET 'utf8'
  COLLATE 'utf8_unicode_ci';
