CREATE TABLE `community_post_comment` (
    `comment_id` BIGINT NOT NULL AUTO_INCREMENT,
    `post_id` BIGINT NOT NULL,
    `author_member_id` BIGINT NOT NULL,
    `parent_comment_id` BIGINT NULL,
    `reply_to_comment_id` BIGINT NULL,
    `body` VARCHAR(255) NOT NULL,
    `created_at` DATETIME(6) NOT NULL,
    `updated_at` DATETIME(6) NOT NULL,
    `deleted_at` DATETIME(6) NULL,
    `lock_version` BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (`comment_id`),
    INDEX `idx_post_comment_post_parent_order`
        (`post_id`, `parent_comment_id`, `created_at`, `comment_id`),
    INDEX `idx_post_comment_parent_order`
        (`parent_comment_id`, `created_at`, `comment_id`),
    INDEX `idx_post_comment_post_deleted`
        (`post_id`, `deleted_at`),
    INDEX `idx_post_comment_author` (`author_member_id`),
    INDEX `idx_post_comment_reply_to` (`reply_to_comment_id`),
    CONSTRAINT `fk_post_comment_post`
        FOREIGN KEY (`post_id`) REFERENCES `community_post` (`post_id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `fk_post_comment_author`
        FOREIGN KEY (`author_member_id`) REFERENCES `member` (`member_id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `fk_post_comment_parent`
        FOREIGN KEY (`parent_comment_id`) REFERENCES `community_post_comment` (`comment_id`)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT `fk_post_comment_reply_to`
        FOREIGN KEY (`reply_to_comment_id`) REFERENCES `community_post_comment` (`comment_id`)
        ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE=InnoDB;
