package client.proxy;

import client.rpcClient.RpcClient;
import client.rpcClient.impl.NettyRpcClient;
import common.message.RpcRequest;
import common.message.RpcResponse;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class ClientProxy implements InvocationHandler {

    private RpcClient client;
    public ClientProxy() {
        this.client = new NettyRpcClient();
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .params(args)
                .paramTypes(method.getParameterTypes())
                .build();
        RpcResponse rpcResponse = client.sendRequest(rpcRequest);
        return rpcResponse.getData();
    }

    public <T>T getProxy(Class<T> clazz) {
        Object o =  Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ClientProxy());
        return (T) o;
    }
}
