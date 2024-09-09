package com.ecommerce.apigateway.service;

import com.ecommerce.apigateway.payload.request.ForgotPasswordDTO;
import com.ecommerce.apigateway.payload.request.UserDTO;
import com.ecommerce.apigateway.payload.response.ForgotPasswordResponse;
import com.ecommerce.apigateway.payload.response.UserDTOResponse;
import java.util.concurrent.CompletableFuture;

public interface AuthenticationService {

  UserDTOResponse createUser(UserDTO userDTO) throws InterruptedException;

  ForgotPasswordResponse forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

  UserDTOResponse checkDuplicateEmailIDs(UserDTO userDTO);

  CompletableFuture<Void> sendMail(String to, String fullName, String subject);
}
