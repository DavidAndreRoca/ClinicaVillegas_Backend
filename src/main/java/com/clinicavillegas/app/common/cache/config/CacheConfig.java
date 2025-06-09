package com.clinicavillegas.app.common.cache.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.global.ttl-minutes:60}")
    private long globalCacheTtlMinutes;

    @Value("${app.cache.global.max-size:1000}")
    private long globalCacheMaxSize;

    @Bean
    public CacheManager cacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(globalCacheMaxSize)
                .expireAfterWrite(globalCacheTtlMinutes, TimeUnit.MINUTES));
        return cacheManager;
    }
}