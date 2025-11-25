package com.ssafy.gateway.jwt;

import com.ssafy.gateway.common.error.BusinessException;
import com.ssafy.gateway.common.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtUtil {

    private final String publicKeyString;
    private PublicKey publicKey;

    public JwtUtil(JwtProperties jwtProperties) {
        this.publicKeyString = jwtProperties.eccPublicKey();
        log.info("Public Key: {}", publicKeyString);
    }

    @PostConstruct
    protected void init() {
        try {
            String publicKeyPEM = publicKeyString.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "").replaceAll("\s", "");
            byte[] publicKeyBytes = Decoders.BASE64.decode(publicKeyPEM);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            this.publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            log.error("Failed to initialize public key", e);
            throw new BusinessException(ErrorCode.FAIL_KEY_INIT);
        }
    }

    public Claims getClaims(String token) {
        try {
            return Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}