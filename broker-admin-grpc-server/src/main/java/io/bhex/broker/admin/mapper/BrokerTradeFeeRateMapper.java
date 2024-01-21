package io.bhex.broker.admin.mapper;


import io.bhex.broker.admin.model.BrokerTradeFeeRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @Description:
 * @Date: 2018/9/28 下午3:28
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Mapper
@Component
public interface BrokerTradeFeeRateMapper extends tk.mybatis.mapper.common.Mapper<BrokerTradeFeeRate> {


    @Update("update tb_broker_trade_fee_rate set action_time = #{actionTime},   " +
            " maker_fee_rate = #{makerFeeRate}, maker_reward_to_taker_rate = #{makerRewardToTakerRate}," +
            "taker_fee_rate = #{takerFeeRate}, taker_reward_to_maker_rate = #{takerRewardToMakerRate}," +
            " updated_at = #{updatedAt} where id = #{id} and deleted = 0")
    int updateExchangeTradeFeeRate(BrokerTradeFeeRate setting);

    @Select("select * from tb_broker_trade_fee_rate where broker_id = #{brokerId} and exchange_id = #{exchangeId} " +
            " and symbol_id = #{symbolId} and deleted = 0 order by action_time desc limit 1")
    BrokerTradeFeeRate getLatestSetting(@Param("brokerId") Long brokerId, @Param("exchangeId") Long exchangeId, @Param("symbolId") String symbolId);

}
