package io.bhex.broker.admin.model;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @Description:
 * @Date: 2018/11/21 下午3:42
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Data
@Table(name = "tb_broker_account_trade_fee_group")
public class BrokerAccountTradeFeeGroup {
    @Id
    @GeneratedValue(generator = "JDBC")
    private Long id;//序号

    private Long brokerId;//券商id

    private String groupName;

    private Long accountCount;

    //maker折扣比例
    private BigDecimal makerFeeRateAdjust;

    //taker折扣比例
    private BigDecimal takerFeeRateAdjust;

    //maker奖励分成
    private BigDecimal takerRewardToMakerRateAdjust;

    private Integer status;//状态

    private Timestamp createdAt;//用户注册时间

    private Timestamp updatedAt;//用户注册时间

}
