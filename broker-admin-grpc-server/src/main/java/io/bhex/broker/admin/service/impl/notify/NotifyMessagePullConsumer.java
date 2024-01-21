package io.bhex.broker.admin.service.impl.notify;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import io.bhex.broker.admin.model.MqOffset;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.*;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Deprecated
//@Service
@Slf4j
public class NotifyMessagePullConsumer extends AbstractMessageConsumer{

    private MQPullConsumerScheduleService scheduleService;

    @Override
    //@PostConstruct
    public void init() throws Exception{
        try {
            super.init();

            scheduleService=new MQPullConsumerScheduleService(consumerGroup);
            scheduleService.getDefaultMQPullConsumer().setNamesrvAddr(nameServer);
            scheduleService.registerPullTaskCallback(topic,new NotifyPullTaskCallback());
            scheduleService.start();
        }catch (MQClientException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }


    public class NotifyPullTaskCallback implements PullTaskCallback {

        @Override
        public void doPullTask(MessageQueue mq, PullTaskContext context) {
            MQPullConsumer consumer = context.getPullConsumer();
            try {
                MqOffset offset = notifyUtil.fetchConsumeOffset(mq);
                if(Objects.isNull(offset)){
                    return;
                }
                if(offset.getOffset() < 0) {
                    offset.setOffset(0L);
                }

                PullResult pullResult = consumer.pull(mq, getTags(), offset.getOffset(), 32);
                switch (pullResult.getPullStatus()) {
                    case FOUND:
                        List<MessageExt> messageExtList = pullResult.getMsgFoundList();
                        ConsumeStatus status=messageProc(messageExtList);
                        if(status==ConsumeStatus.SUCCESS || status==ConsumeStatus.IGNORE){
                            notifyUtil.increOffset(offset);
                        }
                        break;
                    case NO_MATCHED_MSG:
                        break;
                    case NO_NEW_MSG:
                        break;
                    case OFFSET_ILLEGAL:
                        notifyUtil.increOffset(offset);
                        break;
                    default:
                        break;
                }

                //重新拉取 建议超过5s这样就不会重复获取
                context.setPullNextDelayTimeMillis(10000);

            }catch(Exception e){
                log.error(e.getMessage(), e);
            }
        }
    }


    private String getTags() {

        List<String> envTags=targetTags.stream().map(i->i+"-"+runEnv).collect(Collectors.toList());
        return Joiner.on(" || ").join(envTags);
    }


    private ConsumeStatus messageProc(List<MessageExt> msgs){
        for (MessageExt messageExt : msgs) {
            log.info("message,topic={},tags={},msgid={}",
                    messageExt.getTopic(),messageExt.getTags(),messageExt.getMsgId());
            String key=runEnv+"-"+"msgid-"+messageExt.getMsgId();
            boolean lock=notifyUtil.getLock(key,3600);
            if(!lock){
                log.info("Hasn't consume message lock,msgid={}",messageExt.getMsgId());
                return ConsumeStatus.IGNORE;
            }

            String tags = messageExt.getTags();
            List<String> tagList = Splitter.on("||").splitToList(tags).stream()
                    .map(i->i.replace("-"+runEnv,""))
                    .collect(Collectors.toList());

            for (String tag : tagList) {
                if(targetTags.contains(tag)){
                    Long brokerId=AbstractMessageConsumer.jsonStringToObject(messageExt.getBody(),Long.class);
                    log.info("match tag,tag={},brokerId={}",tag,brokerId);
                    try{
                        notifyMessageService.saveNotify(tag,brokerId);
                        log.info("process message success");
                    }catch (Exception e){
                        log.error(e.getMessage(),e);
                        notifyUtil.releaseLock(key,5);
                        return ConsumeStatus.FAIL;
                    }
                }
            }
            return ConsumeStatus.SUCCESS;
        }
        return ConsumeStatus.FAIL;
    }

    @PreDestroy
    public void destroy(){
        if(Objects.nonNull(scheduleService)){
            scheduleService.shutdown();
        }
    }


    enum ConsumeStatus{
        SUCCESS,FAIL,IGNORE;
    }








}
