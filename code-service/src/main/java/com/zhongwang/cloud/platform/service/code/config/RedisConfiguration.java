package com.zhongwang.cloud.platform.service.code.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import com.zhongwang.cloud.platform.bamboo.web.common.PrefixRedisKeySerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.scripting.support.ResourceScriptSource;

//@EnableCaching
@Configuration
@ConditionalOnExpression("'${code.config.serial.policy.strategy}' == 'CACHE' || '${code.config.serial.policy.strategy}' == 'DB_CACHE'")
public class RedisConfiguration {

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Value("${spring.redis.prefix:${spring.application.name}}")
    private String prefix;

    @Bean
    @ConditionalOnMissingBean(name = "redisTemplate")
    public RedisTemplate<Object, Object> redisTemplate() {
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        GenericFastJsonRedisSerializer fastJsonSerializer = new GenericFastJsonRedisSerializer();

        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new PrefixRedisKeySerializer(prefix));
        redisTemplate.setValueSerializer(fastJsonSerializer);
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(fastJsonSerializer);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    @ConditionalOnMissingBean(StringRedisTemplate.class)
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);
        template.setKeySerializer(new PrefixRedisKeySerializer(prefix));
        return template;
    }

    /**
     * 初始化的脚本：从缓存中获取当前流水号和流水上限
     *
     * @return
     */
    @Bean
    public RedisScript<Integer[]> fetchCodeRuleSerialNoScript() {
        DefaultRedisScript<Integer[]> fetchCodeRuleSerialNoScript = new DefaultRedisScript<>();
        fetchCodeRuleSerialNoScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("lua/fetchCodeRuleSerialNo.lua")));
        fetchCodeRuleSerialNoScript.setResultType(Integer[].class);
        return fetchCodeRuleSerialNoScript;
    }

    @Bean
    public CacheManager cacheManager() {
        return new RedisCacheManager(redisTemplate());
    }

    @Bean
    public OpenEntityManagerInViewFilter openEntityManagerInViewFilter() {
        return new OpenEntityManagerInViewFilter();
    }

}
