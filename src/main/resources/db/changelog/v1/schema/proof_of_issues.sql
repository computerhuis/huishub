-- liquibase formatted sql
-- changeset ronlievens:schema-1
CREATE TABLE proof_of_issues
(
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    issue_date   DATETIME NOT NULL,
    issued_by    BIGINT   NOT NULL,
    behalf_of_id BIGINT,
    recipient_id BIGINT   NOT NULL,
    equipment_id BIGINT   NOT NULL,
    signed_image LONGBLOB,

    CONSTRAINT proof_of_issues_pk PRIMARY KEY (id),
    CONSTRAINT proof_of_issues_fk_1 FOREIGN KEY (recipient_id) REFERENCES persons (id) ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT proof_of_issues_fk_2 FOREIGN KEY (behalf_of_id) REFERENCES donors (id) ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT proof_of_issues_fk_3 FOREIGN KEY (issued_by) REFERENCES persons (id) ON UPDATE RESTRICT ON DELETE CASCADE,
    CONSTRAINT proof_of_issues_fk_4 FOREIGN KEY (equipment_id) REFERENCES equipment (id) ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE = InnoDB
  CHARACTER SET 'utf8'
  COLLATE 'utf8_unicode_ci';
CREATE INDEX proof_of_issues_i_1 ON proof_of_issues (behalf_of_id);
CREATE INDEX proof_of_issues_i_2 ON proof_of_issues (recipient_id);
CREATE INDEX proof_of_issues_i_3 ON proof_of_issues (issue_date);
CREATE INDEX proof_of_issues_i_4 ON proof_of_issues (equipment_id);
