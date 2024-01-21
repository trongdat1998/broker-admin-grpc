package io.bhex.broker.admin.service.impl.notify;

import io.bhex.base.redis.cluster.CacheClusterClient;
import io.bhex.broker.admin.mapper.MqOffsetMapper;
import io.bhex.broker.admin.model.MqOffset;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class NotifyUtil {

    @Resource
    private MqOffsetMapper mqOffsetMapper;

    @Resource(name="notifyRedisTemplate")
    private StringRedisTemplate redisTemplate;

    private String runEnv="";

    private String activityProfile="";

    @Resource
    private Environment environment;

    @PostConstruct
    public void init(){

        if(environment.getActiveProfiles().length==0){
            log.info("runEnv=null");
        }else{
            activityProfile=environment.getActiveProfiles()[0];
            runEnv=environment.getActiveProfiles()[0].split("_")[0];
            log.info("runEnv={}",runEnv);
            log.info("activityProfile={}",activityProfile);
        }

    }

    public String getRunEnv(){
        return this.runEnv;
    }

    public String getActivityProfile(){
        return this.activityProfile;
    }

    public MqOffset fetchConsumeOffset(MessageQueue mq) {
        String brokerName=mq.getBrokerName();
        String topic=mq.getTopic();
        int mqQueueId=mq.getQueueId();

        Example exp=new Example(MqOffset.class);
        exp.createCriteria().andEqualTo("brokerName",brokerName)
                .andEqualTo("topic",topic)
                .andEqualTo("queueId",mqQueueId);


        List<MqOffset> list=mqOffsetMapper.selectByExample(exp);
        if(list.size()==0){

            MqOffset entity=new MqOffset();
            entity.setBrokerName(brokerName);
            entity.setOffset(0L);
            entity.setQueueId(mqQueueId);
            entity.setTopic(topic);
            entity.setCreatedAt(System.currentTimeMillis());

            int row=mqOffsetMapper.insertSelective(entity);
            if(row==1){
                return entity;
            }

            log.error("get offset exception");
            return null;
        }

        if(list.size()>1){
            log.error("Offset more than one,size={}",list.size());
            return null;
        }

        return list.get(0);

    }

    public void increOffset(MqOffset offset) {

        MqOffset tmp=new MqOffset();
        tmp.setId(offset.getId());
        tmp.setOffset(offset.nextOffset());
        tmp.setCreatedAt(System.currentTimeMillis());

        int row=mqOffsetMapper.updateByPrimaryKeySelective(tmp);
        if(row==1){
            return;
        }

        log.error("Update offset fail,brokerName={},topic={},queueId={}"
                ,offset.getBrokerName(),offset.getTopic(),offset.getQueueId());
        return;
    }

    public boolean getLock(String key,int expireSeconds) {

        boolean success=redisTemplate.opsForValue().setIfAbsent(key,"lock");
        if(success){
            try{
                Boolean expireSuccess=redisTemplate.expire(key,expireSeconds, TimeUnit.SECONDS);
                if(expireSuccess){
                    return success;
                }
            }catch (Exception e){
                log.error(e.getMessage(),e);
                redisTemplate.delete(key);
            }
        }

        return false;

    }

    public void releaseLock(String key,int tryNumber) {
        int i=0;
        do{

            boolean success=redisTemplate.delete(key);
            if(success){
                i=tryNumber;
                log.info("delete redis key success,key={}",key);
            }else{
                log.warn("delete redis key fail,key={}",key);
                i++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(),e);
                }
            }
        }while (i<tryNumber);

    }
}
