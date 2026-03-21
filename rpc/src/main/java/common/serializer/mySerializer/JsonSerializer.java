package common.serializer.mySerializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import common.message.RpcRequest;
import common.message.RpcResponse;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class JsonSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        return JSON.toJSONBytes(obj);
    }

    @Override
    public Object deserialize(byte[] bytes,int messageType) {
        Object obj = null;

        switch (messageType){
            case 0:
                RpcRequest rpcRequest = JSON.parseObject(bytes,RpcRequest.class);
                Object[] objects = new Object[rpcRequest.getParams().length];
                for (int i = 0; i < objects.length; i++) {
                    Class<?> paramType = rpcRequest.getParamTypes()[i];
                    if (!paramType.isAssignableFrom(rpcRequest.getParams()[i].getClass())) {
                        objects[i] = JSONObject.toJavaObject((JSONObject)rpcRequest.getParams()[i],paramType);
                    }else  {
                        objects[i] = rpcRequest.getParams()[i];
                    }
                }
                rpcRequest.setParams(objects);
                obj = rpcRequest;
                break;
            case 1:
                RpcResponse rpcResponse = JSON.parseObject(bytes,RpcResponse.class);
                Class<?> dataType = rpcResponse.getDataType();

                if (!dataType.isAssignableFrom(rpcResponse.getData().getClass())) {
                    rpcResponse.setData(JSON.toJavaObject((JSONObject)rpcResponse.getData(),dataType));
                }else   {
                    rpcResponse.setData(rpcResponse.getData());
                }
                obj = rpcResponse;
                break;
            default:
                log.error("not support message type");
        }
        return obj;
    }

    @Override
    public int getType() {
        return 1;
    }
}
