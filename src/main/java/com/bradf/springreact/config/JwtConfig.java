package com.bradf.springreact.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "application.jwt")
@Getter
@Setter
public class JwtConfig {
    private String secret;
    private Duration accessExpiration;
    private Duration refreshExpiration;
}
