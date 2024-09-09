package com.ecommerce.userservice.payload.request;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({"emailId", "firstName", "lastName", "avatarName", "age"})
public class UpdateUserDTO {

  @Email(
      regexp =
          "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@"
              + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\"
              + ".[A-Za-z]{2,})$")
  @NotBlank
  @Schema(description = "username of the user", example = "testuser@gmail.com")
  private String emailId;

  @Schema(description = "display picture of the user", example = "default.png")
  private String avatarName;

  @NotBlank
  @Schema(description = "firstname of the user", example = "Test")
  private String firstName;

  @NotBlank
  @Schema(description = "lastname of the user", example = "User")
  private String lastName;

  @Positive
  @Schema(description = "age of the user", example = "1")
  private int age;
}
