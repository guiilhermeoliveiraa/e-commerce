package com.javacore.spring_api_app.service.limit;

import io.github.bucket4j.ConsumptionProbe;

public interface RateLimitService {
    ConsumptionProbe tryConsume(Long userId);
}
