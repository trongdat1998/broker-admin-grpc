package io.bhex.broker.admin.mapper;


import io.bhex.broker.admin.model.BrokerAccountTradeFeeDetail;
import io.bhex.broker.admin.model.BrokerTradeFeeRate;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/9/28 下午3:28
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@Mapper
@Component
public interface BrokerAccountTradeFeeDetailMapper extends tk.mybatis.mapper.common.Mapper<BrokerAccountTradeFeeDetail> {

    @Select("select * from tb_broker_account_trade_fee_detail where broker_id = #{brokerId} and account_id = #{accountId} and deleted = 0 limit 1")
    BrokerAccountTradeFeeDetail getBrokerAccountTradeFeeDetail(@Param("brokerId") Long brokerId, @Param("accountId") Long accountId);

    // @Select("select account_id from tb_broker_account_trade_fee_detail where broker_id = #{brokerId} and account_id = #{accountId} and deleted = 0 limit 1")
    @SelectProvider(type = Provider.class, method="getExistedAccountIds")
    List<Long> getExistedAccountIds(@Param("brokerId") Long brokerId, @Param("accountIds") List<Long> accountIds);

    @Select("select * from tb_broker_account_trade_fee_detail " +
            "where broker_id = #{brokerId} and group_id = #{groupId} and deleted = 0")
    List<BrokerAccountTradeFeeDetail> getBrokerAccountTradeFeeDetails(@Param("brokerId") Long brokerId,
                                                                      @Param("groupId") Long groupId);


    @Select("select account_id from tb_broker_account_trade_fee_detail " +
            "where broker_id = #{brokerId} and group_id = #{groupId} and send_status = 0 and deleted = 0")
    List<Long> getUnSendedAccountIds(@Param("brokerId") Long brokerId, @Param("groupId") Long groupId);

    @Update("update tb_broker_account_trade_fee_detail set status = #{status} where broker_id = #{brokerId} and group_id = #{groupId} and deleted = 0")
    int updateStatus(@Param("brokerId") Long brokerId, @Param("groupId") Long groupId,  @Param("status") Integer status);

    @UpdateProvider(type = Provider.class, method="updateSendStatus")
    int updateSendStatus(@Param("brokerId") Long brokerId, @Param("groupId") Long groupId, @Param("accountIds") List<Long> accountIds, @Param("sendStatus") Integer sendStatus);

    @Update("update tb_broker_account_trade_fee_detail set send_status = #{sendStatus} where broker_id = #{brokerId} and group_id = #{groupId} and deleted = 0")
    int updateSendStatusByGroup(@Param("brokerId") Long brokerId, @Param("groupId") Long groupId, @Param("sendStatus") Integer sendStatus);

    @UpdateProvider(type = Provider.class, method="deleteAccountIds")
    int deleteAccountIds(@Param("brokerId") Long brokerId, @Param("groupId") Long groupId,  @Param("accountIds") List<Long> accountIds);


    class Provider {
        public String getExistedAccountIds(Map<String, Object> parameter) {
            List<Long> accountIds = (List<Long>) parameter.get("accountIds");
            List<String> strings = accountIds.stream().map(exchangeId -> exchangeId + "").collect(Collectors.toList());
            String insql = String.join(",", strings);
            return new SQL() {
                {
                    SELECT("*").
                    FROM("tb_broker_account_trade_fee_detail").
                    WHERE("broker_id = #{brokerId}")
                            .WHERE("deleted = 0")
                            .WHERE("account_id in (" + insql + ")");

                }
            }.toString();
        }

        public String updateSendStatus(Map<String, Object> parameter) {
            List<Long> accountIds = (List<Long>) parameter.get("accountIds");
            List<String> strings = accountIds.stream().map(exchangeId -> exchangeId + "").collect(Collectors.toList());
            String insql = String.join(",", strings);
            return new SQL() {
                {
                    UPDATE("tb_broker_account_trade_fee_detail")
                            .SET("send_status = #{sendStatus}");
                    WHERE("broker_id = #{brokerId}")
                            .WHERE("group_id = #{groupId}")
                            .WHERE("deleted = 0")
                            .WHERE("account_id in (" + insql + ")");

                }
            }.toString();
        }


        public String deleteAccountIds(Map<String, Object> parameter) {
            List<Long> accountIds = (List<Long>) parameter.get("accountIds");
            List<String> strings = accountIds.stream().map(exchangeId -> exchangeId + "").collect(Collectors.toList());
            String insql = String.join(",", strings);
            return new SQL() {
                {
                    DELETE_FROM("tb_broker_account_trade_fee_detail");
                    WHERE("broker_id = #{brokerId}")
                            .WHERE("group_id = #{groupId}")
                            .WHERE("deleted = 0")
                            .WHERE("account_id in (" + insql + ")");

                }
            }.toString();
        }
    }
}
