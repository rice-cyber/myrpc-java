package server.netty.handler;

import common.message.RpcRequest;
import common.message.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import server.provider.ServiceProvider;
import server.ratelimit.RateLimit;
import server.ratelimit.provider.RateLimitProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class NettyRpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private ServiceProvider serviceProvider;


    public NettyRpcServerHandler(ServiceProvider serviceProvider) {
        this.serviceProvider = serviceProvider;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        RpcResponse response = handle((RpcRequest) msg);
        ctx.writeAndFlush(response);
        ctx.close();
    }


    private RpcResponse handle(RpcRequest request) {

        RateLimit rateLimit = serviceProvider.getRateLimitProvider().getRateLimit(request.getInterfaceName());
        if (!rateLimit.getToken()){
            log.info("服务限流！！");
            return RpcResponse.fail();
        }

        Object service= serviceProvider.getService(request.getInterfaceName());
        try {
            Method method = service.getClass().getMethod(request.getMethodName(),request.getParamTypes());
            Object[] params = request.getParams();
            Object response = method.invoke(params);
            return RpcResponse.success(response);
        }catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e ) {
            log.error("调用rpc服务失败",e);
            return RpcResponse.fail();
        }

    }

}
