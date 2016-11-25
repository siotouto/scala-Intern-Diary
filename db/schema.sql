DROP TABLE IF EXISTS user
CREATE TABLE user (
       `id` BIGINT UNSIGNED NOT NULL,

       `name` VARBINARY(32) NOT NULL,

       `created_at` DATETIME NOT NULL,
       `updated_at` DATETIME NOT NULL,

       PRIMARY KEY (id),
       UNIQUE KEY (name),

       KEY (created_at),
       KEY (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS entry
CREATE TABLE entry (
       `id` BIGINT UNSIGNED NOT NULL,

       `user_id` BIGINT UNSIGNED NOT NULL,
       `title` VARCHAR(256) NOT NULL,
       `body` VARCHAR(8000) NOT NULL,

       `created_at` DATETIME NOT NULL,
       `updated_at` DATETIME NOT NULL,

       PRIMARY KEY (id),
       
       KEY (user_id),
       KEY (user_id, updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DROP TABLE IF EXISTS comment
CREATE TABLE comment (
       `id` BIGINT UNSIGNED NOT NULL,

       `entry_id` BIGINT UNSIGNED NOT NULL,
       `commenter_id` BIGINT UNSIGNED NOT NULL,
       `body` VARCHAR(1024) NOT NULL,

       `created_at` DATETIME NOT NULL,
       `updated_at` DATETIME NOT NULL,

       PRIMARY KEY (id),

       KEY (entry_id),
       KEY (commenter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
