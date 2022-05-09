package com.banana.diners.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("oauth2.client")
@Data
public class Oauth2ClientConfiguration {
    private String clientId;
    private String secret;
    private String grant_type;
    private String scope;
}
