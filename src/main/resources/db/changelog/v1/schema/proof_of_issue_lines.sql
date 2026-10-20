-- liquibase formatted sql
-- changeset ronlievens:schema-1
CREATE TABLE proof_of_issue_lines
(
    proof_of_issue_id BIGINT         NOT NULL,
    line_id           BIGINT         NOT NULL,
    description       TEXT           NOT NULL,
    quantity          DECIMAL(10, 2) NOT NULL DEFAULT 1,
    unit              VARCHAR(50)    NOT NULL DEFAULT 'pcs',

    CONSTRAINT proof_of_issue_lines_pk PRIMARY KEY (proof_of_issue_id, line_id),
    CONSTRAINT proof_of_issue_lines_fk_1 FOREIGN KEY (proof_of_issue_id) REFERENCES proof_of_issues (id) ON UPDATE RESTRICT ON DELETE CASCADE
) ENGINE = InnoDB
  CHARACTER SET 'utf8'
  COLLATE 'utf8_unicode_ci';
