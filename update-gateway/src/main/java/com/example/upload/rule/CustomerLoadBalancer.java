package com.example.upload.rule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class CustomerLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    private final String serviceId;

    public CustomerLoadBalancer(String serviceId, ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        if (serviceInstanceListSupplierProvider != null) {
            ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
                    .getIfAvailable(NoopServiceInstanceListSupplier::new);
            return supplier.get().next().map(instance -> getInstanceResponse(request, instance));
        }
        return null;
    }

    private Response<ServiceInstance> getInstanceResponse(
            Request request,
            List<ServiceInstance> instances) {
        if (instances.isEmpty()) {
            log.warn("No servers available for service: " + this.serviceId);
            return new EmptyResponse();
        }
        ServiceInstance instance = instances.get(10 % instances.size());

        return new DefaultResponse(instance);
    }

//    public ServiceInstance choose(ServerWebExchange exchange, DiscoveryClient discoveryClient) {
//        URI originalUrl = (URI) exchange.getAttributes().get(GATEWAY_REQUEST_URL_ATTR);
//        ServerHttpRequest request = exchange.getRequest();
//        String path = request.getPath().value();
//        String instancesId = originalUrl.getHost();
//        if(instancesId.equals("upload-service")){
//            if (path.contains("/api/v1/upload/test")) {
//                try {
//                    List<ServiceInstance> instances = discoveryClient.getInstances(instancesId);
//                    String uuid = request.getHeaders().get("uuid").get(0);
//                    int hash = uuid.hashCode() >>> 16;
//                    int index = hash % instances.size();
//                    return instances.get(index);
//                } catch (Exception e) {
//                    //do nothing
//                }
//            }
//        }
//}
}
