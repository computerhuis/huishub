-- liquibase formatted sql
-- changeset ronlievens:schema-1
CREATE TABLE timesheets
(
    person_id    BIGINT                 NOT NULL,
    activity_id  BIGINT                 NOT NULL,
    registered   DATETIME DEFAULT NOW() NOT NULL,
    unregistered DATETIME,

    CONSTRAINT timesheets_pk PRIMARY KEY (person_id, registered, activity_id),
    CONSTRAINT timesheets_fk_1 FOREIGN KEY (person_id) REFERENCES persons (id) ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT timesheets_fk_2 FOREIGN KEY (activity_id) REFERENCES activities (id) ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE = InnoDB
  CHARACTER SET 'utf8'
  COLLATE 'utf8_unicode_ci';
