package com.ssafy.auth.infrastructure.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.ssafy.auth.common.exception.BusinessException;
import com.ssafy.auth.common.exception.ErrorCode;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private PrivateKey privateKey;
    private PublicKey openKey;
    private final String secretKey;
    private final String publicKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    public JwtUtil(JwtProperties jwtProperties) {
        this.secretKey = jwtProperties.eccPrivateKey();
        this.publicKey = jwtProperties.eccPublicKey();
        this.accessTokenValidityInMilliseconds = jwtProperties.accessTokenValidityInSeconds() * 1000;
        this.refreshTokenValidityInMilliseconds = jwtProperties.refreshTokenValidityInSeconds() * 1000;
    }

    @PostConstruct
    protected void init() {
        try {
            // --- 개인 키 초기화 (기존과 동일) ---
            String privateKeyPEM = secretKey.replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
            byte[] privateKeyBytes = Decoders.BASE64.decode(privateKeyPEM);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            this.privateKey = keyFactory.generatePrivate(privateKeySpec);

            // --- 공개 키 초기화 (수정된 부분) ---
            String publicKeyPEM = publicKey.replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "").replaceAll("\\s", "");
            byte[] publicKeyBytes = Decoders.BASE64.decode(publicKeyPEM);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes); // 1. 공개 키용 Spec 사용
            this.openKey = keyFactory.generatePublic(publicKeySpec); // 2. 공개 키 생성 메소드 사용

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AUTH_FAIL_KEY_INIT);
        }
    }

    public String createAccessToken(long userId) {
        Claims claims = Jwts.claims()
                .subject(String.valueOf(userId))
                .build();

        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(validity)
                .signWith(privateKey)
                .compact();
    }

    public String createRefreshToken(long userId) {
        Claims claims = Jwts.claims()
                .subject(String.valueOf(userId))
                .build();

        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds); // 리프레시 토큰 유효 시간 사용

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(validity)
                .signWith(privateKey)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(openKey).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    public long getUserIdFromToken(String token) {
        String subject = Jwts.parser()
                .verifyWith(openKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        return Long.parseLong(subject);
    }
}
