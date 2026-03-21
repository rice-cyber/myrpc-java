package server.netty.initializer;

import common.serializer.myCode.MyDecoder;
import common.serializer.myCode.MyEncoder;
import common.serializer.mySerializer.JsonSerializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import server.netty.handler.NettyRpcServerHandler;
import server.provider.ServiceProvider;

public class NettyRpcSeverInitializer extends ChannelInitializer<SocketChannel> {

    private ServiceProvider serviceProvider;

    public NettyRpcSeverInitializer(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void initChannel(SocketChannel ch)  {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new MyEncoder(new JsonSerializer()));
        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new NettyRpcServerHandler(serviceProvider));
    }
}
