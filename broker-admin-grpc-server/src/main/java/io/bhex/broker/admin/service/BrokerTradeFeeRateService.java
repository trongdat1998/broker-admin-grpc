package io.bhex.broker.admin.service;

import io.bhex.base.admin.common.EditBrokerAccountTradeFeeGroupResponse;
import io.bhex.broker.admin.model.BrokerAccountTradeFeeDetail;
import io.bhex.broker.admin.model.BrokerAccountTradeFeeGroup;
import io.bhex.broker.admin.model.BrokerTradeFeeRate;

import java.util.List;

/**
 * @Description:
 * @Date: 2018/9/28 下午3:50
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
public interface BrokerTradeFeeRateService {


    boolean updateTradeFeeSetting(BrokerTradeFeeRate tradeFeeRate);

    BrokerTradeFeeRate getLatestTradeFeeSetting(Long brokerId, Long exchangeId, String symbolId);


}
