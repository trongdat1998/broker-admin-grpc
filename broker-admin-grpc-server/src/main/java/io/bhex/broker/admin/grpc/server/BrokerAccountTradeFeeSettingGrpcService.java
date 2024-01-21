package io.bhex.broker.admin.grpc.server;

import io.bhex.base.admin.common.*;
import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.broker.admin.model.BrokerAccountTradeFeeDetail;
import io.bhex.broker.admin.model.BrokerAccountTradeFeeGroup;
import io.bhex.broker.admin.service.BrokerAccountTradeFeeRateService;
import io.grpc.stub.StreamObserver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/11/21 下午4:56
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */

@GrpcService
public class BrokerAccountTradeFeeSettingGrpcService
        extends BrokerAccountTradeFeeSettingServiceGrpc.BrokerAccountTradeFeeSettingServiceImplBase {

    @Autowired
    private BrokerAccountTradeFeeRateService feeService;

    @Override
    public void editBrokerAccountTradeFeeGroup(EditBrokerAccountTradeFeeGroupRequest request,
                                                 StreamObserver<EditBrokerAccountTradeFeeGroupResponse> responseObserver) {
        EditBrokerAccountTradeFeeGroupResponse.Builder builder = EditBrokerAccountTradeFeeGroupResponse.newBuilder();
        io.bhex.base.admin.common.BrokerAccountTradeFeeGroup requestGroup = request.getBrokerAccountTradeFeeGroup();
        EditBrokerAccountTradeFeeGroupResponse.Result result = feeService.validateGroupName(requestGroup.getId(), requestGroup.getGroupName());
        if(!result.equals(EditBrokerAccountTradeFeeGroupResponse.Result.OK)){
            builder.setResult(result);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        List<Long> existedOtherGroupAccountIds = feeService
                .getExistedAccountIds(requestGroup.getBrokerId(), requestGroup.getId(), requestGroup.getAccountIdList());
        if(!CollectionUtils.isEmpty(existedOtherGroupAccountIds)){
            builder.setResult(EditBrokerAccountTradeFeeGroupResponse.Result.ACCOUT_ID_EXISTED);
            builder.addAllExistedInOtherGroupAccountId(existedOtherGroupAccountIds);
            responseObserver.onNext(builder.build());
            responseObserver.onCompleted();
            return;
        }

        BrokerAccountTradeFeeGroup group = new BrokerAccountTradeFeeGroup();
        BeanUtils.copyProperties(requestGroup, group);
        group.setMakerFeeRateAdjust(new BigDecimal(requestGroup.getMakerFeeRateAdjust()));
        group.setTakerRewardToMakerRateAdjust(new BigDecimal(requestGroup.getTakerRewardToMakerRateAdjust()));
        group.setTakerFeeRateAdjust(new BigDecimal(requestGroup.getTakerFeeRateAdjust()));

        BrokerAccountTradeFeeRateService.AccountIdsDTO accountIdsDTO = feeService.editBrokerAccountTradeFeeGroup(group, requestGroup.getAccountIdList(), true);

        builder.addAllAddedAccountId(accountIdsDTO.getAddedAccountIds());
        builder.addAllDeletedAccountId(accountIdsDTO.getDeletedAccountIds());
        builder.addAllModifiedAccountId(accountIdsDTO.getModifiedAccountIds());
        builder.setResult(EditBrokerAccountTradeFeeGroupResponse.Result.OK);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getBrokerAccountTradeFeeGroups(GetBrokerAccountTradeFeeGroupsRequest request,
                                               StreamObserver<GetBrokerAccountTradeFeeGroupsResponse> responseObserver) {
        List<BrokerAccountTradeFeeGroup> list = feeService.getBrokerAccountTradeFeeGroups(request.getBrokerId());


        GetBrokerAccountTradeFeeGroupsResponse.Builder builder = GetBrokerAccountTradeFeeGroupsResponse.newBuilder();
        if(!CollectionUtils.isEmpty(list)){
            List<io.bhex.base.admin.common.BrokerAccountTradeFeeGroup> resList = list.stream().map(group -> {
                io.bhex.base.admin.common.BrokerAccountTradeFeeGroup.Builder groupBuilder = io.bhex.base.admin.common.BrokerAccountTradeFeeGroup.newBuilder();
                BeanUtils.copyProperties(group, groupBuilder);
                groupBuilder.setCreatedAt(group.getCreatedAt().getTime());
                groupBuilder.setMakerFeeRateAdjust(group.getMakerFeeRateAdjust().toPlainString());
                groupBuilder.setTakerFeeRateAdjust(group.getTakerFeeRateAdjust().toPlainString());
                groupBuilder.setTakerRewardToMakerRateAdjust(group.getTakerRewardToMakerRateAdjust().toPlainString());
                return groupBuilder.build();
            }).collect(Collectors.toList());
            builder.addAllBrokerAccountTradeFeeGroup(resList);
        }

        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

//    @Override
//    public void getExistedAccountIds(GetExistedAccountIdsRequest request,
//                                     StreamObserver<GetExistedAccountIdsResponse> responseObserver) {
//        GetExistedAccountIdsResponse.Builder builder = GetExistedAccountIdsResponse.newBuilder();
//        List<Long> existedIds = feeService.getExistedAccountIds(request.getBrokerId(),0L, request.getAccountIdList());
//        builder.addAllAccountId(existedIds);
//        responseObserver.onNext(builder.build());
//        responseObserver.onCompleted();
//    }

    @Override
    public void saveBrokerAccountTradeFeeDetails(SaveBrokerAccountTradeFeeDetailsRequest request,
                                                 StreamObserver<SaveBrokerAccountTradeFeeDetailsResponse> responseObserver) {
        feeService.saveBrokerAccountTradeFeeDetails(request.getBrokerId(), request.getGroupId(),
                request.getAccountIdList(), request.getStatus());
        SaveBrokerAccountTradeFeeDetailsResponse.Builder builder = SaveBrokerAccountTradeFeeDetailsResponse.newBuilder();

        builder.setResult(true);
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getBrokerAccountTradeFeeGroup(GetBrokerAccountTradeFeeGroupRequest request, StreamObserver<GetBrokerAccountTradeFeeGroupResponse> responseObserver) {
        GetBrokerAccountTradeFeeGroupResponse.Builder builder = GetBrokerAccountTradeFeeGroupResponse.newBuilder();

        io.bhex.base.admin.common.BrokerAccountTradeFeeGroup.Builder groupBuilder = io.bhex.base.admin.common.BrokerAccountTradeFeeGroup.newBuilder();
        BrokerAccountTradeFeeGroup group = feeService.getBrokerAccountTradeFeeGroup(request.getBrokerId(), request.getGroupId());
        if(group != null){
            BeanUtils.copyProperties(group, groupBuilder);
            groupBuilder.setCreatedAt(group.getCreatedAt().getTime());
            groupBuilder.setMakerFeeRateAdjust(group.getMakerFeeRateAdjust().toPlainString());
            groupBuilder.setTakerFeeRateAdjust(group.getTakerFeeRateAdjust().toPlainString());
            groupBuilder.setTakerRewardToMakerRateAdjust(group.getTakerRewardToMakerRateAdjust().toPlainString());
        }

        List<BrokerAccountTradeFeeDetail> details = feeService.getBrokerAccountTradeFeeDetails(request.getBrokerId(), request.getGroupId());
        if(!CollectionUtils.isEmpty(details)){
            List<Long> accountIds = details.stream().map(detail -> detail.getAccountId()).collect(Collectors.toList());
            groupBuilder.addAllAccountId(accountIds);
        }
        builder.setBrokerAccountTradeFeeGroup(groupBuilder.build());
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();

    }

    @Override
    public void enableBrokerAccountTradeFeeGroup(EnableBrokerAccountTradeFeeGroupRequest request,
                                                 StreamObserver<EnableBrokerAccountTradeFeeGroupResponse> responseObserver) {
        io.bhex.base.admin.common.BrokerAccountTradeFeeGroup.Builder groupBuilder = io.bhex.base.admin.common.BrokerAccountTradeFeeGroup.newBuilder();
        BrokerAccountTradeFeeGroup group = feeService.getBrokerAccountTradeFeeGroup(request.getBrokerId(), request.getGroupId());
        if(group == null){
            responseObserver.onNext(EnableBrokerAccountTradeFeeGroupResponse.newBuilder().build());
            responseObserver.onCompleted();
            return;
        }
        BeanUtils.copyProperties(group, groupBuilder);
        groupBuilder.setCreatedAt(group.getCreatedAt().getTime());
        groupBuilder.setMakerFeeRateAdjust(group.getMakerFeeRateAdjust().toPlainString());
        groupBuilder.setTakerFeeRateAdjust(group.getTakerFeeRateAdjust().toPlainString());
        groupBuilder.setTakerRewardToMakerRateAdjust(group.getTakerRewardToMakerRateAdjust().toPlainString());

        List<Long> accountIds = feeService.enableBrokerAccountTradeFeeGroup(request.getBrokerId(), request.getGroupId());
        groupBuilder.addAllAccountId(accountIds);
        EnableBrokerAccountTradeFeeGroupResponse response = EnableBrokerAccountTradeFeeGroupResponse
                .newBuilder().setBrokerAccountTradeFeeGroup(groupBuilder.build()).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void disableBrokerAccountTradeFeeGroup(DisableBrokerAccountTradeFeeGroupRequest request,
                                                  StreamObserver<DisableBrokerAccountTradeFeeGroupResponse> responseObserver) {
        BrokerAccountTradeFeeGroup group = feeService.getBrokerAccountTradeFeeGroup(request.getBrokerId(), request.getGroupId());
        if(group == null){
            responseObserver.onNext(DisableBrokerAccountTradeFeeGroupResponse.newBuilder().build());
            responseObserver.onCompleted();
            return;
        }
        List<Long> accountIds = feeService.disableBrokerAccountTradeFeeGroup(request.getBrokerId(), request.getGroupId());
        DisableBrokerAccountTradeFeeGroupResponse response = DisableBrokerAccountTradeFeeGroupResponse
                .newBuilder().addAllAccountId(accountIds).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateSendStatus(UpdateSendStatusRequest request, StreamObserver<UpdateSendStatusResponse> responseObserver) {
        feeService.updateSendStatus(request.getBrokerId(), request.getGroupId(), request.getAccountIdList(),
                1, request.getOpType());
        UpdateSendStatusResponse response = UpdateSendStatusResponse
                .newBuilder().setResult(true).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
