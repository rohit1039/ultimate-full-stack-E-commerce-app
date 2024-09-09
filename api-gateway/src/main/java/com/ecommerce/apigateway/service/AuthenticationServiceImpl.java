package com.ecommerce.apigateway.service;

import static com.ecommerce.apigateway.model.Role.ROLE_ADMIN;
import static com.ecommerce.apigateway.model.Role.ROLE_USER;
import static com.ecommerce.apigateway.util.Constants.DEFAULT_ADMIN_USER;
import static java.util.Objects.isNull;

import com.ecommerce.apigateway.exception.DuplicateUsernameException;
import com.ecommerce.apigateway.exception.PasswordCriteriaException;
import com.ecommerce.apigateway.exception.UserNotFoundException;
import com.ecommerce.apigateway.model.User;
import com.ecommerce.apigateway.payload.request.ForgotPasswordDTO;
import com.ecommerce.apigateway.payload.request.UserDTO;
import com.ecommerce.apigateway.payload.response.ForgotPasswordResponse;
import com.ecommerce.apigateway.payload.response.UserDTOResponse;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * This service class contains the business logic for user authentication and account management. It
 * interacts with the database and sends email notifications.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationServiceImpl.class);

  private final MongoTemplate mongoTemplate;

  private final ModelMapper modelMapper;

  private final PasswordEncoder passwordEncoder;

  private final JavaMailSender mailSender;

  /**
   * Creates a new user account.
   *
   * @param userDTO the user data
   * @return the user data with additional fields such as avatar name and role
   * @throws DuplicateUsernameException if the email address is already in use
   */
  @Override
  public UserDTOResponse createUser(UserDTO userDTO) throws DuplicateUsernameException {

    if (isNull(checkDuplicateEmailIDs(userDTO))) {
      User dtoToUser = this.modelMapper.map(userDTO, User.class);
      dtoToUser.setEnabled(true);
      dtoToUser.setAvatarName("default.png");
      dtoToUser.setPassword(passwordEncoder.encode(dtoToUser.getPassword()));
      if (dtoToUser.getEmailId().equals(DEFAULT_ADMIN_USER)) {
        dtoToUser.setRole(ROLE_ADMIN);
      } else {
        dtoToUser.setRole(ROLE_USER);
      }
      User savedUser = this.mongoTemplate.save(dtoToUser);
      LOGGER.info("{}", "***** User registered successfully *****");
      UserDTOResponse userDTOResponse = this.modelMapper.map(savedUser, UserDTOResponse.class);
      return userDTOResponse;
    }
    throw new DuplicateUsernameException("Duplicate emailIds not allowed");
  }

  /**
   * Sends a password reset email to the user.
   *
   * @param forgotPasswordDTO the user's email address
   * @return the user data with additional fields such as first name, last name, and avatar name
   * @throws UserNotFoundException if the user does not exist
   * @throws PasswordCriteriaException if the new password does not meet the criteria
   */
  @Override
  public ForgotPasswordResponse forgotPassword(ForgotPasswordDTO forgotPasswordDTO)
      throws UserNotFoundException, PasswordCriteriaException {

    String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#$@!%&*?])[A-Za-z\\d#$@!%&*?]{8,}$";
    Query query = new Query();
    query.addCriteria(Criteria.where("emailId").is(forgotPasswordDTO.getUsername()));
    User userInDB = this.mongoTemplate.findOne(query, User.class);
    if (!isNull(userInDB)) {
      String newEncodedPassword = this.passwordEncoder.encode(forgotPasswordDTO.getNewPassword());
      if (forgotPasswordDTO.getNewPassword().matches(REGEX)) {
        if (this.passwordEncoder.matches(
            forgotPasswordDTO.getOldPassword(), userInDB.getPassword())) {
          LOGGER.info(" ***** Old password entered correctly *****");
          userInDB.setPassword(newEncodedPassword);
          User updatedUser = this.mongoTemplate.save(userInDB);
          ForgotPasswordResponse passwordResponse = new ForgotPasswordResponse();
          passwordResponse.setEmailId(updatedUser.getEmailId());
          passwordResponse.setFirstName(updatedUser.getFirstName());
          passwordResponse.setLastName(updatedUser.getLastName());
          passwordResponse.setAge(updatedUser.getAge());
          passwordResponse.setAvatarName(updatedUser.getAvatarName());
          return passwordResponse;
        } else {
          LOGGER.error(" ##### Old password entered is wrong #####");
          throw new PasswordCriteriaException("Old password is wrong, please enter correctly");
        }
      } else {
        throw new PasswordCriteriaException(
            "Password should be of minimum 8 characters and should contain "
                + "minimum 1 uppercase letter, minimum 1 lowercase letter, minimum 1 special character, minimum 1 "
                + "digit");
      }
    } else {
      throw new UserNotFoundException(
          "User doesn't exists with " + "username: " + forgotPasswordDTO.getUsername());
    }
  }

  /**
   * Checks if the email address is already in use.
   *
   * @param userDTO the user data
   * @return the user if the email is not in use, null otherwise
   * @throws DuplicateUsernameException if the email address is already in use
   */
  @Override
  public UserDTOResponse checkDuplicateEmailIDs(UserDTO userDTO) throws DuplicateUsernameException {

    Query query = new Query();
    query.addCriteria(Criteria.where("emailId").is(userDTO.getEmailId()));
    User user = this.mongoTemplate.findOne(query, User.class);
    if (isNull(user)) {
      return null;
    } else {
      throw new DuplicateUsernameException(
          "User already exist with emailId: " + userDTO.getEmailId());
    }
  }

  /**
   * Sends an email to the specified recipient.
   *
   * @param to the recipient's email address
   * @param fullName the recipient's full name
   * @param subject the email subject
   */
  @Override
  @Async("asyncTaskExecutor")
  public CompletableFuture<Void> sendMail(String to, String fullName, String subject) {

    CompletableFuture<Void> future = new CompletableFuture<>();
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message);
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(getMailBody(fullName), true);
      mailSender.send(message);
      future.complete(null);
    } catch (MessagingException e) {
      LOGGER.error("Failed to send mail: {}", e.getLocalizedMessage());
      future.completeExceptionally(e);
    }
    return future;
  }

  /**
   * Returns the email body for the password reset email.
   *
   * @param fullName the recipient's full name
   * @return the email body
   */
  private String getMailBody(String fullName) {

    String body =
        "<div style=\"font-family: Helvetica,Arial,sans-serif;min-width:1000px;overflow:auto;"
            + "line-height:1.5;background-color:#F2F2F2;\"> "
            + "<div style=\"margin:50px auto 50px auto;width:85%;"
            + "padding:10px 10px;background-color:#FFFFFF;\">"
            + "<p>"
            + "Dear "
            + "<b>"
            + fullName
            + "</b>,"
            + "</p> "
            + "<p> This mail is to inform you that, your registration with us is <b "
            + "style='color:green'>SUCCESSFUL</b>. </p> "
            + "<p> Welcome, to the India's No. 1 clothing brand, "
            + "where you can find products at a very budget friendly price range."
            + "<p> Make sure to add your "
            + "products to cart before it gets out of stock due to high demand. </p>"
            + "<p> Please feel free to "
            + "browse and order your choice of products. </p>"
            + " <br />"
            + "<br/>"
            + "<div style='margin-top:2px;"
            + "line-height:1'><p>Best Regards,</p><p>Rohit Parida</p><p>Ph. - (+91) "
            + "7978251158</p></div></div></div>";
    return body;
  }
}
