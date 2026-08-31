package by.shakhau.ps.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.jwt")
@Getter
@Setter
public class SecurityProps {

    private String secret;
    private long accessExpiration;
    private long refreshExpiration;
    private int maxSessionCount;
}
