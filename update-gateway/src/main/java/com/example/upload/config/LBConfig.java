package com.example.upload.config;

import com.example.upload.filter.CustomLoadBalancerClientFilter;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LBConfig {
    @Bean
    public CustomLoadBalancerClientFilter loadBalancerClientFilter(LoadBalancerClientFactory clientFactory,
                                                                   LoadBalancerProperties properties) {
        return new CustomLoadBalancerClientFilter(clientFactory,properties);
    }
}
