package com.example.upload.filter;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义负载均衡参数
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
@ConfigurationProperties(prefix = "cloud.gateway.customer")
@Component
public class CustomerLbProperties {
    private List<Lbs> lbsList;
    /**
     * 负载均衡参数 包含需要指定的负载均衡的url，服务Id
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class Lbs{
        private String url;
        private String serviceId;
    }
}
