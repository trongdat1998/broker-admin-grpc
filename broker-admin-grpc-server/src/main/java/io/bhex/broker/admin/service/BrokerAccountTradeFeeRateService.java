package io.bhex.broker.admin.service;

import io.bhex.base.admin.common.EditBrokerAccountTradeFeeGroupResponse;
import io.bhex.base.admin.common.UpdateSendStatusRequest;
import io.bhex.broker.admin.model.BrokerAccountTradeFeeDetail;
import io.bhex.broker.admin.model.BrokerAccountTradeFeeGroup;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @Date: 2018/11/22 上午11:25
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public interface BrokerAccountTradeFeeRateService {

    @Data
    class AccountIdsDTO{
        List<Long> deletedAccountIds = new ArrayList<>();
        List<Long> modifiedAccountIds = new ArrayList<>();
        List<Long> addedAccountIds = new ArrayList<>();
    }

    AccountIdsDTO editBrokerAccountTradeFeeGroup(BrokerAccountTradeFeeGroup group, List<Long> accountIds, boolean enable);

    List<BrokerAccountTradeFeeGroup> getBrokerAccountTradeFeeGroups(Long brokerId);

    BrokerAccountTradeFeeGroup getBrokerAccountTradeFeeGroup(Long brokerId, Long groupId);

    boolean saveBrokerAccountTradeFeeDetails(Long brokerId, Long groupId, List<Long> accountIds, int status);

    List<BrokerAccountTradeFeeDetail> getBrokerAccountTradeFeeDetails(Long brokerId, Long groupId);

    List<Long> getExistedAccountIds(Long brokerId, Long groupId, List<Long> accountIds);

    EditBrokerAccountTradeFeeGroupResponse.Result validateGroupName(Long groupId, String groupName);

    List<Long> enableBrokerAccountTradeFeeGroup(Long brokerId, Long groupId);

    List<Long> disableBrokerAccountTradeFeeGroup(Long brokerId, Long groupId);

    void updateSendStatus(Long brokerId, Long groupId, List<Long> accountIds, int sendStatus, UpdateSendStatusRequest.OpType opType);
}
