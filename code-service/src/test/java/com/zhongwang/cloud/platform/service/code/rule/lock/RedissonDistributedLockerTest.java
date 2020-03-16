package com.zhongwang.cloud.platform.service.code.rule.lock;

import com.zhongwang.cloud.platform.service.code.common.lock.DistributedLocker;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.zhongwang.cloud.platform.service.code.rule.lock.RedissonDistributedLockerTest.RedisKeyUtil.DISTRIBUTED_LOCK_NO;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.junit.Assert.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK, properties = {
        "spring.cloud.config.enabled:false",
        "spring.config.name:unit"
})
@AutoConfigureMockMvc
@Slf4j
public class RedissonDistributedLockerTest {

    private int lockTime = 3000;

    @Autowired
    private DistributedLocker distributedLocker;

    @Getter@Setter
    private Integer count = 0;

    /**
     * 测试锁的可靠性：
     * 1.开启1000个线程对同一个资源连续加锁1000次，资源的自增应该是线性的并符合预期。
     *
     * @throws InterruptedException
     */
    @Test
    public void lock_reliable_should_be_ok() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(1000);
        for (int i = 0; i < 1000; i++) {
            executorService.execute(() -> {
                try {
                    distributedLocker.lock(() -> {
                        Integer count = getCount();
                        setCount(count + 1);
                    }, DISTRIBUTED_LOCK_NO, lockTime);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, NANOSECONDS);
    }

    /**
     * 测试锁的安全性：
     * 1.线程对资源的占用时间超过预期，需要及时释放
     *
     */
    @Test
    public void lock_safety_should_be_ok() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                try {
                    distributedLocker.lock(() -> {
                        Integer count = getCount();
                        setCount(count + 1);
                        Thread.sleep((new Random().nextInt(9) % 9 + 1) * 100);
                    }, DISTRIBUTED_LOCK_NO, 300);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, NANOSECONDS);
    }

    class RedisKeyUtil {
        public static final String DISTRIBUTED_LOCK_NO = "LOCK_NO";
    }

}
