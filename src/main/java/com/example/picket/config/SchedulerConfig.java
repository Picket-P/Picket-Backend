package com.example.picket.config;

import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT5M")
public class SchedulerConfig {
    @Bean
    public RedisLockProvider lockProvider(RedisConnectionFactory redisConnectionFactory) {
        return new RedisLockProvider(redisConnectionFactory);
    }

    @Bean
    public ThreadPoolTaskScheduler configureScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(5); // 스레드 풀 크기
        threadPoolTaskScheduler.setThreadGroupName("scheduler thread pool"); // 스레드 그룹명
        threadPoolTaskScheduler.setThreadNamePrefix("picket-scheduler-"); // 스레드명 접두사
        threadPoolTaskScheduler.initialize(); // 스케줄러 초기화
        return threadPoolTaskScheduler;
    }
}
