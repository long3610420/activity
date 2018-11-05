drop table if exists `t_account`;
drop table if exists `t_account_action`;
drop table if exists `t_subaccount`;
drop table if exists `t_clearing_subaccount`;
drop table if exists `t_subaccount_transactions`;
drop table if exists `t_subaccount_transactions_aggregation`;

create table `t_account` (
	f_id bigint unsigned auto_increment comment '主键id',
	f_version bigint unsigned not null default 0 comment '乐观锁数据版本',
	f_user_id bigint unsigned not null comment '关联的用户id',
	f_type tinyint unsigned not null comment '账户类型：1、系统账户 2、现货账户 3、借贷账户',
	f_state tinyint unsigned not null comment '账户状态：1、正常 2、锁定',
	f_created_at bigint unsigned not null default 0 comment '记录账户创建时间',
	f_updated_at bigint unsigned not null default 0 comment '记录账户更新时间',
	PRIMARY KEY ( f_id ),
	UNIQUE KEY uniq_userid_type ( f_user_id, f_type )
) ENGINE=InnoDB auto_increment = 100000 DEFAULT CHARSET=utf8 COMMENT='账户表';

create table `t_account_action` (
	f_id bigint unsigned auto_increment comment '主键id',
	f_version bigint unsigned not null default 0 comment '乐观锁数据版本',
	f_action varchar(15) not null comment '业务动作：冻结 frozen、解冻 unfrozen、系统充值 systemin、取消系统充值 systemout、转账 transfer',
	f_user_id bigint unsigned not null comment '关联的用户id',
	f_account_id bigint unsigned not null comment '关联的账户id',
	f_to_account_id bigint unsigned not null default 0 comment '关联的转入账户id',
	f_order_id bigint unsigned not null default 0 comment '关联的订单id',
	f_currency varchar(10) not null comment '币种类型：cny、eth、etc',
	f_amount decimal(36,10) unsigned not null comment '发生金额',
	f_source_type varchar(50) not null comment '业务发生类型',
	f_source_id bigint not null comment '业务发生原始单据ID',
	f_source_ext varchar(100) not null default '' comment '业务扩展',
	f_state tinyint unsigned not null default 1 comment '账户状态：1、未生成流水 2、已生成流水',
	f_created_at bigint unsigned not null default 0 comment '记录业务创建时间',
	f_updated_at bigint unsigned not null default 0 comment '记录业务更新时间',
	PRIMARY KEY ( f_id ),
	UNIQUE KEY uniq_srcid_srctype_curr ( f_source_id, f_source_type, f_currency )
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='业务动作表';

create table `t_subaccount` (
	f_id bigint unsigned auto_increment comment '主键id',
	f_version bigint unsigned not null default 0 comment '乐观锁数据版本',
	f_user_id bigint unsigned not null comment '关联的用户id',
	f_account_id bigint unsigned not null comment '关联的账户id',
	f_currency varchar(10) not null comment '币种类型：cny、eth、etc',
	f_type tinyint unsigned not null comment '子账户类型：1、系统子账户 2、交易子账户 3、冻结子账户 4、借贷子帐户',
	f_balance decimal(36,10) not null default 0 comment '子账户余额',
	f_created_at bigint unsigned not null default 0 comment '记录子账户创建时间',
	f_updated_at bigint unsigned not null default 0 comment '记录子账户更新时间',
	PRIMARY KEY ( f_id ),
	UNIQUE KEY uniq_accountid_currency_type_orderid ( f_account_id, f_currency, f_type )
) ENGINE=InnoDB auto_increment = 100000 DEFAULT CHARSET=utf8 COMMENT='子账户表';

create table `t_clearing_subaccount` (
	f_id bigint unsigned auto_increment comment '主键id',
	f_version bigint unsigned not null default 0 comment '乐观锁数据版本',
	f_user_id bigint unsigned not null comment '关联的用户id',
	f_account_id bigint unsigned not null comment '关联的账户id',
	f_order_id bigint unsigned not null comment '关联的账户订单id',
	f_currency varchar(10) not null comment '币种类型：cny、eth、etc',
	f_type tinyint unsigned not null comment '子账户类型：5、清算子账户',
	f_balance decimal(36,10) not null default 0 comment '子账户余额',
	f_created_at bigint unsigned not null default 0 comment '记录子账户创建时间',
	f_updated_at bigint unsigned not null default 0 comment '记录子账户更新时间',
	PRIMARY KEY ( f_id ),
	UNIQUE KEY uniq_orderid_currency_type ( f_order_id, f_currency, f_type  )
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='清算子账户表';

create table `t_subaccount_transactions` (
	f_id bigint unsigned auto_increment comment '主键id',
	f_version bigint unsigned not null default 0 comment '乐观锁数据版本',
	f_action_id bigint unsigned not null comment '关联的账户业务行为id',
	f_user_id bigint unsigned not null comment '关联的用户id',
	f_account_id bigint unsigned not null comment '关联的账户id',
	f_subaccount_id bigint unsigned not null comment '关联的子账户id',
	f_amount decimal(36,10) not null comment '变动金额：正值为增加、负值为减少',
	f_currency varchar(10) not null comment '币种类型：cny、eth、etc',
	f_type tinyint unsigned not null comment '子账户类型：1、系统子账户 2、交易子账户 3、冻结子账户 4、借贷子帐户 5、清算子账户',
	f_balance decimal(36,10) not null comment '账户余额（变动后）',
	f_source_type varchar(50) not null comment '业务发生类型',
	f_source_type_code varchar(20) not null comment '业务发生类型code',
	f_source_id bigint not null comment '业务发生原始单据ID',
	f_source_ext varchar(100) not null default '' comment '业务扩展',
	f_created_at bigint unsigned not null default 0 comment '记录业务流水创建时间',
	f_updated_at bigint unsigned not null default 0 comment '记录业务流水更新时间',
	PRIMARY KEY ( f_id )
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='账户流水表';

alter table t_clearing_subaccount add index idx_user_id(f_user_id);

CREATE TABLE t_subaccount_transactions_aggregation (
  f_id bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键id',
  f_version bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '乐观锁数据版本',
  f_user_id bigint(20) unsigned NOT NULL COMMENT '关联的用户id',
  f_account_id bigint(20) unsigned NOT NULL COMMENT '关联的账户id',
  f_subaccount_id bigint(20) unsigned NOT NULL COMMENT '关联的子账户id',
  f_currency varchar(10) NOT NULL COMMENT '币种类型：cny、eth、etc',
  f_type tinyint(3) unsigned NOT NULL COMMENT '子账户类型：1、系统子账户 2、交易子账户 3、冻结子账户 4、借贷子帐户 5、清算子账户',
  f_action_key varchar(10) NOT NULL COMMENT '本次聚合操作的id',
  f_amount_sum decimal(36,10) NOT NULL COMMENT '本次聚合累计的金额',
  f_amount_abs decimal(36,10) NOT NULL COMMENT '本次聚合累计的绝对金额',
  f_trans_count int(10) unsigned NOT NULL COMMENT '本次聚合累计的记录条数',
  f_max_id bigint(20) unsigned NOT NULL COMMENT '本次聚合的最大记录id',
  f_last_id bigint(20) unsigned NOT NULL COMMENT '用户所有子账户，上一次聚合的最大记录id',
  f_start_time bigint(20) unsigned NOT NULL COMMENT '本次聚合操作第一条记录创建时间',
  f_end_time bigint(20) unsigned NOT NULL COMMENT '本次聚合操作最后一条记录创建时间',
  f_created_at bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '聚合记录创建时间',
  f_updated_at bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT '聚合记录更新时间',
  PRIMARY KEY (f_id),
  UNIQUE KEY uniq_subaccount_lastid (f_account_id, f_currency, f_type, f_last_id),
  KEY idx_user_id (f_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='账户流水聚合表';
