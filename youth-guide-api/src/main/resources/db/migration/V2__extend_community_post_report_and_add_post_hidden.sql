ALTER TABLE `community_post_report`
    ADD COLUMN `reason` VARCHAR(32) NOT NULL,
    ADD COLUMN `detail` VARCHAR(255) NULL,
    ADD CONSTRAINT `uq_community_post_report_post_reporter`
        UNIQUE (`post_id`, `reporter_member_id`);

ALTER TABLE `community_post`
    ADD COLUMN `hidden_at` DATETIME(6) NULL;
