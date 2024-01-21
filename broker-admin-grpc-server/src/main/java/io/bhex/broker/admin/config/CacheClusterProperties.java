/*
 ************************************
 * @项目名称: guild-grpc
 * @文件名称: CacheClusterConfig
 * @Date 2018/11/04
 * @Author will.zhao@bhex.io
 * @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 * 注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的。
 **************************************
 */
package io.bhex.broker.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ConfigurationProperties(prefix = "spring.redis-cluster")
public class CacheClusterProperties {

    private List<String> shards = new ArrayList<String>();

    public List<String> getShards() {
        return this.shards;
    }

}

