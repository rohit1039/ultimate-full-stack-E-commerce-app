package com.ecommerce.userservice.security;

import com.ecommerce.userservice.model.User;
import java.util.Collection;
import java.util.Set;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomUserDetails implements UserDetails {

  private String username;

  private transient String password; // don't show up on serialized places.

  private transient User user; // user for only login operation, don't use in JWT.

  private Set<GrantedAuthority> authorities;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {

    return authorities;
  }

  @Override
  public String getPassword() {

    return password;
  }

  @Override
  public String getUsername() {

    return username;
  }

  @Override
  public boolean isAccountNonExpired() {

    return true;
  }

  @Override
  public boolean isAccountNonLocked() {

    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {

    return true;
  }

  @Override
  public boolean isEnabled() {

    return true;
  }
}
