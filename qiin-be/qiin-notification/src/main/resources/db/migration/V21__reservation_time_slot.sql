ALTER TABLE reservation DROP INDEX uk_asset_time;
ALTER TABLE reservation DROP COLUMN start_at, DROP COLUMN end_at;
ALTER TABLE reservation
    ADD COLUMN start_at DATETIME(0) NOT NULL COMMENT '예약 시작 시간',
    ADD COLUMN end_at   DATETIME(0) NOT NULL COMMENT '예약 종료 시간';

ALTER TABLE reservation DROP COLUMN actual_start_at, DROP COLUMN actual_end_at;
ALTER TABLE reservation
    ADD COLUMN actual_start_at DATETIME(6) COMMENT '예약 실제 시작 시간',
    ADD COLUMN actual_end_at   DATETIME(6) COMMENT '예약 실제 종료 시간';

CREATE TABLE reservation_slot (
    `reservation_slot_id`        BIGINT PRIMARY KEY AUTO_INCREMENT,

    `reservation_id` BIGINT NOT NULL,
    `asset_id`       BIGINT NOT NULL,
    `start_at`        DATETIME(0) NOT NULL COMMENT '예약 시작 시간',
    `created_at`  DATETIME(6) NOT NULL COMMENT '생성 시각',

    CONSTRAINT fk_slot_reservation
        FOREIGN KEY (reservation_id)
        REFERENCES reservation(reservation_id),

    CONSTRAINT fk_slot_asset
        FOREIGN KEY (asset_id)
        REFERENCES asset(asset_id),

    CONSTRAINT uk_asset_slot
        UNIQUE (asset_id, start_at)

);

