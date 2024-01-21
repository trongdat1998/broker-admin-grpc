package io.bhex.broker.admin.mapper;


import io.bhex.broker.admin.model.BrokerAccountTradeFeeDetail;
import io.bhex.broker.admin.model.BrokerAccountTradeFeeGroup;
import io.bhex.broker.admin.model.BrokerTradeFeeRate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description:
 * @Date: 2018/9/28 下午3:28
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Mapper
@Component
public interface BrokerAccountTradeFeeGroupMapper extends tk.mybatis.mapper.common.Mapper<BrokerAccountTradeFeeGroup> {

    @Select("select * from tb_broker_account_trade_fee_group where broker_id = #{brokerId} order by id desc")
    List<BrokerAccountTradeFeeGroup> getBrokerAccountTradeFeeGroups(@Param("brokerId") Long brokerId);

    @Select("select * from tb_broker_account_trade_fee_group where group_name = #{groupName}")
    BrokerAccountTradeFeeGroup countGroupName(@Param("groupName") String groupName);

    @Update("update tb_broker_account_trade_fee_group set status = #{status} where broker_id = #{brokerId} and id = #{id}")
    int updateStatus(@Param("brokerId") Long brokerId, @Param("id") Long id, @Param("status") Integer status);
}
