package com.ecommerce.userservice.service;

import com.ecommerce.userservice.model.Role;
import com.ecommerce.userservice.payload.request.UpdateUserDTO;
import com.ecommerce.userservice.payload.response.UserDTOResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

	UserDTOResponse getUserByUsername(String username);

	Page<UserDTOResponse> usersList(int pageNumber, int pageSize, String query);

	UserDTOResponse updateUser(UpdateUserDTO updateUserDTO, String username);

	List<UserDTOResponse> listAll();

	UserDTOResponse updateRole(String username, Role role);

	String deleteUser(String username);

}
