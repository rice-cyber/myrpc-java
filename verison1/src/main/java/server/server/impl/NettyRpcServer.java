package server.server.impl;

import client.netty.nettyInitializer.NettyClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import server.netty.initializer.NettyRpcSeverInitializer;
import server.provider.ServiceProvider;
import server.server.RpcServer;

@Slf4j
public class NettyRpcServer implements RpcServer {

    private ServiceProvider serviceProvider;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    private Channel serviceChannel;


    public NettyRpcServer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void start(int port) {
         bossGroup = new NioEventLoopGroup(1);
         workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new NettyRpcSeverInitializer(serviceProvider));

            ChannelFuture channelFuture = bootstrap.bind(port).sync();
            serviceChannel = channelFuture.channel();
            channelFuture.channel().closeFuture().addListener(future -> shutDown());
        }catch (InterruptedException e){
            log.error("netty server start error",e);
            shutDown();
        }
    }

    @Override
    public void stop() {
        if (serviceChannel != null) {
            serviceChannel.close();
        }

    }

    private void shutDown() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

    }
}
