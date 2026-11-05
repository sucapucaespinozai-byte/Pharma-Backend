package com.example.tienda.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.core.Ordered;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching(order = Ordered.LOWEST_PRECEDENCE - 1)
public class CacheConfig implements CachingConfigurer {
    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);


    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            private void advertir(String operacion, Cache cache, RuntimeException ex) {
                log.warn("Redis no disponible: operacion={}, cache={}, error={}. Se continua con Oracle.",
                        operacion, cache.getName(), ex.getClass().getSimpleName());
            }
            @Override
            public void handleCacheGetError(RuntimeException ex, Cache cache, Object key) {
                advertir("leer", cache, ex);
            }
            @Override
            public void handleCachePutError(RuntimeException ex, Cache cache, Object key, Object value) {
                advertir("guardar", cache, ex);
            }
            @Override
            public void handleCacheEvictError(RuntimeException ex, Cache cache, Object key) {
                advertir("invalidar", cache, ex);
            }
            @Override
            public void handleCacheClearError(RuntimeException ex, Cache cache) {
                advertir("limpiar", cache, ex);
            }
        };
    }
}
