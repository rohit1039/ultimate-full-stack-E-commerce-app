package com.ecommerce.apigateway.model;

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
  private String emailId;

  @Field(name = "password")
  private String password;

  @Field(name = "avatar_name")
  private String avatarName;

  @Field(name = "contact_number")
  private String contactNumber;

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
}
