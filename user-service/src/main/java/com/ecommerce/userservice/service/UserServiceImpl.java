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

/**
 * Implementation of the UserService interface for managing and processing user data. This service
 * provides functionalities such as retrieving, updating, listing, deleting users, and managing user
 * roles.
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

  private final MongoTemplate mongoTemplate;

  private final ModelMapper modelMapper;

  /**
   * Retrieves user details by the given username.
   *
   * @param username the username of the user to be retrieved. This corresponds to the "emailId"
   *     field in the database.
   * @return the user details wrapped in a {@code UserDTOResponse} object if the user exists and is
   *     enabled.
   * @throws UserNotFoundException if the user does not exist or if the user is disabled.
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
   * Retrieves a paginated list of users, optionally filtered by a search key.
   *
   * @param pageNumber the page number to retrieve, starting from 1
   * @param pageSize the number of users per page
   * @param searchKey a string to filter the users by their email ID, first name, last name, or role
   *     (case-insensitive)
   * @return a paginated list of users as {@link Page<UserDTOResponse>}
   */
  @Override
  public Page<UserDTOResponse> usersList(int pageNumber, int pageSize, String searchKey) {

    Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
    return getPageOfFilteredUsers(pageable, searchKey);
  }

  /**
   * Updates the details of an existing user based on the provided user data.
   *
   * @param updateUserDTO the DTO containing the updated user information such as age, avatar name,
   *     first name, and last name
   * @param username the unique username of the user to be updated
   * @return a response object containing the updated user information
   * @throws UserNotFoundException if the user does not exist or is disabled
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
   * Retrieves a list of all users mapped to {@link UserDTOResponse}. The method fetches all users
   * from the database, converts them to DTOs, and returns them as a list.
   *
   * @return a list of {@link UserDTOResponse} objects representing all users in the system.
   */
  @Override
  public List<UserDTOResponse> listAll() {

    List<User> users = this.mongoTemplate.findAll(User.class);
    return users.stream()
        .map(u -> this.modelMapper.map(u, UserDTOResponse.class))
        .collect(Collectors.toList());
  }

  /**
   * Updates the role of a specific user identified by their username.
   *
   * @param username the username of the user whose role is to be updated
   * @param role the new role to assign to the user
   * @return a response object containing the updated user details
   * @throws UserNotFoundException if no user is found with the specified username
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
   * Deletes a user by their username. If the user exists and is currently enabled, this method
   * disables the user and removes them from the system. If the user does not exist or is already
   * deleted, an exception is thrown.
   *
   * @param username the username (email ID) of the user to delete
   * @return a message confirming the user deletion with the username
   * @throws UserNotFoundException if the user does not exist or is already deleted
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
   * Retrieves a page of filtered user data based on provided pagination details and a search key.
   * The search is performed across multiple fields such as emailId, firstName, lastName, and role.
   *
   * @param pageable the pagination information, including the page number and size
   * @param searchKey the key used to filter users by matching against emailId, firstName, lastName,
   *     or role
   * @return a paginated list of filtered users represented as UserDTOResponse objects
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
