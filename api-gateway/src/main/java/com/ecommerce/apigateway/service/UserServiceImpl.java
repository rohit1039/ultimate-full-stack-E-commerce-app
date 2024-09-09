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

/** This class provides the implementation of the user service. */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

  private final MongoTemplate mongoTemplate;

  private final ModelMapper modelMapper;

  /**
   * This method returns the user details based on the username.
   *
   * @param username the username of the user
   * @return the user details
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
