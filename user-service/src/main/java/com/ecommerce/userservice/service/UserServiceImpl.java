package com.ecommerce.userservice.service;

import com.ecommerce.userservice.exception.UserNotFoundException;
import com.ecommerce.userservice.model.Role;
import com.ecommerce.userservice.model.User;
import com.ecommerce.userservice.payload.request.UpdateUserDTO;
import com.ecommerce.userservice.payload.response.UserDTOResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
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

  /**
   * This method returns a page of users based on the search criteria.
   *
   * @param pageNumber the page number
   * @param pageSize the page size
   * @param searchKey the search key
   * @return the page of users
   */
  @Override
  public Page<UserDTOResponse> usersList(int pageNumber, int pageSize, String searchKey) {

    Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
    return getPageOfFilteredUsers(pageable, searchKey);
  }

  /**
   * This method updates the user details based on the username.
   *
   * @param updateUserDTO the user details to update
   * @param username the username of the user
   * @return the updated user details
   */
  @Override
  public UserDTOResponse updateUser(UpdateUserDTO updateUserDTO, String username) {

    Query query = new Query();
    query.addCriteria(Criteria.where("_id").is(username));
    User userInDB = this.mongoTemplate.findOne(query, User.class);
    if (userInDB != null) {
      if (userInDB.isEnabled()) {
        // Build the updated user object
        User setUpdatedUser =
            userInDB.toBuilder()
                .emailId(userInDB.getEmailId())
                .age(updateUserDTO.getAge())
                .avatarName(updateUserDTO.getAvatarName())
                .firstName(updateUserDTO.getFirstName())
                .lastName(updateUserDTO.getLastName())
                .build();
        // Save the updated user
        User saveUpdatedUser = this.mongoTemplate.save(setUpdatedUser);
        LOGGER.info("Updated user saved successfully to DB");
        // Map the updated user object to the UpdateUserDTO object
        UserDTOResponse updatedUser = this.modelMapper.map(saveUpdatedUser, UserDTOResponse.class);
        return updatedUser;
      } else {
        // Throw an exception if the user does not exist
        throw new UserNotFoundException("User does not exist with username: " + username);
      }
    } else {
      // Throw an exception if the user does not exist
      throw new UserNotFoundException("User does not exist with username: " + username);
    }
  }

  /**
   * This method returns a list of all users.
   *
   * @return a list of all users
   */
  @Override
  public List<UserDTOResponse> listAll() {

    List<User> users = this.mongoTemplate.findAll(User.class);
    return users.stream()
        .map(u -> this.modelMapper.map(u, UserDTOResponse.class))
        .collect(Collectors.toList());
  }

  /**
   * This method updates the role of the user based on the username.
   *
   * @param username the username of the user
   * @param role the role of the user
   * @return the updated user details
   */
  @Override
  public UserDTOResponse updateRole(String username, Role role) {

    Query query = new Query();
    query.addCriteria(Criteria.where("emailId").is(username));
    User userInDB = this.mongoTemplate.findOne(query, User.class);
    if (userInDB != null) {
      // Update the role of the user
      userInDB.setRole(role);
      // Save the updated user
      User updatedUser = this.mongoTemplate.save(userInDB);
      // Map the updated user object to the UserApiResponse object
      UserDTOResponse userDTO = this.modelMapper.map(updatedUser, UserDTOResponse.class);
      // Set the role in the response
      userDTO.setRole(updatedUser.getRole());
      return userDTO;
    } else {
      // Throw an exception if the user does not exist
      throw new UserNotFoundException("User does not exist with username: " + username);
    }
  }

  /**
   * This method deletes the user based on the username.
   *
   * @param username the username of the user
   * @return a message indicating whether the user was deleted or not
   */
  @Override
  public String deleteUser(String username) {

    Query query = new Query();
    query.addCriteria(Criteria.where("emailId").is(username));
    User userInDB = this.mongoTemplate.findOne(query, User.class);
    if (userInDB != null) {
      if (!userInDB.isEnabled()) {
        // Throw an exception if the user is already deleted
        throw new UserNotFoundException("User already been deleted or does not exist");
      }
      // Update the status of the user to disabled
      userInDB.setEnabled(false);
      // Remove the user from the database
      this.mongoTemplate.remove(userInDB);
      return "User deleted successfully with username: " + username;
    } else {
      // Throw an exception if the user does not exist
      throw new UserNotFoundException("User does not exist with username: " + username);
    }
  }

  /**
   * This method returns a page of filtered users based on the search criteria.
   *
   * @param pageable the pageable object
   * @param searchKey the search key
   * @return the page of filtered users
   */
  public Page<UserDTOResponse> getPageOfFilteredUsers(Pageable pageable, String searchKey) {

    Query query = new Query();
    query
        .with(pageable)
        .addCriteria(
            new Criteria()
                .orOperator(
                    Criteria.where("emailId").regex(searchKey, "i"),
                    Criteria.where("firstName").regex(searchKey, "i"),
                    Criteria.where("lastName").regex(searchKey, "i"),
                    Criteria.where("role").regex(searchKey, "i")));
    List<User> users = this.mongoTemplate.find(query, User.class);
    Page<UserDTOResponse> page =
        PageableExecutionUtils.getPage(
                users,
                pageable,
                () -> this.mongoTemplate.count(Query.of(query).limit(-1).skip(-1), User.class))
            .map(u -> this.modelMapper.map(u, UserDTOResponse.class));
    return new PageImpl<>(page.getContent(), pageable, page.getTotalElements());
  }
}
