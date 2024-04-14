package com.ecommerce.userservice.service.authentication;

import com.ecommerce.userservice.payload.request.ForgotPasswordDTO;
import com.ecommerce.userservice.payload.request.UserDTO;
import com.ecommerce.userservice.payload.response.ForgotPasswordResponse;
import com.ecommerce.userservice.payload.response.UserDTOResponse;
import java.util.concurrent.CompletableFuture;

public interface AuthenticationService {

  UserDTOResponse createUser(UserDTO userDTO) throws InterruptedException;

  ForgotPasswordResponse forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

  UserDTOResponse checkDuplicateEmailIDs(UserDTO userDTO);

  CompletableFuture<Void> sendMail(String to, String fullName, String subject);
}
