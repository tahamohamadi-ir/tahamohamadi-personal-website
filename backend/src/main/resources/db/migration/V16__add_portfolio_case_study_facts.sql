ALTER TABLE portfolio_project_translation
    ADD COLUMN role_text varchar(255) NULL,
    ADD COLUMN client_label varchar(255) NULL,
    ADD COLUMN team_description text NULL,
    ADD COLUMN outcome_text text NULL;
