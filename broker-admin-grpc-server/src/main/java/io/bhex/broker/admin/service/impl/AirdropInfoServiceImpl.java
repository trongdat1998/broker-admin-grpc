package io.bhex.broker.admin.service.impl;

import io.bhex.broker.admin.mapper.AirdropInfoMapper;
import io.bhex.broker.admin.mapper.AssetSnapshotRecordMapper;
import io.bhex.broker.admin.mapper.TransferGroupInfoMapper;
import io.bhex.broker.admin.mapper.TransferRecordMapper;
import io.bhex.broker.admin.model.AssetSnapshotRecord;
import io.bhex.broker.admin.service.AirdropInfoService;
import io.bhex.broker.grpc.admin.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.server.grpc.server.service
 * @Author: ming.xu
 * @CreateDate: 10/11/2018 7:09 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@Slf4j
@Service
public class AirdropInfoServiceImpl implements AirdropInfoService {

    @Autowired
    private AirdropInfoMapper airdropInfoMapper;

    @Autowired
    private TransferRecordMapper transferRecordMapper;

    @Autowired
    private TransferGroupInfoMapper transferGroupInfoMapper;

    @Autowired
    private AssetSnapshotRecordMapper assetSnapshotRecordMapper;

    @Override
    public QueryAirdropInfoReply queryAirdropInfo(QueryAirdropInfoRequest request) {
        Example example = new Example(io.bhex.broker.admin.model.AirdropInfo.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotEmpty(request.getTitle())) {
            criteria.andLike("title", "%" + request.getTitle() + "%");
        }
        if (request.getBeginTime() != 0) {
            criteria.andGreaterThanOrEqualTo("airdropTime", request.getBeginTime());
        }
        if (request.getEndTime() != 0) {
            criteria.andLessThanOrEqualTo("airdropTime", request.getEndTime());
        }
        criteria.andEqualTo("brokerId", request.getBrokerId());
        example.setOrderByClause("created_at desc");
        List<io.bhex.broker.admin.model.AirdropInfo> airdropInfos = airdropInfoMapper.selectByExample(example);
        List<AirdropInfo> infos = processAirdropInfo(airdropInfos);
        QueryAirdropInfoReply reply = QueryAirdropInfoReply.newBuilder()
                .addAllAirdropInfo(infos)
                .build();
        return reply;
    }

    @Override
    public QueryAirdropInfoReply listScheduleAirdrop() {
        Example example = new Example(io.bhex.broker.admin.model.AirdropInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("status", io.bhex.broker.admin.model.AirdropInfo.STATUS_INIT);
        criteria.andLessThanOrEqualTo("airdropTime", System.currentTimeMillis());
        List<io.bhex.broker.admin.model.AirdropInfo> airdropInfos = airdropInfoMapper.selectByExample(example);
        List<AirdropInfo> infos = processAirdropInfo(airdropInfos);
        QueryAirdropInfoReply reply = QueryAirdropInfoReply.newBuilder()
                .addAllAirdropInfo(infos)
                .build();
        return reply;
    }

    private List<AirdropInfo> processAirdropInfo(List<io.bhex.broker.admin.model.AirdropInfo> airdropInfos) {
        List<AirdropInfo> infos = new ArrayList<>();
        if (!CollectionUtils.isEmpty(airdropInfos)) {
            infos = airdropInfos.stream().map(info -> {
                AirdropInfo.Builder builder = AirdropInfo.newBuilder();
                BeanUtils.copyProperties(info, builder);
                builder.setUserAccountIds(info.getUserAccountIds());
                builder.setUserIds(info.getUserIds());
                builder.setAirdropTokenNum(info.getAirdropTokenNum().toString());
                builder.setHaveTokenNum(info.getHaveTokenNum().toString());
                return builder.build();
            }).collect(Collectors.toList());
        }
        return infos;
    }

    @Override
    public CreateAirdropInfoReply createAirdropInfo(CreateAirdropInfoRequest request) {
        io.bhex.broker.admin.model.AirdropInfo airdropInfo = new io.bhex.broker.admin.model.AirdropInfo();
        BeanUtils.copyProperties(request, airdropInfo);
        airdropInfo.setStatus(io.bhex.broker.admin.model.AirdropInfo.STATUS_INIT);
        airdropInfo.setAirdropTokenNum(new BigDecimal(request.getAirdropTokenNum()));
        airdropInfo.setHaveTokenNum(new BigDecimal(request.getHaveTokenNum()));
        airdropInfo.setTransferAssetAmount(StringUtils.isNotEmpty(request.getTransferAssetAmount()) ? new BigDecimal(request.getTransferAssetAmount()) : new BigDecimal(0));
        airdropInfo.setUpdatedAt(System.currentTimeMillis());
        airdropInfo.setCreatedAt(System.currentTimeMillis());
        Boolean isOk = airdropInfoMapper.insert(airdropInfo) > 0 ? true : false;
        CreateAirdropInfoReply reply = CreateAirdropInfoReply.newBuilder()
                .setResult(isOk)
                .setAirdropId(airdropInfo.getId())
                .build();
        return reply;
    }

    @Override
    public AirdropInfo getAirdropInfo(GetAirdropInfoRequest request) {
        io.bhex.broker.admin.model.AirdropInfo info = getAirdropInfo(request.getAirdropId(), request.getBrokerId());
        AirdropInfo.Builder builder = AirdropInfo.newBuilder();
        if (null != info) {
            BeanUtils.copyProperties(info, builder);
            builder.setAirdropTokenNum(info.getAirdropTokenNum().toString());
            builder.setHaveTokenNum(info.getHaveTokenNum().toString());
        }
        return builder.build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LockAndAirdropReply lockAndAirdrop(LockAndAirdropRequest request) {
        LockAndAirdropReply.Builder builder = LockAndAirdropReply.newBuilder();
        io.bhex.broker.admin.model.AirdropInfo airdropInfo = airdropInfoMapper.getAirdropInfAndLock(request.getAirdropId(), request.getBrokerId());
        if (airdropInfo.getStatus() == io.bhex.broker.admin.model.AirdropInfo.STATUS_INIT) {
            airdropInfo.setStatus(io.bhex.broker.admin.model.AirdropInfo.STATUS_AIRDOP);
            airdropInfoMapper.updateByPrimaryKeySelective(airdropInfo);
            builder.setIsLocked(true);
        } else {
            builder.setIsLocked(false);
        }
        return builder.build();
    }

    private io.bhex.broker.admin.model.AirdropInfo getAirdropInfo(Long airdropId, Long brokerId) {
        Example example = new Example(io.bhex.broker.admin.model.AirdropInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", airdropId);
        criteria.andEqualTo("brokerId", brokerId);
        return airdropInfoMapper.selectOneByExample(example);
    }

    @Override
    public AddTransferRecordReply addTransferRecord(AddTransferRecordRequest request) {
        List<TransferRecord> transferRecordList = request.getTransferRecordListList();
        if (!CollectionUtils.isEmpty(transferRecordList)) {
            // 生成分组信息
            io.bhex.broker.admin.model.TransferGroupInfo groupInfo = new io.bhex.broker.admin.model.TransferGroupInfo();
            groupInfo.setAirdropId(request.getAirdropId());
            groupInfo.setBrokerId(request.getBrokerId());
            groupInfo.setStatus(io.bhex.broker.admin.model.TransferGroupInfo.STATUS_INIT);
            groupInfo.setTransferCount(new Long(transferRecordList.size()));
            groupInfo.setTransferAssetAmount(StringUtils.EMPTY);
            groupInfo.setTransferAssetAmount(new BigDecimal(0).toString());
            groupInfo.setTokenId(request.getTokenId());
            groupInfo.setCreatedAt(System.currentTimeMillis());
            transferGroupInfoMapper.insert(groupInfo);
            // 记录每笔转账信息
            Long groupId = groupInfo.getId();
            BigDecimal assetAmount = new BigDecimal(0);
            for (TransferRecord record : transferRecordList) {
                io.bhex.broker.admin.model.TransferRecord r = new io.bhex.broker.admin.model.TransferRecord();
                BeanUtils.copyProperties(record, r);
                r.setGroupId(groupId);
                r.setAirdropId(request.getAirdropId());
                r.setTokenAmount(new BigDecimal(record.getTokenAmount()));
                r.setCreatedAt(System.currentTimeMillis());
                assetAmount = assetAmount.add(r.getTokenAmount());
                transferRecordMapper.insert(r);
            }
            // 更新总钱数
            groupInfo = new io.bhex.broker.admin.model.TransferGroupInfo();
            groupInfo.setId(groupId);
            groupInfo.setTransferAssetAmount(assetAmount.toString());
            transferGroupInfoMapper.updateByPrimaryKeySelective(groupInfo);
        }
        AddTransferRecordReply reply = AddTransferRecordReply.newBuilder()
                .setResult(true)
                .build();
        return reply;
    }

    @Override
    public AddAssetSnapshotReply addAssetSnapshot(AddAssetSnapshotRequest request) {
        List<AssetSnapshot> assetSnapshotList = request.getAssetSnapshotListList();
        if (!CollectionUtils.isEmpty(assetSnapshotList)) {
            List<AssetSnapshotRecord> records = assetSnapshotList.stream().map(record -> {
                AssetSnapshotRecord r = new AssetSnapshotRecord();
                BeanUtils.copyProperties(record, r);
                r.setAssetAmount(new BigDecimal(record.getAssetAmount()));
                r.setCreatedAt(System.currentTimeMillis());
                return r;
            }).collect(Collectors.toList());
            for (AssetSnapshotRecord r : records) {
                assetSnapshotRecordMapper.insertSelective(r);
            }
        }
        AddAssetSnapshotReply reply = AddAssetSnapshotReply.newBuilder()
                .setResult(true)
                .build();
        return reply;
    }

    @Override
    public TransferGroupInfo getTransferGroupInfo(GetTransferGroupInfoRequest request) {
        Example example = new Example(io.bhex.broker.admin.model.AirdropInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", request.getGroupId());
        criteria.andEqualTo("brokerId", request.getBrokerId());
        criteria.andEqualTo("airdropId", request.getBrokerId());

        io.bhex.broker.admin.model.TransferGroupInfo groupInfo = transferGroupInfoMapper.selectOneByExample(example);
        TransferGroupInfo.Builder builder = TransferGroupInfo.newBuilder();
        BeanUtils.copyProperties(groupInfo, builder);

        return builder.build();
    }

    @Override
    public UpdateAirdropStatusReply updateAirdropStatus(UpdateAirdropStatusRequest request) {
        UpdateAirdropStatusReply.Builder builder = UpdateAirdropStatusReply.newBuilder();
        builder.setResult(false);
        Example example = new Example(io.bhex.broker.admin.model.AirdropInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", request.getAirdropId());
        criteria.andEqualTo("brokerId", request.getBrokerId());
        io.bhex.broker.admin.model.AirdropInfo airdropInfo = airdropInfoMapper.selectOneByExample(example);
        if (null != airdropInfo) {
            airdropInfo.setStatus(request.getStatus());
            Boolean isOk = airdropInfoMapper.updateByExample(airdropInfo, example) > 0 ? true : false;
            builder.setResult(isOk);
        }
        return builder.build();
    }

    @Override
    public TransferRecordFilterReply transferRecordFilter(TransferRecordFilterRequest request) {
        TransferRecordFilterReply.Builder builder = TransferRecordFilterReply.newBuilder();
        List<Long> accountIdsList = request.getAccountIdsList();
        List<Long> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(accountIdsList)) {
            Example example = new Example(io.bhex.broker.admin.model.TransferRecord.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("airdropId", request.getAirdropId());
            criteria.andIn("accountId", accountIdsList);
            List<io.bhex.broker.admin.model.TransferRecord> transferRecords = transferRecordMapper.selectByExample(example);
            List<Long> sentAccountIds = transferRecords.stream().map(tr -> {
                return tr.getAccountId();
            }).collect(Collectors.toList());
            for (Long id : accountIdsList) {
                if (!sentAccountIds.contains(id)) {
                    result.add(id);
                }
            }
        }
        builder.addAllAccountIds(result);
        return builder.build();
    }

    @Override
    public ListAllTransferGroupReply listAllTransferGroup(ListAllTransferGroupRequest request) {
        Example example = new Example(io.bhex.broker.admin.model.TransferGroupInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("airdropId", request.getAirdropId());
        criteria.andEqualTo("brokerId", request.getBrokerId());
        List<io.bhex.broker.admin.model.TransferGroupInfo> transferGroupInfos = transferGroupInfoMapper.selectByExample(example);
        List<TransferGroupInfo> groupInfos = transferGroupInfos.stream().map(info -> {
            TransferGroupInfo.Builder builder = TransferGroupInfo.newBuilder();
            BeanUtils.copyProperties(info, builder);
            return builder.build();
        }).collect(Collectors.toList());
        return ListAllTransferGroupReply.newBuilder()
                .addAllTransferGroupInfos(groupInfos)
                .build();
    }

    @Override
    public UpdateTransferGroupStatusReply updateTransferGroupStatus(UpdateTransferGroupStatusRequest request) {
        UpdateTransferGroupStatusReply.Builder builder = UpdateTransferGroupStatusReply.newBuilder();
        builder.setResult(false);
        Example example = new Example(io.bhex.broker.admin.model.TransferGroupInfo.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", request.getGroupId());
        criteria.andEqualTo("airdropId", request.getAirdropId());
        criteria.andEqualTo("brokerId", request.getBrokerId());
        io.bhex.broker.admin.model.TransferGroupInfo transferGroupInfo = transferGroupInfoMapper.selectOneByExample(example);
        if (null != transferGroupInfo) {
            transferGroupInfo.setStatus(request.getStatus());
            Boolean isOk = transferGroupInfoMapper.updateByExample(transferGroupInfo, example) > 0 ? true : false;
            builder.setResult(isOk);
            // 如果本分组发送成功，要更新空投信息中的投放人数、投放钱数
            updateAirdropUserCount(transferGroupInfo);
        }
        return builder.build();
    }

    /**
     * 更新空投信息中的投放人数、投放钱数
     *
     * @param transferGroupInfo
     */
    private void updateAirdropUserCount(io.bhex.broker.admin.model.TransferGroupInfo transferGroupInfo) {
        // 如果本分组发送成功，要更新空投信息中的投放人数、投放钱数
        if (io.bhex.broker.admin.model.TransferGroupInfo.STATUS_SUCCESS == transferGroupInfo.getStatus()) {
            Example airdropExample = new Example(io.bhex.broker.admin.model.AirdropInfo.class);
            Example.Criteria airdropCriteria = airdropExample.createCriteria();
            airdropCriteria.andEqualTo("id", transferGroupInfo.getAirdropId());
            airdropCriteria.andEqualTo("brokerId", transferGroupInfo.getBrokerId());
            io.bhex.broker.admin.model.AirdropInfo airdropInfo = airdropInfoMapper.selectOneByExample(airdropExample);
            if (null != airdropInfo) {
                airdropInfo.setUserCount(airdropInfo.getUserCount() + transferGroupInfo.getTransferCount());
                airdropInfo.setTransferAssetAmount(airdropInfo.getTransferAssetAmount().add(new BigDecimal(transferGroupInfo.getTransferAssetAmount())));
                airdropInfo.setUpdatedAt(System.currentTimeMillis());
                airdropInfoMapper.updateByExample(airdropInfo, airdropExample);
            }
        }
    }

    @Override
    public ListTransferRecordReply listTransferRecordByGroupId(ListTransferRecordRequest request) {
        Example example = new Example(io.bhex.broker.admin.model.TransferRecord.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("airdropId", request.getAirdropId());
        criteria.andEqualTo("brokerId", request.getBrokerId());
        criteria.andEqualTo("groupId", request.getGroupId());
        List<io.bhex.broker.admin.model.TransferRecord> transferRecords = transferRecordMapper.selectByExample(example);
        List<TransferRecord> recordList = transferRecords.stream().map(tr -> {
            TransferRecord.Builder builder = TransferRecord.newBuilder();
            BeanUtils.copyProperties(tr, builder);
            builder.setTokenAmount(tr.getTokenAmount().toString());
            return builder.build();
        }).collect(Collectors.toList());
        ListTransferRecordReply reply = ListTransferRecordReply.newBuilder()
                .addAllTransferRecord(recordList)
                .build();
        return reply;
    }
}
