package com.ecommerce.apigateway.security;

import com.ecommerce.apigateway.model.User;
import com.ecommerce.apigateway.payload.response.UserDTOResponse;
import com.ecommerce.apigateway.service.UserService;
import com.ecommerce.apigateway.util.SecurityUtils;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements ReactiveUserDetailsService {

  private final ModelMapper modelMapper;
  private final UserService userService;

  @Override
  public Mono<UserDetails> findByUsername(String username) {
    log.info("Attempting to load user by username: {}", username);

    return Mono.defer(
        () -> {
          UserDTOResponse userDTOResponse = this.userService.getUserByUsername(username);

          if (userDTOResponse == null) {
            log.error("User not found with username: {}", username);
            return Mono.error(
                new UsernameNotFoundException("User not found with username: " + username));
          }

          User user = this.modelMapper.map(userDTOResponse, User.class);

          if (!user.isEnabled()) {
            log.error("User with username: {} is not enabled", username);
            return Mono.error(
                new UsernameNotFoundException("User not enabled with username: " + username));
          }

          Set<GrantedAuthority> authorities =
              Set.of(SecurityUtils.convertToAuthority(String.valueOf(user.getRole())));

          log.info("Successfully loaded user details for username: {}", username);

          return Mono.just(
              CustomUserDetails.builder()
                  .user(user)
                  .username(username)
                  .password(user.getPassword())
                  .authorities(authorities)
                  .build());
        });
  }
}
