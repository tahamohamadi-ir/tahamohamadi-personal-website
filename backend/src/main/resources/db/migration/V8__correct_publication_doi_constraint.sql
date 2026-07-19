ALTER TABLE publication DROP CONSTRAINT ck_publication_doi;
ALTER TABLE publication
    ADD CONSTRAINT ck_publication_doi
        CHECK (doi IS NULL OR doi ~* E'^10\\.[0-9]{4,9}/[-._;()/:a-z0-9]+$');
