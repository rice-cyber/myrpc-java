package server.provider;

import server.ratelimit.provider.RateLimitProvider;
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

    private RateLimitProvider rateLimitProvider;

    public ServiceProvider(int port, String host) {
        this.port = port;
        this.host = host;
        this.interfaceProvider = new HashMap<>();
        this.serviceRegister = new ZKServiceRegister();
        this.rateLimitProvider = new RateLimitProvider();
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

    public RateLimitProvider getRateLimitProvider() {
        return rateLimitProvider;
    }



}
