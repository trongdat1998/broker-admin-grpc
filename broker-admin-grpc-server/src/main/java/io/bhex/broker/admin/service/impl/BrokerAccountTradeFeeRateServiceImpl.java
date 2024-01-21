package io.bhex.broker.admin.service.impl;

import io.bhex.base.admin.common.EditBrokerAccountTradeFeeGroupResponse;
import io.bhex.base.admin.common.UpdateSendStatusRequest;
import io.bhex.broker.admin.mapper.BrokerAccountTradeFeeDetailMapper;
import io.bhex.broker.admin.mapper.BrokerAccountTradeFeeGroupMapper;
import io.bhex.broker.admin.model.BrokerAccountTradeFeeDetail;
import io.bhex.broker.admin.model.BrokerAccountTradeFeeGroup;
import io.bhex.broker.admin.service.BrokerAccountTradeFeeRateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Date: 2018/11/22 上午11:25
 * @Author: liwei
 * @Copyright（C）: 2018 BlueHelix Inc. All rights reserved.
 */
@Slf4j
@Service
public class BrokerAccountTradeFeeRateServiceImpl implements BrokerAccountTradeFeeRateService {
    public static final int INIT_STATUS = 0;
    public static final int ENABLE_STATUS = 1;
    public static final int DISABLE_STATUS = 2;


    @Autowired
    private BrokerAccountTradeFeeGroupMapper brokerAccountTradeFeeGroupMapper;
    @Autowired
    private BrokerAccountTradeFeeDetailMapper brokerAccountTradeFeeDetailMapper;

    private boolean modifiedDiscount(BrokerAccountTradeFeeGroup inputGroup){
        BrokerAccountTradeFeeGroup groupInDb = brokerAccountTradeFeeGroupMapper.selectByPrimaryKey(inputGroup.getId());
        if(groupInDb.getMakerFeeRateAdjust().compareTo(inputGroup.getMakerFeeRateAdjust()) != 0){
            return true;
        }
        if(groupInDb.getTakerFeeRateAdjust().compareTo(inputGroup.getTakerFeeRateAdjust()) != 0){
            return true;
        }
        if(groupInDb.getTakerRewardToMakerRateAdjust().compareTo(inputGroup.getTakerRewardToMakerRateAdjust()) != 0){
            return true;
        }
        return false;
    }

    @Override
    public AccountIdsDTO editBrokerAccountTradeFeeGroup(BrokerAccountTradeFeeGroup group, List<Long> accountIds, boolean enable) {

        if(group.getId() == null || group.getId() == 0){//新建群组
            group.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            group.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            group.setStatus(INIT_STATUS);
            brokerAccountTradeFeeGroupMapper.insertSelective(group);
            saveBrokerAccountTradeFeeDetails(group.getBrokerId(), group.getId(), accountIds, INIT_STATUS);
            return new AccountIdsDTO();
        }

        //对比
        boolean modifiedDiscount = modifiedDiscount(group);

        List<BrokerAccountTradeFeeDetail> details = brokerAccountTradeFeeDetailMapper
                .getBrokerAccountTradeFeeDetails(group.getBrokerId(), group.getId());
        List<Long> accountIdsInDb = Optional.ofNullable(details).orElse(new ArrayList<>())
                .stream().map(detail -> detail.getAccountId()).collect(Collectors.toList());

        List<Long> deletedAccountIds = new ArrayList<>();
        List<Long> modifiedAccountIds = new ArrayList<>();
        List<Long> addedAccountIds = new ArrayList<>();

        for(Long inputAccountId : accountIds){
            if(!accountIdsInDb.contains(inputAccountId)){
                addedAccountIds.add(inputAccountId);
            }
            else if(modifiedDiscount){
                modifiedAccountIds.add(inputAccountId);
            }
        }

        for(Long accountInDb : accountIdsInDb){
            if(!accountIds.contains(accountInDb)){
                deletedAccountIds.add(accountInDb);
            }
        }

        log.info("added:{} deleted:{} modified:{}", addedAccountIds, deletedAccountIds, modifiedAccountIds);
        group.setStatus(ENABLE_STATUS);
        brokerAccountTradeFeeGroupMapper.updateByPrimaryKeySelective(group);

        AccountIdsDTO dto = new AccountIdsDTO();
        //added add
        if(!CollectionUtils.isEmpty(addedAccountIds)) {
            dto.setAddedAccountIds(addedAccountIds);
            saveBrokerAccountTradeFeeDetails(group.getBrokerId(), group.getId(), addedAccountIds, ENABLE_STATUS);
        }
        //deleted
        if(!CollectionUtils.isEmpty(deletedAccountIds)){
            dto.setDeletedAccountIds(deletedAccountIds);
            brokerAccountTradeFeeDetailMapper.deleteAccountIds(group.getBrokerId(), group.getId(), deletedAccountIds);
        }

        //modified 不动
        if(!CollectionUtils.isEmpty(modifiedAccountIds)){
            dto.setModifiedAccountIds(modifiedAccountIds);
        }
        return dto;

    }

    @Override
    public List<BrokerAccountTradeFeeGroup> getBrokerAccountTradeFeeGroups(Long brokerId){
        return brokerAccountTradeFeeGroupMapper.getBrokerAccountTradeFeeGroups(brokerId);
    }

    @Override
    public BrokerAccountTradeFeeGroup getBrokerAccountTradeFeeGroup(Long brokerId, Long groupId) {
        return brokerAccountTradeFeeGroupMapper.selectByPrimaryKey(groupId);
    }

    @Override
    public boolean saveBrokerAccountTradeFeeDetails(Long brokerId, Long groupId, List<Long> accountIds, int status) {
        for(Long accountId : accountIds){
            BrokerAccountTradeFeeDetail detail = new BrokerAccountTradeFeeDetail();
            detail.setAccountId(accountId);
            detail.setBrokerId(brokerId);
            detail.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            detail.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            detail.setGroupId(groupId);
            detail.setStatus(status);
            detail.setSendStatus(0);
            detail.setDeleted(0);
            brokerAccountTradeFeeDetailMapper.insert(detail);
        }
        return true;
    }

    @Override
    public List<BrokerAccountTradeFeeDetail> getBrokerAccountTradeFeeDetails(Long brokerId, Long groupId) {
        return brokerAccountTradeFeeDetailMapper.getBrokerAccountTradeFeeDetails(brokerId, groupId);
    }

    private boolean existedOtherGroup(Long brokerId, Long groupId, Long accountId){
        BrokerAccountTradeFeeDetail detail = brokerAccountTradeFeeDetailMapper.getBrokerAccountTradeFeeDetail(brokerId, accountId);
        if(detail == null){
            return false;
        }
        if(groupId == null || groupId == 0){
            return true;
        }
        if(!detail.getGroupId().equals(groupId)){
            return true;
        }
        return false;
    }

    @Override
    public List<Long> getExistedAccountIds(Long brokerId, Long groupId, List<Long> accountIds) {
        if(CollectionUtils.isEmpty(accountIds)){
            return new ArrayList<>();
        }
        List<Long> existedOtherGroupAccountIds = accountIds.stream()
                .filter(accountId -> existedOtherGroup(brokerId, groupId,accountId))
                .collect(Collectors.toList());
        log.info("existedOtherGroupAccountIds:{}",existedOtherGroupAccountIds);

        return existedOtherGroupAccountIds;
    }

    public EditBrokerAccountTradeFeeGroupResponse.Result validateGroupName(Long groupId, String groupName){
        BrokerAccountTradeFeeGroup existedGroup = brokerAccountTradeFeeGroupMapper.countGroupName(groupName);
        if(existedGroup != null){
            if(groupId == null || groupId == 0){//新加群组的名称被别人占用
                return EditBrokerAccountTradeFeeGroupResponse.Result.GROUP_EXISTED;
            }
            if(!existedGroup.getId().equals(groupId)){//改到别人的组名上去了
                return EditBrokerAccountTradeFeeGroupResponse.Result.GROUP_EXISTED;
            }
        }

        if(groupId != null && groupId > 0){
            BrokerAccountTradeFeeGroup groupInDb = brokerAccountTradeFeeGroupMapper.selectByPrimaryKey(groupId);
            if(groupInDb == null){
                return EditBrokerAccountTradeFeeGroupResponse.Result.GROUP_ID_ERROR;
            }
            if(groupInDb.getStatus() == 0){
                return EditBrokerAccountTradeFeeGroupResponse.Result.DISABLE_STATUS;
            }
        }
        return EditBrokerAccountTradeFeeGroupResponse.Result.OK;
    }

    @Override
    public List<Long> enableBrokerAccountTradeFeeGroup(Long brokerId, Long groupId) {
        BrokerAccountTradeFeeGroup group = getBrokerAccountTradeFeeGroup(brokerId, groupId);
        if(group.getStatus() == DISABLE_STATUS){//禁用到启用
            brokerAccountTradeFeeDetailMapper.updateSendStatusByGroup(brokerId, groupId, 0);
        }
        brokerAccountTradeFeeDetailMapper.updateStatus(brokerId, groupId, ENABLE_STATUS);

        List<Long> accountIds = brokerAccountTradeFeeDetailMapper.getUnSendedAccountIds(brokerId, groupId);

        return accountIds;
    }

    @Override
    public List<Long> disableBrokerAccountTradeFeeGroup(Long brokerId, Long groupId) {
        BrokerAccountTradeFeeGroup group = getBrokerAccountTradeFeeGroup(brokerId, groupId);
        if(group.getStatus() == ENABLE_STATUS){//启用到禁用
            brokerAccountTradeFeeDetailMapper.updateSendStatusByGroup(brokerId, groupId, 0);
        }
        brokerAccountTradeFeeDetailMapper.updateStatus(brokerId, groupId, DISABLE_STATUS);
        List<Long> accountIds = brokerAccountTradeFeeDetailMapper.getUnSendedAccountIds(brokerId, groupId);
        return accountIds;
    }

    @Override
    public void updateSendStatus(Long brokerId, Long groupId, List<Long> accountIds, int sendStatus, UpdateSendStatusRequest.OpType opType) {
        if(!CollectionUtils.isEmpty(accountIds)){
            brokerAccountTradeFeeDetailMapper.updateSendStatus(brokerId, groupId, accountIds, sendStatus);
        }

        List<Long> unSendedAccountIds = brokerAccountTradeFeeDetailMapper.getUnSendedAccountIds(brokerId, groupId);
        if(CollectionUtils.isEmpty(unSendedAccountIds)){//发送完毕后更新群组状态
            if(opType.getNumber() == UpdateSendStatusRequest.OpType.DISABLE_VALUE){
                brokerAccountTradeFeeGroupMapper.updateStatus(brokerId, groupId, DISABLE_STATUS);
            }
            else if(opType.getNumber() == UpdateSendStatusRequest.OpType.ENABLE_VALUE){
                brokerAccountTradeFeeGroupMapper.updateStatus(brokerId, groupId, ENABLE_STATUS);
            }
        }
    }
}
