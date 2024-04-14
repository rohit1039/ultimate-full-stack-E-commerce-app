package com.ecommerce.userservice.security.jwt;

import static org.springframework.util.ObjectUtils.isEmpty;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger LOGGER =
      LogManager.getLogger(JwtAuthenticationFilter.class.getName());

  private final UserDetailsService userDetailsService;

  private final TokenHelper jwtTokenHelper;

  /**
   * @param request
   * @param response
   * @param filterChain
   * @throws ServletException
   * @throws IOException
   */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    // 1. get token
    String requestToken = request.getHeader("Authorization");
    String username = null;
    String token = null;
    if (!isEmpty(requestToken) && requestToken.startsWith("Bearer ")) {
      token = requestToken.substring(7);
      try {
        username = this.jwtTokenHelper.extractUsername(token);

      } catch (IllegalArgumentException | ExpiredJwtException e) {
        LOGGER.error("{}", e.getLocalizedMessage());
      } catch (MalformedJwtException e) {
        LOGGER.warn("{}", e.getLocalizedMessage());
      }

    } else {
      if (!(request.getMethod().equals("POST") || request.getMethod().equals("GET"))
          && !(request.getRequestURI().endsWith("register")
              || request.getRequestURI().endsWith("login"))
          && !(request.getRequestURI().endsWith("password"))) {
        LOGGER.error("{}", "JWT token doesn't starts with Bearer");
      }
    }
    // once we got the token , now validate
    if (!isEmpty(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
      UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
      if (this.jwtTokenHelper.validateToken(token, userDetails)) {
        // Now do the authentication
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(
            new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

      } else {
        LOGGER.error("{}", "Invalid JWT token");
      }
    }
    filterChain.doFilter(request, response);
  }
}
