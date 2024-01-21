package io.bhex.broker.admin.grpc.server;


import io.bhex.base.grpc.annotation.GrpcService;
import io.bhex.broker.admin.service.NotifyMessageService;
import io.bhex.broker.grpc.admin.*;
import io.bhex.broker.grpc.proto.AdminCommonResponse;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;


@Slf4j
@GrpcService
public class NotificationGrpcService extends AdminBrokerNotifyServiceGrpc.AdminBrokerNotifyServiceImplBase{

    @Resource
    private NotifyMessageService notifyMessageService;

    @Override
    public void listNotification(ListNotificationRequest request, StreamObserver<ListNotificationResponse> responseObserver) {

        long brokerId=request.getBrokerId();
        long userId=request.getUserId();

        ListNotificationResponse resp=null;

        try{
            Map<String,Integer> result = notifyMessageService.listNotification(userId,brokerId);

            List<Notification> list= result.keySet().stream().map(key->{
                Integer total = result.get(key);

                return Notification.newBuilder().setNumber(total).setNotifyType(key).build();
            }).collect(Collectors.toList());

            resp=ListNotificationResponse.newBuilder().setResult(true).addAllNotification(list).build();

        }catch (Exception e){
            log.error(e.getMessage(),e);
            resp=ListNotificationResponse.newBuilder().setResult(false).setMessage(e.getMessage()).build();
        }

        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }

    @Override
    public void decreNotification(DecreNotificationRequest request,
                                  StreamObserver<AdminCommonResponse> responseObserver) {

        long brokerId=request.getBrokerId();
        NotifyType type=request.getNotifyType();

        AdminCommonResponse resp=null;

        try{
            notifyMessageService.decreNotification(brokerId,type);
            resp=AdminCommonResponse.newBuilder().setSuccess(true).build();

        }catch (Exception e){
            log.error(e.getMessage(),e);
            resp=AdminCommonResponse.newBuilder().setSuccess(false).setMsg(e.getMessage()).build();
        }

        responseObserver.onNext(resp);
        responseObserver.onCompleted();

    }

    @Override
    public void clearNotification(DecreNotificationRequest request,
                                  StreamObserver<AdminCommonResponse> responseObserver){

        long brokerId=request.getBrokerId();
        NotifyType type=request.getNotifyType();

        AdminCommonResponse resp=null;

        try{
            notifyMessageService.clearNotification(brokerId,type);
            resp=AdminCommonResponse.newBuilder().setSuccess(true).build();

        }catch (Exception e){
            log.error(e.getMessage(),e);
            resp=AdminCommonResponse.newBuilder().setSuccess(false).setMsg(e.getMessage()).build();
        }

        responseObserver.onNext(resp);
        responseObserver.onCompleted();

    }


}
