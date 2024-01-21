package io.bhex.broker.admin.service;

import io.bhex.broker.grpc.admin.NotifyType;

import java.util.Map;

public interface NotifyMessageService {

    Map<String,Integer> listNotification(Long userId, Long orgId);

    void decreNotification(long brokerId, NotifyType type);

    void clearNotification(long brokerId, NotifyType type);
}
