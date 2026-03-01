package com.tupack.palletsortingapi.user.application.impl;

import com.tupack.palletsortingapi.user.configuration.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class JwtService {

  private final JwtProperties props;
  private final Key key;

  public JwtService(JwtProperties props) {
    this.props = props;
    this.key = Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(String subject, Map<String, Object> claims) {
    Instant now = Instant.now();
    Instant exp = now.plus(props.getAccessTokenExpirationMinutes(), ChronoUnit.MINUTES);
    return Jwts.builder()
        .setSubject(subject)
        .addClaims(claims)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(exp))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String generateRefreshToken(String subject) {
    Instant now = Instant.now();
    Instant exp = now.plus(props.getRefreshTokenExpirationDays(), ChronoUnit.DAYS);
    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(Date.from(now))
        .setExpiration(Date.from(exp))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public boolean isTokenValid(String token, String expectedSubject) {
    try {
      String subject = extractSubject(token);
      return expectedSubject.equals(subject) && !isExpired(token);
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public boolean isExpired(String token) {
    Date exp = extractAllClaims(token).getExpiration();
    return exp.before(new Date());
  }

  public String extractSubject(String token) {
    return extractAllClaims(token).getSubject();
  }

  //    public Claims extractAllClaims(String token) {
  //        return Jwts.parserBuilder()
  //                .setSigningKey(key)
  //                .build()
  //                .parseClaimsJws(token)
  //                .getBody();
  //    }

  /**
   * Extracts the username from the JWT token
   * @param token the JWT token
   * @return the username contained in the token
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts a specific claim from the token
   * @param token the JWT token
   * @param claimsResolver function to resolve the claim
   * @return the extracted claim
   */
  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  /**
   * Extracts all claims from the token
   * @param token the JWT token
   * @return all claims contained in the token
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token)
        .getBody();
  }

  /**
   * Checks if the token has expired
   * @param token the JWT token
   * @return true if the token is expired, false otherwise
   */
  boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Extracts the expiration date from the token
   * @param token the JWT token
   * @return the expiration date
   */
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public Key getSigningKey() {
    return key;
  }
}
