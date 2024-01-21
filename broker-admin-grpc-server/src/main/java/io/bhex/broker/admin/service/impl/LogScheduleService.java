package io.bhex.broker.admin.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import io.bhex.bhop.common.entity.BusinessLog;
import io.bhex.bhop.common.mapper.BusinessLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Slf4j
@Service
public class LogScheduleService {
    @Autowired
    private BusinessLogMapper businessLogMapper;

    //@Scheduled(cron = "37 46 * * * ?")
    public void redeemNewFund() {
        long id = 0;
        for (;;) {
            Example example =  Example.builder(BusinessLog.class)
                    .orderByAsc("id")
                    .build();
            PageHelper.startPage(0, 1000);
            Example.Criteria criteria =   example.createCriteria()
                    .andGreaterThan("id", id);
            List<BusinessLog> logs = businessLogMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(logs)) {
                break;
            }
            for (BusinessLog log : logs) {
                if (log.getRequestInfo().startsWith("{") && log.getRequestInfo().endsWith("}")) {
                    try {
                        JSONObject jo = JSONObject.parseObject(log.getRequestInfo());
                        boolean uidKey = jo.containsKey("userId") && jo.getLong("userId") != null;
                        if (uidKey) {
                            log.setEntityId(jo.getString("userId"));
                            businessLogMapper.updateByPrimaryKeySelective(log);
                        }
                    } catch (Exception e) {

                    }
                }
                id = log.getId();
            }
            log.info("id:{}", id + "");
        }
    }

    //@Scheduled(cron = "37 27 * * * ?")
    public void reloadWithdrawVerify() {
        long id = 0;
        for (;;) {
            Example example =  Example.builder(BusinessLog.class)
                    .orderByAsc("id")
                    .build();
            PageHelper.startPage(0, 1000);
            Example.Criteria criteria =   example.createCriteria()
                    .andEqualTo("opType", "withdrawOrderVerify")
                    .andGreaterThan("id", id);
            List<BusinessLog> logs = businessLogMapper.selectByExample(example);
            if (CollectionUtils.isEmpty(logs)) {
                break;
            }
            for (BusinessLog l : logs) {
                if (l.getRequestInfo().startsWith("{") && l.getRequestInfo().endsWith("}")) {
                    try {
                        JSONObject jo = JSONObject.parseObject(l.getRequestInfo());
                        boolean withdrawOrderIdKey = jo.containsKey("withdrawOrderId") && jo.getLong("withdrawOrderId") != null;
                        if (withdrawOrderIdKey && jo.getLong("withdrawOrderId") > 1000000) {
                            l.setEntityId(jo.getString("withdrawOrderId"));
                            l.setSubType(jo.getBoolean("verifyPassed") ? "Passed" : "Rejected");
                            businessLogMapper.updateByPrimaryKeySelective(l);
                            log.info("log:{}", log);
                        }
                    } catch (Exception e) {

                    }
                }
                id = l.getId();
            }
            log.info("id:{}", id + "");
        }
    }

}
