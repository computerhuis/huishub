-- liquibase formatted sql
-- changeset ronlievens:schema-1
CREATE VIEW timesheet_overview AS
SELECT ts.*,
       a.name as activity_name,
       i.first_name,
       i.infix,
       i.last_name
FROM timesheets ts
         LEFT JOIN activities a ON a.id = ts.activity_id
         LEFT JOIN dco.persons i ON i.id = ts.person_id;
