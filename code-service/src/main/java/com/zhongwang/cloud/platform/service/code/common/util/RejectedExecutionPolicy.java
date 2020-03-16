package com.zhongwang.cloud.platform.service.code.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class RejectedExecutionPolicy {

    private static final Map<String, RejectedExecutionHandler> map = new HashMap<>();

    static {
        map.put("CallerRunsPolicy", new ThreadPoolExecutor.CallerRunsPolicy());
        map.put("AbortPolicy", new ThreadPoolExecutor.AbortPolicy());
        map.put("DiscardPolicy", new ThreadPoolExecutor.DiscardPolicy());
        map.put("DiscardOldestPolicy", new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    public static RejectedExecutionHandler rejectedExecutionPolicy(String policy) {
        return map.get(policy);
    }

}
