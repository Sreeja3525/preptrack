package com.preptrack.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

// Configures the thread pool used by all @Async methods (e.g., weekly report generation)
// Without this, @Async would use Spring's SimpleAsyncTaskExecutor which creates a new thread
// per invocation — fine for demos, terrible under load
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Value("${async.core-pool-size}")
    private int corePoolSize;

    @Value("${async.max-pool-size}")
    private int maxPoolSize;

    @Value("${async.queue-capacity}")
    private int queueCapacity;

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);    // always-alive threads
        executor.setMaxPoolSize(maxPoolSize);      // scale up under load
        executor.setQueueCapacity(queueCapacity);  // tasks wait here if all threads busy
        executor.setThreadNamePrefix("preptrack-async-");
        executor.initialize();
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return taskExecutor();
    }
}
