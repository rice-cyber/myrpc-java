package client.proxy;

import client.circuitBreaker.CircuitBreaker;
import client.circuitBreaker.CircuitBreakerProvider;
import client.retry.GuavaRetry;
import client.rpcClient.RpcClient;
import client.rpcClient.impl.NettyRpcClient;
import client.serviceCenter.ServiceCenter;
import client.serviceCenter.impl.ZKServiceCenter;
import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Slf4j
public class ClientProxy implements InvocationHandler {

    private RpcClient client;
    private ServiceCenter serviceCenter;
    private CircuitBreakerProvider circuitBreakerProvider;
    public ClientProxy() throws InterruptedException {
        this.client = new NettyRpcClient();
        this.serviceCenter = new ZKServiceCenter();
        this.circuitBreakerProvider = new CircuitBreakerProvider();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .build();

        CircuitBreaker circuitBreaker = circuitBreakerProvider.getCircuitBreaker(method.getName());
        if (!circuitBreaker.allowRequest()){
            log.info("Circuit breaker not allow request");
            return null;
        }
        RpcResponse rpcResponse ;
        if (serviceCenter.checkRetry(rpcRequest.getInterfaceName())){
            rpcResponse = new GuavaRetry().sendServiceWithRetry(rpcRequest,client);
        }else {
            rpcResponse = client.sendRequest(rpcRequest);
        }

        if (rpcResponse.getCode() == 200){
            circuitBreaker.readSuccess();
        }else if (rpcResponse.getCode() == 500){
            circuitBreaker.readFailure();
        }


        return rpcResponse.getData();
    }

    public <T>T getProxy(Class<T> clazz) throws InterruptedException {
        Object o =  Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ClientProxy());
        return (T) o;
    }
}
