ALTER TABLE site_setting_translation
    ADD COLUMN footer_statement varchar(500) NULL,
    ADD COLUMN footer_availability varchar(500) NULL,
    ADD COLUMN footer_rights varchar(500) NULL;
