package com.lyl.shortlink.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;


@Configuration
public class UrlExecutorConfiguration {
    @Bean
    public ExecutorService urlThreadPoolTaskExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger count = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "thread-url-" + count.incrementAndGet());
            }
        };
        int core = Runtime.getRuntime().availableProcessors();
        core = Math.min(core, 4);
        int max = core * 2 + 1;
        return new ThreadPoolExecutor(
                core,
                max,
                3,
                java.util.concurrent.TimeUnit.SECONDS,
                new java.util.concurrent.LinkedBlockingQueue<>(40),
                threadFactory,
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        //此方法返回可用处理器的虚拟机的最大数量; 不小于1
//        int core = Runtime.getRuntime().availableProcessors();
//        core = Math.min(core, 4);
//        //设置核心线程数
//        executor.setCorePoolSize(core);
//        //设置最大线程数
//        executor.setMaxPoolSize(core * 2 + 1);
//        //除核心线程外的线程存活时间
//        executor.setKeepAliveSeconds(3);
//        //如果传入值大于0，底层队列使用的是LinkedBlockingQueue,否则默认使用SynchronousQueue
//        executor.setQueueCapacity(40);
//        //线程名称前缀
//        executor.setThreadNamePrefix("thread-url");
//        //设置拒绝策略
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//        return executor;
    }
}
