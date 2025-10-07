package org.siriusxi.htec.fa.infra.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Date;

import static java.lang.String.format;

/**
 * Helper class to build, and verify a JWT token and extract the claim from a token.
 *
 * @author Venkata Anil Kumar
 */

@Slf4j
public final class JwtTokenHelper {
    
    private JwtTokenHelper() {
    }
    
    /**
     * Generates a token fot the user.
     *
     * @param id       of the user.
     * @param username of the the login user.
     * @return a valid JWT token.
     */
    public static String generateAccessToken(int id, String username) {
        var now = System.currentTimeMillis();
        return Jwts
            .builder()
            .setId(String.valueOf(id))
            .setSubject(format("%d,%s", id, username))
            .setIssuer(JwtConfig.ISSUER)
            .setIssuedAt(new Date(now))
            .setExpiration(Date.from(ZonedDateTime.now()
                .plusDays(JwtConfig.TOKEN_EXPIRY_DURATION)
                .toInstant()))
            .signWith(JwtConfig.key(), JwtConfig.SIGNATURE_ALGORITHM)
            .compact();
    }
    
    /**
     * Extracts the User id claim from the JWT token
     *
     * @param token - token to analyze
     * @return the User Id claim contained in the token
     */
    public static int getUserId(String token) {
        return Integer.parseInt(getClaims(token)
            .getSubject()
            .split(",")[0]);
    }
    
    /**
     * Extracts the username claim from the JWT token
     *
     * @param token - token to analyze
     * @return the Username claim contained in the token
     */
    public static String getUsername(String token) {
        return getClaims(token)
            .getSubject()
            .split(",")[1];
    }
    
    /**
     * Extracts the expiration date claim from the JWT token
     *
     * @param token - token to analyze
     * @return the expiration date claim contained in the token
     */
    public static Date tokenExpiredAt(String token) {
        return getClaims(token)
            .getExpiration();
    }
    
    /**
     * Extracts the claims from the JWT token
     *
     * @param token - token to analyze
     * @return the all claims contained in the token
     */
    private static Claims getClaims(String token) {
        return Jwts
            .parser()
            .verifyWith(JwtConfig.key())
            .build()
            .parseSignedClaims(token.replace(JwtConfig.TOKEN_PREFIX, ""))
            .getPayload();
    }
    
    /**
     * Extracts the claim from the JWT token
     *
     * @param token - token to analyze
     * @return whether the token is a valid token or not.
     */
    public static boolean validate(String token) {
        try {
            getClaims(token);
            return true;
        } catch (SecurityException ex) {
            log.error("Invalid JWT signature - {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token - {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token - {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token - {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty - {}", ex.getMessage());
        }
        return false;
    }
    
    /**
     * Allows generating a real base64 encoded secret key.
     */
    public static String getRealBase64EncodedSecret() {
        return Base64.getEncoder().encodeToString(getRealSecret());
    }
    
    /**
     * Allows generating a real secret key.
     */
    public static byte[] getRealSecret() {
        return Keys.secretKeyFor(JwtConfig.SIGNATURE_ALGORITHM).getEncoded();
    }
    
    /**
     * jwt configuration interface.
     */
    private static class JwtConfig {
        static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.HS512;
        // It should be kept encoded in an environment variable
        static final String SECRET =
            """
                5s2BCxpNxdI58mAaAllBr/psyu91aCusvXy+kew9ytxQ/zh\
                RtvcZMxVAjmkq8pVkSMA81+9Y0D4W06qGre+hYg==""";
        static final String TOKEN_PREFIX = "Bearer ";
        static final String ISSUER = "siriusx.io";
        static final int TOKEN_EXPIRY_DURATION = 7; // In days
        private JwtConfig() {
        }
        
        static SecretKey key() {
            return Keys.hmacShaKeyFor(Base64.getDecoder().decode(SECRET));
        }
    }
}
