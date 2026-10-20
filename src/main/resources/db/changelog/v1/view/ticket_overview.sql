-- liquibase formatted sql
-- changeset ronlievens:schema-1
CREATE VIEW ticket_overview AS
SELECT t.id,
       ts.status,
       t.equipment_id,
       t.ticket_type,
       t.registered,
       t.unregistered,
       t.subject,
       t.description,
       i.first_name,
       i.infix,
       i.last_name,
       e.category,
       e.manufacturer,
       e.model
FROM tickets t
         LEFT JOIN equipment e ON t.equipment_id = e.id
         LEFT JOIN ticket_status ts ON t.id = ts.ticket_id
         LEFT JOIN persons i ON i.id = ts.volunteer_id

WHERE
    date = (SELECT MAX (date) FROM ticket_status WHERE ticket_id = ts.ticket_id);
