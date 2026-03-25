package server.ratelimit.impl;

import server.ratelimit.RateLimit;

import java.util.concurrent.atomic.AtomicLong;

public class TokenBucketRateLimiter implements RateLimit {

    private final long RATE;

    private volatile AtomicLong timestamp;

    private final long CAPACITY;

    private volatile AtomicLong curCapacity;

    public TokenBucketRateLimiter(long rate,int curCapacity) {
        this.RATE = rate;
        this.timestamp = new AtomicLong(System.currentTimeMillis());
        this.CAPACITY = rate;
        this.curCapacity = new AtomicLong(curCapacity);
    }


    @Override
    public  boolean getToken() {
        long current = System.currentTimeMillis();
        long oldTimeStamp = timestamp.get();
        long oldCapacity = curCapacity.get();

        if (current - oldTimeStamp >= RATE) {
            long newTokens = current - oldTimeStamp/RATE;
            long newCapacity = Math.min(oldCapacity+newTokens, CAPACITY);

            if (timestamp.compareAndSet(oldTimeStamp,current)) {
                curCapacity.set(newCapacity);
                oldCapacity = newCapacity;
            }
        }

        while(oldCapacity >0){
            if (curCapacity.compareAndSet(oldCapacity,oldCapacity -1)){
                return true;
            }
            oldCapacity = curCapacity.get();
        }
        return false;
    }
}
