package com.billiards.store.common.util;

import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    
    @Value("${app.jwtSecret}")
    private String jwtSecret;
    
    @Value("${app.jwtExpirationMs}")
    private long jwtExpirationMs;

    @Value("${app.jwtRefreshExpirationMs}")
    private long jwtRefreshExpirationMs;

    @PostConstruct
    void validateSecretConfiguration() {
        if (!StringUtils.hasText(jwtSecret)) {
            throw new IllegalStateException("JWT secret must be configured");
        }
        if (jwtExpirationMs <= 0 || jwtRefreshExpirationMs <= 0) {
            throw new IllegalStateException("JWT expiration values must be positive");
        }
        try {
            key();
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT secret must be a valid Base64-encoded key with sufficient length", e);
        }
    }

    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, jwtExpirationMs, "ACCESS");
    }

    public String generateRefreshToken(Authentication authentication) {
        return generateToken(authentication, jwtRefreshExpirationMs, "REFRESH");
    }

    public String generateToken(Authentication authentication, long expiration, String tokenType) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        
        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .claim("roles", userPrincipal.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .collect(Collectors.toList())
                )
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expiration))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public long getAccessTokenExpirationMs() {
        return jwtExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return jwtRefreshExpirationMs;
    }

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
            .parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken, String expectedTokenType) {
        try {
            var claims = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(authToken).getBody();
            
            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!expectedTokenType.equals(tokenType)) {
                logger.warn("Token type mismatch. Expected: {}, Got: {}", expectedTokenType, tokenType);
                return false;
            }
            logger.info("Token validate successfully");
            return true;
        } catch (ExpiredJwtException e) {
            logger.debug("Token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("Invalid JWT format: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported JWT: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(authToken);
            return true;
        } catch (ExpiredJwtException e) {
            logger.debug("Token expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            logger.warn("Invalid JWT format: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.warn("Unsupported JWT: {}", e.getMessage());
        } catch (SignatureException e) {
            logger.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("JWT validation failed: {}", e.getMessage());
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        try {
            var claims = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            logger.debug("Failed to check token expiration: {}", e.getMessage());
            return false;
        }
    }

    public long getExpirationTime(String token) {
        try {
            var claims = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
            return claims.getExpiration().getTime();
        } catch (JwtException e) {
            logger.debug("Failed to extract expiration time: {}", e.getMessage());
            return -1;
        }
    }

    public long getRemainingExpirationMs(String token) {
        try {
            var claims = Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody();
            return Math.max(claims.getExpiration().getTime() - System.currentTimeMillis(), 0);
        } catch (JwtException e) {
            logger.debug("Failed to calculate remaining token lifetime: {}", e.getMessage());
            return -1;
        }
    }
}
