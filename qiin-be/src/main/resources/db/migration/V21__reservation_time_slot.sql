ALTER TABLE reservation DROP INDEX uk_asset_time;
ALTER TABLE reservation DROP COLUMN start_at, DROP COLUMN end_at;

CREATE TABLE reservation_slot (
    `reservation_slot_id`        BIGINT PRIMARY KEY AUTO_INCREMENT,

    `reservation_id` BIGINT NOT NULL,
    `asset_id`       BIGINT NOT NULL,
    `start_at`        TIMESTAMP(6) NOT NULL COMMENT '예약 시작 시간',
    `created_at`  TIMESTAMP(6) NOT NULL COMMENT '생성 시각',

    CONSTRAINT fk_slot_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES reservation(reservation_id),

    CONSTRAINT fk_slot_asset
        FOREIGN KEY (asset_id)
        REFERENCES asset(asset_id),

    CONSTRAINT uk_asset_slot
        UNIQUE (asset_id, start_at)

);

