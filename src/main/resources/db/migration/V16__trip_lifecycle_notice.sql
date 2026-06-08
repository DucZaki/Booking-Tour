ALTER TABLE `dat_cho`
    ADD COLUMN `trip_started_notice_sent_at` datetime DEFAULT NULL,
    ADD COLUMN `trip_completed_notice_sent_at` datetime DEFAULT NULL;
