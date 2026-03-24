package client.retry;

import client.rpcClient.RpcClient;
import com.github.rholder.retry.*;
import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
public class GuavaRetry {


    public RpcResponse sendServiceWithRetry(RpcRequest request,RpcClient client){
        Retryer<RpcResponse> retryer = RetryerBuilder.<RpcResponse>newBuilder()
                .retryIfException()
                .retryIfResult(response -> Objects.equals(response.getCode(),500))
                .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3))
                .withRetryListener(new RetryListener() {
                    @Override
                    public <V> void onRetry(Attempt<V> attempt) {
                        log.info("Retry attempt #{}",attempt.getAttemptNumber());
                    }
                })
                .build();
        try {
            return retryer.call(() -> client.sendRequest(request));
        }catch (Exception e){
            log.error("Send request failed",e);
        }

        return RpcResponse.fail();
    }

}
