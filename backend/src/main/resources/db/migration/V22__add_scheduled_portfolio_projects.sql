ALTER TABLE portfolio_project ADD COLUMN scheduled_for timestamptz NULL;
ALTER TABLE portfolio_project DROP CONSTRAINT ck_portfolio_project_status;
ALTER TABLE portfolio_project ADD CONSTRAINT ck_portfolio_project_status CHECK (status IN ('DRAFT', 'SCHEDULED', 'PUBLISHED', 'ARCHIVED'));
ALTER TABLE portfolio_project ADD CONSTRAINT ck_portfolio_project_scheduled_for CHECK (status <> 'SCHEDULED' OR scheduled_for IS NOT NULL);
CREATE INDEX ix_portfolio_project_scheduled_publish ON portfolio_project (scheduled_for, id) WHERE status = 'SCHEDULED' AND deleted_at IS NULL;
