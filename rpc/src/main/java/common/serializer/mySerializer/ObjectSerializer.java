package common.serializer.mySerializer;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class ObjectSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray();
            oos.close();
            bos.close();
        } catch (IOException e) {
            log.error("对象序列化失败", e);
        }
        return bytes;
    }

    @Override
    public Object deserialize(byte[] bytes,int MessageType) {
        Object obj = null;
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        try {
            ObjectInputStream ois = new ObjectInputStream(bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        }catch (IOException | ClassNotFoundException e){
            log.error("反序列化失败",e);
        }
        return obj;
    }

    @Override
    public int getType() {
        return 0;
    }
}
