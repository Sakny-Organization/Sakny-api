package com.sakny.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "shuftipro")
public class ShuftiProProperties {
    private String clientId;
    private String secretKey;
    private String apiUrl = "https://api.shuftipro.com";
    private String callbackUrl;
}
