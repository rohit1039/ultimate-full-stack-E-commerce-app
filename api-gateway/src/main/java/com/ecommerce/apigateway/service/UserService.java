package com.ecommerce.apigateway.service;

import com.ecommerce.apigateway.model.Role;
import com.ecommerce.apigateway.payload.response.UserDTOResponse;

public interface UserService {

  UserDTOResponse getUserByUsername(String username);
}
