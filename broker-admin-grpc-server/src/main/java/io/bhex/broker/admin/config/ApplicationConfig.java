package io.bhex.broker.admin.config;


import io.bhex.broker.common.redis.StringKeySerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

@Slf4j
@Getter
@Setter
@Configuration
public class ApplicationConfig {

    @Resource
    private Environment environment;

    @Bean
    public StringRedisTemplate notifyRedisTemplate(RedisConnectionFactory redisConnectionFactory) {

        String prefix=environment.getProperty("broker.redis-key-prefix");
        if(StringUtils.isEmpty(prefix)){
            prefix="";
        }

        log.info("Redis key prefix={}",prefix);

        StringRedisTemplate template = new StringRedisTemplate();
        template.setKeySerializer(new StringKeySerializer(prefix));
        template.setHashKeySerializer(new StringKeySerializer(prefix));
        template.setConnectionFactory(redisConnectionFactory);
        return template;
    }

}
