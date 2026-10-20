-- liquibase formatted sql
-- changeset ronlievens:schema-1
CREATE VIEW ticket_log_overview AS
SELECT tl.*,
       i.first_name,
       i.infix,
       i.last_name
FROM ticket_log tl
         LEFT JOIN persons i ON i.id = tl.volunteer_id;
