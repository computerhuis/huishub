-- liquibase formatted sql
-- changeset ronlievens:schema-1
CREATE TABLE donor_persons
(
    donor_id  BIGINT NOT NULL,
    person_id BIGINT NOT NULL,

    CONSTRAINT donor_persons_pk PRIMARY KEY (donor_id, person_id),
    CONSTRAINT donor_persons_fk_1 FOREIGN KEY (donor_id) REFERENCES donors (id) ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT donor_persons_fk_2 FOREIGN KEY (person_id) REFERENCES persons (id) ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE = InnoDB
  CHARACTER SET 'utf8'
  COLLATE 'utf8_unicode_ci';
