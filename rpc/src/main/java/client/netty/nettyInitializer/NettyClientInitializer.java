package client.netty.nettyInitializer;

import client.netty.handler.NettyClientHandler;
import common.serializer.myCode.MyDecoder;
import common.serializer.myCode.MyEncoder;
import common.serializer.mySerializer.JsonSerializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;


public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {


    @Override
    protected void initChannel(SocketChannel ch)  {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast(new MyEncoder(new JsonSerializer()));

        pipeline.addLast(new MyDecoder());
        pipeline.addLast(new NettyClientHandler());
    }
}
