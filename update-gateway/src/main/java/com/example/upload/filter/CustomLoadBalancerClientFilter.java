package com.example.upload.filter;

import com.example.upload.rule.IpHashLoadBalancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.DelegatingServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.*;

/**
 * 自定义负载均衡Filter
 */
@Slf4j
public class CustomLoadBalancerClientFilter extends ReactiveLoadBalancerClientFilter {
    private final CustomerLbProperties customerLbProperties;
    private final LoadBalancerProperties properties;
    private final LoadBalancerClientFactory clientFactory;
    private final IpHashLoadBalancer loadBalancer;

    public CustomLoadBalancerClientFilter(LoadBalancerClientFactory clientFactory,
                                          LoadBalancerProperties properties,
                                          CustomerLbProperties customerLbProperties,
                                          IpHashLoadBalancer loadBalancer) {
        super(clientFactory, properties);
        this.customerLbProperties = customerLbProperties;
        this.clientFactory = clientFactory;
        this.properties = properties;
        this.loadBalancer = loadBalancer;
    }

    /**
     * 针对配置参数中的url 进行拦截并指定期负载均衡策略为 ipHash
     *
     * @param exchange HTTP请求-响应交互。提供对HTTP的访问请求和响应，并公开额外的服务器端处理,相关属性和特性
     * @param chain    为请求的链式拦截器，链接的Web请求处理可能用于实现横切的
     * @return Mono<Void>
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (customerLbProperties != null) {
            if (customerLbProperties.getLbs() != null && customerLbProperties.getLbs().size() > 0) {
                boolean isMatch = customerLbProperties.getLbs().stream().anyMatch(n -> path.contains(n.getUrl()));
                if (isMatch) {
                    return choose(exchange).doOnNext(response -> {
                        URI url = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
                        if (!response.hasServer()) {
                            throw NotFoundException.create(properties.isUse404(),
                                    "Unable to find instance for " + url.getHost());
                        }
                        String schemePrefix = exchange.getAttribute(GATEWAY_SCHEME_PREFIX_ATTR);
                        String overrideScheme = null;
                        if (schemePrefix != null) {
                            overrideScheme = url.getScheme();
                        }
                        DelegatingServiceInstance serviceInstance = new DelegatingServiceInstance(
                                response.getServer(), overrideScheme);
                        URI requestUrl = reconstructURI(serviceInstance, exchange.getRequest().getURI());
                        exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
                    }).then(chain.filter(exchange));
                } else {
                    return super.filter(exchange, chain);
                }
            } else {
                return super.filter(exchange, chain);
            }
        } else {
            return super.filter(exchange, chain);
        }
    }

    /**
     * 根据配置的url 选择执行要执行的负载均衡策略
     *
     * @param exchange HTTP请求-响应交互。提供对HTTP的访问请求和响应
     * @return Response<ServiceInstance>
     */
    private Mono<Response<ServiceInstance>> choose(ServerWebExchange exchange) {
        URI uri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        if (loadBalancer != null) {
            return loadBalancer.choose(new DefaultRequest<>(exchange));
        } else {
            assert uri != null;
            log.error("No loadbalancer available for:{}", uri.getHost());
            throw new NotFoundException("No loadbalancer available for" + uri.getHost());
        }
    }

}
