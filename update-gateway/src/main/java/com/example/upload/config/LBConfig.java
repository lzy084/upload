package com.example.upload.config;

import com.example.upload.filter.CustomLoadBalancerClientFilter;
import com.example.upload.filter.CustomerLbProperties;
import com.example.upload.rule.IpHashLoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class LBConfig {
    @Autowired
    Environment environment;
    @Autowired
    LoadBalancerClientFactory clientFactory;
    @Autowired
    LoadBalancerProperties properties;
    @Autowired
    DiscoveryClient discoveryClient;

    @Bean(value = "customerLbProperties")
    @ConditionalOnMissingBean
    public CustomerLbProperties customerLbProperties() {
        return new CustomerLbProperties();
    }

    @Bean
    public CustomLoadBalancerClientFilter loadBalancerClientFilter(CustomerLbProperties customerLbProperties) {
        return new CustomLoadBalancerClientFilter(
                clientFactory,
                properties,
                customerLbProperties,
                ipHashLoadBalancer(customerLbProperties));
    }

    @Bean
    public IpHashLoadBalancer ipHashLoadBalancer(CustomerLbProperties customerLbProperties) {
        return new IpHashLoadBalancer(customerLbProperties, discoveryClient);
    }
}
