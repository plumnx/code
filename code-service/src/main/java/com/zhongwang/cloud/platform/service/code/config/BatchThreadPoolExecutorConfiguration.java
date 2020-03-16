package com.zhongwang.cloud.platform.service.code.config;

import com.zhongwang.cloud.platform.service.code.common.util.RejectedExecutionPolicy;
import com.zhongwang.cloud.platform.service.code.config.bean.BatchThreadPool;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableAsync
@EnableConfigurationProperties(BatchThreadPool.class)
public class BatchThreadPoolExecutorConfiguration {

    private final BatchThreadPool batchThreadPool;

    @Autowired
    public BatchThreadPoolExecutorConfiguration(BatchThreadPool batchThreadPool) {
        this.batchThreadPool = batchThreadPool;
    }

    @Bean(name = "threadPool")
    public ThreadPool threadPool() {
        return new ThreadPool(batchThreadPool);
    }

    public static class ThreadPool {

        @Getter
        private ExecutorService executor;

        public ThreadPool(BatchThreadPool batchThreadPool) {
            this.executor = new ThreadPoolExecutor(
                    batchThreadPool.getCorePoolSize(),
                    batchThreadPool.getMaxPoolSize(),
                    batchThreadPool.getKeepAliveSeconds(),
                    TimeUnit.SECONDS,
                    new LinkedBlockingQueue<>(),
                    RejectedExecutionPolicy.rejectedExecutionPolicy(
                            batchThreadPool.getRejectedExecutionHandler()));
        }

        public int getAlivedCount() {
            return ((ThreadPoolExecutor)this.executor).getMaximumPoolSize() - ((ThreadPoolExecutor)this.executor).getActiveCount();
        }

        public void execute(Runnable runnable) {
            this.executor.execute(runnable);
        }
    }

}
