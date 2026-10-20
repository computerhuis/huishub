-- liquibase formatted sql
-- changeset ronlievens:schema-1
CREATE TABLE ticket_log
(
    ticket_id    BIGINT   NOT NULL,
    date         DATETIME NOT NULL DEFAULT NOW(),
    volunteer_id BIGINT   NOT NULL,
    log          TEXT     NOT NULL,

    CONSTRAINT ticket_log_pk PRIMARY KEY (ticket_id, date),
    CONSTRAINT ticket_log_fk_1 FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT ticket_log_fk_2 FOREIGN KEY (volunteer_id) REFERENCES persons (id) ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE = InnoDB
  CHARACTER SET 'utf8'
  COLLATE 'utf8_unicode_ci';
