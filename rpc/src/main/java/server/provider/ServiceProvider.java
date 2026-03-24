package server.provider;

import server.serviceRegister.ServiceRegister;
import server.serviceRegister.impl.ZKServiceRegister;


import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {

    private Map<String, Object> interfaceProvider;

    private int port;
    private String host;

    private ServiceRegister serviceRegister;

    public ServiceProvider(int port, String host) {
        this.port = port;
        this.host = host;
        this.interfaceProvider = new HashMap<>();
        this.serviceRegister = new ZKServiceRegister();
    }

    public void provideService(Object service) {
        String serviceName = service.getClass().getName();
        Class<?>[] interfaceName = service.getClass().getInterfaces();
        for (Class<?> clazz : interfaceName) {
            interfaceProvider.put(clazz.getName(), service);
            serviceRegister.register(clazz.getName(),new InetSocketAddress(host,port),false);
        }
    }

    public Object getService(String serviceName) {
        return interfaceProvider.get(serviceName);
    }



}
