package io.bhex.broker.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tb_broker_trade_fee_rate")
public class BrokerTradeFeeRate {

    @Id
    private Long id;

    private Long brokerId;

    private Long exchangeId;

    private Integer securityType;

    private String symbolId;

    private int level;

   // private String baseTokenId;

   // private String quoteToeknId;

    private BigDecimal makerFeeRate;
    private BigDecimal makerRewardToTakerRate;
    private BigDecimal takerFeeRate;
    private BigDecimal takerRewardToMakerRate;


    /**
     * 生效时间
     */
    private Timestamp actionTime;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    /**
     * 0-未删除 1-已删除
     */
    private int deleted;

}
