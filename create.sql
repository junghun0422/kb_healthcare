# DATABASE 생성
	CREATE DATABASE kb_healthcare;

# 회원 테이블 생성
-- 	DROP TABLE `kb_healthcare`.`user`;
CREATE TABLE IF NOT EXISTS `kb_healthcare`.`user` (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '회원 시퀀스',
    name VARCHAR(50) NOT NULL COMMENT '이름',
    nick_name VARCHAR(50) NOT NULL COMMENT '닉네임',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일',
    password VARCHAR(255) NOT NULL COMMENT '패스워드',
    create_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() COMMENT '가입일시',
    update_datetime TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP() COMMENT '수정일시'
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT '회원';

# 회원 고유키 테이블 생성
-- 	DROP TABLE `kb_healthcare`.`user_identifier`;
CREATE TABLE IF NOT EXISTS `kb_healthcare`.`user_identifier` (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '회원 고유키 관리 시퀀스',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '회원 시퀀스',
    record_key VARCHAR(100) NOT NULL UNIQUE COMMENT '사용자 구분 키',
    create_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP() COMMENT '등록일시',
    UNIQUE KEY `record_key` (`record_key`),
    KEY `idx_user_id_record_key` (`user_id`,`record_key`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT '회원 고유키 관리'
;

# 회원 역할 테이블
-- 	DROP TABLE `kb_healthcare`.`user_roles`;
CREATE TABLE IF NOT EXISTS `kb_healthcare`.`user_roles` (
    user_id BIGINT UNSIGNED NOT NULL COMMENT '회원 시퀀스',
    role VARCHAR(50) NOT NULL COMMENT '역할 (USER, ADMIN 등)',
    PRIMARY KEY (`user_id`, `role`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='회원 역할'
;


-- 	DROP TABLE `kb_healthcare`.`source`;
CREATE TABLE IF NOT EXISTS `kb_healthcare`.`source` (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '출처 시퀀스',
    record_key VARCHAR(100) NOT NULL COMMENT '사용자 구분 키',
    mode TINYINT UNSIGNED NOT NULL COMMENT '유형',
    product_name VARCHAR(50) NOT NULL COMMENT 'OS',
    vender VARCHAR(50) NOT NULL COMMENT '브랜드',
    `name` VARCHAR(50) NOT NULL COMMENT '요청 출처',
    `type` VARCHAR(50) NULL COMMENT '타입',
    memo VARCHAR(100) NULL COMMENT '메모',
    last_update TIMESTAMP NOT NULL COMMENT '마지막 수정 일시',
    create_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP() COMMENT '등록일시',
    UNIQUE KEY uq_user_source (record_key, mode)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT '출처'
;

-- 	DROP TABLE `kb_healthcare`.`record`;
CREATE TABLE IF NOT EXISTS `kb_healthcare`.`record` (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '기록 시퀀스',
    source_id BIGINT UNSIGNED NOT NULL COMMENT '출처 시퀀스',
    steps INT UNSIGNED NOT NULL COMMENT '걸음수',
    period_from TIMESTAMP NOT NULL COMMENT '부터(기간)',
    period_to TIMESTAMP NOT NULL COMMENT '까지(기간)',
    distance_value FLOAT NOT NULL COMMENT '거리',
    distance_unit CHAR(2) NOT NULL COMMENT '거리 단위',
    calories_value FLOAT NOT NULL COMMENT '칼로리',
    calories_unit CHAR(4) NOT NULL COMMENT '칼로리 단위',
    create_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP() COMMENT '등록일시',
    UNIQUE KEY uq_user_period (source_id, period_from, period_to)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT '기록'
;

-- 	DROP TABLE `kb_healthcare`.`record_fail`;
CREATE TABLE IF NOT EXISTS `kb_healthcare`.`record_fail`(
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '기록실패 시퀀스',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '회원 시퀀스',
    record_key VARCHAR(100) NOT NULL COMMENT '사용자 구분 키',
    source_id BIGINT UNSIGNED NOT NULL COMMENT '출처 시퀀스',
    entry_json JSON NOT NULL COMMENT '건강 기록 실패 데이터',
    retry_count TINYINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '재시도 회수',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '상태',
    create_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() COMMENT '등록일시',
    update_datetime TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP() COMMENT '수정일시',
    INDEX idx_status_retry (status, retry_count)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT '기록실패'
;