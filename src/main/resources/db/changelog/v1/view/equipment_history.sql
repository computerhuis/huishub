-- liquibase formatted sql
-- changeset ronlievens:schema-1
CREATE VIEW equipment_history AS
SELECT t.id as ticket_id,
       ts.status,
       t.equipment_id,
       t.ticket_type,
       t.registered,
       t.subject,
       i.first_name,
       i.infix,
       i.last_name
FROM tickets t
         LEFT JOIN equipment e ON t.equipment_id = e.id
         LEFT JOIN ticket_status ts ON t.id = ts.ticket_id
         LEFT JOIN persons i ON i.id = ts.volunteer_id

WHERE
    date = (SELECT MAX (date) FROM ticket_status WHERE ticket_id = ts.ticket_id);
