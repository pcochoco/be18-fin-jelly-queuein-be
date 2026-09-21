package com.beyond.qiin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;
// 예외 처리 시 로그 남김
// try, catch는 spring cache에서 대신 해줌

@Slf4j
public class FailOpenCacheErrorHandler implements CacheErrorHandler {

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        logFailOpen("GET", exception, cache, key);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        logFailOpen("PUT", exception, cache, key);
    }

    // 특정 키 하나 삭제
    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        logFailOpen("EVICT", exception, cache, key);
    }

    // 해당 cache 이름에 대한 캐시를 전체 삭제
    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        logFailOpen("CLEAR", exception, cache, null);
    }

    private void logFailOpen(String operation, RuntimeException exception, Cache cache, Object key) {
        log.warn(
                "Cache operation failed. Continuing without cache. operation={}, cacheName={}, key={}, exceptionType={}, error={}",
                operation,
                cache.getName(),
                summarizeKey(key),
                exception.getClass().getName(),
                exception.getMessage(),
                exception);
    }

    private String summarizeKey(Object key) {
        if (key == null) {
            return "none";
        }

        String keyString = String.valueOf(key);
        return "type=%s,hash=%s,length=%d"
                .formatted(
                        key.getClass().getSimpleName(), Integer.toHexString(keyString.hashCode()), keyString.length());
    }
}
