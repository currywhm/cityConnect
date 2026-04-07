CREATE TABLE IF NOT EXISTS `app_sessions` (
  `id` CHAR(26) NOT NULL,
  `user_id` CHAR(26) NOT NULL,
  `session_token` VARCHAR(128) NOT NULL,
  `expires_at` DATETIME(3) NOT NULL,
  `created_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_app_sessions_session_token` (`session_token`),
  KEY `idx_app_sessions_user_id` (`user_id`),
  KEY `idx_app_sessions_expires_at` (`expires_at`),
  CONSTRAINT `fk_app_sessions_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT IGNORE INTO `admin_users` (`id`, `login_name`, `display_name`, `role_code`, `status`)
VALUES ('01ADMIN000000000000000001', 'admin', '系统管理员', 'super_admin', 'active');

INSERT IGNORE INTO `faq_articles` (`id`, `title`, `keywords_json`, `answer_text`, `status`, `sort_no`)
VALUES
  (
    '01FAQ000000000000000000001',
    '活动发布后为什么看不到？',
    JSON_ARRAY('活动', '审核', '发布'),
    '新活动默认进入待审核状态，审核通过后才会出现在公开列表中。',
    'published',
    10
  ),
  (
    '01FAQ000000000000000000002',
    '充值后余额多久到账？',
    JSON_ARRAY('充值', '余额', '支付'),
    '支付回调成功后会立即入账；如果订单仍是 created 或 prepaying，请稍后刷新订单状态。',
    'published',
    20
  );
