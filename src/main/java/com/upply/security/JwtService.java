package com.upply.security;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.secret-key}")
    private String secretKey;





    // secret key = signInKey They’re essentially the same thing, just used in different context
    // signInKey is used to sign and verify the token (ensuring it hasn’t been tampered with).
    // Keys.hmacShaKeyFor() ensures the key is strong enough for the HMAC-SHA algorithm (e.g., HS256).
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails) {

        // method reference to transform each GrantedAuthority object into just its name (a String).
        var authorities = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()

                // signs the token with your private key so it can’t be forged
                .signWith(getSignInKey())

                .setSubject(userDetails.getUsername())

                .setClaims(extraClaims)
                .claim("authorities", authorities)

                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))

                .compact();
    }

    // This overload allows you to add custom claims (call after successful authentication)
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails);
    }

    // This overload just uses default empty claims (call after successful authentication)
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }





    private Claims extractAllClaims(String token) {

        return Jwts

                // create an object that can read and validate JWT tokens
                .parserBuilder()

                // When you generated the token earlier, you signed it using a secret key ===> .signWith(getSignInKey())
                // So now, when you parse it, you must use the same secret key to verify the signature.
                // If the token was tampered with or forged, this verification will fail and throw an exception.
                .setSigningKey(getSignInKey())

                .build()

                // This is where the actual parsing and verification happen.
                // Verifies the signature using your secret key.
                // Decodes the payload (the middle part of the JWT).
                // If the token is invalid (e.g., wrong key, modified, or expired), it throws an exception
                .parseClaimsJws(token)

                // This retrieves just the body of the JWT, which is the payload
                .getBody();
    }





    public String extractUsername(String jwtToken) {
        return extractAllClaims(jwtToken).getSubject();
    }

    public boolean isTokenNotExpired(String jwtToken) {
        return extractAllClaims(jwtToken).getExpiration().after(new Date());
    }

    public boolean isTokenValid(String jwtToken, UserDetails userDetails) {
        final String username = extractUsername(jwtToken);
        return (username.equals(userDetails.getUsername()) && isTokenNotExpired(jwtToken));
    }
}
