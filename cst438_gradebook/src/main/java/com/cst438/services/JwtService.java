package com.cst438.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;

@Component
public class JwtService {
	static final long EXPIRATIONTIME = 86400000; // 1 day in ms
	static final String PREFIX = "Bearer";
	// Generate secret key. Only for the demonstration
	// You should read it from the application configuration
	//static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
	static  Key key;
	static final String str= "21825474757a1a6de4d4fe60d8826aebb132127b6baa8152a1210277ef6da3921825474757a1a6de4d4fe60d8826aebb132127b6baa8152a1210277ef6da39";
	public JwtService() {
		byte[] val = new byte[str.length()/2];
		for (int i = 0; i < val.length; i++) {
			int index = i * 2;
			int j = Integer.parseInt(str.substring(index, index + 2), 16);
			val[i] = (byte) j;
		}
		key = Keys.hmacShaKeyFor(val);
	}


	// Generate JWT token
	public String getToken(String username) {
		String token = Jwts.builder()
			  .setSubject(username)
			  .setExpiration(new Date(System.currentTimeMillis() + EXPIRATIONTIME))
			  .signWith(key)
			  .compact();
		return token;
  }

	// Get a token from request Authorization header, 
	// parse a token and get username
	public String getAuthUser(HttpServletRequest request) {
		String token = request.getHeader(HttpHeaders.AUTHORIZATION);
	
		if (token != null) {
			String user = Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(token.replace(PREFIX, ""))
					.getBody()
					.getSubject();
	    
			if (user != null)
				return user;
		}

		return null;
	}
}
