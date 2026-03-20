package client.rpcClient.impl;

import client.netty.nettyInitializer.NettyClientInitializer;
import client.rpcClient.RpcClient;
import client.serviceCenter.ServiceCenter;
import client.serviceCenter.impl.ZKServiceCenter;
import common.message.RpcRequest;
import common.message.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyRpcClient implements RpcClient {


    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;

    private ServiceCenter serviceCenter;
    public NettyRpcClient() {
        this.serviceCenter = new ZKServiceCenter();
    }

    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new NettyClientInitializer());
    }

    @Override
    public RpcResponse sendRequest(RpcRequest request) {

        InetSocketAddress address = serviceCenter.serviceDiscovery(request.getInterfaceName());
        String host = address.getHostName();
        int port = address.getPort();
        try{
            ChannelFuture channelFuture = bootstrap.connect(host, port);
            channelFuture.channel().writeAndFlush(request);
            channelFuture.channel().closeFuture().sync();
            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RpcResponse");
            return channelFuture.channel().attr(key).get();
        }catch (InterruptedException e){
            log.error("netty 服务器通信中断",e);
        }
        return null;
    }
}
