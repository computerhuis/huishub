-- liquibase formatted sql
-- changeset ronlievens:schema-0
create table person_roles
(
    person_id BIGINT,
    authority VARCHAR(225)
);
alter table person_roles
    add constraint person_roles_pk primary key (person_id, authority);
alter table person_roles
    add constraint person_roles_fk_1 foreign key (person_id) references persons (ID) on delete cascade;
create index person_roles_fk_1_i
    on person_roles (person_id);
