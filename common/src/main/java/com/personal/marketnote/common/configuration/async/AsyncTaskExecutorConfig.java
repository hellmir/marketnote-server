package com.personal.marketnote.common.configuration.async;

import com.personal.marketnote.common.logging.MdcTaskDecorator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncTaskExecutorConfig {

    private final MdcTaskDecorator mdcTaskDecorator;

    // 사용처는 향후 @Async 도입 시 @Async("mdcAsyncTaskExecutor") 명시. 현재 코드베이스 사용처 0건이지만 #4942 결정 사안 2번에 따라 인프라 사전 등록.
    @Bean(name = "mdcAsyncTaskExecutor")
    public ThreadPoolTaskExecutor mdcAsyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-");
        executor.setTaskDecorator(mdcTaskDecorator);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        return executor;
    }
}
