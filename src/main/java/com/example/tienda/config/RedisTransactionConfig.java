package com.example.tienda.config;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RedisTransactionConfig {
    @Bean RedisCacheManagerBuilderCustomizer transactionAwareCache() {
        return builder -> builder.transactionAware();
    }
}
