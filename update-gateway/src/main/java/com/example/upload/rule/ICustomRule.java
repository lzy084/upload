package com.example.upload.rule;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.server.ServerWebExchange;

public interface ICustomRule {
    ServiceInstance choose(ServerWebExchange exchange, DiscoveryClient discoveryClient);
}
