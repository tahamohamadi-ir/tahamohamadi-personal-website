CREATE TABLE portfolio_project_media (
    portfolio_project_id uuid NOT NULL,
    media_asset_id uuid NOT NULL,
    sort_order integer NOT NULL,
    CONSTRAINT pk_portfolio_project_media PRIMARY KEY (portfolio_project_id, media_asset_id),
    CONSTRAINT uq_portfolio_project_media_sort_order UNIQUE (portfolio_project_id, sort_order),
    CONSTRAINT ck_portfolio_project_media_sort_order CHECK (sort_order >= 0),
    CONSTRAINT fk_portfolio_project_media_project FOREIGN KEY (portfolio_project_id) REFERENCES portfolio_project (id) ON DELETE CASCADE,
    CONSTRAINT fk_portfolio_project_media_asset FOREIGN KEY (media_asset_id) REFERENCES media_asset (id) ON DELETE RESTRICT
);
CREATE INDEX ix_portfolio_project_media_asset ON portfolio_project_media (media_asset_id, portfolio_project_id);
