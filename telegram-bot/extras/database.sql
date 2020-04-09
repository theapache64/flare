DROP DATABASE IF EXISTS `flare`;
CREATE DATABASE `flare`;
USE `flare`;
CREATE TABLE users(
    id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
    tgm_id INT NOT NULL UNIQUE,
    group_name VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT  CURRENT_TIMESTAMP
);