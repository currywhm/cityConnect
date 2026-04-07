CREATE TABLE IF NOT EXISTS `users` (
  `id` CHAR(26) NOT NULL,
  `openid` VARCHAR(64) NOT NULL,
  `unionid` VARCHAR(64) DEFAULT NULL,
  `appid` VARCHAR(64) NOT NULL,
  `nickname` VARCHAR(64) NOT NULL,
  `avatar_url` VARCHAR(512) NOT NULL DEFAULT '',
  `status` VARCHAR(16) NOT NULL DEFAULT 'active',
  `registered_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `last_login_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_openid` (`openid`),
  KEY `idx_users_unionid` (`unionid`),
  KEY `idx_users_status` (`status`),
  KEY `idx_users_registered_at` (`registered_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `admin_users` (
  `id` CHAR(26) NOT NULL,
  `login_name` VARCHAR(64) NOT NULL,
  `display_name` VARCHAR(64) NOT NULL,
  `role_code` VARCHAR(32) NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'active',
  `password_hash` VARCHAR(255) DEFAULT NULL,
  `last_login_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_users_login_name` (`login_name`),
  KEY `idx_admin_users_role_code` (`role_code`),
  KEY `idx_admin_users_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_profiles` (
  `user_id` CHAR(26) NOT NULL,
  `city` VARCHAR(64) NOT NULL DEFAULT '',
  `bio` VARCHAR(255) NOT NULL DEFAULT '',
  `phone_country_code` VARCHAR(8) DEFAULT NULL,
  `phone_masked` VARCHAR(32) NOT NULL DEFAULT '',
  `gender` VARCHAR(16) DEFAULT NULL,
  `birthday` DATE DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_user_profiles_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_credit` (
  `user_id` CHAR(26) NOT NULL,
  `credit_score` INT NOT NULL DEFAULT 100,
  `no_show_count` INT NOT NULL DEFAULT 0,
  `withdraw_count` INT NOT NULL DEFAULT 0,
  `complete_count` INT NOT NULL DEFAULT 0,
  `report_count` INT NOT NULL DEFAULT 0,
  `risk_level` VARCHAR(16) NOT NULL DEFAULT 'normal',
  `restricted_until` DATETIME(3) DEFAULT NULL,
  `last_no_show_at` DATETIME(3) DEFAULT NULL,
  `notes` VARCHAR(255) NOT NULL DEFAULT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`user_id`),
  KEY `idx_user_credit_risk_level` (`risk_level`),
  KEY `idx_user_credit_restricted_until` (`restricted_until`),
  CONSTRAINT `fk_user_credit_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `user_blocks` (
  `id` CHAR(26) NOT NULL,
  `user_id` CHAR(26) NOT NULL,
  `blocked_user_id` CHAR(26) NOT NULL,
  `reason` VARCHAR(255) NOT NULL DEFAULT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_blocks_user_blocked` (`user_id`, `blocked_user_id`),
  KEY `idx_user_blocks_blocked_user_id` (`blocked_user_id`),
  CONSTRAINT `fk_user_blocks_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_user_blocks_blocked_user_id`
    FOREIGN KEY (`blocked_user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `wallet_accounts` (
  `user_id` CHAR(26) NOT NULL,
  `total_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `withdrawable_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `bonus_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `frozen_balance` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`user_id`),
  CONSTRAINT `fk_wallet_accounts_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `activities` (
  `id` CHAR(26) NOT NULL,
  `organizer_user_id` CHAR(26) NOT NULL,
  `title` VARCHAR(120) NOT NULL,
  `category` VARCHAR(32) NOT NULL,
  `description` TEXT NOT NULL,
  `location_name` VARCHAR(120) NOT NULL,
  `location_address` VARCHAR(255) NOT NULL,
  `latitude` DECIMAL(10,7) DEFAULT NULL,
  `longitude` DECIMAL(10,7) DEFAULT NULL,
  `area_code` VARCHAR(32) NOT NULL DEFAULT 'citywide',
  `cover_url` VARCHAR(512) NOT NULL DEFAULT '',
  `fee_mode` VARCHAR(32) NOT NULL DEFAULT 'onsite_confirm',
  `fee_amount` DECIMAL(10,2) DEFAULT NULL,
  `fee_desc` VARCHAR(255) NOT NULL DEFAULT '',
  `review_status` VARCHAR(16) NOT NULL DEFAULT 'pending',
  `activity_status` VARCHAR(16) NOT NULL DEFAULT 'open',
  `settlement_status` VARCHAR(16) NOT NULL DEFAULT 'pending',
  `publish_fee_amount` DECIMAL(10,2) NOT NULL DEFAULT 5.00,
  `publish_fee_paid` TINYINT(1) NOT NULL DEFAULT 0,
  `publish_fee_order_id` CHAR(26) DEFAULT NULL,
  `publish_fee_refunded_at` DATETIME(3) DEFAULT NULL,
  `accepted_fee_total` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `participant_count` INT NOT NULL DEFAULT 1,
  `capacity` INT NOT NULL,
  `reviewed_at` DATETIME(3) DEFAULT NULL,
  `ended_at` DATETIME(3) DEFAULT NULL,
  `ended_reason` VARCHAR(64) DEFAULT NULL,
  `start_at` DATETIME(3) NOT NULL,
  `end_at` DATETIME(3) NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_activities_organizer_user_id` (`organizer_user_id`),
  KEY `idx_activities_review_status` (`review_status`),
  KEY `idx_activities_activity_status` (`activity_status`),
  KEY `idx_activities_settlement_status` (`settlement_status`),
  KEY `idx_activities_start_at` (`start_at`),
  KEY `idx_activities_area_code` (`area_code`),
  KEY `idx_activities_lat_lng` (`latitude`, `longitude`),
  CONSTRAINT `fk_activities_organizer_user_id`
    FOREIGN KEY (`organizer_user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `activity_media` (
  `id` CHAR(26) NOT NULL,
  `activity_id` CHAR(26) NOT NULL,
  `media_type` VARCHAR(16) NOT NULL DEFAULT 'image',
  `media_url` VARCHAR(512) NOT NULL,
  `sort_no` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_activity_media_activity_id` (`activity_id`),
  KEY `idx_activity_media_sort_no` (`sort_no`),
  CONSTRAINT `fk_activity_media_activity_id`
    FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `activity_reviews` (
  `id` CHAR(26) NOT NULL,
  `activity_id` CHAR(26) NOT NULL,
  `reviewer_admin_id` CHAR(26) DEFAULT NULL,
  `decision` VARCHAR(16) NOT NULL,
  `reason` VARCHAR(255) NOT NULL DEFAULT '',
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_activity_reviews_activity_id` (`activity_id`),
  KEY `idx_activity_reviews_reviewer_admin_id` (`reviewer_admin_id`),
  CONSTRAINT `fk_activity_reviews_activity_id`
    FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_activity_reviews_reviewer_admin_id`
    FOREIGN KEY (`reviewer_admin_id`) REFERENCES `admin_users` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `payment_orders` (
  `id` CHAR(26) NOT NULL,
  `order_no` VARCHAR(40) NOT NULL,
  `user_id` CHAR(26) NOT NULL,
  `order_type` VARCHAR(32) NOT NULL,
  `biz_id` CHAR(26) DEFAULT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY',
  `status` VARCHAR(16) NOT NULL DEFAULT 'created',
  `channel` VARCHAR(32) NOT NULL DEFAULT 'wechat_pay',
  `client_request_id` VARCHAR(64) DEFAULT NULL,
  `prepay_id` VARCHAR(128) DEFAULT NULL,
  `prepay_package` VARCHAR(255) DEFAULT NULL,
  `transaction_id` VARCHAR(128) DEFAULT NULL,
  `sub_mch_id` VARCHAR(64) DEFAULT NULL,
  `attach_json` JSON DEFAULT NULL,
  `callback_json` JSON DEFAULT NULL,
  `fail_code` VARCHAR(64) DEFAULT NULL,
  `fail_message` VARCHAR(255) DEFAULT NULL,
  `paid_at` DATETIME(3) DEFAULT NULL,
  `closed_at` DATETIME(3) DEFAULT NULL,
  `refunded_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_orders_order_no` (`order_no`),
  UNIQUE KEY `uk_payment_orders_client_request_id` (`client_request_id`),
  KEY `idx_payment_orders_user_id` (`user_id`),
  KEY `idx_payment_orders_order_type_biz_id` (`order_type`, `biz_id`),
  KEY `idx_payment_orders_status` (`status`),
  KEY `idx_payment_orders_paid_at` (`paid_at`),
  CONSTRAINT `fk_payment_orders_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `applications` (
  `id` CHAR(26) NOT NULL,
  `activity_id` CHAR(26) NOT NULL,
  `applicant_user_id` CHAR(26) NOT NULL,
  `attempt_no` INT NOT NULL DEFAULT 1,
  `quote` VARCHAR(500) NOT NULL DEFAULT '',
  `status` VARCHAR(32) NOT NULL DEFAULT 'pending',
  `accepted_by_user_id` CHAR(26) DEFAULT NULL,
  `decision_reason` VARCHAR(255) NOT NULL DEFAULT '',
  `accept_fee_amount` DECIMAL(10,2) NOT NULL DEFAULT 5.00,
  `accept_fee_paid` TINYINT(1) NOT NULL DEFAULT 0,
  `accept_fee_order_id` CHAR(26) DEFAULT NULL,
  `conversation_id` CHAR(26) DEFAULT NULL,
  `client_request_id` VARCHAR(64) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `accepted_at` DATETIME(3) DEFAULT NULL,
  `rejected_at` DATETIME(3) DEFAULT NULL,
  `withdrawn_at` DATETIME(3) DEFAULT NULL,
  `no_show_at` DATETIME(3) DEFAULT NULL,
  `paid_at` DATETIME(3) DEFAULT NULL,
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_applications_activity_applicant_attempt` (`activity_id`, `applicant_user_id`, `attempt_no`),
  UNIQUE KEY `uk_applications_client_request_id` (`client_request_id`),
  KEY `idx_applications_activity_id` (`activity_id`),
  KEY `idx_applications_applicant_user_id` (`applicant_user_id`),
  KEY `idx_applications_status` (`status`),
  KEY `idx_applications_created_at` (`created_at`),
  CONSTRAINT `fk_applications_activity_id`
    FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_applications_applicant_user_id`
    FOREIGN KEY (`applicant_user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT,
  CONSTRAINT `fk_applications_accepted_by_user_id`
    FOREIGN KEY (`accepted_by_user_id`) REFERENCES `users` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `activity_participants` (
  `id` CHAR(26) NOT NULL,
  `activity_id` CHAR(26) NOT NULL,
  `user_id` CHAR(26) NOT NULL,
  `role` VARCHAR(16) NOT NULL DEFAULT 'participant',
  `join_status` VARCHAR(32) NOT NULL DEFAULT 'accepted',
  `source_application_id` CHAR(26) DEFAULT NULL,
  `joined_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `left_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_participants_activity_user` (`activity_id`, `user_id`),
  KEY `idx_activity_participants_user_id` (`user_id`),
  KEY `idx_activity_participants_join_status` (`join_status`),
  CONSTRAINT `fk_activity_participants_activity_id`
    FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_activity_participants_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT,
  CONSTRAINT `fk_activity_participants_source_application_id`
    FOREIGN KEY (`source_application_id`) REFERENCES `applications` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `conversations` (
  `id` CHAR(26) NOT NULL,
  `biz_type` VARCHAR(16) NOT NULL,
  `biz_id` CHAR(26) DEFAULT NULL,
  `title` VARCHAR(120) NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'active',
  `last_message_id` CHAR(26) DEFAULT NULL,
  `last_message_seq` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `last_message_preview` VARCHAR(255) NOT NULL DEFAULT '',
  `last_message_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversations_biz_type_biz_id` (`biz_type`, `biz_id`),
  KEY `idx_conversations_status` (`status`),
  KEY `idx_conversations_last_message_at` (`last_message_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `conversation_members` (
  `id` CHAR(26) NOT NULL,
  `conversation_id` CHAR(26) NOT NULL,
  `user_id` CHAR(26) NOT NULL,
  `member_role` VARCHAR(16) NOT NULL DEFAULT 'member',
  `can_send` TINYINT(1) NOT NULL DEFAULT 1,
  `joined_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `left_at` DATETIME(3) DEFAULT NULL,
  `last_read_message_id` CHAR(26) DEFAULT NULL,
  `last_read_message_seq` BIGINT UNSIGNED NOT NULL DEFAULT 0,
  `last_read_at` DATETIME(3) DEFAULT NULL,
  `unread_count` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conversation_members_conversation_user` (`conversation_id`, `user_id`),
  KEY `idx_conversation_members_user_id` (`user_id`),
  KEY `idx_conversation_members_unread_count` (`unread_count`),
  CONSTRAINT `fk_conversation_members_conversation_id`
    FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_conversation_members_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `messages` (
  `id` CHAR(26) NOT NULL,
  `conversation_id` CHAR(26) NOT NULL,
  `message_seq` BIGINT UNSIGNED NOT NULL,
  `client_msg_id` VARCHAR(64) NOT NULL,
  `sender_user_id` CHAR(26) DEFAULT NULL,
  `sender_role` VARCHAR(16) NOT NULL,
  `msg_type` VARCHAR(32) NOT NULL DEFAULT 'text',
  `content_text` MEDIUMTEXT NOT NULL,
  `ext_json` JSON DEFAULT NULL,
  `moderation_status` VARCHAR(16) NOT NULL DEFAULT 'pass',
  `moderation_reason` VARCHAR(255) DEFAULT NULL,
  `client_sent_at` DATETIME(3) DEFAULT NULL,
  `sent_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `recalled_at` DATETIME(3) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_messages_conversation_seq` (`conversation_id`, `message_seq`),
  UNIQUE KEY `uk_messages_conversation_client_msg_id` (`conversation_id`, `client_msg_id`),
  KEY `idx_messages_sender_user_id` (`sender_user_id`),
  KEY `idx_messages_sent_at` (`sent_at`),
  CONSTRAINT `fk_messages_conversation_id`
    FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_messages_sender_user_id`
    FOREIGN KEY (`sender_user_id`) REFERENCES `users` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `notifications` (
  `id` CHAR(26) NOT NULL,
  `user_id` CHAR(26) NOT NULL,
  `type` VARCHAR(32) NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `content` VARCHAR(255) NOT NULL DEFAULT '',
  `biz_type` VARCHAR(32) NOT NULL DEFAULT '',
  `biz_id` CHAR(26) DEFAULT NULL,
  `read_status` VARCHAR(16) NOT NULL DEFAULT 'unread',
  `read_at` DATETIME(3) DEFAULT NULL,
  `extra_json` JSON DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_notifications_user_read_status` (`user_id`, `read_status`),
  KEY `idx_notifications_type` (`type`),
  KEY `idx_notifications_created_at` (`created_at`),
  CONSTRAINT `fk_notifications_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `wallet_ledger` (
  `id` CHAR(26) NOT NULL,
  `user_id` CHAR(26) NOT NULL,
  `biz_type` VARCHAR(32) NOT NULL,
  `biz_id` CHAR(26) DEFAULT NULL,
  `direction` VARCHAR(16) NOT NULL,
  `currency` CHAR(3) NOT NULL DEFAULT 'CNY',
  `change_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `withdrawable_change_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `bonus_change_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `frozen_change_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `total_after` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `withdrawable_after` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `bonus_after` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `frozen_after` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `remark` VARCHAR(255) NOT NULL DEFAULT '',
  `operator_type` VARCHAR(32) NOT NULL DEFAULT 'system',
  `operator_id` CHAR(26) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_wallet_ledger_user_created` (`user_id`, `created_at`),
  KEY `idx_wallet_ledger_biz_type_biz_id` (`biz_type`, `biz_id`),
  CONSTRAINT `fk_wallet_ledger_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `withdraw_requests` (
  `id` CHAR(26) NOT NULL,
  `request_no` VARCHAR(40) NOT NULL,
  `user_id` CHAR(26) NOT NULL,
  `amount` DECIMAL(10,2) NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'pending',
  `channel` VARCHAR(32) NOT NULL DEFAULT 'wechat_wallet',
  `channel_account_snapshot_json` JSON DEFAULT NULL,
  `reviewer_admin_id` CHAR(26) DEFAULT NULL,
  `reviewed_at` DATETIME(3) DEFAULT NULL,
  `paid_at` DATETIME(3) DEFAULT NULL,
  `reject_reason` VARCHAR(255) DEFAULT NULL,
  `client_request_id` VARCHAR(64) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_withdraw_requests_request_no` (`request_no`),
  UNIQUE KEY `uk_withdraw_requests_client_request_id` (`client_request_id`),
  KEY `idx_withdraw_requests_user_id` (`user_id`),
  KEY `idx_withdraw_requests_status` (`status`),
  CONSTRAINT `fk_withdraw_requests_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT,
  CONSTRAINT `fk_withdraw_requests_reviewer_admin_id`
    FOREIGN KEY (`reviewer_admin_id`) REFERENCES `admin_users` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `activity_settlements` (
  `id` CHAR(26) NOT NULL,
  `activity_id` CHAR(26) NOT NULL,
  `organizer_user_id` CHAR(26) NOT NULL,
  `participant_count` INT NOT NULL DEFAULT 0,
  `paid_participant_count` INT NOT NULL DEFAULT 0,
  `gross_accept_fee` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `organizer_income_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `publish_fee_refund_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `settlement_status` VARCHAR(16) NOT NULL DEFAULT 'pending',
  `settlement_reason` VARCHAR(255) NOT NULL DEFAULT '',
  `rule_snapshot_json` JSON DEFAULT NULL,
  `settled_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_settlements_activity_id` (`activity_id`),
  KEY `idx_activity_settlements_organizer_user_id` (`organizer_user_id`),
  KEY `idx_activity_settlements_status` (`settlement_status`),
  CONSTRAINT `fk_activity_settlements_activity_id`
    FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_activity_settlements_organizer_user_id`
    FOREIGN KEY (`organizer_user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `commission_records` (
  `id` CHAR(26) NOT NULL,
  `user_id` CHAR(26) NOT NULL,
  `activity_id` CHAR(26) NOT NULL,
  `settlement_id` CHAR(26) NOT NULL,
  `commission_type` VARCHAR(32) NOT NULL,
  `base_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `rate_value` DECIMAL(5,4) NOT NULL DEFAULT 0.5000,
  `amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `status` VARCHAR(16) NOT NULL DEFAULT 'settled',
  `ledger_id` CHAR(26) DEFAULT NULL,
  `settled_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_commission_records_user_id` (`user_id`),
  KEY `idx_commission_records_activity_id` (`activity_id`),
  KEY `idx_commission_records_settlement_id` (`settlement_id`),
  CONSTRAINT `fk_commission_records_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT,
  CONSTRAINT `fk_commission_records_activity_id`
    FOREIGN KEY (`activity_id`) REFERENCES `activities` (`id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_commission_records_settlement_id`
    FOREIGN KEY (`settlement_id`) REFERENCES `activity_settlements` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `support_tickets` (
  `id` CHAR(26) NOT NULL,
  `ticket_no` VARCHAR(40) NOT NULL,
  `user_id` CHAR(26) NOT NULL,
  `source_conversation_id` CHAR(26) DEFAULT NULL,
  `category` VARCHAR(32) NOT NULL,
  `priority` VARCHAR(16) NOT NULL DEFAULT 'normal',
  `status` VARCHAR(16) NOT NULL DEFAULT 'open',
  `tags_json` JSON DEFAULT NULL,
  `summary` VARCHAR(255) NOT NULL,
  `assignee_admin_id` CHAR(26) DEFAULT NULL,
  `first_response_at` DATETIME(3) DEFAULT NULL,
  `resolved_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_support_tickets_ticket_no` (`ticket_no`),
  KEY `idx_support_tickets_user_id` (`user_id`),
  KEY `idx_support_tickets_status` (`status`),
  KEY `idx_support_tickets_assignee_admin_id` (`assignee_admin_id`),
  CONSTRAINT `fk_support_tickets_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT,
  CONSTRAINT `fk_support_tickets_source_conversation_id`
    FOREIGN KEY (`source_conversation_id`) REFERENCES `conversations` (`id`)
    ON DELETE SET NULL,
  CONSTRAINT `fk_support_tickets_assignee_admin_id`
    FOREIGN KEY (`assignee_admin_id`) REFERENCES `admin_users` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `support_ticket_messages` (
  `id` CHAR(26) NOT NULL,
  `ticket_id` CHAR(26) NOT NULL,
  `sender_type` VARCHAR(16) NOT NULL,
  `sender_id` CHAR(26) DEFAULT NULL,
  `content_text` TEXT NOT NULL,
  `attachments_json` JSON DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_support_ticket_messages_ticket_id` (`ticket_id`),
  KEY `idx_support_ticket_messages_created_at` (`created_at`),
  CONSTRAINT `fk_support_ticket_messages_ticket_id`
    FOREIGN KEY (`ticket_id`) REFERENCES `support_tickets` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `faq_articles` (
  `id` CHAR(26) NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `keywords_json` JSON DEFAULT NULL,
  `answer_text` TEXT NOT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'published',
  `sort_no` INT NOT NULL DEFAULT 0,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_faq_articles_status` (`status`),
  KEY `idx_faq_articles_sort_no` (`sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `reports` (
  `id` CHAR(26) NOT NULL,
  `reporter_user_id` CHAR(26) NOT NULL,
  `target_type` VARCHAR(32) NOT NULL,
  `target_id` CHAR(26) NOT NULL,
  `reason_type` VARCHAR(32) NOT NULL,
  `content_text` VARCHAR(500) NOT NULL DEFAULT '',
  `evidence_json` JSON DEFAULT NULL,
  `status` VARCHAR(16) NOT NULL DEFAULT 'pending',
  `reviewer_admin_id` CHAR(26) DEFAULT NULL,
  `decision` VARCHAR(32) DEFAULT NULL,
  `decision_remark` VARCHAR(255) DEFAULT NULL,
  `reviewed_at` DATETIME(3) DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_reports_reporter_user_id` (`reporter_user_id`),
  KEY `idx_reports_target` (`target_type`, `target_id`),
  KEY `idx_reports_status` (`status`),
  CONSTRAINT `fk_reports_reporter_user_id`
    FOREIGN KEY (`reporter_user_id`) REFERENCES `users` (`id`)
    ON DELETE RESTRICT,
  CONSTRAINT `fk_reports_reviewer_admin_id`
    FOREIGN KEY (`reviewer_admin_id`) REFERENCES `admin_users` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `message_moderation_logs` (
  `id` CHAR(26) NOT NULL,
  `user_id` CHAR(26) DEFAULT NULL,
  `conversation_id` CHAR(26) DEFAULT NULL,
  `message_id` CHAR(26) DEFAULT NULL,
  `hit_category` VARCHAR(32) NOT NULL,
  `hit_keyword` VARCHAR(128) DEFAULT NULL,
  `action` VARCHAR(32) NOT NULL,
  `raw_text` MEDIUMTEXT NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_message_moderation_logs_user_id` (`user_id`),
  KEY `idx_message_moderation_logs_conversation_id` (`conversation_id`),
  KEY `idx_message_moderation_logs_hit_category` (`hit_category`),
  CONSTRAINT `fk_message_moderation_logs_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE SET NULL,
  CONSTRAINT `fk_message_moderation_logs_conversation_id`
    FOREIGN KEY (`conversation_id`) REFERENCES `conversations` (`id`)
    ON DELETE SET NULL,
  CONSTRAINT `fk_message_moderation_logs_message_id`
    FOREIGN KEY (`message_id`) REFERENCES `messages` (`id`)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS `audit_logs` (
  `id` CHAR(26) NOT NULL,
  `operator_type` VARCHAR(32) NOT NULL,
  `operator_id` CHAR(26) DEFAULT NULL,
  `action` VARCHAR(64) NOT NULL,
  `target_type` VARCHAR(32) NOT NULL,
  `target_id` CHAR(26) DEFAULT NULL,
  `request_id` VARCHAR(64) DEFAULT NULL,
  `before_json` JSON DEFAULT NULL,
  `after_json` JSON DEFAULT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  KEY `idx_audit_logs_operator` (`operator_type`, `operator_id`),
  KEY `idx_audit_logs_target` (`target_type`, `target_id`),
  KEY `idx_audit_logs_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
