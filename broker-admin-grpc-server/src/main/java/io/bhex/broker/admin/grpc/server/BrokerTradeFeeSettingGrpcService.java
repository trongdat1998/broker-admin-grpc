package io.bhex.broker.admin.grpc.server;

import io.bhex.base.admin.common.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.broker.admin.model.BrokerTradeFeeRate;
import io.bhex.broker.admin.service.BrokerTradeFeeRateService;

import io.grpc.stub.StreamObserver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;


/**
 * @Description:券商对交易所下面的币对交易费的设置
 * @Date: 2018/9/5 下午3:25
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@GrpcService
public class BrokerTradeFeeSettingGrpcService extends BrokerTradeFeeSettingServiceGrpc.BrokerTradeFeeSettingServiceImplBase {

    @Autowired
    private BrokerTradeFeeRateService feeRateService;

    @Override
    public void updateBrokerTradeFee(UpdateBrokerTradeFeeRequest request, StreamObserver<UpdateBrokerTradeFeeReply> responseObserver) {
        UpdateBrokerTradeFeeReply.Builder builder = UpdateBrokerTradeFeeReply.newBuilder();
        BrokerTradeFeeRate setting = new BrokerTradeFeeRate();
        BeanUtils.copyProperties(request, setting);
        setting.setMakerFeeRate(new BigDecimal(request.getMakerFeeRate()));
        setting.setMakerRewardToTakerRate(new BigDecimal(request.getMakerRewardToTakerRate()));
        setting.setTakerFeeRate(new BigDecimal(request.getTakerFeeRate()));
        setting.setTakerRewardToMakerRate(new BigDecimal(request.getTakerRewardToMakerRate()));
        feeRateService.updateTradeFeeSetting(setting);

        builder.setResult(true);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getLatestBrokerTradeFee(GetLatestBrokerTradeFeeRequest request, StreamObserver<BrokerTradeFeeRateReply> responseObserver) {
        BrokerTradeFeeRateReply.Builder builder = BrokerTradeFeeRateReply.newBuilder();
        BrokerTradeFeeRate setting = feeRateService.getLatestTradeFeeSetting(request.getBrokerId(), request.getExchangeId(), request.getSymbolId());
        if(setting != null){
            BeanUtils.copyProperties(setting, builder);
            builder.setMakerFeeRate(setting.getMakerFeeRate().toString());
            builder.setMakerRewardToTakerRate(setting.getMakerRewardToTakerRate().toString());
            builder.setTakerFeeRate(setting.getTakerFeeRate().toString());
            builder.setTakerRewardToMakerRate(setting.getTakerRewardToMakerRate().toString());
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

    }

//    @Override
//    public void getMinTradeFeeRate(GetMinTradeFeeRateRequest request, StreamObserver<GetMinTradeFeeRateReply> responseObserver) {
//        BigDecimal minMakerFeeRate = feeRateService.getMinMakerFeeRate(request.getExchangeId(), request.getSymoblId());
//        BigDecimal minTakerFeeRate = feeRateService.getMinTakerFeeRate(request.getExchangeId(), request.getSymoblId());
//        GetMinTradeFeeRateReply reply = GetMinTradeFeeRateReply.newBuilder()
//                .setMinMakerFeeRate(minMakerFeeRate.toString())
//                .setMinTakerFeeRate(minTakerFeeRate.toString())
//                .build();
//        responseObserver.onNext(reply);
//        responseObserver.onCompleted();
//
//    }
}
