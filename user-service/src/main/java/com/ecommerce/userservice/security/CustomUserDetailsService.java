package com.ecommerce.userservice.security;

import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.payload.response.UserDTOResponse;
import com.ecommerce.userservice.service.UserService;
import com.ecommerce.userservice.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

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
				Set<GrantedAuthority> authorities = Set
					.of(SecurityUtils.convertToAuthority(String.valueOf(user.getRole())));

				return CustomUserDetails.builder()
					.user(user)
					.username(username)
					.password(user.getPassword())
					.authorities(authorities)
					.build();
			}
		}
		else {
			throw new UsernameNotFoundException("User not found with " + "username: " + username);
		}
		throw new UsernameNotFoundException("User not found with " + "username: " + username);
	}

}
