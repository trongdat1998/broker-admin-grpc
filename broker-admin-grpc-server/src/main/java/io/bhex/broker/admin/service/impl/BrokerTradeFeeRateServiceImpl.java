package io.bhex.broker.admin.service.impl;

import io.bhex.base.admin.common.EditBrokerAccountTradeFeeGroupResponse;
import io.bhex.broker.admin.mapper.BrokerAccountTradeFeeDetailMapper;
import io.bhex.broker.admin.mapper.BrokerAccountTradeFeeGroupMapper;
import io.bhex.broker.admin.mapper.BrokerTradeFeeRateMapper;
import io.bhex.broker.admin.model.BrokerAccountTradeFeeDetail;
import io.bhex.broker.admin.model.BrokerAccountTradeFeeGroup;
import io.bhex.broker.admin.model.BrokerTradeFeeRate;
import io.bhex.broker.admin.service.BrokerTradeFeeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/10/31 下午3:41
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Service
public class BrokerTradeFeeRateServiceImpl implements BrokerTradeFeeRateService{

    @Autowired
    private BrokerTradeFeeRateMapper brokerTradeFeeRateMapper;

    @Override
    public boolean updateTradeFeeSetting(BrokerTradeFeeRate tradeFeeRate){

        tradeFeeRate.setSecurityType(0);

        tradeFeeRate.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        tradeFeeRate.setDeleted(0);

        BrokerTradeFeeRate setting = brokerTradeFeeRateMapper.getLatestSetting(tradeFeeRate.getBrokerId(),
                tradeFeeRate.getExchangeId(), tradeFeeRate.getSymbolId());

        //如果没有记录则新加一条默认的数据
        if(setting == null){
            tradeFeeRate.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            tradeFeeRate.setActionTime(new Timestamp(System.currentTimeMillis()));
            brokerTradeFeeRateMapper.insert(tradeFeeRate);
            return true;
        }


        tradeFeeRate.setId(setting.getId());
        tradeFeeRate.setActionTime(new Timestamp(System.currentTimeMillis()));
        brokerTradeFeeRateMapper.updateExchangeTradeFeeRate(tradeFeeRate);
        return true;
    }

    @Override
    public BrokerTradeFeeRate getLatestTradeFeeSetting(Long brokerId, Long exchangeId, String symbolId){

        return brokerTradeFeeRateMapper.getLatestSetting(brokerId, exchangeId, symbolId);
    }



}
