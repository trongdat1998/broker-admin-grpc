package io.bhex.broker.admin.service.impl.notify;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Deprecated
//@Service
@Slf4j
public class NotifyMessageConsumer extends  AbstractMessageConsumer{

    private DefaultMQPushConsumer consumer;


    @Override
    //@PostConstruct
    public void init() throws Exception {
        consumer=new DefaultMQPushConsumer(consumerGroup);

        try {
            super.init();

            consumer.setNamesrvAddr(nameServer);
            consumer.setMessageModel(MessageModel.CLUSTERING);
            consumer.subscribe(topic, getTags());
            consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP);
            consumer.setConsumeTimestamp(getStartTime());
            //consumer.subscribe(topic, MessageSelector.bySql("runEnv = "+ runEnv ));
            consumer.registerMessageListener(new NotifyMessageListener());
            consumer.start();

        } catch (MQClientException e) {
            log.error(e.getMessage(),e);
            throw e;
        }

    }

    //format 20131223171201
    private String getStartTime(){
        LocalDateTime ldt=LocalDateTime.now().minusMinutes(10L);
        return ldt.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private String getTags() {

        List<String> envTags=targetTags.stream().map(i->i+"-"+runEnv).collect(Collectors.toList());
        return Joiner.on(" || ").join(envTags);
    }

    @PreDestroy
    public void destroy(){
        if(Objects.nonNull(consumer)){
            consumer.shutdown();
        }
    }


    class NotifyMessageListener implements MessageListenerConcurrently{

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {

            for (MessageExt messageExt : msgs) {

                String key=runEnv+"-"+"msgid-"+messageExt.getMsgId();
                log.info("message,topic={},tags={},msgid={}",
                        messageExt.getTopic(),messageExt.getTags(),messageExt.getMsgId());
                boolean lock=notifyUtil.getLock(key,3600);
                if(!lock){
                    log.info("Does not get lock for consume message,msgid={}",messageExt.getMsgId());
                    try {
                        consumer.sendMessageBack(messageExt,3);
                    } catch (Exception e) {
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

                String tags = messageExt.getTags();
                List<String> tagList = Splitter.on("||").splitToList(tags).stream()
                        .map(i->i.replace("-"+runEnv,""))
                        .collect(Collectors.toList());

                boolean isMatch=false;
                for (String tag : tagList) {
                    if(targetTags.contains(tag)){

                        Long brokerId=AbstractMessageConsumer.jsonStringToObject(messageExt.getBody(),Long.class);
                        try{
                            notifyMessageService.saveNotify(tag,brokerId);
                            isMatch|=true;
                        }catch (Exception e){
                            log.error(e.getMessage(),e);
                            notifyUtil.releaseLock(key,5);
                            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                        }

                    }
                }

                if(isMatch){
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }else{
                    return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                }

            }

            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
    }









}
