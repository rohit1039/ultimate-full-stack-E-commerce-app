package com.ecommerce.apigateway.payload.request;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({
  "emailId",
  "password",
  "firstName",
  "lastName",
  "contactNumber",
  "avatarName",
  "age"
})
public class UserDTO {

  @Email(
      regexp =
          "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
              + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\"
              + ".[A-Za-z]{2,})$")
  @NotBlank
  @Schema(description = "username of the user", example = "testuser@gmail.com")
  private String emailId;

  @NotBlank
  @Size(min = 8, max = 16)
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[#$@!%&*?])[A-Za-z\\d#$@!%&*?]{8,}$",
      message =
          "minimum "
              + "1 uppercase letter, "
              + "minimum 1 lowercase letter, "
              + "minimum 1 special character, "
              + "minimum 1 "
              + "number, "
              + "minimum 8 characters ")
  @Schema(description = "password of the user", example = "Test@7978")
  private String password;

  @Schema(description = "display picture of the user", example = "default.png")
  private String avatarName;

  @NotBlank
  @Schema(description = "firstname of the user", example = "Test")
  private String firstName;

  @NotBlank
  @Schema(description = "lastname of the user", example = "User")
  private String lastName;

  @NotBlank
  @Schema(description = "contact number of the user", example = "123456789")
  private String contactNumber;

  @Positive
  @Schema(description = "age of the user", example = "1")
  private int age;
}
