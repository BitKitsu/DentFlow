-- V8: Add advanced fields to patient table

ALTER TABLE patient
    ADD COLUMN pesel VARCHAR(11),
    ADD COLUMN gender VARCHAR(10),
    ADD COLUMN address_street VARCHAR(100),
    ADD COLUMN address_city VARCHAR(100),
    ADD COLUMN address_zip VARCHAR(20),
    ADD COLUMN address_country VARCHAR(100);
