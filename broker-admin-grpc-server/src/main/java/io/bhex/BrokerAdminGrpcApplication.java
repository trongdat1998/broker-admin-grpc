package io.bhex;

import io.bhex.base.idgen.snowflake.SnowflakeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.Resource;

/**
 * @ProjectName: broker
 * @Package: io.bhex.broker.admin
 * @Author: ming.xu
 * @CreateDate: 23/08/2018 12:14 PM
 * @Copyright（C）: 2018 BHEX Inc. All rights reserved.
 */
@EnableScheduling
@EnableConfigurationProperties
@SpringBootApplication(scanBasePackages = {"io.bhex"})
public class BrokerAdminGrpcApplication {


    @Resource
    private Environment environment;

    @Bean(name = "idGenerator")
    public SnowflakeGenerator orderIdGenerator() {
        long datacenterId = environment.getProperty("snowflake.datacenterId", Long.class);
        long workerId = environment.getProperty("snowflake.workerId", Long.class);

        return SnowflakeGenerator.newInstance(datacenterId, workerId);
    }

    public static void main(String[] args) {
        SpringApplication.run(BrokerAdminGrpcApplication.class);
    }
}
