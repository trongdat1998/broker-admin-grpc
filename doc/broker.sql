CREATE TABLE `tb_admin_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `default_language` varchar(20) NOT NULL COMMENT '默认语言',
  `email` varchar(255) DEFAULT NULL COMMENT '邮箱地址',
  `org_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '交易平台所使用的id',
  `org_name` varchar(255) DEFAULT NULL COMMENT '交易所简称',
  `saas_org_id` bigint(20) NOT NULL COMMENT 'saas平台对应的id',
  `area_code` varchar(20) DEFAULT NULL COMMENT '手机区号',
  `telephone` varchar(50) DEFAULT NULL COMMENT '手机号码',
  `password` varchar(50) NOT NULL COMMENT '密码',
  `status` tinyint(4) NOT NULL COMMENT '状态值 暂时没使用',
  `created_at` timestamp(3) NOT NULL COMMENT '用户注册时间',
  `created_ip` varchar(20) DEFAULT NULL COMMENT '用户注册IP',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除: 1=删除 0=正常',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;

CREATE TABLE `tb_init_password_token` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'pk',
  `admin_user_id` bigint(20) NOT NULL COMMENT 'map to adminUserId',
  `token` varchar(255) NOT NULL COMMENT 'token',
  `created_at` timestamp(3) NOT NULL,
  `expired_at` timestamp NULL DEFAULT NULL,
  `validate_result` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;

CREATE TABLE `tb_broker_trade_fee_rate` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `exchange_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '交易所id',
  `broker_id` bigint(20) NOT NULL DEFAULT '0' COMMENT '是哪个券商设置的',
  `security_type` tinyint(4) NOT NULL DEFAULT '0' COMMENT '证券类型',
  `symbol_id` varchar(200) COLLATE utf8mb4_bin NOT NULL DEFAULT '0' COMMENT '证券类型',
  `level` tinyint(4) NOT NULL DEFAULT '0' COMMENT '级别',
  `maker_fee_rate` decimal(65,18) NOT NULL COMMENT 'maker_fee_rate',
  `maker_reward_to_taker_rate` decimal(65,18) NOT NULL COMMENT 'maker_reward_to_taker_rate',
  `taker_fee_rate` decimal(65,18) NOT NULL COMMENT 'taker_fee_rate',
  `taker_reward_to_maker_rate` decimal(65,18) NOT NULL COMMENT 'taker_reward_to_maker_rate',
  `action_time` date NOT NULL COMMENT '生效时间',
  `create_at` timestamp(3) NOT NULL COMMENT '创建时间',
  `update_at` timestamp(3) NOT NULL COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '0-未删除 1-已删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin COMMENT='交易所交易费率表';