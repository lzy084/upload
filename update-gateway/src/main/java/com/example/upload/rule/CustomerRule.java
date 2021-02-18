package com.example.upload.rule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Slf4j
public class CustomerRule implements ICustomRule {
    @Override
    public ServiceInstance choose(ServerWebExchange exchange, DiscoveryClient discoveryClient) {
        URI originalUrl = (URI) exchange.getAttributes().get(GATEWAY_REQUEST_URL_ATTR);
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();
        String instancesId = originalUrl.getHost();
        if(instancesId.equals("upload-service")){
            if (path.contains("/api/v1/upload/test")) {
                try {
                    List<ServiceInstance> instances = discoveryClient.getInstances(instancesId);
                    String uuid = request.getHeaders().get("uuid").get(0);
                    int hash = uuid.hashCode() >>> 16;
                    int index = hash % instances.size();
                    return instances.get(index);
                } catch (Exception e) {
                    //do nothing
                }
            }
        }

        return null;
    }
}
