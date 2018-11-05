drop table if exists `activity_user`;
drop table if exists `activity_first_charge`;
drop table if exists `activity_reward`;
drop table if exists `activity_config`;
drop table if exists `activity_deductible`;
drop table if exists `activity_snapshot`;
drop table if exists `activity_reward_lock`;
drop table if exists `activity_prize`;

CREATE TABLE `activity_user` (
  `f_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` bigint(20) unsigned NOT NULL COMMENT '用户id',
  `code` varchar(9) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '邀请码',
  `inviter_uid` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '邀请人id',
  `inviter_code` varchar(9) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '邀请人的邀请码',
  `is_recharge` tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '是否充值(0:未充值,1:已充值)',
  `is_reminded` tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '是否被提醒(0:未提醒,1:已提醒)',
  `phone_num` char(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '手机号',
  `reward` DECIMAL(36,18) NOT NULL DEFAULT '0' COMMENT '奖励数量',
  `f_version` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁数据版本',
  `f_created_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `f_updated_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`f_id`),
  UNIQUE KEY `idx_uid` (`uid`) USING BTREE COMMENT '用户id',
  KEY `idx_inv_uid` (`inviter_uid`) USING BTREE COMMENT '邀请人的用户id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='活动用户表';

CREATE TABLE `activity_first_charge` (
  `f_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` bigint(20) unsigned NOT NULL COMMENT '用户id',
  `activity_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '活动id',
  `recharge_date` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '充值时间',
  `currency` varchar(10) NOT NULL DEFAULT '' COMMENT '充值币种',
  `amount` DECIMAL(36,18)  NOT NULL DEFAULT '0' COMMENT '充值数量',
  `f_version` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁数据版本',
  `f_created_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `f_updated_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`f_id`),
  UNIQUE KEY `idx_uid` (`uid`) USING BTREE COMMENT '用户id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='活动首充表';

CREATE TABLE `activity_reward` (
  `f_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` bigint(20) unsigned NOT NULL COMMENT '用户id',
  `activity_id` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '活动id',
  `inviter_uid` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '邀请的用户id',
  `currency` varchar(10) NOT NULL DEFAULT '' COMMENT '奖励币种',
  `amount` DECIMAL(36,18)  NOT NULL DEFAULT '0' COMMENT '奖励数量',
  `reason` varchar(100) NOT NULL DEFAULT '' COMMENT '奖励原因',
  `reward_type` tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '奖励类型(0:充值奖励,1:邀请奖励,2:瓜分奖励)',
  `is_enabled` tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '是否有效(0:有效,1:失效)',
  `f_version` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁数据版本',
  `f_created_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `f_updated_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`f_id`),
  KEY `idx_uid` (`uid`) USING BTREE COMMENT '用户id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='活动奖励表';

CREATE TABLE `activity_config` (
  `f_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL DEFAULT '' COMMENT '活动名称',
  `summary` varchar(255) NOT NULL DEFAULT '' COMMENT '活动简介',
  `begin_date` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '活动开始时间',
  `end_date` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '活动结束时间',
  `status` tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '活动状态(0:正常,1:失效)',
  `rule` varchar(255) NOT NULL DEFAULT '' COMMENT '活动规则json',
  `f_version` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁数据版本',
  `f_created_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `f_updated_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`f_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='活动配置表';

CREATE TABLE `activity_deductible` (
  `f_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` bigint(20) unsigned NOT NULL COMMENT '用户id',
  `fee` bigint(10) NOT NULL DEFAULT '0' COMMENT '抵扣手续费',
  `fee_currency` varchar(10) NOT NULL DEFAULT '' COMMENT '抵扣币种',
  `fee_amount` DECIMAL(36,18)  NOT NULL DEFAULT '0' COMMENT '抵扣数量',
  `fee_date` bigint(10) NOT NULL DEFAULT '0' COMMENT '抵扣时间',
  `order_id` bigint(10) NOT NULL DEFAULT '0' COMMENT '抵扣交易订单id',
  `f_version` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁数据版本',
  `f_created_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `f_updated_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`f_id`),
  KEY `idx_uid` (`uid`) USING BTREE COMMENT '用户id',
  UNIQUE KEY `idx_order_id` (`order_id`) USING BTREE COMMENT '交易订单id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='活动抵扣表';

CREATE TABLE `activity_snapshot` (
  `f_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` bigint(20) unsigned NOT NULL COMMENT '用户id',
  `frequency` tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '快照次数',
  `recharge_date` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '快照时间',
  `amount` DECIMAL(36,18) unsigned NOT NULL DEFAULT '0' COMMENT '当前账户资产(单位usdt)',
  `reward` DECIMAL(36,18) unsigned NOT NULL DEFAULT '0' COMMENT '奖励数量(单位usdt)',
  `f_version` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁数据版本',
  `f_created_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `f_updated_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`f_id`),
  KEY `idx_uid` (`uid`) USING BTREE COMMENT '用户id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='活动快照表';

CREATE TABLE `activity_reward_lock` (
  `f_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` bigint(20) unsigned NOT NULL COMMENT '用户id',
  `start_date` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '锁仓开始时间',
  `end_date` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '解锁时间',
  `currency` varchar(10) NOT NULL DEFAULT '' COMMENT '充值币种',
  `amount` DECIMAL(50,30) unsigned NOT NULL DEFAULT '0' COMMENT '解锁数量',
  `state` tinyint(2) unsigned NOT NULL DEFAULT '0' COMMENT '状态(1:等待解锁,2:解锁成功,3:解锁失败)',
  `f_version` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁数据版本',
  `f_created_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `f_updated_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`f_id`),
  KEY `idx_uid` (`uid`) USING BTREE COMMENT '用户id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='锁仓记录表';

CREATE TABLE `activity_prize` (
  `f_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `uid` bigint(20) unsigned NOT NULL COMMENT '用户id',
  `f_type` tinyint(2)  NOT NULL DEFAULT '0' COMMENT '中奖类型(1:10万，2:1万,3:8百,4:1百)',
  `prize_date` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '中奖时间',
  `phone` varchar(100) NOT NULL DEFAULT '' COMMENT '手机号',
  `state` tinyint(2) DEFAULT '0' COMMENT '真假数据 0：假数据1：真数据',
  `f_version` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁数据版本',
  `f_created_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '创建时间',
  `f_updated_at` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '更新时间',
  PRIMARY KEY (`f_id`),
  UNIQUE KEY `idx_uid` (`uid`) USING BTREE COMMENT '用户id'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='中奖记录表';