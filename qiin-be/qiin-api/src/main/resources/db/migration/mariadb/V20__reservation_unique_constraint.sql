ALTER TABLE reservation
ADD CONSTRAINT uk_asset_time
UNIQUE (asset_id, start_at, end_at);