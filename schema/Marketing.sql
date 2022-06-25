/*
 Navicat Premium Data Transfer

 Source Server         : smartMarketing
 Source Server Type    : MySQL
 Source Server Version : 80029
 Source Host           : 106.15.126.24:3306
 Source Schema         : marketing

 Target Server Type    : MySQL
 Target Server Version : 80029
 File Encoding         : 65001

 Date: 23/06/2022 03:22:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for advertisement
-- ----------------------------
DROP TABLE IF EXISTS `advertisement`;
CREATE TABLE `advertisement`
(
    `id`    int                                                    NOT NULL AUTO_INCREMENT,
    `name`  varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '广告名称',
    `url`   varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '广告url',
    `price` double(10, 2)                                          NULL DEFAULT NULL COMMENT '广告报价',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of advertisement
-- ----------------------------
INSERT INTO `advertisement`
VALUES (1, '汽车广告1', 'www.car1.com', 11.00);
INSERT INTO `advertisement`
VALUES (2, '汽车广告2', 'www.car2.com', 44.00);

-- ----------------------------
-- Table structure for advertisement_label
-- ----------------------------
DROP TABLE IF EXISTS `advertisement_label`;
CREATE TABLE `advertisement_label`
(
    `advertisementId` int NOT NULL COMMENT '广告id',
    `labelId`         int NULL DEFAULT NULL COMMENT '标签id',
    PRIMARY KEY (`advertisementId`) USING BTREE
) ENGINE = InnoDB
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of advertisement_label
-- ----------------------------
INSERT INTO `advertisement_label`
VALUES (1, 1);
INSERT INTO `advertisement_label`
VALUES (2, 1);

-- ----------------------------
-- Table structure for label
-- ----------------------------
DROP TABLE IF EXISTS `label`;
CREATE TABLE `label`
(
    `id`   int                                                    NOT NULL AUTO_INCREMENT,
    `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '标签名称',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 2
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of label
-- ----------------------------
INSERT INTO `label`
VALUES (1, 'car');

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for black_list
-- ----------------------------

CREATE TABLE black_list
(
    userid CHAR(1) PRIMARY KEY
);


-- ----------------------------
-- Table structure for user_ad_count
-- ----------------------------

CREATE TABLE user_ad_count
(
    dt     varchar(255),
    userid CHAR(1),
    adid   CHAR(1),
    count  BIGINT,
    PRIMARY KEY (dt, userid, adid)
);

-- ----------------------------
-- Table structure for area_city_ad_count
-- ----------------------------
CREATE TABLE area_city_ad_count
(
    dt    VARCHAR(20),
    area  VARCHAR(100),
    city  VARCHAR(100),
    adid  VARCHAR(20),
    count BIGINT,
    PRIMARY KEY (dt, area, city, adid)
);
