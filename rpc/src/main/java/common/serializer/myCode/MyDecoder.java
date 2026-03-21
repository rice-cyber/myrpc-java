package common.serializer.myCode;

import common.serializer.mySerializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MyDecoder extends ByteToMessageDecoder {


    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out)  {
        short MessageType = in.readShort();
        short type = in.readShort();
        int length = in.readInt();
        Serializer serializer = Serializer.getSerializerByCode(type);
        if (serializer == null) {
            log.error("not support serializer:{}",type);
            return;
        }
        byte[] bytes = new byte[length];
        in.readBytes(bytes);
        Object obj = serializer.deserialize(bytes,MessageType);
        out.add(obj);

    }
}
