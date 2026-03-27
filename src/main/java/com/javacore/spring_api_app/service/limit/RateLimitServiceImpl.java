package com.javacore.spring_api_app.service.limit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitServiceImpl implements RateLimitService {

    private final Map<Long, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public ConsumptionProbe tryConsume(Long userId) {
        Bucket bucket = buckets.computeIfAbsent(userId, id -> createNewBucket());
        return bucket.tryConsumeAndReturnRemaining(userId);
    }

    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
                5,
                Refill.intervally(5, Duration.ofHours(1))
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
