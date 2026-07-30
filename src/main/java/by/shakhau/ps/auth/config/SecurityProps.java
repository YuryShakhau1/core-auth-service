package by.shakhau.ps.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
@ConfigurationProperties(prefix = "spring.jwt")
@Getter
@Setter
public class SecurityProps {

    private String secret;
    private long accessExpiration;
    private long refreshExpiration;
    private int maxSessionCount;
    private String adminInitSecretHash;

    public void setAdminInitSecret(String adminInitSecret) {
        setAdminInitSecretHash(DigestUtils.sha256Hex(adminInitSecret));
    }
}
