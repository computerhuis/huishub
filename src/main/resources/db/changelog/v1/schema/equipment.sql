-- liquibase formatted sql
-- changeset ronlievens:schema-1
CREATE TABLE equipment
(
    id            BIGINT                 NOT NULL AUTO_INCREMENT,
    category      ENUM ('DESKTOP', 'LAPTOP', 'TABLET', 'MOBILE', 'SIM', 'USB_STICK'),
    status        ENUM ('CUSTOMER_OWNED','INCOMING_GIFT','SUITABLE_FOR_GIFT','RESERVED','GIFT_ISSUED','DEMOLITION', 'SOLD'),

    owner_id      BIGINT,
    donor_id      BIGINT,
    behalf_of_id  BIGINT,

    serial_number VARCHAR(512),
    manufacturer  VARCHAR(255),
    model         VARCHAR(255),

    specification JSON,

    registered    DATETIME DEFAULT NOW() NOT NULL,
    unregistered  DATETIME,

    CONSTRAINT equipment_pk PRIMARY KEY (id),
    CONSTRAINT equipment_fk_1 FOREIGN KEY (owner_id) REFERENCES persons (id) ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT equipment_fk_2 FOREIGN KEY (donor_id) REFERENCES donors (id) ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT equipment_fk_3 FOREIGN KEY (behalf_of_id) REFERENCES donors (id) ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE = InnoDB
  CHARACTER SET 'utf8'
  COLLATE 'utf8_unicode_ci';
CREATE INDEX equipment_i_1 ON equipment (owner_id);
CREATE INDEX equipment_i_2 ON equipment (donor_id);
CREATE UNIQUE INDEX equipment_i_3 ON equipment (serial_number);
