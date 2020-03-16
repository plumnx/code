package com.zhongwang.cloud.platform.service.code.config;

import com.google.common.base.Strings;
import com.zhongwang.cloud.platform.service.code.config.bean.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SentinelServersConfig;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(Config.class)
@EnableConfigurationProperties(Redisson.class)
public class RedissonConfiguration {

    @Autowired
    private Redisson redisson;

    /**
     * 哨兵模式自动装配
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "redisson.master-name")
    RedissonClient redissonSentinel() {
        Config config = new Config();
        SentinelServersConfig serverConfig = config.useSentinelServers().addSentinelAddress(redisson.getSentinelAddresses())
                .setMasterName(redisson.getMasterName())
                .setTimeout(redisson.getTimeout())
                .setMasterConnectionPoolSize(redisson.getMasterConnectionPoolSize())
                .setSlaveConnectionPoolSize(redisson.getSlaveConnectionPoolSize());

        if (!Strings.isNullOrEmpty(redisson.getPassword())) {
            serverConfig.setPassword(redisson.getPassword());
        }
        return org.redisson.Redisson.create(config);
    }

    /**
     * 单机模式自动装配
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(name = "redisson.address")
    RedissonClient redissonSingle() {
        Config config = new Config();
        SingleServerConfig serverConfig = config.useSingleServer()
                .setDatabase(redisson.getDatabase())
                .setAddress(redisson.getAddress())
                .setTimeout(redisson.getTimeout())
                .setConnectionPoolSize(redisson.getConnectionPoolSize())
                .setConnectionMinimumIdleSize(redisson.getConnectionMinimumIdleSize());

        if (!Strings.isNullOrEmpty(redisson.getPassword())) {
            serverConfig.setPassword(redisson.getPassword());
        }

        return org.redisson.Redisson.create(config);
    }

}
