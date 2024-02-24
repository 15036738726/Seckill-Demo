/*
 Navicat Premium Data Transfer

 Source Server         : Test
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : localhost:3306
 Source Schema         : seckill_test

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 24/02/2024 21:11:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_id` int(0) NULL DEFAULT NULL COMMENT '商品ID',
  `goods_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品名称',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '现价',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细描述',
  `status` int(0) NULL DEFAULT NULL COMMENT '默认是1，表示正常状态, -1表示删除, 0下架',
  `total_stocks` int(0) NULL DEFAULT NULL COMMENT '总库存',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  `spike` int(0) NULL DEFAULT NULL COMMENT '是否参与秒杀1是0否',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, 18, '小米12s', 4999.00, 'xxxxxx', 1, 1000, '2023-02-23 11:35:56', '2024-02-24 21:09:54', 1);
INSERT INTO `goods` VALUES (2, 59, '华为mate50', 6999.00, 'xxxxxx', 1, 10, '2023-02-23 11:35:56', '2023-02-23 11:35:56', 1);
INSERT INTO `goods` VALUES (3, 66, '锤子pro2', 1999.00, 'xxxxxx', 1, 100, '2023-02-23 11:35:56', '2023-02-23 11:35:56', 0);
INSERT INTO `goods` VALUES (4, 78, '理想汽车', 30000.00, 'xxxx', 1, 50, '2024-02-24 12:33:09', '2024-02-24 12:33:13', 0);

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `userid` int(0) NULL DEFAULT NULL COMMENT '用户ID',
  `goodsid` int(0) NULL DEFAULT NULL COMMENT '商品ID',
  `createtime` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
