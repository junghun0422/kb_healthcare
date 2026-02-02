# DATABASE 생성
CREATE DATABASE kb_healthcare;

# 회원 테이블 생성
-- DROP TABLE `kb_healthcare`.`user`;
CREATE TABLE IF NOT EXISTS `kb_healthcare`.`user` (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '회원 시퀀스',
    `name` VARCHAR(50) NOT NULL COMMENT '이름',
    nick_name VARCHAR(50) NOT NULL COMMENT '닉네임',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '이메일',
    password VARCHAR(255) NOT NULL COMMENT '패스워드',
    create_datetime TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP() COMMENT '가입일시',
    update_datetime TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP() COMMENT '수정일시'
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT '회원'
;

# 회원 고유키 관리
-- DROP TABLE `kb_healthcare`.`user_identifier`;
CREATE TABLE IF NOT EXISTS `kb_healthcare`.`user_identifier` (
    id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT COMMENT '회원 고유키 관리 시퀀스',
    user_id BIGINT UNSIGNED NOT NULL COMMENT '회원 시퀀스',
    record_key VARCHAR(100) NOT NULL UNIQUE COMMENT '사용자 구분 키',
    create_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP() COMMENT '등록일시'
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT '회원 고유키 관리'
;

# 회원 역할
-- DROP TABLE `kb_healthcare`.`user_roles`;
CREATE TABLE IF NOT EXISTS `kb_healthcare`.`user_roles` (
    user_id BIGINT UNSIGNED NOT NULL COMMENT '회원 시퀀스',
    role VARCHAR(50) NOT NULL COMMENT '역할 (USER, ADMIN 등)',
    PRIMARY KEY (`user_id`, `role`)
) DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT='회원 역할'
;
