package io.bhex.broker.admin.service;

import io.bhex.broker.grpc.admin.*;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin.service
 * @Author: ming.xu
 * @CreateDate: 10/11/2018 7:11 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
public interface AirdropInfoService {

    QueryAirdropInfoReply queryAirdropInfo(QueryAirdropInfoRequest request);

    QueryAirdropInfoReply listScheduleAirdrop();

    CreateAirdropInfoReply createAirdropInfo(CreateAirdropInfoRequest request);

    AirdropInfo getAirdropInfo(GetAirdropInfoRequest request);

    LockAndAirdropReply lockAndAirdrop(LockAndAirdropRequest request);

    AddTransferRecordReply addTransferRecord(AddTransferRecordRequest request);

    AddAssetSnapshotReply addAssetSnapshot(AddAssetSnapshotRequest request);

    TransferGroupInfo getTransferGroupInfo(GetTransferGroupInfoRequest request);

    UpdateAirdropStatusReply updateAirdropStatus(UpdateAirdropStatusRequest request);

    TransferRecordFilterReply transferRecordFilter(TransferRecordFilterRequest request);

    ListAllTransferGroupReply listAllTransferGroup(ListAllTransferGroupRequest request);

    UpdateTransferGroupStatusReply updateTransferGroupStatus(UpdateTransferGroupStatusRequest request);

    ListTransferRecordReply listTransferRecordByGroupId(ListTransferRecordRequest request);
}
