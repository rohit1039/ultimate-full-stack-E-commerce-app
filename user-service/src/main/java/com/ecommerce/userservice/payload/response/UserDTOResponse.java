package com.ecommerce.userservice.payload.response;

import com.ecommerce.userservice.model.Role;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.core.Relation;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({ "emailId", "password", "firstName", "lastName", "avatarName", "age", "enabled", "role" })
@Relation(collectionRelation = "users", itemRelation = "user")
public class UserDTOResponse extends CollectionModel<UserDTOResponse> {

	private String emailId;

	private String password;

	private String avatarName;

	private String firstName;

	private String lastName;

	private int age;

	private boolean isEnabled;

	private Role role;

}
