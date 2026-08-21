ALTER TABLE `notification`
    ADD COLUMN `event_outbox_id` BINARY(16) NOT NULL COMMENT 'Outbox 이벤트 ID' AFTER `receiver_id`,
    ADD CONSTRAINT `uk_notification_receiver_outbox`
        UNIQUE (`receiver_id`, `event_outbox_id`);
