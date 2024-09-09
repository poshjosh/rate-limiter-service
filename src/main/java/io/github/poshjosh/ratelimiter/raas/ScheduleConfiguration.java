package io.github.poshjosh.ratelimiter.raas;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ComponentScan("io.github.poshjosh.ratelimiter.raas.persistence")
public class ScheduleConfiguration {
}