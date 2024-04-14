package com.ecommerce.userservice.model;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Document(collection = "users")
public class User {

  @Indexed(unique = true)
  @MongoId
  @Field(name = "username")
  private String emailID;

  @Field(name = "password")
  private String password;

  @Field(name = "avatar_name")
  private String avatarName;

  @Field(name = "first_name")
  private String firstName;

  @Field(name = "last_name")
  private String lastName;

  @Field(name = "age")
  private int age;

  @Field(name = "enabled")
  private boolean isEnabled;

  @Field(name = "role")
  private Role role;

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    User user = (User) o;
    return isEnabled == user.isEnabled
        && Objects.equals(emailID, user.emailID)
        && Objects.equals(password, user.password)
        && Objects.equals(avatarName, user.avatarName)
        && Objects.equals(firstName, user.firstName)
        && Objects.equals(lastName, user.lastName)
        && Objects.equals(age, user.age)
        && role == user.role;
  }

  @Override
  public int hashCode() {

    return Objects.hash(emailID, password, avatarName, firstName, lastName, age, isEnabled, role);
  }
}
