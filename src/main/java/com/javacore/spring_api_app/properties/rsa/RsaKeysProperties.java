package com.javacore.spring_api_app.properties.rsa;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@ConfigurationProperties(prefix = "spring.rsa")
public record RsaKeysProperties(
        RSAPrivateKey privateKey,
        RSAPublicKey publicKey
) {
}
