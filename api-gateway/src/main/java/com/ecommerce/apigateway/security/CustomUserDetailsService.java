package com.ecommerce.apigateway.security;

import com.ecommerce.apigateway.model.User;
import com.ecommerce.apigateway.payload.response.UserDTOResponse;
import com.ecommerce.apigateway.service.UserService;
import com.ecommerce.apigateway.util.SecurityUtils;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final ModelMapper modelMapper;

  private final UserService userService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

    UserDTOResponse userDTOResponse = this.userService.getUserByUsername(username);
    if (userDTOResponse != null) {
      User user = this.modelMapper.map(userDTOResponse, User.class);
      if (user.isEnabled()) {
        Set<GrantedAuthority> authorities =
            Set.of(SecurityUtils.convertToAuthority(String.valueOf(user.getRole())));
        return CustomUserDetails.builder()
            .user(user)
            .username(username)
            .password(user.getPassword())
            .authorities(authorities)
            .build();
      }
    } else {
      throw new UsernameNotFoundException("User not found with " + "username: " + username);
    }
    throw new UsernameNotFoundException("User not found with " + "username: " + username);
  }
}
