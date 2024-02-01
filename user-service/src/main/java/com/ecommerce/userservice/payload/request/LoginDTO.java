package com.ecommerce.userservice.payload.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class LoginDTO {

	@Schema(description = "username of the user", example = "testuser@gmail.com")
	private String username;

	@Schema(description = "password of the user", example = "Test@7978")
	private String password;

}
