package client.circuitBreaker;

import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreaker {

    private volatile CircuitBreakerState state = CircuitBreakerState.CLOSED;
    private AtomicInteger failureCount = new AtomicInteger(0);
    private AtomicInteger successCount = new AtomicInteger(0);
    private AtomicInteger requestCount = new AtomicInteger(0);

    private final int failureThreshold;

    private final double halfOpenSuccessRate;

    private final  long retryTimePeriod;

    private volatile long lastFailureTime = 0;

    public CircuitBreaker(int failureThreshold, double halfOpenSuccessRate, long retryTimePeriod) {
        this.failureThreshold = failureThreshold;
        this.halfOpenSuccessRate = halfOpenSuccessRate;
        this.retryTimePeriod = retryTimePeriod;
    }

    public synchronized boolean allowRequest(){
        long currentTime = System.currentTimeMillis();
        switch(state){
            case OPEN:
                if(currentTime - lastFailureTime > retryTimePeriod){
                    state = CircuitBreakerState.HALF_OPEN;
                    resetCounts();
                    return true;
                }
                return  false;
            case HALF_OPEN:
                requestCount.incrementAndGet();
                return  true;
            case CLOSED:
            default:
                return true;
        }
    }

    public synchronized void readSuccess(){
        if (state == CircuitBreakerState.HALF_OPEN){
            successCount.incrementAndGet();
            if (successCount.get()>halfOpenSuccessRate*requestCount.get()){
                state = CircuitBreakerState.CLOSED;
                resetCounts();
            }
        }else {
            resetCounts();
        }
    }

    public synchronized void readFailure(){
        failureCount.incrementAndGet();
        lastFailureTime = System.currentTimeMillis();
        if (state == CircuitBreakerState.HALF_OPEN){
            state = CircuitBreakerState.OPEN;
            lastFailureTime = System.currentTimeMillis();
        }else if (failureCount.get()>failureThreshold){
            state = CircuitBreakerState.OPEN;
        }
    }

    private void resetCounts(){
        failureCount.set(0);
        successCount.set(0);
        requestCount.set(0);
    }


}

enum CircuitBreakerState {
    CLOSED,
    OPEN,
    HALF_OPEN
}
