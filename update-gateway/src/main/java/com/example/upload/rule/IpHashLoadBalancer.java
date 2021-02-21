package com.example.upload.rule;

import com.example.upload.filter.CustomerLbProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Slf4j
public class IpHashLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    private final CustomerLbProperties properties;
    private final DiscoveryClient discoveryClient;

    public IpHashLoadBalancer(CustomerLbProperties properties, DiscoveryClient discoveryClient) {
        this.properties = properties;
        this.discoveryClient = discoveryClient;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        ServerWebExchange exchange=(ServerWebExchange)request.getContext();
        return getInstanceResponse(exchange);

    }
    private Mono<Response<ServiceInstance>> getInstanceResponse(ServerWebExchange exchange) {
        URI originalUrl = (URI) exchange.getAttributes().get(GATEWAY_REQUEST_URL_ATTR);
        String instanceId = originalUrl.getHost();
        List<ServiceInstance> serviceInstances = getServiceInstance();
        if (serviceInstances.isEmpty()) {
            log.warn("No severs available for is empty !");
            return Mono.just(new EmptyResponse()) ;
        }
        if (properties.getLbs().stream().anyMatch(n -> originalUrl.getPath().contains(n.getUrl()))) {
            int hash = instanceId.hashCode() >>> 16;
            int index = hash % serviceInstances.size();
            return Mono.just(new DefaultResponse(serviceInstances.get(index)));
        }
        return  Mono.just(new EmptyResponse());
    }

    private List<ServiceInstance> getServiceInstance() {
        List<CustomerLbProperties.Lbs> lbsList = properties.getLbs();
        List<ServiceInstance> serviceInstances = new ArrayList<>(8);
        for (CustomerLbProperties.Lbs lb : lbsList) {
            serviceInstances.addAll(discoveryClient.getInstances(lb.getServiceId()));
        }
        return serviceInstances;
    }

}
