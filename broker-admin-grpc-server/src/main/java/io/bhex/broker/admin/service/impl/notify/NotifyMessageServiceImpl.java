package io.bhex.broker.admin.service.impl.notify;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.google.common.collect.*;
import io.bhex.base.admin.AuthInfo;
import io.bhex.base.admin.ListAllAuthByUserIdReply;
import io.bhex.base.admin.ListAllAuthByUserIdRequest;
import io.bhex.bhop.common.service.AdminUserAuthService;
import io.bhex.broker.admin.service.NotifyMessageService;
import io.bhex.broker.admin.util.SlackMessageUtil;
import io.bhex.broker.grpc.admin.NotifyType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotifyMessageServiceImpl implements NotifyMessageService {

    @Resource(name = "notifyRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Resource
    private AdminUserAuthService adminUserAuthService;

    @Resource
    private SlackMessageUtil slackMessageUtil;

    @Resource
    private NotifyUtil notifyUtil;

    private String runEnv;

    private String TAG_OTC = "otc-appeal";

    private String TAG_KYC = "kyc-application";

    private String TAG_WITHDRAW = "withdraw";

    private String TAG_OTC_CONFIRM = "otc-unconfirm";

    private String FIELD_BRONTIME = "borntime";

    private String FIELD_NOTIFY = "notify";

    private String FIELD_TOTAL = "total";

    private String BHEX = "bhex_com";

    private Multimap<Long, String> authBizTypeMap = ArrayListMultimap.create();

    private Map<Integer, String> notifyTypeMap = Maps.newHashMap();

    /**
     * 构建权限id与消息提示的业务标签映射
     */
    @PostConstruct
    public void init() {

        authBizTypeMap.put(20L, TAG_KYC);
        authBizTypeMap.put(21L, TAG_WITHDRAW);
        authBizTypeMap.put(30L, TAG_OTC);
        authBizTypeMap.put(30L, TAG_OTC_CONFIRM);

        notifyTypeMap.put(0, TAG_KYC);
        notifyTypeMap.put(1, TAG_OTC);
        notifyTypeMap.put(2, TAG_WITHDRAW);
        notifyTypeMap.put(3, TAG_OTC_CONFIRM);

        runEnv = notifyUtil.getRunEnv();
    }

    /**
     * 缓存提示消息
     *
     * @param bizType
     * @param brokerId
     */
    @Deprecated
    public void saveNotify(String bizType, Long brokerId) {

        String key = buildKey(bizType, brokerId);
        log.info("notify key={}", key);
        boolean exist = redisTemplate.hasKey(key);
        if (exist) {
            String borntimeStr = (String) redisTemplate.opsForHash().get(key, FIELD_BRONTIME);
            Long bornTime = Long.parseLong(borntimeStr);
            long diff = System.currentTimeMillis() - bornTime.longValue();
            //5s内消息忽略
            if (diff < 5000) {
                return;
            }

            String totalStr = (String) redisTemplate.opsForHash().get(key, FIELD_TOTAL);
            Long total = Long.parseLong(totalStr);
            cacheNotifyMessage(key, System.currentTimeMillis(), total + 1);
        } else {
            cacheNotifyMessage(key, System.currentTimeMillis(), 1L);
        }

        sendWeixinMessage(bizType);

    }

    //bhex发送微信消息
    private void sendWeixinMessage(String bizType) {

        log.info("Send wx msg,begin,tag={}", bizType);
        if (!notifyUtil.getActivityProfile().equals(BHEX)) {
            return;
        }

        String message = "";
        if (bizType.equals(TAG_OTC)) {
            message = "现在有OTC申述";
        }

        if (bizType.equals(TAG_KYC)) {
            message = "现在有kyc申请";
        }

        if (bizType.equals(TAG_WITHDRAW)) {
            message = "现在有提现申请";
        }

        if (bizType.equals(TAG_OTC_CONFIRM)) {
            message = "现在有otc订单长时间未确认";
        }

        message += "，请尽快处理";
        try {
            slackMessageUtil.sendSlackMsg(message);
            log.info("Send wx msg,success,tag={}", bizType);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 初始化提示消息缓存
     *
     * @param key
     */
    private void cacheNotifyMessage(String key, long borntime, long total) {
        Map<String, Object> inMap = Maps.newHashMap();
        inMap.put(FIELD_BRONTIME, borntime + "");
        inMap.put(FIELD_NOTIFY, "true");
        inMap.put(FIELD_TOTAL, total + "");
        redisTemplate.opsForHash().putAll(key, inMap);
    }

    /**
     * 删除提示消息
     *
     * @param bizType
     * @param brokerId
     */
    public void clearNotify(String bizType, Long brokerId) {

        String key = buildKey(bizType, brokerId);
        redisTemplate.delete(key);
    }

    private String buildKey(String bizType, Long brokerId) {
        return Joiner.on('-').join(Lists.newArrayList(runEnv, "admin", bizType, brokerId.toString()));
    }

    @Override
    public Map<String, Integer> listNotification(Long userId, Long orgId) {

        //查询权限
        List<Long> authIds = listUserAuthIds(orgId, userId);
        if (CollectionUtils.isEmpty(authIds)) {
            return Maps.newHashMap();
        }

        //构建业务类型与缓存key的映射
        Set<Pair<String, String>> pairs = Sets.newHashSet();
        authIds.forEach(id -> {
            Collection<String> bizTypes = authBizTypeMap.get(id);
            bizTypes.forEach(bt -> {
                String key = buildKey2(bt, orgId);
                Pair<String, String> kv = Pair.of(bt, key);
                pairs.add(kv);
            });
        });

        Map<String, Integer> result = Maps.newHashMap();

        //查询提示消息
        pairs.forEach(kv -> {
            String totalStr = (String) redisTemplate.opsForValue().get(kv.getValue());
            int number = 0;
            if (StringUtils.isNotEmpty(totalStr)) {
                try {
                    number = Integer.valueOf(totalStr);
                } catch (Exception e) {
                    log.error("totalStr={},error={}", totalStr, e.getMessage());
                    number = 0;
                }

            }
            result.put(kv.getKey(), number);

        });

        return result;

    }

    @Override
    public void decreNotification(long brokerId, NotifyType type) {

        log.info("decreNotification,brokerId={},notifyType={}", brokerId, type);
        String bizType = notifyTypeMap.getOrDefault(type.getNumber(), "null");
        String key = buildKey2(bizType, brokerId);
        log.info("decreNotification,key={}", key);
        boolean exists = redisTemplate.hasKey(key);
        if (exists) {
            log.info("Key exist,perform decre,key={}", key);
            Long total = redisTemplate.opsForValue().decrement(key);
            log.info("perform decre,key={},total={}", key, total);
            if (total.longValue() < 0) {
                redisTemplate.delete(key);
            }
        }
    }

    @Override
    public void clearNotification(long brokerId, NotifyType type) {

        log.info("clearNotification,brokerId={},notifyType={}", brokerId, type);
        String bizType = notifyTypeMap.getOrDefault(type.getNumber(), "null");
        String key = buildKey2(bizType, brokerId);
        log.info("clearNotification,key={}", key);
        Boolean success = redisTemplate.delete(key);
        log.info("perform clear,key={},success={}", key, success);
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    public void scanNotify() {

        String lockKey = runEnv + "-notify";
        boolean lock = notifyUtil.getLock(lockKey, 120);
        if (!lock) {
            log.warn("Could not get lock to perform notice");
            return;
        }

        log.warn("To perform notice");
        notifyTypeMap.values().forEach(tag -> {
            String key = buildNotifyKey(tag, 6002L);
            boolean exists = redisTemplate.hasKey(key);
            if (exists) {
                sendWeixinMessage(tag);
                redisTemplate.delete(key);
            }
        });

        notifyUtil.releaseLock(lockKey, 3);

    }

    /**
     * 查询用户权限id集合
     *
     * @param orgId
     * @param userId
     * @return
     */
    private List<Long> listUserAuthIds(Long orgId, Long userId) {

        String key = Joiner.on('-').join(Lists.newArrayList(runEnv, "admin", orgId.toString(), userId.toString()));

        List<Long> authIds = null;
        String listStr = redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotEmpty(listStr)) {
            authIds = JSON.parseArray(listStr, Long.class);
            return authIds;
        }

        ListAllAuthByUserIdRequest req = ListAllAuthByUserIdRequest.newBuilder()
                .setOrgId(orgId)
                .setUserId(userId)
                .build();

        ListAllAuthByUserIdReply resp = adminUserAuthService.listAllAuthByUserId(req);
        List<AuthInfo> authList = resp.getAuthPathInfosList();
        authIds = authList.stream()
                .filter(i -> authBizTypeMap.containsKey(i.getAuthId())).map(i -> i.getAuthId())
                .collect(Collectors.toList());

        redisTemplate.opsForValue().set(key, JSON.toJSONString(authIds), 120, TimeUnit.SECONDS);
        return authIds;
    }

    protected String buildKey2(String tag, Long brokerId) {
        return Joiner.on("-").join(Lists.newArrayList(runEnv, tag, brokerId.toString()));
    }

    protected String buildNotifyKey(String tag, Long brokerId) {
        return Joiner.on("-").join(Lists.newArrayList(runEnv, tag, "notify", brokerId.toString()));
    }

}
