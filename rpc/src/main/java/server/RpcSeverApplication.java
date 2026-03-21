package server;

import server.provider.ServiceProvider;
import server.server.RpcServer;
import server.server.impl.NettyRpcServer;

public class RpcSeverApplication {

    public static void main(String[] args) {
        ServiceProvider serviceProvider = new ServiceProvider(9999,"127.0.0.1");
        // todo根据配置读取
        RpcServer rpcServer = new NettyRpcServer(serviceProvider);
        rpcServer.start(9999);
    }
}
