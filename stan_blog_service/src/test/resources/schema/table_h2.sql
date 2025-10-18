DROP TABLE IF EXISTS  stan_blog_core_user;
CREATE TABLE `stan_blog_core_user`
(
    `ID`             bigint NOT NULL AUTO_INCREMENT,
    `USERNAME`       varchar(64)   DEFAULT NULL,
    `PASSWORD`       varchar(64)   DEFAULT NULL,
    `PHONE_NUM`      varchar(20)   DEFAULT NULL,
    `EMAIL`          varchar(120)  DEFAULT NULL,
    `LAST_NAME`      varchar(32)   DEFAULT NULL,
    `FIRST_NAME`     varchar(32)   DEFAULT NULL,
    `ADDRESS`        varchar(120)  DEFAULT NULL,
    `AVATAR_URL`     varchar(2000) DEFAULT NULL,
    `INTRODUCTION`   varchar(1000) DEFAULT NULL,
    `BLOG`           varchar(200)  DEFAULT NULL,
    `BACKGROUND_IMG` varchar(2000) DEFAULT NULL,
    `PHOTO_IMG`      varchar(2000) DEFAULT NULL,
    `PROFESSION`     varchar(100)  DEFAULT NULL,
    `EMAIL_VERIFIED` bit(1)        DEFAULT FALSE,
    `CREATE_TIME`    datetime      DEFAULT NULL,
    `CREATE_BY`      varchar(30)   DEFAULT NULL,
    `UPDATE_TIME`    datetime      DEFAULT NULL,
    `UPDATE_BY`      varchar(30)   DEFAULT NULL,
    `DELETED`        bit(1)        DEFAULT FALSE,
    PRIMARY KEY (`ID`),
    UNIQUE KEY `stan_blog_core_user_ID_uindex` (`ID`),
    UNIQUE KEY `stan_blog_core_user_EMAIL_uindex` (`EMAIL`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 10
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
;

DROP TABLE IF EXISTS stan_blog_core_user_feature;
CREATE TABLE `stan_blog_core_user_feature`
(
    `ID`                bigint NOT NULL AUTO_INCREMENT,
    `USER_ID`           bigint NOT NULL,
    `ARTICLE_MODULE`    bit(1)      DEFAULT TRUE,
    `PLAN_MODULE`       bit(1)      DEFAULT TRUE,
    `VOCABULARY_MODULE` bit(1)      DEFAULT TRUE,
    `COLLECTION_MODULE` bit(1)      DEFAULT TRUE,
    `CREATE_TIME`       datetime    DEFAULT NULL,
    `CREATE_BY`         varchar(30) DEFAULT NULL,
    `UPDATE_TIME`       datetime    DEFAULT NULL,
    `UPDATE_BY`         varchar(30) DEFAULT NULL,
    `DELETED`           bit(1)      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`ID`),
    UNIQUE KEY `stan_blog_core_user_feature_USER_ID_uindex` (`USER_ID`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
;

DROP TABLE IF EXISTS stan_blog_core_user_role;
CREATE TABLE `stan_blog_core_user_role`
(
    `ID`          bigint NOT NULL AUTO_INCREMENT,
    `ROLE`        varchar(30) DEFAULT NULL,
    `USER_ID`     bigint NOT NULL,
    `CREATE_TIME` datetime    DEFAULT NULL,
    `CREATE_BY`   varchar(30) DEFAULT NULL,
    `UPDATE_TIME` datetime    DEFAULT NULL,
    `UPDATE_BY`   varchar(30) DEFAULT NULL,
    `DELETED`     bit(1)      NOT NULL DEFAULT FALSE,
    PRIMARY KEY (`ID`),
    KEY `stan_blog_core_user_role_USER_ID_index` (`USER_ID`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 40
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS stan_blog_content_general_info;
-- base content info
create table stan_blog_content_general_info
(
    ID                varchar(32)                              not null
        primary key,
    TITLE             varchar(128) collate utf8mb4_unicode_ci  not null,
    DESCRIPTION       varchar(1000) collate utf8mb4_unicode_ci null,
    COVER_IMG_URL     varchar(2000) collate utf8mb4_unicode_ci null,
    CONTENT_TYPE      varchar(20)                              not null,
    OWNER_ID          bigint                                   not null,
    LIKE_COUNT        bigint default 0                         not null,
    VIEW_COUNT        bigint default 0                         not null,
    PUBLIC_TO_ALL     bit default false                        not null,
    PUBLISH_TIME      datetime                                 null,
    CONTENT_PROTECTED bit default false                        not null,
    TOPIC             varchar(32)                              null,
    DELETED           bit default false                        not null,
    CREATE_BY         varchar(64)                              null,
    CREATE_TIME       datetime                                 null,
    UPDATE_BY         varchar(64)                              null,
    UPDATE_TIME       datetime                                 null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS stan_blog_content_admin;
-- user content admin table 
create table stan_blog_content_admin
(
    CONTENT_ID                varchar(32)                      not null
        primary key,
    RECOMMENDED       bit default false                        not null,
    BANNED            bit default false                        not null,
    REASON            varchar(200)                             null,
    DELETED           bit default false                        not null,
    CREATE_BY         varchar(64)                              null,
    CREATE_TIME       datetime                                 null,
    UPDATE_BY         varchar(64)                              null,
    UPDATE_TIME       datetime                                 null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS stan_blog_article_info;
-- base article info
create table stan_blog_article_info
(
    CONTENT_ID  varchar(32)                             not null
        primary key,
    SUB_TITLE   varchar(128) collate utf8mb4_unicode_ci null,
    CONTENT     longtext collate utf8mb4_unicode_ci     null,
    DELETED     bit default false                       not null,
    CREATE_BY   varchar(64)                             null,
    CREATE_TIME datetime                                null,
    UPDATE_BY   varchar(64)                             null,
    UPDATE_TIME datetime                                null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS stan_blog_plan_info;
-- base plan info
create table stan_blog_plan_info
(
    CONTENT_ID        varchar(32)       not null
        primary key,
    TARGET_START_TIME datetime          null,
    TARGET_END_TIME   datetime          null,
    DELETED           bit default false not null,
    CREATE_BY         varchar(64)       null,
    CREATE_TIME       datetime          null,
    UPDATE_BY         varchar(64)       null,
    UPDATE_TIME       datetime          null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS stan_blog_plan_progress;
-- plan progress
create table stan_blog_plan_progress
(
    ID          varchar(32)      not null
        primary key,
    PLAN_ID  varchar(32)       not null,
    DESCRIPTION varchar(1000)    null,
    UPDATER_ID  bigint           not null,
    DELETED     bit default FALSE not null,
    CREATE_BY   varchar(64)      null,
    CREATE_TIME datetime         null,
    UPDATE_BY   varchar(64)      null,
    UPDATE_TIME datetime         null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS stan_blog_collection_info;
CREATE TABLE stan_blog_collection_info
(
    CONTENT_ID varchar(32) NOT NULL PRIMARY KEY,
    DELETED bit(1) NOT NULL DEFAULT false,
    CREATE_BY varchar(64) DEFAULT NULL,
    CREATE_TIME datetime DEFAULT NULL,
    UPDATE_BY varchar(64) DEFAULT NULL,
    UPDATE_TIME datetime DEFAULT NULL
) ENGINE = InnoDB 
  DEFAULT CHARSET = utf8mb4 
  COLLATE = utf8mb4_unicode_ci; 

DROP TABLE IF EXISTS stan_blog_vocabulary_info;
-- base vocabulary info
create table stan_blog_vocabulary_info
(
    CONTENT_ID    varchar(32)       not null
        primary key,
    LANGUAGE      varchar(32)       not null,
    LANGUAGE_FLAG varchar(255),
    DELETED       bit default false not null,
    CREATE_BY     varchar(64)       null,
    CREATE_TIME   datetime          null,
    UPDATE_BY     varchar(64)       null,
    UPDATE_TIME   datetime          null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS stan_blog_word_info;
create table stan_blog_word_info (
  ID            bigint auto_increment 
      primary key,
  VOCABULARY_ID varchar(32) not null,
  TEXT varchar(200)         not null,
  MEANING_IN_CHINESE varchar(2000) null,
  MEANING_IN_ENGLISH varchar(2000) null,
  PART_OF_SPEECH varchar(32) null,
  DELETED bit(1) not null DEFAULT false,
  CREATE_BY varchar(64) null,
  CREATE_TIME datetime null,
  UPDATE_BY varchar(64) null,
  UPDATE_TIME datetime null
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE = utf8mb4_unicode_ci;


DROP TABLE IF EXISTS stan_blog_tag_info;
create table stan_blog_tag_info
(
    ID          bigint auto_increment
        primary key,
    KEYWORD     varchar(32)       not null,
    DELETED     bit default false not null,
    CREATE_BY   varchar(64)       null,
    CREATE_TIME datetime          null,
    UPDATE_BY   varchar(64)       null,
    UPDATE_TIME datetime          null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS stan_blog_tag_relationship;
create table stan_blog_tag_relationship
(
    ID          bigint auto_increment
        primary key,
    TAG_ID      bigint            not null,
    PARENT_ID   bigint            null,
    COLLECTION_ID  varchar(32)       not null,
    DELETED     bit default false null,
    CREATE_BY   varchar(64)       null,
    CREATE_TIME datetime          null,
    UPDATE_BY   varchar(64)       null,
    UPDATE_TIME datetime          null
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

DROP TABLE IF EXISTS stan_blog_content_tag;
CREATE TABLE stan_blog_content_tag (
    ID          bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
    CONTENT_ID  varchar(32)       NOT NULL,
    TAG_ID      int               NOT NULL,
    DELETED     bit(1)            NOT NULL DEFAULT false
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

ALTER TABLE `stan_blog_content_general_info` ADD INDEX (`TOPIC`);

CREATE TABLE IF NOT EXISTS `stan_blog_comment` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `content_id` VARCHAR(32) NOT NULL,
    `content_type` VARCHAR(50) NOT NULL,
    `content` TEXT NOT NULL,
    `user_id` BIGINT(20) NOT NULL,
    `user_name` VARCHAR(255) NOT NULL,
    `user_avatar_url` VARCHAR(500),
    `parent_id` BIGINT(20),
    `like_count` BIGINT NOT NULL DEFAULT 0,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `ip_address` VARCHAR(50),
    `create_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `create_by` VARCHAR(30) DEFAULT NULL,
    `update_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) ,
    `update_by` VARCHAR(30) DEFAULT NULL ,
    PRIMARY KEY (`id`),
    INDEX `idx_content` (`content_id`, `content_type`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_is_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci; 

-- Add reply quote fields to comment table for Teams-style flat display
ALTER TABLE `stan_blog_comment`
ADD COLUMN `reply_to_user_name` VARCHAR(255),
ADD COLUMN `reply_to_content` VARCHAR(500); 

-- Notification system table
CREATE TABLE IF NOT EXISTS `stan_blog_notification` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `recipient_id` BIGINT(20) NOT NULL,
    `sender_id` BIGINT(20),
    `notification_type` VARCHAR(50) NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `message` VARCHAR(1000) NOT NULL,
    `is_read` TINYINT(1) NOT NULL DEFAULT 0,
    `related_content_id` VARCHAR(255),
    `related_content_type` VARCHAR(50),
    `related_content_title` VARCHAR(255),
    `action_url` VARCHAR(500),
    `metadata` VARCHAR(2000),
    `create_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `create_by` VARCHAR(30) DEFAULT NULL,
    `update_time` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    `update_by` VARCHAR(30) DEFAULT NULL,
    `deleted` TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    INDEX `idx_recipient_id` (`recipient_id`),
    INDEX `idx_notification_type` (`notification_type`),
    INDEX `idx_is_read` (`is_read`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_deleted` (`deleted`),
    INDEX `idx_recipient_read` (`recipient_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- File resource table for uploads
CREATE TABLE IF NOT EXISTS `stan_blog_file_resource` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `original_filename` VARCHAR(255) NOT NULL,
  `stored_filename` VARCHAR(255) NOT NULL,
  `storage_path` VARCHAR(1000) NOT NULL,
  `size_in_bytes` BIGINT NOT NULL,
  `content_type` VARCHAR(255) NULL,
  `owner_id` BIGINT NOT NULL,
  `public_to_all` TINYINT(1) NOT NULL DEFAULT 0,
  `checksum` VARCHAR(128) NULL,
  `create_time` DATETIME NULL,
  `create_by` VARCHAR(64) NULL,
  `update_time` DATETIME NULL,
  `update_by` VARCHAR(64) NULL,
  `deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX `idx_owner_id` (`owner_id`),
  INDEX `idx_public` (`public_to_all`),
  INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- data.sql content
-- end of sql