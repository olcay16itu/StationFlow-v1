package com.stationflow.backend.security.services;

import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {
    
    private final Map<String, RequestBucket> buckets = new ConcurrentHashMap<>();
    
    // 5 requests per hour
    private static final int MAX_REQUESTS = 5;
    private static final long TIME_WINDOW_MILLIS = TimeUnit.HOURS.toMillis(1);

    public boolean isAllowed(String key) {
        RequestBucket bucket = buckets.computeIfAbsent(key, k -> new RequestBucket());
        return bucket.tryConsume();
    }

    private static class RequestBucket {
        private int tokens;
        private long lastRefillTime;

        public RequestBucket() {
            this.tokens = MAX_REQUESTS;
            this.lastRefillTime = System.currentTimeMillis();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens > 0) {
                tokens--;
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            if (now - lastRefillTime > TIME_WINDOW_MILLIS) {
                tokens = MAX_REQUESTS;
                lastRefillTime = now;
            }
        }
    }
}
