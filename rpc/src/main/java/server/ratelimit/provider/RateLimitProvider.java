package server.ratelimit.provider;

import server.ratelimit.RateLimit;
import server.ratelimit.impl.TokenBucketRateLimiter;

import java.util.HashMap;
import java.util.Map;

public class RateLimitProvider {

    private Map<String, RateLimit> rateLimitMap = new HashMap<>();

    public RateLimit getRateLimit(String interfaceName) {
        if (!rateLimitMap.containsKey(interfaceName)) {
            RateLimit rateLimit = new TokenBucketRateLimiter(100,10);
            rateLimitMap.put(interfaceName, rateLimit);
            return rateLimit;
        }
        return rateLimitMap.get(interfaceName);
    }
}
