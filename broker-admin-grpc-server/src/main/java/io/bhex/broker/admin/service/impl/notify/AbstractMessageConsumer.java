package io.bhex.broker.admin.service.impl.notify;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.MQPullConsumerScheduleService;
import org.apache.rocketmq.client.exception.MQClientException;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Deprecated
@Slf4j
public class AbstractMessageConsumer {

    protected final static String topic="broker";

    @Value("${spring.rocketmq.name-servers}")
    protected String nameServer;

    @Value("${spring.rocketmq.consumer-group}")
    protected String consumerGroup;

    protected String  runEnv;

    @Resource
    protected NotifyUtil notifyUtil;

    @Resource
    protected NotifyMessageServiceImpl notifyMessageService;

    protected List<String> targetTags= Lists.newArrayList("withdraw","kyc-application","otc-appeal");

    public void init() throws Exception{
        try {
            runEnv=notifyUtil.getRunEnv();
            if (StringUtils.isEmpty(runEnv)) {
                log.error("Miss runEnv config");
                throw new IllegalArgumentException("Miss runEnv config");
            }

        }catch(Exception e){
            log.error(e.getMessage(), e);
            throw e;
        }
    }


    protected static <T> T jsonStringToObject(byte[] bytes, Class<T> clazz) {
        return JSON.parseObject(bytes,clazz);
    }

}
