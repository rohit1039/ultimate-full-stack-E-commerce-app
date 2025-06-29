package com.ecommerce.apigateway.service;

import com.ecommerce.apigateway.exception.UserNotFoundException;
import com.ecommerce.apigateway.model.User;
import com.ecommerce.apigateway.payload.response.UserDTOResponse;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * Implementation of the {@link UserService} interface, providing services for user management.
 * This class performs operations related to retrieving user details and maps entities
 * to their corresponding DTOs using ModelMapper.
 * It leverages MongoTemplate to perform MongoDB queries for data persistence.
 * Exception handling is included to manage scenarios such as disabled users
 * or non-existent users.
 *
 * Responsibilities:
 * - Retrieve user details by username.
 * - Handle cases where a user is found but disabled.
 * - Throw appropriate exceptions for non-existent users.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

  private final MongoTemplate mongoTemplate;

  private final ModelMapper modelMapper;

  /**
   * Retrieves a user's details by their username.
   * This method fetches the user from the database based on the provided username,
   * validates if the user is enabled, and maps the user entity to a DTO response object.
   *
   * @param username the username to search for in the database
   * @return UserDTOResponse containing the details of the user, including username,
   *         role, and other relevant information
   * @throws UserNotFoundException if the user does not exist or is disabled
   */
  @Override
  public UserDTOResponse getUserByUsername(String username) {

    Query query = new Query();
    query.addCriteria(Criteria.where("emailId").is(username));
    User user = this.mongoTemplate.findOne(query, User.class);
    if (user != null) {
      if (user.isEnabled()) {
        // Map the user object to the UserApiResponse object
        UserDTOResponse userDTO = this.modelMapper.map(user, UserDTOResponse.class);
        // Set the role in the response
        userDTO.setRole(user.getRole());
        return userDTO;
      } else {
        // Throw an exception if the user is disabled
        throw new UserNotFoundException("User is disabled with username: " + username);
      }
    } else {
      // Throw an exception if the user does not exist
      throw new UserNotFoundException("User does not exist with username: " + username);
    }
  }
}
